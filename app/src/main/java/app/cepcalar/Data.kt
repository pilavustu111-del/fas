package app.cepcalar

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Song(
    val mediaId: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val isVideo: Boolean,
    val artUrl: String? = null,
    val remote: Boolean = false,
    val link: String? = null,
) {
    val statKey: String get() = "$title|$artist".lowercase()
}

fun Song.toItem(): MediaItem {
    val extras = Bundle().apply {
        putBoolean("video", isVideo)
        putBoolean("remote", remote)
        putString("art", artUrl)
        putString("album", album)
        putString("link", link)
        putLong("dur", durationMs)
    }
    return MediaItem.Builder()
        .setMediaId(mediaId)
        .setUri(uri)
        .setRequestMetadata(MediaItem.RequestMetadata.Builder().setMediaUri(uri).build())
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist.ifEmpty { null })
                .setExtras(extras)
                .build()
        )
        .build()
}

fun songFromItem(i: MediaItem): Song {
    val md = i.mediaMetadata
    val ex = md.extras
    return Song(
        mediaId = i.mediaId,
        uri = i.localConfiguration?.uri ?: i.requestMetadata.mediaUri ?: Uri.EMPTY,
        title = md.title?.toString() ?: "",
        artist = md.artist?.toString() ?: "",
        album = ex?.getString("album") ?: "",
        durationMs = ex?.getLong("dur") ?: 0L,
        isVideo = ex?.getBoolean("video") == true,
        artUrl = ex?.getString("art"),
        remote = ex?.getBoolean("remote") == true,
        link = ex?.getString("link"),
    )
}

data class LrcLine(val timeMs: Long, val text: String)
data class Lyrics(val synced: Boolean, val lines: List<LrcLine>)
data class Artist(val id: Long, val name: String, val pic: String)

/** Telefondaki müzik ve klipleri MediaStore üzerinden otomatik bulur. */
object Library {
    private fun clean(s: String?): String {
        val v = s?.trim() ?: ""
        return if (v.isEmpty() || v == "<unknown>" || v.equals("unknown", true)) "" else v
    }

    private fun split(title: String, artist: String): Pair<String, String> {
        if (artist.isEmpty()) {
            val i = title.indexOf(" - ")
            if (i > 0) return Pair(title.substring(i + 3).trim(), title.substring(0, i).trim())
        }
        return Pair(title, artist)
    }

    fun scan(ctx: Context, allVideos: Boolean): List<Song> {
        val out = ArrayList<Song>()
        val cr = ctx.contentResolver

        runCatching {
            val proj = arrayOf(
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
            )
            cr.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, proj,
                "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000",
                null, "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val (t, a) = split(clean(c.getString(1)), clean(c.getString(2)))
                    out.add(
                        Song(
                            mediaId = "a$id",
                            uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                            title = t.ifEmpty { "Adsız" }, artist = a, album = clean(c.getString(3)),
                            durationMs = c.getLong(4), isVideo = false,
                        )
                    )
                }
            }
        }

        runCatching {
            val proj = arrayOf(
                MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.ARTIST, MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.RELATIVE_PATH,
            )
            val musicPath = Regex("(?i)(music|müzik|muzik|klip|clip|şarkı|sarki|song)")
            val skipPath = Regex("(?i)(dcim|whatsapp|camera|screen|telegram|snapchat|instagram)")
            cr.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
                "${MediaStore.Video.Media.DURATION} >= 30000", null,
                "${MediaStore.Video.Media.DISPLAY_NAME} COLLATE NOCASE ASC"
            )?.use { c ->
                while (c.moveToNext()) {
                    val id = c.getLong(0)
                    val name = (c.getString(1) ?: "").substringBeforeLast('.')
                    val artistRaw = clean(c.getString(2))
                    val path = c.getString(4) ?: ""
                    val ok = allVideos || artistRaw.isNotEmpty() ||
                        (musicPath.containsMatchIn(path) && !skipPath.containsMatchIn(path))
                    if (!ok) continue
                    val (t, a) = split(name.replace('_', ' '), artistRaw)
                    out.add(
                        Song(
                            mediaId = "v$id",
                            uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id),
                            title = t.ifEmpty { "Adsız klip" }, artist = a, album = "",
                            durationMs = c.getLong(3), isVideo = true,
                        )
                    )
                }
            }
        }
        return out
    }
}

/** Dinleme sayıları ve ayarlar. */
object Store {
    private fun sp(c: Context) = c.getSharedPreferences("cep", Context.MODE_PRIVATE)

    fun loadStats(c: Context): Map<String, Int> {
        val j = runCatching { JSONObject(sp(c).getString("stats", "{}") ?: "{}") }.getOrDefault(JSONObject())
        val m = HashMap<String, Int>()
        for (k in j.keys()) m[k] = j.optInt(k, 0)
        return m
    }

    fun inc(c: Context, key: String): Map<String, Int> {
        val m = loadStats(c).toMutableMap()
        m[key] = (m[key] ?: 0) + 1
        sp(c).edit().putString("stats", JSONObject(m as Map<*, *>).toString()).apply()
        return m
    }

    // Asistanın öğrendiği: hangi ruh halinde hangi parçayı sonuna kadar dinledin
    fun moodMap(c: Context): Map<String, Int> {
        val o = runCatching { JSONObject(sp(c).getString("moodmap", "{}") ?: "{}") }.getOrDefault(JSONObject())
        val m = HashMap<String, Int>()
        o.keys().forEach { m[it] = o.optInt(it) }
        return m
    }

    fun bumpMood(c: Context, mood: String, key: String) {
        val m = moodMap(c).toMutableMap()
        val k = "$mood|$key"
        m[k] = (m[k] ?: 0) + 1
        sp(c).edit().putString("moodmap", JSONObject(m as Map<*, *>).toString()).apply()
    }

    fun getTheme(c: Context) = sp(c).getInt("themeMode", 0) // 0 otomatik, 1 gündüz, 2 gece
    fun setTheme(c: Context, v: Int) = sp(c).edit().putInt("themeMode", v).apply()
    fun getName(c: Context) = sp(c).getString("userName", "Mikail") ?: "Mikail"
    fun setName(c: Context, v: String) = sp(c).edit().putString("userName", v.trim()).apply()
    fun getVoiceOn(c: Context) = sp(c).getBoolean("voiceOn", false)
    fun setVoiceOn(c: Context, v: Boolean) = sp(c).edit().putBoolean("voiceOn", v).apply()
    fun getMuted(c: Context) = sp(c).getBoolean("muted", false)
    fun setMuted(c: Context, v: Boolean) = sp(c).edit().putBoolean("muted", v).apply()

    const val DEFAULT_MODEL = "gemini-flash-latest"
    fun getModel(c: Context) = sp(c).getString("geminiModel", DEFAULT_MODEL)?.ifBlank { DEFAULT_MODEL } ?: DEFAULT_MODEL
    fun setModel(c: Context, m: String) = sp(c).edit().putString("geminiModel", m.trim()).apply()
    fun getKey(c: Context) = sp(c).getString("geminiKey", "") ?: ""
    fun setKey(c: Context, k: String) = sp(c).edit().putString("geminiKey", k.trim()).apply()

    fun loadLikes(c: Context): List<Song> {
        val arr = runCatching { JSONArray(sp(c).getString("likes", "[]") ?: "[]") }.getOrDefault(JSONArray())
        val out = ArrayList<Song>()
        for (i in 0 until arr.length()) {
            val o = arr.optJSONObject(i) ?: continue
            out.add(
                Song(
                    mediaId = o.optString("id"), uri = Uri.parse(o.optString("preview")),
                    title = o.optString("title"), artist = o.optString("artist"),
                    album = o.optString("album"), durationMs = 30000L, isVideo = false,
                    artUrl = o.optString("art").ifEmpty { null }, remote = true,
                    link = o.optString("link").ifEmpty { null },
                )
            )
        }
        return out
    }

    fun saveLikes(c: Context, list: List<Song>) {
        val arr = JSONArray()
        for (s in list) {
            arr.put(
                JSONObject().put("id", s.mediaId).put("preview", s.uri.toString())
                    .put("title", s.title).put("artist", s.artist).put("album", s.album)
                    .put("art", s.artUrl ?: "").put("link", s.link ?: "")
            )
        }
        sp(c).edit().putString("likes", arr.toString()).apply()
    }

    fun getAllVideos(c: Context) = sp(c).getBoolean("allVideos", false)
    fun setAllVideos(c: Context, v: Boolean) = sp(c).edit().putBoolean("allVideos", v).apply()
}

/** İnternet: sözler (lrclib.net) ve benzer sanatçılar (Deezer). İkisi de anahtarsız. */
object Net {
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    private fun get(url: String): String? = try {
        val c = URL(url).openConnection() as HttpURLConnection
        c.connectTimeout = 8000
        c.readTimeout = 8000
        c.setRequestProperty("User-Agent", "Yirdas/1.0")
        if (c.responseCode in 200..299) c.inputStream.bufferedReader().use { it.readText() } else null
    } catch (e: Exception) {
        null
    }

    private fun parseLrc(raw: String): Lyrics? {
        if (raw.length < 2) return null
        val body = raw.substring(1)
        if (raw[0] == 'S') {
            val re = Regex("\\[(\\d+):(\\d+(?:[.,]\\d+)?)\\]")
            val out = ArrayList<LrcLine>()
            for (line in body.lines()) {
                val text = line.replace(Regex("\\[[^\\]]*\\]"), "").trim()
                for (m in re.findAll(line)) {
                    val t = (m.groupValues[1].toLong() * 60000 +
                        (m.groupValues[2].replace(',', '.').toDouble() * 1000).toLong())
                    out.add(LrcLine(t, text))
                }
            }
            out.sortBy { it.timeMs }
            return if (out.isEmpty()) null else Lyrics(true, out)
        }
        return Lyrics(false, body.lines().map { LrcLine(-1, it) })
    }

    fun lyrics(ctx: Context, s: Song): Lyrics? {
        val f = File(ctx.filesDir, "lrc_" + s.statKey.hashCode() + ".txt")
        if (f.exists()) return parseLrc(f.readText())
        var o: JSONObject? = null
        if (s.artist.isNotEmpty()) {
            val body = get(
                "https://lrclib.net/api/get?artist_name=${enc(s.artist)}&track_name=${enc(s.title)}" +
                    "&duration=${s.durationMs / 1000}"
            )
            o = body?.let { runCatching { JSONObject(it) }.getOrNull() }
        }
        if (o == null) {
            val body = get("https://lrclib.net/api/search?q=" + enc((s.artist + " " + s.title).trim()))
            val arr = body?.let { runCatching { JSONArray(it) }.getOrNull() }
            if (arr != null && arr.length() > 0) o = arr.optJSONObject(0)
        }
        if (o == null) return null
        val synced = o.optString("syncedLyrics", "").takeIf { it.isNotBlank() && it != "null" }
        val plain = o.optString("plainLyrics", "").takeIf { it.isNotBlank() && it != "null" }
        val raw = when {
            synced != null -> "S$synced"
            plain != null -> "P$plain"
            else -> return null
        }
        runCatching { f.writeText(raw) }
        return parseLrc(raw)
    }

    class ApiResult(val text: String?, val error: String?)

    /** Google Gemini API (AI Studio'nun ücretsiz katmanı). Anahtar kullanıcıdan alınır, telefonda saklanır. */
    fun ask(key: String, model: String, system: String, msgs: List<Pair<String, String>>): ApiResult = try {
        val c = URL("https://generativelanguage.googleapis.com/v1beta/models/${enc(model)}:generateContent")
            .openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.connectTimeout = 15000
        c.readTimeout = 90000
        c.doOutput = true
        c.setRequestProperty("Content-Type", "application/json")
        c.setRequestProperty("x-goog-api-key", key)
        val contents = JSONArray()
        for ((role, text) in msgs) {
            contents.put(
                JSONObject().put("role", if (role == "assistant") "model" else "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", text)))
            )
        }
        val body = JSONObject()
            .put("systemInstruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("responseMimeType", "application/json"))
        c.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
        val code = c.responseCode
        val txt = (if (code in 200..299) c.inputStream else c.errorStream)
            ?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) {
            ApiResult(
                null, when (code) {
                    400 -> "İstek reddedildi. API anahtarı yanlış olabilir, ayar simgesinden kontrol et."
                    401, 403 -> "API anahtarı geçersiz ya da yetkisiz. Ayar simgesinden kontrol et."
                    404 -> "Model adı bulunamadı. Ayar simgesinden modeli kontrol et."
                    429 -> "Ücretsiz günlük/dakikalık sınıra takıldın. Biraz bekleyip tekrar dene."
                    else -> "Sunucu hatası ($code)."
                }
            )
        } else {
            val parts = JSONObject(txt).getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts")
            val sb = StringBuilder()
            for (i in 0 until parts.length()) sb.append(parts.getJSONObject(i).optString("text", ""))
            ApiResult(sb.toString(), null)
        }
    } catch (e: Exception) {
        ApiResult(null, "Sunucuya ulaşılamadı ya da cevap okunamadı. İnternetini kontrol et.")
    }

    fun searchArtist(name: String): Artist? {
        val body = get("https://api.deezer.com/search/artist?q=${enc(name)}&limit=1") ?: return null
        val d = runCatching { JSONObject(body).optJSONArray("data") }.getOrNull() ?: return null
        val a = d.optJSONObject(0) ?: return null
        return Artist(a.optLong("id"), a.optString("name"), a.optString("picture_medium"))
    }

    fun related(id: Long, limit: Int = 8): List<Artist> {
        val body = get("https://api.deezer.com/artist/$id/related?limit=$limit") ?: return emptyList()
        val d = runCatching { JSONObject(body).optJSONArray("data") }.getOrNull() ?: return emptyList()
        val out = ArrayList<Artist>()
        for (i in 0 until d.length()) {
            val a = d.optJSONObject(i) ?: continue
            out.add(Artist(a.optLong("id"), a.optString("name"), a.optString("picture_medium")))
        }
        return out
    }

    fun topTracks(id: Long): List<Song> {
        val body = get("https://api.deezer.com/artist/$id/top?limit=15") ?: return emptyList()
        val d = runCatching { JSONObject(body).optJSONArray("data") }.getOrNull() ?: return emptyList()
        val out = ArrayList<Song>()
        for (i in 0 until d.length()) {
            val t = d.optJSONObject(i) ?: continue
            val prev = t.optString("preview")
            if (prev.isBlank()) continue
            out.add(
                Song(
                    mediaId = "d" + t.optLong("id"), uri = Uri.parse(prev),
                    title = t.optString("title"),
                    artist = t.optJSONObject("artist")?.optString("name") ?: "",
                    album = t.optJSONObject("album")?.optString("title") ?: "",
                    durationMs = 30000, isVideo = false,
                    artUrl = t.optJSONObject("album")?.optString("cover_medium"), remote = true,
                    link = t.optString("link").ifEmpty { null },
                )
            )
        }
        return out
    }
}

data class AiPlan(val reply: String, val picks: List<Int>, val discover: List<String>)

/** Yapay zekâ asistanın talimatı ve cevap ayrıştırıcısı. */
object Ai {
    private val PROMPT = """
Sen "Yırdaş" müzik uygulamasının yapay zekâ asistanısın. Kullanıcı Türkçe yazar ya da konuşur; ruh halini veya ne dinlemek istediğini söyler. Aşağıda kullanıcının telefonundaki şarkıların listesi var (no|şarkı|sanatçı|dinlenme sayısı).

Yalnızca tek bir JSON nesnesi döndür, başka hiçbir şey yazma:
{"reply":"1-2 cümlelik sıcak, kısa Türkçe cevap","picks":[liste numaraları],"discover":["sanatçı adı"]}

Kurallar:
- picks: yalnızca listedeki numaralar, çalınma sırasıyla, en fazla 25. Numara uydurma.
- İsteğe göre ruh halini seç. "Ruh halim kötü, solgun/hüzünlü şarkılar aç" denirse hüzünlü, yavaş, duygusal parçalar. "İyileştirecek, neşelendirecek, heyecanlı, oyun havası" denirse hareketli, neşeli, enerjik parçalar (halay, oyun havası, kavak havaları dahil). Karar verirken sanatçıyı, şarkı adını ve bildiğin tür bilgisini kullan; çok dinlenen sanatçı ve parçalara öncelik ver.
- discover: kullanıcının zevkine ve istediği ruh haline uyan, listede olmayan en çok 4 sanatçı adı (telefonda olmayan yeni keşifler). Gerek yoksa boş bırak.
- Liste boşsa ya da uygun parça yoksa picks boş kalsın, reply'da bunu dürüstçe söyle.
- Müzik dışı bir şey sorulursa kısaca müziğe yönlendir.
""".trimIndent()

    fun system(lib: String) = PROMPT + "\n\nŞARKI LİSTESİ:\n" + lib

    fun parse(raw: String): AiPlan? {
        val s = raw.indexOf('{')
        val e = raw.lastIndexOf('}')
        if (s < 0 || e <= s) return null
        val o = runCatching { JSONObject(raw.substring(s, e + 1)) }.getOrNull() ?: return null
        val picks = ArrayList<Int>()
        o.optJSONArray("picks")?.let { a ->
            for (i in 0 until a.length()) {
                val v = a.opt(i)
                val n = when (v) {
                    is Number -> v.toInt()
                    is String -> v.trim().toIntOrNull() ?: -1
                    else -> -1
                }
                if (n >= 0) picks.add(n)
            }
        }
        val disc = ArrayList<String>()
        o.optJSONArray("discover")?.let { a ->
            for (i in 0 until a.length()) {
                val n = a.optString(i, "").trim()
                if (n.isNotEmpty()) disc.add(n)
            }
        }
        return AiPlan(o.optString("reply", "").ifBlank { "Hazırım." }, picks, disc)
    }
}

class LocalPlan(val mood: String?, val picks: List<Song>, val reply: String)

/** Telefonun içinde çalışan, internetsiz ve anahtarsız asistan: ruh halini anlar, parçaları puanlar, dinledikçe öğrenir. */
object LocalAi {
    private val UP_ASK = listOf("iyileş", "neşel", "moral", "düzel", "hareketli", "enerji", "oyun", "halay", "kavak", "heyecan", "coş", "dans", "parti", "spor", "canlı", "eğlen")
    private val SAD_ASK = listOf("solgun", "hüzün", "ağla", "yalnız", "dertli", "buruk", "melankol", "duygusal", "damar", "acı", "yas", "kırık")
    private val CALM_ASK = listOf("sakin", "huzur", "rahat", "uyku", "dinlen", "akustik", "yavaş", "relax", "stres")
    private val BAD = listOf("kötü", "üzgün", "mutsuz", "canım sıkkın", "bunal", "moralim bozuk")

    private val UP_KW = listOf("oyun", "halay", "kavak", "dans", "parti", "party", "remix", "mix", "bounce", "club", "hop", "trap", "drill", "enerji", "coş", "bomba", "yırt", "hızlı", "2.0", "feat", "ft.")
    private val SAD_KW = listOf("ağla", "yalnız", "ayrılık", "veda", "gözyaş", "keşke", "gitme", "ölmek", "özle", "hasret", "yara", "acı", "bitti", "unut", "hüzün", "slowed", "yağmur", "affet", "gece", "kal ", "sensiz", "bensiz", "vazgeç", "yok", "ağlama")
    private val CALM_KW = listOf("akustik", "acoustic", "piano", "piyano", "lofi", "lo-fi", "slow", "sakin", "huzur", "uyku", "enstrüman", "instrumental", "ambient", "ninni")

    private fun has(t: String, l: List<String>) = l.any { t.contains(it) }

    fun mood(text: String): String? {
        val t = text.lowercase()
        return when {
            has(t, UP_ASK) -> "up"
            has(t, SAD_ASK) -> "sad"
            has(t, CALM_ASK) -> "calm"
            has(t, BAD) -> "up" // sadece "ruh halim kötü" dediyse iyileştirmeye çalış
            else -> null
        }
    }

    private val MUSIC_VERBS = listOf("aç", "çal", "dinle", "şarkı", "müzik", "liste", "parça")

    /** Müzik isteği olmayan konuşmalara hazır cevaplar. Müzik isteğiyse null döner. */
    fun chat(text: String): String? {
        val t = text.lowercase()
        fun any(vararg k: String) = k.any { t.contains(it) }
        if (mood(t) != null || has(t, MUSIC_VERBS)) return null
        return when {
            any("merhaba", "selam", "hey", "günaydın", "iyi akşamlar") -> "Merhaba! Ruh halin nasıl, ne dinlemek istersin?"
            any("nasılsın", "naber", "ne haber") -> "İyiyim, teşekkür ederim. Sen nasılsın? İstersen ruh haline göre bir şeyler çalayım."
            any("adın", "kimsin", "ismin") -> "Ben Yırdaş, müzik asistanınım."
            any("ne yapabil", "yardım", "neler yap") -> "Ruh haline göre şarkı seçip çalabilirim. Duraklat, devam, sonraki, önceki de diyebilirsin. Mesela hüzünlü şarkılar aç de."
            any("teşekkür", "sağ ol", "sağol") -> "Rica ederim!"
            any("saat kaç") -> "Saat " + java.text.SimpleDateFormat("HH:mm", java.util.Locale("tr")).format(java.util.Date()) + "."
            else -> "Bunu tam anlayamadım. Ruh halini söyle ya da hüzünlü şarkılar aç gibi bir şey de."
        }
    }

    private fun hits(t: String, l: List<String>) = l.count { t.contains(it) }

    fun plan(text: String, songs: List<Song>, count: (Song) -> Int, learned: Map<String, Int>): LocalPlan {
        if (songs.isEmpty()) return LocalPlan(null, emptyList(), "Telefonunda şarkı bulamadım. Müzik eklenince tekrar dene.")
        val m = mood(text)
        val rnd = java.util.Random()
        val scored = songs.map { s ->
            val t = (s.title + " " + s.artist + " " + s.album).lowercase()
            var sc = Math.log(1.0 + count(s)) * 0.8 + rnd.nextDouble() * 1.5
            if (m != null) {
                val own = when (m) { "up" -> UP_KW; "sad" -> SAD_KW; else -> CALM_KW }
                val opp = when (m) { "up" -> SAD_KW + CALM_KW; "sad" -> UP_KW; else -> UP_KW }
                sc += hits(t, own) * 3.0 - hits(t, opp) * 2.5
                sc += (learned["$m|${s.statKey}"] ?: 0) * 4.0
            }
            s to sc
        }.sortedByDescending { it.second }
        val picks = scored.take(25).map { it.first }
        val reply = when (m) {
            "up" -> "Moralini düzeltecek hareketli parçaları seçtim, başlıyoruz."
            "sad" -> "Biraz hüzünlü, duygusal parçalar seçtim."
            "calm" -> "Sakin, dinlendirici parçaları açtım."
            else -> "En çok dinlediklerine göre bir liste hazırladım."
        } + " Hangi parçayı sonuna kadar dinlersen bir sonraki sefer onu öne alırım."
        return LocalPlan(m, picks, reply)
    }
}
