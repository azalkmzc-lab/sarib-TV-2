package com.example.data.remote

import android.util.Log
import com.example.data.model.MatchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MatchesApiClient(
    var apiUrlBase: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day="
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMatches(day: Int = 0): List<MatchItem> = withContext(Dispatchers.IO) {
        try {
            val url = if (apiUrlBase.endsWith("=") || apiUrlBase.endsWith("&day=")) {
                "$apiUrlBase$day"
            } else if (apiUrlBase.contains("day=")) {
                apiUrlBase.replace(Regex("day=[-0-9]+"), "day=$day")
            } else {
                "${apiUrlBase.trimEnd('&', '?')}&day=$day"
            }

            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Android; SARIB TV App)")
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            parseMatchesJson(jsonStr)
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error fetching matches from API: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseMatchesJson(jsonStr: String): List<MatchItem> {
        val matchesList = mutableListOf<MatchItem>()
        try {
            val trimmed = jsonStr.trim()
            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parseMatchObject(obj, i)?.let { matchesList.add(it) }
                }
            } else if (trimmed.startsWith("{")) {
                val rootObj = JSONObject(trimmed)
                // Might have "matches", "data", "result", or "items"
                val array = rootObj.optJSONArray("matches")
                    ?: rootObj.optJSONArray("data")
                    ?: rootObj.optJSONArray("result")
                    ?: rootObj.optJSONArray("items")

                if (array != null) {
                    for (i in 0 until array.length()) {
                        val obj = array.optJSONObject(i) ?: continue
                        parseMatchObject(obj, i)?.let { matchesList.add(it) }
                    }
                } else {
                    // Try iterating keys if categories are keyed
                    val keys = rootObj.keys()
                    var index = 0
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = rootObj.opt(key)
                        if (value is JSONArray) {
                            for (i in 0 until value.length()) {
                                val obj = value.optJSONObject(i) ?: continue
                                parseMatchObject(obj, index++)?.let { matchesList.add(it) }
                            }
                        } else if (value is JSONObject) {
                            parseMatchObject(value, index++)?.let { matchesList.add(it) }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error parsing matches JSON: ${e.message}", e)
        }
        return matchesList
    }

    private fun parseMatchObject(obj: JSONObject, index: Int): MatchItem? {
        val id = obj.optString("id", obj.optString("match_id", "m_$index"))
        val homeTeam = obj.optString("home_team", obj.optString("team1", obj.optString("homeTeam", obj.optString("team_home", obj.optString("first_team", "الفريق المضيف")))))
        val awayTeam = obj.optString("away_team", obj.optString("team2", obj.optString("awayTeam", obj.optString("team_away", obj.optString("second_team", "الفريق الضيف")))))

        if (homeTeam.isBlank() && awayTeam.isBlank()) return null

        val league = obj.optString("league", obj.optString("championship", obj.optString("league_name", obj.optString("tournament", "مباريات اليوم"))))
        val leagueIcon = obj.optString("league_icon", obj.optString("league_logo", obj.optString("championship_logo", "")))

        val homeLogo = obj.optString("home_logo", obj.optString("team1_logo", obj.optString("home_icon", obj.optString("team_home_logo", ""))))
        val awayLogo = obj.optString("away_logo", obj.optString("team2_logo", obj.optString("away_icon", obj.optString("team_away_logo", ""))))

        val time = obj.optString("match_time", obj.optString("time", obj.optString("start_time", "09:00 م")))
        val date = obj.optString("match_date", obj.optString("date", "اليوم"))

        val rawStatus = obj.optString("status", obj.optString("match_status", "لم تبدأ"))
        val homeScore = obj.optInt("home_score", obj.optInt("team1_score", obj.optInt("score1", 0)))
        val awayScore = obj.optInt("away_score", obj.optInt("team2_score", obj.optInt("score2", 0)))

        val isLive = rawStatus.contains("مباشر", ignoreCase = true) ||
                rawStatus.contains("live", ignoreCase = true) ||
                rawStatus.contains("شوط", ignoreCase = true) ||
                rawStatus.contains("دقيقة", ignoreCase = true) ||
                rawStatus.contains("جارية", ignoreCase = true)

        val status = when {
            isLive -> "$homeScore - $awayScore"
            rawStatus.contains("انتهت", ignoreCase = true) || rawStatus.contains("ft", ignoreCase = true) || rawStatus.contains("ended", ignoreCase = true) -> "$homeScore - $awayScore"
            else -> "لم تبدأ"
        }

        // Try extracting stream link if any
        val streamUrl = obj.optString("stream_url", obj.optString("live_url", obj.optString("server", obj.optString("link", ""))))

        return MatchItem(
            id = id,
            leagueName = league,
            leagueIconUrl = leagueIcon,
            homeTeam = homeTeam,
            homeLogoUrl = homeLogo,
            awayTeam = awayTeam,
            awayLogoUrl = awayLogo,
            matchTime = time,
            matchDate = date,
            status = status,
            homeScore = homeScore,
            awayScore = awayScore,
            streamUrl = streamUrl,
            isLive = isLive,
            isFavorite = false
        )
    }
}
