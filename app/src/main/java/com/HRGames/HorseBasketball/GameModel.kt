package com.HRGames.HorseBasketball

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import java.util.UUID

internal class GameModel(app: Application, private val saved: SavedStateHandle) : AndroidViewModel(app) {
    val records = app.getSharedPreferences("track_records", 0)
    var screen by mutableStateOf(Screen.valueOf(saved["screen"] ?: Screen.Home.name))
        private set
    var race = saved.get<Race>("race") ?: Race()
        private set
    var paused by mutableStateOf(screen == Screen.Race)
        private set
    var frame by mutableIntStateOf(0)
        private set
    private var id = saved.get<String>("id") ?: ""
    private var accumulator = 0.0

    fun open(page: Screen) {
        screen = page
        saved["screen"] = page.name
        paused = false
        accumulator = 0.0
    }

    fun start() {
        id = UUID.randomUUID().toString()
        race = Race()
        frame++
        open(Screen.Race)
        snapshot()
    }

    fun togglePause() {
        if (screen == Screen.Race) paused = !paused
        accumulator = 0.0
    }

    fun suspendRace() {
        if (screen == Screen.Race) paused = true
        snapshot()
    }

    fun snapshot() {
        saved["race"] = race
        saved["id"] = id
    }

    fun back() {
        when (screen) {
            Screen.Race -> togglePause()
            Screen.Home -> Unit
            else -> open(Screen.Home)
        }
    }

    fun tick(seconds: Double) {
        if (paused || screen != Screen.Race) return
        accumulator += seconds
        while (accumulator + 1e-10 >= STEP && !race.ended) {
            race.step()
            accumulator -= STEP
        }
        frame++
        if (race.ended) {
            if (records.getString("last", "") != id) {
                val edit = records.edit().putString("last", id)
                    .putFloat("speed", maxOf(records.getFloat("speed", 0f), race.peak.toFloat()))
                if (race.finished) {
                    edit.putInt("place", minOf(records.getInt("place", 4), race.place))
                    edit.putFloat("time", minOf(records.getFloat("time", Float.MAX_VALUE), race.elapsed.toFloat()))
                    if (race.place == 1) edit.putInt("wins", records.getInt("wins", 0) + 1)
                }
                edit.apply()
            }
            open(Screen.Results)
            snapshot()
        }
    }
}
