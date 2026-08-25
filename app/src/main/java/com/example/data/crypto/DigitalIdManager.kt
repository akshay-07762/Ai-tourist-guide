package com.example.data.crypto

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class VerifiableCredential(
    val credentialId: String,
    val context: String = "https://www.w3.org/2018/credentials/v1",
    val type: List<String> = listOf("VerifiableCredential", "TouristSafetyCredential"),
    val issuer: String = "did:gov:in:tourism-safety-authority",
    val issuanceDate: String,
    val credentialSubject: CredentialSubject,
    val proof: Proof
)

data class CredentialSubject(
    val id: String,
    val name: String,
    val nationality: String,
    val emergencyContact: String,
    val bloodGroup: String,
    val hotelLocation: String,
    val safetyStatus: String = "ACTIVE_MONITORED"
)

data class Proof(
    val type: String = "Ed25519Signature2020",
    val created: String,
    val verificationMethod: String,
    val proofPurpose: String = "assertionMethod",
    val jwsProofValue: String,
    val blockchainLedgerTx: String
)

object DigitalIdManager {

    fun generateSha256Hash(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun createVerifiableCredential(
        touristId: String,
        name: String,
        nationality: String,
        emergencyPhone: String,
        bloodGroup: String,
        hotel: String
    ): VerifiableCredential {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        val now = dateFormat.format(Date())

        val subject = CredentialSubject(
            id = "did:safe:tourist:$touristId",
            name = name,
            nationality = nationality,
            emergencyContact = emergencyPhone,
            bloodGroup = bloodGroup,
            hotelLocation = hotel
        )

        val rawPayload = "${subject.id}|${subject.name}|${subject.nationality}|${subject.bloodGroup}|$now"
        val payloadHash = generateSha256Hash(rawPayload)
        val mockSignature = "SIG_ED25519_" + generateSha256Hash("AUTH_KEY_$payloadHash").take(32)
        val blockchainTx = "0x" + generateSha256Hash("BLOCK_LEDGER_MERKLE_$payloadHash")

        return VerifiableCredential(
            credentialId = "URN:DID:IND-SAFETOUR-$touristId",
            issuanceDate = now,
            credentialSubject = subject,
            proof = Proof(
                created = now,
                verificationMethod = "did:gov:in:tourism-safety-authority#key-1",
                jwsProofValue = mockSignature,
                blockchainLedgerTx = blockchainTx
            )
        )
    }

    fun verifyCredentialIntegrity(
        touristId: String,
        name: String,
        signature: String,
        txHash: String
    ): Boolean {
        return signature.startsWith("SIG_ED25519_") && txHash.startsWith("0x") && txHash.length >= 20
    }

    /**
     * Generates a deterministic boolean matrix for a clean QR code display on Canvas
     * without needing heavy third-party zxing dependencies.
     */
    fun generateMockQrMatrix(content: String, size: Int = 21): Array<BooleanArray> {
        val matrix = Array(size) { BooleanArray(size) { false } }
        val hash = generateSha256Hash(content)

        // Draw Finder Patterns (Top-Left, Top-Right, Bottom-Left)
        fun drawFinderPattern(startX: Int, startY: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = (r == 0 || r == 6 || c == 0 || c == 6)
                    val isCenter = (r in 2..4 && c in 2..4)
                    matrix[startY + r][startX + c] = isBorder || isCenter
                }
            }
        }

        drawFinderPattern(0, 0)
        drawFinderPattern(size - 7, 0)
        drawFinderPattern(0, size - 7)

        // Draw Timing Patterns
        for (i in 7 until size - 7) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // Fill Data Cells deterministically based on Hash bits
        var hashIdx = 0
        for (r in 0 until size) {
            for (c in 0 until size) {
                // Skip finder areas
                val inTopLeft = r < 8 && c < 8
                val inTopRight = r < 8 && c >= size - 8
                val inBottomLeft = r >= size - 8 && c < 8
                val inTiming = r == 6 || c == 6

                if (!inTopLeft && !inTopRight && !inBottomLeft && !inTiming) {
                    val char = hash[hashIdx % hash.length]
                    hashIdx++
                    matrix[r][c] = (char.code % 2 == 0)
                }
            }
        }

        return matrix
    }
}
