package com.example.data.engine

import com.example.BuildConfig
import com.example.data.model.GeoFenceEntity
import com.example.data.model.IncidentEntity
import com.example.data.model.RiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiSafetyAdvisory(
    val title: String,
    val immediateSteps: List<String>,
    val environmentalThreat: String,
    val searchRescueBriefing: String,
    val isAiGenerated: Boolean = true
)

object GeminiSafetyAdvisor {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateSituationalAdvisory(
        riskLevel: RiskLevel,
        currentZone: GeoFenceEntity?,
        riskFactors: List<String>,
        altitudeMeters: Double,
        batteryPercent: Int,
        isOfflineMode: Boolean,
        weatherHeadline: String? = null,
        routeDeviationMeters: Double? = null
    ): AiSafetyAdvisory = withContext(Dispatchers.IO) {
        // If offline or no valid API key, return rule-based on-device heuristic advisory
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (isOfflineMode || apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getOfflineHeuristicAdvisory(riskLevel, currentZone, riskFactors, altitudeMeters, weatherHeadline, routeDeviationMeters)
        }

        try {
            val prompt = """
                You are the AI Safety Response Engine for a Mountain Tourist Protection System.
                Analyze the following real-time tourist telemetry, offline weather alerts, and itinerary data to provide concise emergency safety directives.

                Current Risk Level: ${riskLevel.displayName}
                Current Geo-Fence Hazard Zone: ${currentZone?.name ?: "None (Open Wilderness)"}
                Zone Category: ${currentZone?.category ?: "Trail"}
                Weather Condition / Alert: ${weatherHeadline ?: "Clear / Mountain Baseline"}
                Route Deviation: ${if (routeDeviationMeters != null && routeDeviationMeters > 0) "${routeDeviationMeters.toInt()}m off planned itinerary" else "On designated path"}
                Risk Factors: ${riskFactors.joinToString("; ")}
                Altitude: ${altitudeMeters.toInt()} meters
                Battery: $batteryPercent%

                Return JSON format:
                {
                  "title": "Short directive summary",
                  "immediateSteps": ["Step 1", "Step 2", "Step 3"],
                  "environmentalThreat": "Summary of terrain/weather threat",
                  "searchRescueBriefing": "Briefing for search & rescue teams"
                }
            """.trimIndent()

            val jsonBody = JSONObject().apply {
                val contentsArr = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val partsArr = JSONArray().apply {
                            put(JSONObject().put("text", prompt))
                        }
                        put("parts", partsArr)
                    }
                    put(contentObj)
                }
                put("contents", contentsArr)

                val genConfig = JSONObject().apply {
                    val responseFormat = JSONObject().apply {
                        put("mimeType", "application/json")
                    }
                    put("responseFormat", responseFormat)
                    put("temperature", 0.3)
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                val rootJson = JSONObject(responseBody)
                val candidates = rootJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    val parsed = JSONObject(text)
                    val steps = mutableListOf<String>()
                    val stepsArr = parsed.optJSONArray("immediateSteps")
                    if (stepsArr != null) {
                        for (i in 0 until stepsArr.length()) {
                            steps.add(stepsArr.getString(i))
                        }
                    }
                    return@withContext AiSafetyAdvisory(
                        title = parsed.optString("title", "Safety Protocol Directive"),
                        immediateSteps = if (steps.isNotEmpty()) steps else listOf("Halt descent", "Seek high ground", "Keep beacon active"),
                        environmentalThreat = parsed.optString("environmentalThreat", weatherHeadline ?: "High altitude weather degradation"),
                        searchRescueBriefing = parsed.optString("searchRescueBriefing", "Tourist telemetry active via BLE relay"),
                        isAiGenerated = true
                    )
                }
            }
        } catch (_: Exception) {}

        return@withContext getOfflineHeuristicAdvisory(riskLevel, currentZone, riskFactors, altitudeMeters, weatherHeadline, routeDeviationMeters)
    }

    private fun getOfflineHeuristicAdvisory(
        riskLevel: RiskLevel,
        currentZone: GeoFenceEntity?,
        riskFactors: List<String>,
        altitudeMeters: Double,
        weatherHeadline: String? = null,
        routeDeviationMeters: Double? = null
    ): AiSafetyAdvisory {
        val weatherNote = weatherHeadline?.let { " [Weather: $it]" } ?: ""
        val routeNote = if (routeDeviationMeters != null && routeDeviationMeters > 150) " Off-route by ${routeDeviationMeters.toInt()}m." else ""

        return when (riskLevel) {
            RiskLevel.CRITICAL -> AiSafetyAdvisory(
                title = "🚨 Critical Emergency Protocol Activated",
                immediateSteps = listOf(
                    "Immediately cease forward movement and retreat from cliff/landslide perimeter",
                    if (weatherHeadline != null) "Severe weather active: Seek solid rock shelter or cabin immediately" else "Activate SOS Beacon on device - BLE mesh packet broadcasting to peers",
                    "Conserve body heat and battery; shelter near sturdy rock formations",
                    "Do not attempt steep descent in fog or darkness"
                ),
                environmentalThreat = "Severe hazard condition: ${currentZone?.name ?: "Extreme off-trail wilderness"}$weatherNote$routeNote at ${altitudeMeters.toInt()}m altitude.",
                searchRescueBriefing = "Priority 1 Alert. Direct rescue unit with mountain extraction equipment to GPS coordinates.",
                isAiGenerated = false
            )
            RiskLevel.HIGH -> AiSafetyAdvisory(
                title = "⚠️ High Risk Hazard Warning",
                immediateSteps = listOf(
                    if (routeDeviationMeters != null && routeDeviationMeters > 150) "Turn back towards planned itinerary waypoint" else "Turn back 180 degrees towards marked forestry trail #4",
                    "Keep mobile screen on low brightness to conserve battery for SOS relay",
                    if (weatherHeadline != null) "Equip waterproof gear & watch for flash mountain runoff" else "Stay in voice/visual range of fellow hikers"
                ),
                environmentalThreat = (currentZone?.warningMessage ?: "Dangerous terrain slope with low signal coverage") + weatherNote + routeNote,
                searchRescueBriefing = "Tourist entering high-risk perimeter. Automated geofence violation logged in Room database.",
                isAiGenerated = false
            )
            RiskLevel.MEDIUM -> AiSafetyAdvisory(
                title = "Advisory: Elevated Trail Risk",
                immediateSteps = listOf(
                    "Verify footing on wet granite rock paths",
                    if (weatherHeadline != null) "Check forecast radar: $weatherHeadline" else "Check distance to nearest shelter (Upper Bhavani Outpost)",
                    if (routeDeviationMeters != null && routeDeviationMeters > 80) "Align GPS bearing with active route waypoint" else "Maintain steady walking pace before sunset"
                ),
                environmentalThreat = "Moderate slope angle and changing mountain weather.$weatherNote",
                searchRescueBriefing = "Routine telemetry tracking with periodic BLE beacon updates.",
                isAiGenerated = false
            )
            else -> AiSafetyAdvisory(
                title = "Normal Safety Status",
                immediateSteps = listOf(
                    "Follow marked route waypoints",
                    "Stay hydrated and monitor daylight",
                    "Keep BLE radio enabled for mesh safety relay"
                ),
                environmentalThreat = if (weatherHeadline != null) weatherHeadline else "No active geographical threats in sector",
                searchRescueBriefing = "All parameters nominal.",
                isAiGenerated = false
            )
        }
    }
}
