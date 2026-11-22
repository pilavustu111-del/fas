@file:OptIn(androidx.media3.common.util.UnstableApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package app.cepcalar

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.lightColorScheme
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Canvas
import androidx.core.view.WindowCompat
import android.app.Activity
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import android.speech.RecognizerIntent
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs

object Pal {
    var dark by mutableStateOf(true)
    fun compute(mode: Int, hour: Int) = when (mode) { 1 -> false; 2 -> true; else -> hour !in 7..18 }
    fun refresh(mode: Int) {
        dark = compute(mode, java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY))
    }
}

val BG: Color get() = if (Pal.dark) Color(0xFF08080B) else Color(0xFFF6EBDD)
val CARD: Color get() = if (Pal.dark) Color(0xFF14141B) else Color(0xFFFBF4EA)
val CHIP: Color get() = if (Pal.dark) Color(0xFF1F1F29) else Color(0xFFEBDCC8)
val GOLD: Color get() = if (Pal.dark) Color(0xFFE5483F) else Color(0xFF8A2B1E) // vurgu rengi
val MUTED: Color get() = if (Pal.dark) Color(0xFF9C9CA8) else Color(0xFF7C6A5B)
val TXT: Color get() = if (Pal.dark) Color(0xFFF3F3F6) else Color(0xFF2B1F17)
val EDGE: Color get() = if (Pal.dark) TXT.copy(alpha = 0.2f) else Color(0x338A2B1E)
val INK = Color.White // vurgu rengi üstündeki yazı
val IMGTXT = Color.White // resimlerin üstündeki yazı

fun tintFor(s: String, sat: Float = 0.35f, v: Float = 0.30f): Color =
    Color.hsv((abs(s.hashCode()) % 360).toFloat(), sat, v)

fun fmt(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%d:%02d".format(s / 60, s % 60)
}

object ThumbCache {
    val cache = LruCache<String, ImageBitmap>(80)
}

@Composable
fun Art(song: Song?, modifier: Modifier = Modifier, corner: Dp = 6.dp, letter: Boolean = true) {
    val ctx = LocalContext.current
    val title = song?.title ?: ""
    Box(
        modifier.clip(RoundedCornerShape(corner)).background(tintFor(title, 0.38f, 0.28f)),
        contentAlignment = Alignment.Center
    ) {
        if (letter) {
            Text(
                (title.trim().firstOrNull()?.uppercase() ?: "♪"),
                color = IMGTXT.copy(alpha = 0.85f), fontWeight = FontWeight.Bold, fontSize = 22.sp
            )
        }
        if (song != null) {
            if (song.artUrl != null) {
                AsyncImage(
                    model = song.artUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else if (!song.remote) {
                val bmp by produceState<ImageBitmap?>(null, song.mediaId) {
                    // Şarkı değişince eski kapak kalmasın: önce önbelleğe (yoksa boşa) sıfırla, sonra yükle.
                    val cached = ThumbCache.cache.get(song.mediaId)
                    value = cached
                    if (cached == null) {
                        value = withContext(Dispatchers.IO) {
                            runCatching {
                                ctx.contentResolver.loadThumbnail(song.uri, Size(320, 320), null).asImageBitmap()
                            }.getOrNull()
                        }?.also { ThumbCache.cache.put(song.mediaId, it) }
                    }
                }
                bmp?.let {
                    Image(it, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun CepApp(vm: MusicVM) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var full by rememberSaveable { mutableStateOf(false) }
    val perms = remember {
        if (Build.VERSION.SDK_INT >= 33)
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.scan()
    }
    val view = LocalView.current
    val dark = Pal.dark
    var settings by rememberSaveable { mutableStateOf(false) }
    SideEffect {
        view.keepScreenOn = vm.voiceOn
        (view.context as? Activity)?.window?.let { w ->
            WindowCompat.getInsetsController(w, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
        }
    }
    LaunchedEffect(vm.themeMode) {
        while (true) {
            Pal.refresh(vm.themeMode)
            delay(60_000)
        }
    }
    LaunchedEffect(Unit) { if (!vm.checkPermission()) launcher.launch(perms) }
    LaunchedEffect(Unit) {
        while (true) {
            vm.tick()
            delay(400)
        }
    }
    LaunchedEffect(vm.openFull) {
        if (vm.openFull) {
            full = true
            vm.openFull = false
        }
    }
    BackHandler(enabled = settings) { settings = false }
    BackHandler(enabled = full) { full = false }

    val scheme = if (dark) darkColorScheme(primary = GOLD, background = BG, surface = CARD, onSurface = TXT, onBackground = TXT)
    else lightColorScheme(primary = GOLD, background = BG, surface = CARD, onSurface = TXT, onBackground = TXT)
    MaterialTheme(colorScheme = scheme) {
        Box(Modifier.fillMaxSize().background(BG)) {
            Column(Modifier.fillMaxSize().imePadding()) {
                Box(Modifier.weight(1f).then(if (tab != 0) Modifier.statusBarsPadding() else Modifier)) {
                    when (tab) {
                        0 -> Home(vm, onGo = { tab = it }, onSettings = { settings = true }, onFull = { full = true }) { launcher.launch(perms) }
                        1 -> SearchTab(vm)
                        2 -> AssistantTab(vm)
                        3 -> LibraryTab(vm)
                        else -> LikedTab(vm)
                    }
                }
                if (vm.current != null && tab != 0) MiniPlayer(vm) { full = true }
                NavBar(tab) { tab = it }
            }
            AnimatedVisibility(
                visible = settings,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) { SettingsScreen(vm) { settings = false } }
            AnimatedVisibility(
                visible = full,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) { FullPlayer(vm) { full = false } }
        }
    }
}

// ---------- Ana sayfa ----------

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(if (selected) GOLD else CHIP)
            .clickable(onClick = onClick).padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(text, color = if (selected) INK else TXT, fontWeight = FontWeight.Medium, fontSize = 15.sp)
    }
}

@Composable
fun SectionTitle(t: String, onAll: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.GraphicEq, null, tint = GOLD, modifier = Modifier.size(24.dp))
        Text(
            t, color = TXT, fontSize = 22.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f).padding(start = 10.dp)
        )
        if (onAll != null) {
            Text("Tümünü Gör →", color = GOLD, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onAll))
        }
    }
}

@Composable
fun Hint(t: String, modifier: Modifier = Modifier) {
    Text(t, color = MUTED, fontSize = 14.sp, modifier = modifier.padding(horizontal = 16.dp, vertical = 10.dp))
}

@Composable
fun rememberVoiceToggle(vm: MusicVM): () -> Unit {
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok) vm.setVoiceOn(true) else vm.voiceNote = "Mikrofon izni verilmedi."
    }
    return {
        if (vm.voiceOn) vm.setVoiceOn(false)
        else if (vm.hasMic()) vm.setVoiceOn(true)
        else perm.launch(Manifest.permission.RECORD_AUDIO)
    }
}

@Composable
fun RoundBtn(on: Boolean, desc: String, icon: ImageVector, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).border(1.dp, EDGE, CircleShape)
            .background(if (on) GOLD else CARD).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = if (on) INK else GOLD) }
}

private const val WOLF_D = "M22 22 C28 18 36 26 44 38 L52 16 L58 38 C66 46 74 58 84 70 C100 70 128 74 148 82 C172 84 188 108 186 142 L178 152 C178 124 168 106 156 100 C158 116 154 128 152 142 L152 166 L138 167 L140 146 C138 130 128 118 118 112 L100 112 L92 114 C92 130 90 150 90 164 L92 167 L78 167 L80 150 C82 134 76 122 68 110 C62 96 58 84 54 70 C48 58 40 50 36 46 L28 38 L24 30 Z"
private const val ROCK_D = "M10 230 L30 184 L62 172 L100 170 L142 166 L176 158 L210 138 L210 230 Z"

private fun DrawScope.ridge(pts: List<Pair<Float, Float>>, color: Color) {
    val p = Path()
    p.moveTo(0f, size.height)
    pts.forEach { (x, y) -> p.lineTo(size.width * x, size.height * y) }
    p.lineTo(size.width, size.height)
    p.close()
    drawPath(p, color)
}

@Composable
fun HeroScene(modifier: Modifier) {
    val dark = Pal.dark
    val bg = BG
    val wolf = remember { PathParser().parsePathString(WOLF_D).toPath() }
    val rock = remember { PathParser().parsePathString(ROCK_D).toPath() }
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (dark) {
            drawRect(Brush.verticalGradient(listOf(Color(0xFF050509), Color(0xFF101118), Color(0xFF1E1F29))))
            listOf(0.08f to 0.10f, 0.22f to 0.06f, 0.35f to 0.14f, 0.5f to 0.05f, 0.62f to 0.19f, 0.9f to 0.12f, 0.15f to 0.27f, 0.44f to 0.31f)
                .forEach { (x, y) -> drawCircle(Color(0x99FFFFFF), 2f, Offset(w * x, h * y)) }
            val c = Offset(w * 0.74f, h * 0.27f)
            val r = w * 0.16f
            drawCircle(Brush.radialGradient(listOf(Color(0x33DDE6FF), Color.Transparent), c, r * 2.4f), r * 2.4f, c)
            val full = Path().apply { addOval(Rect(c, r)) }
            val cut = Path().apply { addOval(Rect(Offset(c.x + r * 0.42f, c.y - r * 0.14f), r * 0.92f)) }
            drawPath(Path.combine(PathOperation.Difference, full, cut), Color(0xFFE9E7EE))
        } else {
            drawRect(Brush.verticalGradient(0f to Color(0xFF86AAE6), 0.45f to Color(0xFFE3C3B8), 0.78f to Color(0xFFF7B98A), 1f to Color(0xFFF2A472)))
            val c = Offset(w * 0.34f, h * 0.46f)
            drawCircle(Brush.radialGradient(listOf(Color(0xAAFFF3C9), Color.Transparent), c, w * 0.34f), w * 0.34f, c)
            drawCircle(Color(0xFFFFF4CF), w * 0.06f, c)
            listOf(Triple(0.2f, 0.12f, 0.30f), Triple(0.62f, 0.2f, 0.34f), Triple(0.85f, 0.08f, 0.22f), Triple(0.45f, 0.34f, 0.26f)).forEach { (x, y, sw) ->
                drawOval(Color(0x66FFFFFF), Offset(w * (x - sw / 2), h * y), androidx.compose.ui.geometry.Size(w * sw, h * 0.07f))
            }
        }
        ridge(listOf(0f to .62f, .12f to .52f, .22f to .60f, .34f to .44f, .46f to .58f, .60f to .50f, .78f to .62f, 1f to .55f), if (dark) Color(0xFF262833) else Color(0xFF9A93B8))
        ridge(listOf(0f to .74f, .18f to .64f, .32f to .72f, .5f to .62f, .68f to .72f, .85f to .66f, 1f to .72f), if (dark) Color(0xFF191A22) else Color(0xFF77708F))
        ridge(listOf(0f to .88f, .25f to .82f, .5f to .88f, .75f to .84f, 1f to .86f), if (dark) Color(0xFF101117) else Color(0xFF4A3F45))
        val sc = w * 0.50f / 200f
        val ty = h * 0.80f - 167f * sc
        withTransform({ translate(w * 0.48f, ty); scale(sc, sc, Offset.Zero) }) {
            drawPath(rock, if (dark) Color(0xFF111218) else Color(0xFF3A2E2A))
            drawPath(wolf, if (dark) Color(0xFF07070A) else Color(0xFF2B2320))
        }
        drawRect(if (dark) Color(0xFF111218) else Color(0xFF3A2E2A), Offset(w * 0.48f + 10f * sc, h * 0.80f + 40f * sc), androidx.compose.ui.geometry.Size(w, h))
        drawRect(Brush.verticalGradient(0.55f to Color.Transparent, 1f to bg))
    }
}

@Composable
fun HomeHero(vm: MusicVM, onSearch: () -> Unit, onSettings: () -> Unit) {
    val hi = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    Box(Modifier.fillMaxWidth().height(400.dp)) {
        HeroScene(Modifier.fillMaxSize())
        Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(R.drawable.yirdas_logo), "Yırdaş",
                    Modifier.size(46.dp).clip(CircleShape).border(1.dp, EDGE, CircleShape)
                )
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Yırdaş", color = TXT, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Serif)
                    Text("Müziğin ruhu, seninle...", color = MUTED, fontSize = 12.sp)
                }
                IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, "Ara", tint = TXT, modifier = Modifier.size(26.dp)) }
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, "Ayarlar", tint = TXT, modifier = Modifier.size(26.dp)) }
            }
            Spacer(Modifier.height(26.dp))
            Text(
                when (hi) { in 5..11 -> "Günaydın,"; in 12..17 -> "Merhaba,"; in 18..22 -> "İyi akşamlar,"; else -> "İyi geceler," },
                color = MUTED, fontSize = 18.sp
            )
            Text(vm.userName.ifBlank { "Dostum" }, color = TXT, fontSize = 38.sp, fontWeight = FontWeight.Bold)
            Text(
                if (Pal.dark) "\"Kimi zaman bir şarkı,\ntüm dertlere iyi gelir.\"" else "\"Bozkırın sesi,\nher zaman seninle.\"",
                color = MUTED, fontSize = 14.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(top = 6.dp)
            )
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.clip(RoundedCornerShape(50)).border(1.dp, EDGE, RoundedCornerShape(50)).background(CARD)
                    .padding(start = 14.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(if (Pal.dark) Icons.Filled.DarkMode else Icons.Filled.LightMode, null, tint = TXT, modifier = Modifier.size(20.dp))
                Text(
                    if (Pal.dark) "Gece Modu" else "Gündüz Modu", color = TXT, fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                Switch(
                    checked = Pal.dark, onCheckedChange = { vm.setTheme(if (it) 2 else 1) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GOLD, checkedThumbColor = INK)
                )
            }
        }
    }
}

@Composable
fun Waveform(active: Boolean, modifier: Modifier) {
    val t by rememberInfiniteTransition(label = "wave").animateFloat(
        0f, 6.2832f, infiniteRepeatable(tween(1100, easing = LinearEasing)), label = "phase"
    )
    val col = GOLD
    Canvas(modifier) {
        val n = 28
        val gap = size.width / n
        for (i in 0 until n) {
            val base = 0.25f + 0.75f * kotlin.math.abs(kotlin.math.sin(i * 0.55f))
            val amp = if (active) 0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin(t + i * 0.6f)) else 0.45f
            val hh = size.height * base * amp
            val x = gap * i + gap / 2
            drawLine(
                col.copy(alpha = if (active) 0.9f else 0.45f),
                Offset(x, size.height / 2 - hh / 2), Offset(x, size.height / 2 + hh / 2),
                strokeWidth = 3f, cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AssistantCard(vm: MusicVM, onOpen: () -> Unit) {
    val toggle = rememberVoiceToggle(vm)
    val shape = RoundedCornerShape(30.dp)
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(shape).border(1.dp, EDGE, shape)
            .background(CARD).clickable(onClick = onOpen).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painterResource(if (Pal.dark) R.drawable.wolf_head_night else R.drawable.wolf_head_day), "Yırdaş Asistan",
            Modifier.size(72.dp).clip(CircleShape).border(2.dp, EDGE, CircleShape)
        )
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text("Yırdaş Asistan", color = TXT, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                when {
                    vm.speaking -> "Konuşuyorum…"
                    vm.voiceOn && vm.listening -> "Dinliyorum…"
                    else -> "Nasıl bir ruh halindesin?"
                },
                color = MUTED, fontSize = 14.sp
            )
            Waveform(vm.listening || vm.speaking, Modifier.padding(top = 6.dp).fillMaxWidth().height(24.dp))
        }
        Box(
            Modifier.size(54.dp).clip(CircleShape).border(1.dp, GOLD, CircleShape)
                .background(if (vm.voiceOn) GOLD else CHIP).clickable(onClick = toggle),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (vm.voiceOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                if (vm.voiceOn) "Sohbet modunu kapat" else "Sohbet modunu aç",
                tint = if (vm.voiceOn) INK else GOLD
            )
        }
    }
}

@Composable
fun HomePlayerCard(vm: MusicVM, onOpen: () -> Unit) {
    val s = vm.current
    val shape = RoundedCornerShape(26.dp)
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(shape).border(1.dp, EDGE, shape)
            .background(CARD).padding(14.dp)
    ) {
        if (s == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Henüz bir şey çalmıyor", color = TXT, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text("Bir şarkı seç ya da karışık çal.", color = MUTED, fontSize = 14.sp)
                }
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(GOLD).clickable { vm.shuffleAll(vm.songs) },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.PlayArrow, "Karışık çal", tint = INK, modifier = Modifier.size(30.dp)) }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Art(s, Modifier.size(96.dp).clickable(onClick = onOpen), 16.dp)
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text(s.title, color = TXT, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = MUTED, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (s.remote) {
                    val liked = vm.isLiked(s)
                    IconButton(onClick = { vm.toggleLike(s) }) {
                        Icon(
                            if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            if (liked) "Beğenmekten vazgeç" else "Beğen", tint = GOLD
                        )
                    }
                } else {
                    Icon(Icons.Filled.GraphicEq, null, tint = GOLD, modifier = Modifier.padding(end = 6.dp))
                }
            }
            PlayerControls(vm)
        }
    }
}

@Composable
fun StatsCard(vm: MusicVM, onAll: () -> Unit) {
    val plays = vm.songs.sumOf { vm.count(it) }
    val ms = vm.songs.sumOf { vm.count(it).toLong() * it.durationMs }
    val top = vm.songs.maxByOrNull { vm.count(it) }?.takeIf { vm.count(it) > 0 }
    val hrs = ms / 3_600_000L
    val mins = (ms / 60_000L) % 60
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clip(shape).border(1.dp, EDGE, shape)
            .background(CARD).padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.GraphicEq, null, tint = GOLD, modifier = Modifier.size(22.dp))
            Text(
                "Dinleme İstatistiklerin", color = TXT, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(start = 8.dp)
            )
            Text("Tüm Detaylar →", color = MUTED, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onAll))
        }
        Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Icon(Icons.Filled.Headphones, null, tint = GOLD)
                Text(if (hrs > 0) "$hrs sa" else "$mins dk", color = TXT, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Toplam Dinleme", color = MUTED, fontSize = 12.sp)
            }
            Column(Modifier.weight(1f)) {
                Icon(Icons.Filled.PlayArrow, null, tint = GOLD)
                Text(String.format(java.util.Locale("tr"), "%,d kez", plays), color = TXT, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Toplam Parça", color = MUTED, fontSize = 12.sp)
            }
            Column(Modifier.weight(1.2f)) {
                Icon(Icons.Filled.Star, null, tint = GOLD)
                Text(top?.title ?: "—", color = TXT, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (top != null) "En çok dinlediğin (${vm.count(top)} kez)" else "En çok dinlediğin", color = MUTED, fontSize = 12.sp, maxLines = 2)
            }
        }
    }
}

@Composable
fun PlaylistCard(title: String, sub: String, cover: Song?, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        Modifier.width(140.dp).height(160.dp).clip(shape).border(1.dp, EDGE, shape).clickable(onClick = onClick)
    ) {
        Art(cover, Modifier.fillMaxSize(), 0.dp, letter = false)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE0B0907)), startY = 160f)
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(title, color = IMGTXT, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text("• $sub", color = Color(0xFFD0D0D8), fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun Home(vm: MusicVM, onGo: (Int) -> Unit, onSettings: () -> Unit, onFull: () -> Unit, onAsk: () -> Unit) {
    val list = when (vm.filter) {
        1 -> vm.songs.filter { !it.isVideo }
        2 -> vm.songs.filter { it.isVideo }
        else -> vm.songs
    }
    val ranked = remember(vm.songs, vm.stats) {
        vm.songs.filter { vm.count(it) > 0 }.sortedByDescending { vm.count(it) }
    }
    val queue = remember(list, vm.stats, vm.recoTracks) { vm.mixed(list.sortedByDescending { vm.count(it) }, 4) }
    val shown = queue.take(14)
    val videos = remember(vm.songs) { vm.songs.filter { it.isVideo } }
    val mixList = remember(vm.songs) { vm.songs.shuffled().take(40) }

    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
        item { HomeHero(vm, onSearch = { onGo(1) }, onSettings = onSettings) }
        if (Pal.dark) {
            item { AssistantCard(vm) { onGo(2) } }
            item { HomePlayerCard(vm, onFull) }
        } else {
            item { HomePlayerCard(vm, onFull) }
            item { AssistantCard(vm) { onGo(2) } }
        }
        if (!vm.hasPermission) {
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Telefondaki müzikleri bulabilmem için izin ver.", color = TXT, fontSize = 16.sp)
                    Button(
                        onClick = onAsk,
                        colors = ButtonDefaults.buttonColors(containerColor = GOLD, contentColor = INK)
                    ) { Text("İzin ver") }
                    Text(
                        "İzin penceresi çıkmazsa Ayarlar > Uygulamalar > Yırdaş > İzinler bölümünden Müzik ve ses ile Fotoğraf ve video iznini aç.",
                        color = MUTED, fontSize = 13.sp
                    )
                }
            }
        } else if (vm.songs.isEmpty()) {
            item { Hint("Bu telefonda henüz müzik bulunamadı. Dosyalar eklenince liste kendiliğinden güncellenir.") }
        }

        if (vm.songs.isNotEmpty()) {
            item { SectionTitle("Senin İçin Hazırlandı") { vm.libMode = 0; onGo(3) } }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        val src = ranked.ifEmpty { vm.songs }.take(30)
                        PlaylistCard("Çok Dinlediklerin", "${src.size} parça", src.firstOrNull()) {
                            vm.play(vm.mixed(src, 4), 0)
                        }
                    }
                    item {
                        PlaylistCard("Karışık Çal", "${mixList.size} parça", mixList.firstOrNull()) {
                            vm.play(vm.mixed(mixList, 4), 0)
                        }
                    }
                    if (videos.isNotEmpty()) {
                        item {
                            PlaylistCard("Klipler", "${videos.size} klip", videos.firstOrNull()) { vm.play(videos, 0) }
                        }
                    }
                    items(vm.similar.take(6)) { a ->
                        StationCard(a) { vm.playStation(a) }
                    }
                }
            }
            item { StatsCard(vm) { vm.libMode = 1; onGo(3) } }
        }

        if (ranked.isNotEmpty()) {
            item { SectionTitle("En Çok Dinlediklerin") { vm.libMode = 1; onGo(3) } }
            itemsIndexed(ranked.take(5)) { i, s ->
                SongRow(s, vm, rank = i + 1, sub = "${vm.count(s)} kez · " + s.artist.ifEmpty { "Sanatçı bilinmiyor" }) {
                    vm.play(ranked, i)
                }
            }
        }

        item { SectionTitle("Parçaların") { vm.libMode = 0; onGo(3) } }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Chip("Tümü", vm.filter == 0) { vm.filter = 0 }
                Chip("Müzik", vm.filter == 1) { vm.filter = 1 }
                Chip("Klipler", vm.filter == 2) { vm.filter = 2 }
            }
        }
        items(shown) { s -> SongRow(s, vm) { vm.play(queue, queue.indexOf(s)) } }

        if (vm.mine.isNotEmpty()) {
            item { SectionTitle("Senin Radyoların") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) { items(vm.mine) { a -> StationCard(a) { vm.playStation(a) } } }
            }
        } else if (!vm.online && vm.songs.isNotEmpty()) {
            item { Hint("İnternete bağlanınca en çok dinlediğin sanatçılara göre istasyonlar burada çıkar.", Modifier.padding(top = 16.dp)) }
        }
        if (videos.isNotEmpty() && vm.filter != 1) {
            item { SectionTitle("Klipler") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(videos) { v ->
                        Column(Modifier.width(220.dp).clickable { vm.play(videos, videos.indexOf(v)) }) {
                            Art(v, Modifier.fillMaxWidth().aspectRatio(16f / 9f), 12.dp)
                            Text(
                                v.title, color = TXT, fontWeight = FontWeight.SemiBold, maxLines = 1,
                                overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(v.artist.ifEmpty { "Klip" }, color = MUTED, fontSize = 13.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationCard(a: Artist, onClick: () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(Modifier.width(140.dp).height(160.dp).clip(shape).border(1.dp, EDGE, shape).background(CARD).clickable(onClick = onClick)) {
        AsyncImage(
            model = a.pic, contentDescription = null, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color(0xEE0B0907)), startY = 160f)
            )
        )
        Text(
            "RADYO", color = Color(0xFFFF8A80), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
        )
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(a.name, color = IMGTXT, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
            Text("• ve benzerleri", color = Color(0xFFD0D0D8), fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
fun SettingRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = TXT, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = GOLD, checkedThumbColor = INK)
        )
    }
}

@Composable
fun SettingsScreen(vm: MusicVM, onClose: () -> Unit) {
    val toggleVoice = rememberVoiceToggle(vm)
    var name by rememberSaveable { mutableStateOf(vm.userName) }
    Column(
        Modifier.fillMaxSize().background(BG).pointerInput(Unit) {}.statusBarsPadding().navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, "Kapat", tint = TXT) }
            Text("Ayarlar", color = TXT, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
        }
        SectionTitle("Görünüm")
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("Otomatik", vm.themeMode == 0) { vm.setTheme(0) }
            Chip("Gündüz", vm.themeMode == 1) { vm.setTheme(1) }
            Chip("Gece", vm.themeMode == 2) { vm.setTheme(2) }
        }
        Text(
            "Otomatik: saat 07.00 ile 19.00 arası gündüz, diğer saatlerde gece teması.",
            color = MUTED, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        SectionTitle("Adın")
        TextField(
            value = name, onValueChange = { name = it; vm.setName(it) }, singleLine = true,
            placeholder = { Text("Adını yaz") }, shape = RoundedCornerShape(12.dp), colors = fieldColors(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        SectionTitle("Asistan")
        SettingRow("Asistan sesli konuşsun", !vm.voiceMuted) { vm.toggleMute() }
        SettingRow("Sohbet modu (eller serbest)", vm.voiceOn) { toggleVoice() }
        Text(
            "Gelişmiş yapay zekâ anahtarı için Asistan sekmesindeki ayar simgesini kullan.",
            color = MUTED, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun SongRow(s: Song, vm: MusicVM, sub: String? = null, rank: Int? = null, onClick: () -> Unit) {
    var menu by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val now = vm.current?.mediaId == s.mediaId
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(start = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (rank != null) {
            Text(
                "$rank", color = if (rank == 1) GOLD else MUTED, fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp), fontSize = 16.sp
            )
        }
        Art(s, Modifier.size(56.dp))
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                s.title, color = if (now) GOLD else TXT, fontSize = 17.sp, maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (s.isVideo) {
                    Text(
                        "KLİP", color = MUTED, fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 6.dp).border(1.dp, MUTED, RoundedCornerShape(3.dp))
                            .padding(horizontal = 4.dp)
                    )
                }
                Text(
                    sub ?: s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = MUTED, fontSize = 14.sp,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (s.remote) {
            val liked = vm.isLiked(s)
            IconButton(onClick = { vm.toggleLike(s) }) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    if (liked) "Beğenmekten vazgeç" else "Beğen",
                    tint = if (liked) GOLD else TXT
                )
            }
        }
        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Daha fazla", tint = TXT)
            }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Sıradakine ekle") }, onClick = {
                    vm.addNext(s)
                    menu = false
                })
                if (s.remote) {
                    val q = android.net.Uri.encode("${s.artist} ${s.title}")
                    DropdownMenuItem(text = { Text("YouTube Music'te ara") }, onClick = {
                        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://music.youtube.com/search?q=$q"))) }
                        menu = false
                    })
                    DropdownMenuItem(text = { Text("Spotify'da ara") }, onClick = {
                        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://open.spotify.com/search/$q"))) }
                        menu = false
                    })
                }
                if (!s.link.isNullOrBlank()) {
                    DropdownMenuItem(text = { Text("Deezer'da aç") }, onClick = {
                        runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(s.link))) }
                        menu = false
                    })
                }
            }
        }
    }
}

// ---------- Ara / Kitaplık / En çok ----------

@Composable
fun Header(t: String, trailing: @Composable () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(t, color = TXT, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
fun SearchTab(vm: MusicVM) {
    var q by rememberSaveable { mutableStateOf("") }
    val res = remember(q, vm.songs) {
        if (q.isBlank()) emptyList()
        else vm.songs.filter { it.title.contains(q, true) || it.artist.contains(q, true) }
    }
    Column(Modifier.fillMaxSize()) {
        Header("Ara")
        TextField(
            value = q, onValueChange = { q = it }, singleLine = true,
            placeholder = { Text("Ne dinlemek istiyorsun?") },
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = MUTED) },
            shape = RoundedCornerShape(8.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (q.isNotBlank() && res.isEmpty()) Hint("Sonuç yok.")
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(res) { s -> SongRow(s, vm) { vm.play(res, res.indexOf(s)) } }
        }
    }
}

@Composable
fun LibraryTab(vm: MusicVM) {
    val shown = remember(vm.songs, vm.recoTracks) { vm.mixed(vm.songs, 5) }
    Column(Modifier.fillMaxSize()) {
        Header("Kitaplığım") {
            Box(
                Modifier.size(48.dp).clip(CircleShape).background(GOLD).clickable { vm.shuffleAll(vm.songs) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Filled.Shuffle, "Karışık çal", tint = INK) }
        }
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Chip("Tüm parçalar", vm.libMode == 0) { vm.libMode = 0 }
            Chip("En çok dinlenen", vm.libMode == 1) { vm.libMode = 1 }
        }
        if (vm.libMode == 1) {
            TopContent(vm)
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${vm.songs.size} parça · Tüm videoları göster", color = MUTED, fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = vm.allVideos, onCheckedChange = { vm.setShowAllVideos(it) },
                    colors = SwitchDefaults.colors(checkedTrackColor = GOLD, checkedThumbColor = INK)
                )
            }
            LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                items(shown) { s ->
                    val c = vm.count(s)
                    SongRow(
                        s, vm,
                        sub = (if (c > 0) "$c kez dinlendi · " else "") + s.artist.ifEmpty { "Sanatçı bilinmiyor" }
                    ) { vm.play(shown, shown.indexOf(s)) }
                }
            }
        }
    }
}

@Composable
fun TopContent(vm: MusicVM) {
    val ranked = remember(vm.songs, vm.stats) {
        vm.songs.filter { vm.count(it) > 0 }.sortedByDescending { vm.count(it) }.take(50)
    }
    Column(Modifier.fillMaxSize()) {
        if (ranked.isEmpty()) Hint("Henüz dinleme kaydı yok. Bir parçayı yaklaşık 30 saniye dinleyince sayılır.")
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(ranked) { i, s ->
                SongRow(
                    s, vm, rank = i + 1,
                    sub = "${vm.count(s)} kez · " + s.artist.ifEmpty { "Sanatçı bilinmiyor" }
                ) { vm.play(ranked, i) }
            }
        }
    }
}

// ---------- Alt çubuklar ----------

@Composable
fun MiniPlayer(vm: MusicVM, onOpen: () -> Unit) {
    val s = vm.current ?: return
    val frac = if (vm.duration > 0) (vm.position.toFloat() / vm.duration).coerceIn(0f, 1f) else 0f
    val shape = RoundedCornerShape(22.dp)
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(shape).border(1.dp, EDGE, shape).background(CARD).clickable(onClick = onOpen)
    ) {
        Row(
            Modifier.padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Art(s, Modifier.size(50.dp), 10.dp)
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(s.title, color = TXT, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = MUTED, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = { vm.prev() }) {
                Icon(Icons.Filled.SkipPrevious, "Önceki", tint = TXT, modifier = Modifier.size(26.dp))
            }
            Box(
                Modifier.size(44.dp).clip(CircleShape).border(2.dp, GOLD, CircleShape).clickable { vm.toggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (vm.isPlaying) "Duraklat" else "Oynat", tint = TXT, modifier = Modifier.size(26.dp)
                )
            }
            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Filled.SkipNext, "Sonraki", tint = TXT, modifier = Modifier.size(26.dp))
            }
        }
        Box(
            Modifier.align(Alignment.BottomCenter).padding(horizontal = 14.dp, vertical = 4.dp)
                .fillMaxWidth().height(2.dp).background(Color(0x33FFFFFF))
        ) { Box(Modifier.fillMaxWidth(frac).fillMaxHeight().background(GOLD)) }
    }
}

@Composable
fun RowScope.NavItem(index: Int, label: String, icon: ImageVector, selected: Int, onSelect: (Int) -> Unit) {
    Column(
        Modifier.weight(1f).clickable { onSelect(index) },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = if (index == selected) GOLD else MUTED, modifier = Modifier.size(26.dp))
        Text(label, color = if (index == selected) GOLD else MUTED, fontSize = 11.sp, maxLines = 1)
    }
}

@Composable
fun NavBar(selected: Int, onSelect: (Int) -> Unit) {
    Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 10.dp).padding(bottom = 6.dp)) {
        val shape = RoundedCornerShape(30.dp)
        Row(
            Modifier.fillMaxWidth().padding(top = 22.dp).clip(shape).border(1.dp, EDGE, shape).background(CARD)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem(0, "Ana Sayfa", Icons.Filled.Home, selected, onSelect)
            NavItem(1, "Ara", Icons.Filled.Search, selected, onSelect)
            Spacer(Modifier.width(80.dp))
            NavItem(3, "Kitaplığım", Icons.Filled.LibraryMusic, selected, onSelect)
            NavItem(4, "Beğenilen", Icons.Filled.Favorite, selected, onSelect)
        }
        Box(
            Modifier.align(Alignment.TopCenter).size(72.dp).clip(CircleShape)
                .border(2.dp, if (selected == 2) GOLD else EDGE, CircleShape).background(CARD)
                .clickable { onSelect(2) },
            contentAlignment = Alignment.Center
        ) {
            Image(painterResource(R.drawable.yirdas_logo), "Asistan", Modifier.size(62.dp).clip(CircleShape))
        }
    }
}

// ---------- Şimdi çalıyor ----------

@Composable
fun PlayerMedia(vm: MusicVM, s: Song, landscape: Boolean) {
    if (s.isVideo) {
        AndroidView(
            factory = { c ->
                PlayerView(c).apply {
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    player = vm.controller
                }
            },
            update = { it.player = vm.controller },
            onRelease = { it.player = null },
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        )
    } else if (landscape) {
        Art(s, Modifier.fillMaxHeight(0.9f).aspectRatio(1f), 8.dp)
    } else {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Art(s, Modifier.size(220.dp), 8.dp) }
    }
}

@Composable
fun PlayerInfo(vm: MusicVM, s: Song) {
    Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                s.title, color = TXT, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(s.artist.ifEmpty { "Sanatçı bilinmiyor" }, color = TXT.copy(alpha = 0.7f), fontSize = 16.sp)
        }
        if (s.remote) {
            val liked = vm.isLiked(s)
            IconButton(onClick = { vm.toggleLike(s) }) {
                Icon(
                    if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    if (liked) "Beğenmekten vazgeç" else "Beğen",
                    tint = if (liked) GOLD else TXT, modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun PlayerControls(vm: MusicVM) {
    var drag by remember { mutableStateOf<Float?>(null) }
    val dur = vm.duration.coerceAtLeast(1L)
    val frac = drag ?: (vm.position.toFloat() / dur).coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = frac,
            onValueChange = { drag = it },
            onValueChangeFinished = {
                drag?.let { vm.seekTo((it * dur).toLong()) }
                drag = null
            },
            colors = SliderDefaults.colors(
                thumbColor = TXT, activeTrackColor = TXT,
                inactiveTrackColor = TXT.copy(alpha = 0.3f)
            )
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(fmt((frac * dur).toLong()), color = TXT.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(fmt(vm.duration), color = TXT.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { vm.toggleShuffle() }) {
                Icon(Icons.Filled.Shuffle, "Karıştır", tint = if (vm.shuffle) GOLD else TXT)
            }
            IconButton(onClick = { vm.prev() }) {
                Icon(Icons.Filled.SkipPrevious, "Önceki", tint = TXT, modifier = Modifier.size(36.dp))
            }
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(GOLD).clickable { vm.toggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (vm.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (vm.isPlaying) "Duraklat" else "Oynat", tint = INK, modifier = Modifier.size(36.dp)
                )
            }
            IconButton(onClick = { vm.next() }) {
                Icon(Icons.Filled.SkipNext, "Sonraki", tint = TXT, modifier = Modifier.size(36.dp))
            }
            IconButton(onClick = { vm.cycleRepeat() }) {
                Icon(
                    if (vm.repeatMode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                    "Tekrarla",
                    tint = if (vm.repeatMode == Player.REPEAT_MODE_OFF) TXT else GOLD
                )
            }
        }
    }
}

@Composable
fun PlayerLyrics(vm: MusicVM, s: Song, modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(TXT.copy(alpha = 0.08f))) {
        val ly = vm.lyrics
        if (ly == null) {
            Text(
                when {
                    vm.lyricsLoading -> "Sözler aranıyor…"
                    s.remote -> "Bu 30 saniyelik önizlemede söz yok."
                    !vm.online -> "Sözler için internete bağlan. Bulunan sözler telefona kaydedilir."
                    else -> "Bu parça için söz bulunamadı."
                },
                color = TXT.copy(alpha = 0.7f), fontSize = 14.sp, modifier = Modifier.padding(16.dp)
            )
        } else {
            LyricsList(ly, vm.position) { vm.seekTo(it) }
        }
    }
}

@Composable
fun FullPlayer(vm: MusicVM, onClose: () -> Unit) {
    val s = vm.current
    val tint = if (Pal.dark) tintFor(s?.title ?: "", 0.45f, 0.38f) else tintFor(s?.title ?: "", 0.25f, 0.95f)
    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(tint, BG)))
            .pointerInput(Unit) {}.statusBarsPadding().navigationBarsPadding().padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.KeyboardArrowDown, "Kapat", tint = TXT, modifier = Modifier.size(34.dp))
            }
            Text(
                "ŞİMDİ ÇALIYOR", color = TXT, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(48.dp))
        }
        if (s != null) {
            if (landscape) {
                // Yatay: solda kapak/video, sağda kaydırılabilir bilgi + kontroller + sözler (tuşlar hep erişilebilir)
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(0.9f).fillMaxHeight().padding(end = 16.dp), contentAlignment = Alignment.Center) {
                        PlayerMedia(vm, s, true)
                    }
                    Column(Modifier.weight(1.1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        PlayerInfo(vm, s)
                        PlayerControls(vm)
                        PlayerLyrics(vm, s, Modifier.fillMaxWidth().height(180.dp).padding(vertical = 8.dp))
                    }
                }
            } else {
                PlayerMedia(vm, s, false)
                PlayerInfo(vm, s)
                PlayerControls(vm)
                PlayerLyrics(vm, s, Modifier.weight(1f).fillMaxWidth().padding(top = 12.dp, bottom = 12.dp))
            }
        }
    }
}

@Composable
fun LyricsList(ly: Lyrics, pos: Long, onSeek: (Long) -> Unit) {
    val state = remember(ly) { LazyListState() }
    val active = if (ly.synced) ly.lines.indexOfLast { it.timeMs <= pos + 200 } else -1
    LaunchedEffect(active, ly) {
        if (active >= 0) state.animateScrollToItem(active, -140)
    }
    LazyColumn(state = state, contentPadding = PaddingValues(16.dp)) {
        itemsIndexed(ly.lines) { i, l ->
            Text(
                l.text.ifBlank { "♪" },
                fontSize = if (ly.synced) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (!ly.synced || i == active) TXT else TXT.copy(alpha = 0.45f),
                modifier = Modifier.padding(vertical = 6.dp).clickable(enabled = ly.synced) { onSeek(l.timeMs) }
            )
        }
    }
}

// ---------- Asistan ----------

@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CHIP, unfocusedContainerColor = CHIP,
    focusedTextColor = TXT, unfocusedTextColor = TXT, cursorColor = GOLD,
    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
    focusedPlaceholderColor = MUTED, unfocusedPlaceholderColor = MUTED,
    focusedLabelColor = MUTED, unfocusedLabelColor = MUTED,
)

@Composable
fun Bubble(m: ChatMsg) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = if (m.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Text(
            m.text, color = if (m.fromUser) INK else TXT, fontSize = 16.sp,
            modifier = Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(16.dp))
                .background(if (m.fromUser) GOLD else CHIP).padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun AssistantTab(vm: MusicVM) {
    var text by rememberSaveable { mutableStateOf("") }
    var editKey by rememberSaveable { mutableStateOf(false) }
    var keyDraft by rememberSaveable { mutableStateOf("") }
    val toggleVoice = rememberVoiceToggle(vm)
    val listState = rememberLazyListState()
    LaunchedEffect(vm.chat.size, vm.aiBusy) {
        val n = vm.chat.size + (if (vm.aiBusy) 1 else 0)
        if (n > 0) listState.animateScrollToItem(n - 1)
    }

    Column(Modifier.fillMaxSize()) {
        Header("Asistan") {
            IconButton(onClick = { vm.toggleMute() }) {
                Icon(
                    if (vm.voiceMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                    if (vm.voiceMuted) "Asistan sesini aç" else "Asistan sesini kapat",
                    tint = if (vm.voiceMuted) MUTED else GOLD
                )
            }
            IconButton(onClick = { editKey = !editKey; keyDraft = "" }) {
                Icon(Icons.Filled.Settings, "Ayarlar", tint = TXT)
            }
        }
        if (editKey) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Anahtarsız da çalışırım: telefonun içindeki asistan internetsiz ve ücretsiz ruh haline göre şarkı seçer, " +
                        "dinledikçe zevkini öğrenir. Daha akıllı cevap istersen isteğe bağlı olarak ücretsiz Google Gemini " +
                        "anahtarı ekleyebilirsin (aistudio.google.com/apikey).",
                    color = TXT, fontSize = 15.sp
                )
                TextField(
                    value = keyDraft, onValueChange = { keyDraft = it }, singleLine = true,
                    placeholder = { Text(if (vm.apiKey.isBlank()) "AIza..." else "Yeni anahtar") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp), colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                var modelDraft by rememberSaveable { mutableStateOf(vm.aiModel) }
                TextField(
                    value = modelDraft, onValueChange = { modelDraft = it }, singleLine = true,
                    label = { Text("Model (varsayılanı bırakabilirsin)") },
                    shape = RoundedCornerShape(8.dp), colors = fieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            vm.saveModel(modelDraft)
                            if (keyDraft.isNotBlank()) vm.saveKey(keyDraft)
                            if (keyDraft.isNotBlank() || vm.apiKey.isNotBlank()) { keyDraft = ""; editKey = false }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GOLD, contentColor = INK)
                    ) { Text("Kaydet") }
                    if (vm.apiKey.isNotBlank()) {
                        Button(
                            onClick = { vm.saveKey(""); keyDraft = ""; editKey = false },
                            colors = ButtonDefaults.buttonColors(containerColor = CHIP, contentColor = TXT)
                        ) { Text("Anahtarı sil") }
                    }
                }
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (vm.chat.isEmpty()) {
                    item {
                        Hint(
                            "Ruh halini ya da ne dinlemek istediğini söyle. Telefonundaki listene bakıp sana göre çalarım, internet ya da anahtar gerekmez. " +
                                "Mesela: \"Ruh halim kötü, iyileştirecek oyun havaları aç\" ya da \"Hüzünlü şarkılar aç\"."
                        )
                    }
                }
                items(vm.chat) { m -> Bubble(m) }
                if (vm.aiBusy) item { Bubble(ChatMsg(false, "Listene bakıyorum…")) }
            }
            Text(
                vm.voiceNote ?: when {
                    vm.speaking -> "Konuşuyorum…"
                    vm.voiceOn && vm.listening -> "Dinliyorum, konuşabilirsin…"
                    vm.voiceOn -> "Sohbet modu açık"
                    else -> "Sohbet modu kapalı. Mikrofon simgesine dokununca eller serbest konuşabilirsin."
                },
                color = if (vm.voiceNote != null) Color(0xFFE5483F) else GOLD, fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Ruh halim kötü, iyileştir", "Hüzünlü şarkılar aç", "Oyun havası, heyecanlı",
                    "Sakin bir şeyler", "Bana enerji ver"
                ).forEach { q -> Chip(q, false) { vm.ask(q) } }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text, onValueChange = { text = it }, maxLines = 3,
                    placeholder = { Text("Ruh halin ne? Ne çalayım?") },
                    shape = RoundedCornerShape(24.dp), colors = fieldColors(),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = toggleVoice) {
                    Icon(
                        if (vm.voiceOn) Icons.Filled.Mic else Icons.Filled.MicOff,
                        if (vm.voiceOn) "Sohbet modunu kapat" else "Sohbet modunu aç",
                        tint = if (vm.voiceOn) GOLD else MUTED
                    )
                }
                IconButton(onClick = { vm.ask(text); text = "" }, enabled = !vm.aiBusy) {
                    Icon(Icons.Filled.Send, "Gönder", tint = if (vm.aiBusy) MUTED else GOLD)
                }
            }
        }
    }
}

// ---------- Beğenilenler ----------

@Composable
fun LikedTab(vm: MusicVM) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        Header("Beğendiklerin") {
            if (vm.likes.isNotEmpty()) {
                IconButton(onClick = {
                    val body = buildString {
                        append("Yırdaş - beğendiğim öneriler\n\n")
                        vm.likes.forEachIndexed { i, s ->
                            append("${i + 1}. ${s.artist} - ${s.title}\n")
                            if (!s.link.isNullOrBlank()) append("   ${s.link}\n")
                        }
                    }
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, body)
                    }
                    runCatching { ctx.startActivity(Intent.createChooser(send, "Listeyi paylaş")) }
                }) { Icon(Icons.Filled.Share, "Listeyi paylaş", tint = TXT) }
            }
        }
        if (vm.likes.isEmpty()) {
            Hint("Listelerin arasına karışan yeni parçalarda kalbe dokun, burada birikir. Sonra paylaş simgesiyle listeyi alıp indirebilirsin.")
        } else {
            Hint("Önizleme 30 saniyedir. Tam hâlini kendi hesabınla indirmek için parçanın menüsünden Deezer, YouTube Music ya da Spotify'da açabilir, listeyi de paylaşabilirsin.")
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            itemsIndexed(vm.likes) { i, s -> SongRow(s, vm) { vm.play(vm.likes, i) } }
        }
    }
}
