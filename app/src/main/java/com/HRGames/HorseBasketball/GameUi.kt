package com.HRGames.HorseBasketball

import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import com.HRGames.HorseBasketball.courtside.CourtsidePolicyActivity
import kotlin.math.ceil
import java.util.Locale

private val Navy = Color(0xff071a51)
private val Panel = Color(0xff102b77)
private val Gold = Color(0xffffbd3e)
private val Mist = Color(0xffb4c9ff)
private val Line = Color.White.copy(alpha = .23f)
private val condensed = FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.BOLD))

@Composable
private fun Label(value: String, size: Int = 12, color: Color = Color.White, heavy: Boolean = false, italic: Boolean = false) {
    Text(value, color = color, fontSize = size.sp, lineHeight = (size * 1.08f).sp,
        fontFamily = if (heavy) condensed else FontFamily.SansSerif,
        fontWeight = if (heavy) FontWeight.Black else FontWeight.SemiBold,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal)
}

@Composable
private fun Kicker(id: Int) {
    Text(stringResource(id).uppercase(Locale.US), color = Mist, fontSize = 10.sp, lineHeight = 12.sp,
        fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
}

@Composable
private fun Action(id: Int, onClick: () -> Unit, primary: Boolean = false, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Box(modifier.heightIn(min = 48.dp).fillMaxWidth().clip(shape)
        .background(if (primary) Color(0xff3538cc) else Panel)
        .border(1.dp, if (primary) Color(0xffa9beff) else Line, shape)
        .clickable(role = Role.Button, onClick = onClick)) {
        if (primary) Image(painterResource(R.drawable.primary_action_plate), null, Modifier.matchParentSize(), contentScale = ContentScale.FillBounds)
        Row(Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Label(stringResource(id), 18, heavy = true)
            Label(if (primary) "▶" else "›", 19, if (primary) Gold else Mist, true)
        }
    }
}

@Composable
private fun TrackMark() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Image(painterResource(R.drawable.court_orange_sphere), null, Modifier.size(17.dp))
        Text(stringResource(R.string.track), color = Color.White, fontWeight = FontWeight.Black,
            fontSize = 10.sp, letterSpacing = 1.7.sp)
    }
}

@Composable
internal fun HorseBasketball(game: GameModel) {
    val art = arenaArt()
    BackHandler { game.back() }
    val shape = RoundedCornerShape(14.dp)
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(Color(0xff1448cd), Navy)))
        .windowInsetsPadding(WindowInsets.displayCutout).padding(9.dp)
        .clip(shape).background(Navy).border(2.dp, Color(0xffebf1ff), shape).padding(2.dp)) {
        when (game.screen) {
            Screen.Home -> Column(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) { Home(game, art) }
                val context = LocalContext.current
                Box(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(role = Role.Button) {
                    context.startActivity(Intent(context, CourtsidePolicyActivity::class.java))
                }, contentAlignment = Alignment.Center) {
                    Label(stringResource(R.string.courtside_privacy_policy), 15, Gold, true)
                }
            }
            Screen.Race -> RaceView(game, art)
            Screen.Rules -> Page(R.string.rules, game) {
                Split({ Tutorial(art) }, {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(R.string.rule_one, R.string.rule_two, R.string.rule_three).forEachIndexed { index, line ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Label("0${index + 1}", 18, Gold, true, true)
                                Spacer(Modifier.width(10.dp))
                                Label(stringResource(line), 15)
                            }
                        }
                        Action(R.string.play, game::start, true)
                    }
                })
            }
            Screen.Results, Screen.Records -> ScorePage(game, art)
        }
    }
}

@Composable
private fun Home(game: GameModel, art: ArenaArt) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val hero: @Composable () -> Unit = {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                Cover(art)
                val size = (maxHeight.value * .13f).toInt().coerceIn(24,48)
                Column(Modifier.padding(start = 19.dp, top = 12.dp)) {
                    TrackMark()
                    Spacer(Modifier.height(7.dp))
                    Label(stringResource(R.string.horse), size, Color.White, true, true)
                    Label(stringResource(R.string.sport_title), size, Gold, true, true)
                }
                Box(Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 8.dp)) {
                    Label(stringResource(R.string.format), 10)
                }
            }
        }
        val menu: @Composable () -> Unit = {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Label(stringResource(R.string.race_setup), 26, heavy = true, italic = true)
                Row(Modifier.fillMaxWidth().padding(bottom = 3.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column { Kicker(R.string.distance); Label("600 m", 16, Gold, true) }
                    Column { Kicker(R.string.clock); Label("1:00", 16, Gold, true) }
                }
                Action(R.string.play, game::start, true)
                Action(R.string.rules, { game.open(Screen.Rules) })
                Action(R.string.records, { game.open(Screen.Records) })
            }
        }
        if (maxHeight > maxWidth) Column {
            Box(Modifier.weight(1f)) { hero() }
            menu()
        } else Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1.65f).fillMaxHeight()) { hero() }
            Box(Modifier.weight(1f)) { menu() }
        }
    }
}

@Composable
private fun Page(title: Int, game: GameModel, body: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clickable(role = Role.Button) { game.open(Screen.Home) }, contentAlignment = Alignment.Center) {
                val home = stringResource(R.string.home)
                Box(Modifier.semantics { contentDescription = home }) { Label("‹", 32, Gold, true) }
            }
            Label(stringResource(title), 27, heavy = true, italic = true)
            Spacer(Modifier.weight(1f))
            TrackMark()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
        Spacer(Modifier.height(10.dp))
        body()
    }
}

@Composable
private fun Split(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxHeight > maxWidth) Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().weight(1f)) { left() }
            Box(Modifier.fillMaxWidth().weight(1.35f).verticalScroll(rememberScrollState())) { right() }
        } else Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(10.dp))) { left() }
            Box(Modifier.weight(1.08f).verticalScroll(rememberScrollState())) { right() }
        }
    }
}

@Composable
private fun Tutorial(art: ArenaArt) {
    var demo by remember { mutableStateOf(Race().apply { countdown = 0.0; distance = 10.0 }) }
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        var last = withFrameNanos { it }
        var pending = 0.0
        while (true) withInfiniteAnimationFrameNanos { now ->
            pending += ((now - last) / 1e9).coerceAtMost(.1)
            last = now
            while (pending >= STEP) {
                if (demo.inRange) demo.shoot()
                demo.step()
                if (demo.distance > 44) demo = Race().apply { countdown = 0.0; distance = 10.0 }
                pending -= STEP
            }
            frame++
        }
    }
    Box(Modifier.fillMaxSize().background(Panel)) {
        Canvas(Modifier.fillMaxSize()) {
            frame
            art.race(drawContext.canvas.nativeCanvas, size.width, size.height, demo)
        }
        Box(Modifier.align(Alignment.TopStart).padding(12.dp)) { Label(stringResource(R.string.rule_tip), 13) }
    }
}

private fun decimal(value: Double) = String.format(Locale.US, "%.1f", value)
private fun placeTitle(place: Int) = when (place) { 1 -> R.string.first; 2 -> R.string.second; else -> R.string.third }

@Composable
private fun ScorePage(game: GameModel, art: ArenaArt) {
    val records = game.screen == Screen.Records
    val r = game.race
    val prefs = game.records
    Page(if (records) R.string.records else R.string.results, game) {
        Split({
            Box(Modifier.fillMaxSize()) {
                Cover(art)
                Column(Modifier.padding(14.dp)) {
                    Kicker(if (records) R.string.track else R.string.finish)
                    val title = if (records) R.string.records else if (r.finished) placeTitle(r.place) else R.string.time_up
                    Label(stringResource(title), 32, Gold, true, true)
                }
            }
        }, {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                if (records && !prefs.contains("last")) Label(stringResource(R.string.empty_records), 17)
                else {
                    val stats = if (records) listOf(
                        R.string.wins to prefs.getInt("wins", 0).toString(),
                        R.string.best_place to listOf("—", "1st", "2nd", "3rd")[prefs.getInt("place", 0)],
                        R.string.fastest to if (prefs.contains("time")) "${decimal(prefs.getFloat("time", 0f).toDouble())} s" else "—",
                        R.string.top_speed to "${decimal(prefs.getFloat("speed", 0f).toDouble())} m/s"
                    ) else listOf(R.string.distance to "${r.distance.toInt()} / 600 m", R.string.baskets to "${r.baskets} / ${r.attempts}",
                        R.string.top_speed to "${decimal(r.peak)} m/s", R.string.race_time to "${decimal(r.elapsed)} s")
                    stats.forEach { (label, value) ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Label(stringResource(label), 14, Mist)
                            Label(value, 17, heavy = true)
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))
                    }
                }
                Action(if (records) R.string.play else R.string.race_again, game::start, true)
                if (!records) Action(R.string.home, { game.open(Screen.Home) })
            }
        })
    }
}

@Composable
private fun RaceView(game: GameModel, art: ArenaArt) {
    val r = game.race
    val paused = game.paused
    LaunchedEffect(r, paused) {
        if (!paused) {
            var last = withFrameNanos { it }
            while (game.screen == Screen.Race && !game.paused) withInfiniteAnimationFrameNanos { now ->
                game.tick((now - last) / 1e9)
                last = now
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        val shoot = stringResource(R.string.shoot)
        Canvas(Modifier.fillMaxSize().semantics { contentDescription = shoot }
            .clickable(enabled = !paused, role = Role.Button, indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { r.shoot() }) {
            game.frame
            art.race(drawContext.canvas.nativeCanvas, size.width, size.height, r)
        }
        game.frame
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Kicker(R.string.position)
                Label("${r.place} / 3", 23, Gold, true, true)
            }
            if (!paused) {
                val pause = stringResource(R.string.pause)
                Box(Modifier.size(48.dp).background(Navy.copy(alpha = .7f), RoundedCornerShape(10.dp))
                    .semantics { contentDescription = pause }.clickable(role = Role.Button, onClick = game::togglePause),
                    contentAlignment = Alignment.Center) { Label("Ⅱ", 24, heavy = true) }
            }
        }
        Column(Modifier.align(Alignment.TopCenter).padding(top = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val seconds = ceil(r.clock).toInt().coerceAtLeast(0)
            Label(String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60), 33, if (r.boost > 0) Gold else Color.White, true)
            Label("${stringResource(R.string.speed)}  ${decimal(r.speed)} m/s", 13)
            if (r.boost > 0) Label(stringResource(R.string.slow_clock), 12, Gold, true)
        }
        val caption = when {
            r.countdown > 0 -> ceil(r.countdown).toInt().toString()
            r.elapsed < .65 -> stringResource(R.string.go)
            r.callTime > 0 -> stringResource(if (r.call == Call.Basket) R.string.basket else R.string.miss)
            r.inRange -> stringResource(R.string.shoot)
            else -> ""
        }
        if (caption.isNotEmpty()) Box(Modifier.align(Alignment.Center)) {
            Label(caption, if (r.countdown > 0) 48 else 23, Gold, true, true)
        }
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 17.dp, vertical = 7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Label(stringResource(R.string.track), 10, Mist)
                Label("${r.distance.toInt()} / 600 m", 12)
            }
            Spacer(Modifier.height(3.dp))
            Box(Modifier.fillMaxWidth().height(3.dp).background(Line)) {
                Box(Modifier.fillMaxWidth((r.distance / TRACK).toFloat().coerceIn(0f, 1f)).fillMaxHeight().background(Gold))
            }
        }
        if (paused) {
            Box(Modifier.fillMaxSize().background(Navy.copy(alpha = .90f)), contentAlignment = Alignment.Center) {
                Column(Modifier.widthIn(max = 280.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Label(stringResource(R.string.paused), 30, Gold, true, true)
                    Action(R.string.resume, game::togglePause, true)
                    Action(R.string.restart, game::start)
                    Action(R.string.home, { game.open(Screen.Home) })
                }
            }
        }
    }
}
