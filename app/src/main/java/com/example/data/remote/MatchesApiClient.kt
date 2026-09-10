package com.example.data.remote

import android.util.Log
import com.example.data.model.MatchEventItem
import com.example.data.model.MatchItem
import com.example.data.model.MatchPlayer
import com.example.data.model.MatchStatisticItem
import com.example.data.model.TeamLineup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MatchesApiClient(
    var apiUrlBase: String = "https://bab-elmoshahd.online/api/index.php?path=matches&day=",
    var apiFootballKey: String = "0f0396f63d80f2bad18ec0e706985c88"
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun fetchMatches(day: Int = 0): List<MatchItem> = withContext(Dispatchers.IO) {
        val matchesList = mutableListOf<MatchItem>()

        // 1. Try API-Football v3 first with direct official API Key
        try {
            val dateStr = getDateString(day)
            val url = "https://v3.football.api-sports.io/fixtures?date=$dateStr"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", apiFootballKey)
                .addHeader("x-rapidapi-key", apiFootballKey)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isNotBlank()) {
                val parsed = parseApiFootballFixtures(jsonStr, dateStr)
                if (parsed.isNotEmpty()) {
                    matchesList.addAll(parsed)
                    Log.d("MatchesApiClient", "Fetched ${parsed.size} fixtures from API-Football for date $dateStr")
                }
            }
        } catch (e: Exception) {
            Log.w("MatchesApiClient", "API-Football fetch error: ${e.message}")
        }

        // 2. Fetch from secondary/legacy stream API (bab-elmoshahd or custom stream_config URL)
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
            if (jsonStr.isNotBlank()) {
                val customMatches = parseMatchesJson(jsonStr)
                if (matchesList.isEmpty()) {
                    matchesList.addAll(customMatches)
                } else {
                    // Enrich API-Football fixtures with streaming links and commentator info from secondary API
                    for (cm in customMatches) {
                        val matchedIndex = matchesList.indexOfFirst {
                            (it.homeTeam.contains(cm.homeTeam, ignoreCase = true) || cm.homeTeam.contains(it.homeTeam, ignoreCase = true)) &&
                            (it.awayTeam.contains(cm.awayTeam, ignoreCase = true) || cm.awayTeam.contains(it.awayTeam, ignoreCase = true))
                        }
                        if (matchedIndex >= 0) {
                            val existing = matchesList[matchedIndex]
                            matchesList[matchedIndex] = existing.copy(
                                streamUrl = if (cm.streamUrl.isNotBlank()) cm.streamUrl else existing.streamUrl,
                                server1 = if (cm.server1.isNotBlank()) cm.server1 else existing.server1,
                                server2 = if (cm.server2.isNotBlank()) cm.server2 else existing.server2,
                                server3 = if (cm.server3.isNotBlank()) cm.server3 else existing.server3,
                                server4 = if (cm.server4.isNotBlank()) cm.server4 else existing.server4,
                                server5 = if (cm.server5.isNotBlank()) cm.server5 else existing.server5,
                                commentator = if (cm.commentator.isNotBlank()) cm.commentator else existing.commentator,
                                channelName = if (cm.channelName.isNotBlank()) cm.channelName else existing.channelName,
                                isLive = existing.isLive || cm.isLive
                            )
                        } else {
                            matchesList.add(cm)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("MatchesApiClient", "Secondary matches API fetch error: ${e.message}")
        }

        matchesList.distinctBy { it.id }
    }

    suspend fun fetchLineups(fixtureId: String): Pair<TeamLineup?, TeamLineup?> = withContext(Dispatchers.IO) {
        try {
            val numId = fixtureId.filter { it.isDigit() }
            if (numId.isBlank()) return@withContext Pair(null, null)

            val url = "https://v3.football.api-sports.io/fixtures/lineups?fixture=$numId"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", apiFootballKey)
                .addHeader("x-rapidapi-key", apiFootballKey)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext Pair(null, null)

            val rootObj = JSONObject(jsonStr)
            val responseArray = rootObj.optJSONArray("response") ?: return@withContext Pair(null, null)
            if (responseArray.length() < 2) return@withContext Pair(null, null)

            val homeObj = responseArray.optJSONObject(0)
            val awayObj = responseArray.optJSONObject(1)

            val homeLineup = parseSingleTeamLineup(homeObj)
            val awayLineup = parseSingleTeamLineup(awayObj)
            Pair(homeLineup, awayLineup)
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error fetching lineups for $fixtureId: ${e.message}")
            Pair(null, null)
        }
    }

    suspend fun fetchEvents(fixtureId: String): List<MatchEventItem> = withContext(Dispatchers.IO) {
        try {
            val numId = fixtureId.filter { it.isDigit() }
            if (numId.isBlank()) return@withContext emptyList()

            val url = "https://v3.football.api-sports.io/fixtures/events?fixture=$numId"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", apiFootballKey)
                .addHeader("x-rapidapi-key", apiFootballKey)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val rootObj = JSONObject(jsonStr)
            val responseArray = rootObj.optJSONArray("response") ?: return@withContext emptyList()
            val eventsList = mutableListOf<MatchEventItem>()

            for (i in 0 until responseArray.length()) {
                val obj = responseArray.optJSONObject(i) ?: continue
                val timeObj = obj.optJSONObject("time")
                val elapsed = timeObj?.optInt("elapsed", 0) ?: 0
                val extra = timeObj?.optString("extra", "").orEmpty()
                val minuteStr = if (extra.isNotBlank() && extra != "null") "$elapsed+$extra'" else "$elapsed'"

                val teamName = obj.optJSONObject("team")?.optString("name", "").orEmpty()
                val playerName = obj.optJSONObject("player")?.optString("name", "").orEmpty()
                val assistName = obj.optJSONObject("assist")?.optString("name", "").orEmpty()
                val type = obj.optString("type", "")
                val detail = obj.optString("detail", "")

                eventsList.add(
                    MatchEventItem(
                        minute = minuteStr,
                        teamName = teamName,
                        playerName = playerName,
                        assistPlayer = if (assistName != "null") assistName else "",
                        type = type,
                        detail = detail
                    )
                )
            }
            eventsList
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error fetching events for $fixtureId: ${e.message}")
            emptyList()
        }
    }

    suspend fun fetchStatistics(fixtureId: String): List<MatchStatisticItem> = withContext(Dispatchers.IO) {
        try {
            val numId = fixtureId.filter { it.isDigit() }
            if (numId.isBlank()) return@withContext emptyList()

            val url = "https://v3.football.api-sports.io/fixtures/statistics?fixture=$numId"
            val request = Request.Builder()
                .url(url)
                .addHeader("x-apisports-key", apiFootballKey)
                .addHeader("x-rapidapi-key", apiFootballKey)
                .addHeader("Accept", "application/json")
                .build()

            val response = client.newCall(request).execute()
            val jsonStr = response.body?.string().orEmpty()
            if (jsonStr.isBlank()) return@withContext emptyList()

            val rootObj = JSONObject(jsonStr)
            val responseArray = rootObj.optJSONArray("response") ?: return@withContext emptyList()
            if (responseArray.length() < 2) return@withContext emptyList()

            val homeObj = responseArray.optJSONObject(0)
            val awayObj = responseArray.optJSONObject(1)

            val homeStatsArray = homeObj?.optJSONArray("statistics") ?: return@withContext emptyList()
            val awayStatsArray = awayObj?.optJSONArray("statistics") ?: return@withContext emptyList()

            val awayStatsMap = mutableMapOf<String, String>()
            for (i in 0 until awayStatsArray.length()) {
                val item = awayStatsArray.optJSONObject(i) ?: continue
                val type = item.optString("type", "")
                val value = item.opt("value")?.toString()?.replace("null", "0") ?: "0"
                if (type.isNotBlank()) {
                    awayStatsMap[type.lowercase()] = value
                }
            }

            val statsList = mutableListOf<MatchStatisticItem>()
            for (i in 0 until homeStatsArray.length()) {
                val item = homeStatsArray.optJSONObject(i) ?: continue
                val type = item.optString("type", "")
                if (type.isBlank()) continue

                val homeValStr = item.opt("value")?.toString()?.replace("null", "0") ?: "0"
                val awayValStr = awayStatsMap[type.lowercase()] ?: "0"

                val arabicName = when (type.lowercase()) {
                    "ball possession" -> "الاستحواذ على الكرة"
                    "total shots" -> "إجمالي التسديدات"
                    "shots on goal" -> "التسديدات على المرمى"
                    "shots off goal" -> "التسديدات خارج المرمى"
                    "blocked shots" -> "تسديدات تصدى لها الدفاع"
                    "shots insidebox" -> "تسديدات من داخل المنطقة"
                    "shots outsidebox" -> "تسديدات من خارج المنطقة"
                    "corner kicks" -> "الضربات الركنية"
                    "offsides" -> "حالات التسلل"
                    "fouls" -> "الأخطاء المرتكبة"
                    "yellow cards" -> "البطاقات الصفراء"
                    "red cards" -> "البطاقات الحمراء"
                    "goalkeeper saves" -> "تصديات حارس المرمى"
                    "total passes" -> "إجمالي التمريرات"
                    "passes accurate" -> "التمريرات الناجحة"
                    "passes %" -> "دقة التمرير"
                    "expected_goals" -> "الأهداف المتوقعة (xG)"
                    else -> type
                }

                val homeNum = homeValStr.replace("%", "").trim().toFloatOrNull() ?: 0f
                val awayNum = awayValStr.replace("%", "").trim().toFloatOrNull() ?: 0f
                val total = homeNum + awayNum

                val homePct = if (total > 0f) (homeNum / total).coerceIn(0f, 1f) else 0.5f
                val awayPct = if (total > 0f) (awayNum / total).coerceIn(0f, 1f) else 0.5f

                statsList.add(
                    MatchStatisticItem(
                        type = type,
                        typeArabic = arabicName,
                        homeValue = homeValStr,
                        awayValue = awayValStr,
                        homePercent = homePct,
                        awayPercent = awayPct
                    )
                )
            }
            statsList
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error fetching statistics for $fixtureId: ${e.message}")
            emptyList()
        }
    }

    private fun parseSingleTeamLineup(obj: JSONObject?): TeamLineup? {
        if (obj == null) return null
        val teamObj = obj.optJSONObject("team")
        val teamName = teamObj?.optString("name", "الفريق").orEmpty()
        val teamLogo = teamObj?.optString("logo", "").orEmpty()
        val coachName = obj.optJSONObject("coach")?.optString("name", "").orEmpty()
        val formation = obj.optString("formation", "4-3-3")

        val starters = mutableListOf<MatchPlayer>()
        val startArray = obj.optJSONArray("startXI")
        if (startArray != null) {
            for (i in 0 until startArray.length()) {
                val pObj = startArray.optJSONObject(i)?.optJSONObject("player") ?: continue
                starters.add(
                    MatchPlayer(
                        id = pObj.optString("id", ""),
                        name = pObj.optString("name", "لاعب"),
                        number = pObj.optInt("number", i + 1),
                        position = pObj.optString("pos", ""),
                        isStarter = true
                    )
                )
            }
        }

        val subs = mutableListOf<MatchPlayer>()
        val subArray = obj.optJSONArray("substitutes")
        if (subArray != null) {
            for (i in 0 until subArray.length()) {
                val pObj = subArray.optJSONObject(i)?.optJSONObject("player") ?: continue
                subs.add(
                    MatchPlayer(
                        id = pObj.optString("id", ""),
                        name = pObj.optString("name", "بديل"),
                        number = pObj.optInt("number", i + 12),
                        position = pObj.optString("pos", ""),
                        isStarter = false
                    )
                )
            }
        }

        return TeamLineup(
            teamName = teamName,
            teamLogo = teamLogo,
            formation = formation,
            coachName = coachName,
            starters = starters,
            substitutes = subs
        )
    }

    private fun parseApiFootballFixtures(jsonStr: String, dateStr: String): List<MatchItem> {
        val list = mutableListOf<MatchItem>()
        try {
            val rootObj = JSONObject(jsonStr)
            val responseArray = rootObj.optJSONArray("response") ?: return emptyList()

            for (i in 0 until responseArray.length()) {
                val item = responseArray.optJSONObject(i) ?: continue
                val fixture = item.optJSONObject("fixture") ?: continue
                val league = item.optJSONObject("league") ?: continue
                val teams = item.optJSONObject("teams") ?: continue
                val goals = item.optJSONObject("goals")

                val fixId = fixture.optString("id", "apif_$i")
                val dateIso = fixture.optString("date", "")
                val venue = fixture.optJSONObject("venue")?.optString("name", "الملعب الرئيسي").orEmpty()
                val statusObj = fixture.optJSONObject("status")
                val shortStatus = statusObj?.optString("short", "NS").orEmpty()
                val elapsed = statusObj?.optInt("elapsed", 0) ?: 0

                val homeObj = teams.optJSONObject("home")
                val awayObj = teams.optJSONObject("away")

                val homeName = homeObj?.optString("name", "المستضيف").orEmpty()
                val homeLogo = homeObj?.optString("logo", "").orEmpty()
                val awayName = awayObj?.optString("name", "الضيف").orEmpty()
                val awayLogo = awayObj?.optString("logo", "").orEmpty()

                val homeGoals = if (goals != null && !goals.isNull("home")) goals.optInt("home", 0) else 0
                val awayGoals = if (goals != null && !goals.isNull("away")) goals.optInt("away", 0) else 0

                val leagueName = league.optString("name", "مباريات اليوم")
                val leagueLogo = league.optString("logo", "")

                val timeStr = formatIsoTimeToArabic(dateIso)
                val isLive = shortStatus in listOf("1H", "HT", "2H", "ET", "P", "LIVE", "BT")
                val isEnded = shortStatus in listOf("FT", "AET", "PEN")

                val displayStatus = when {
                    isLive -> if (elapsed > 0) "$elapsed' ($homeGoals - $awayGoals)" else "مباشر ($homeGoals - $awayGoals)"
                    isEnded -> "انتهت ($homeGoals - $awayGoals)"
                    else -> "لم تبدأ"
                }

                list.add(
                    MatchItem(
                        id = "apif_$fixId",
                        leagueName = leagueName,
                        leagueIconUrl = leagueLogo,
                        homeTeam = homeName,
                        homeLogoUrl = homeLogo,
                        awayTeam = awayName,
                        awayLogoUrl = awayLogo,
                        matchTime = timeStr,
                        matchDate = dateStr,
                        status = displayStatus,
                        homeScore = homeGoals,
                        awayScore = awayGoals,
                        streamUrl = "",
                        isLive = isLive,
                        isFavorite = false,
                        stadium = venue,
                        commentator = "بث مباشر حصري",
                        channelName = "beIN SPORTS HD",
                        isManual = false
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("MatchesApiClient", "Error parsing API-Football response: ${e.message}", e)
        }
        return list
    }

    private fun formatIsoTimeToArabic(isoDate: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val date = inputFormat.parse(isoDate) ?: return "09:00 م"
            val outputFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
            outputFormat.format(date)
        } catch (e: Exception) {
            "09:00 م"
        }
    }

    private fun getDateString(dayOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, dayOffset)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
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

        val stadium = obj.optString("stadium", obj.optString("venue", obj.optString("ground", "الملعب الرئيسي")))
        val commentator = obj.optString("commentator", obj.optString("voice", obj.optString("speaker", "المعلق المعتمد")))
        val channelName = obj.optString("channel", obj.optString("tv", obj.optString("channel_name", "beIN SPORTS HD")))

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

        val s1 = obj.optString("server1", obj.optString("server_1", ""))
        val s2 = obj.optString("server2", obj.optString("server_2", ""))
        val s3 = obj.optString("server3", obj.optString("server_3", ""))
        val s4 = obj.optString("server4", obj.optString("server_4", ""))
        val s5 = obj.optString("server5", obj.optString("server_5", ""))

        val rawStream = obj.optString("stream_url", obj.optString("live_url", obj.optString("server", obj.optString("link", obj.optString("url", "")))))
        val streamUrl = listOf(rawStream, s1, s2, s3).firstOrNull { it.isNotBlank() } ?: ""

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
            isFavorite = false,
            stadium = stadium,
            commentator = commentator,
            channelName = channelName,
            server1 = s1,
            server2 = s2,
            server3 = s3,
            server4 = s4,
            server5 = s5
        )
    }
}
