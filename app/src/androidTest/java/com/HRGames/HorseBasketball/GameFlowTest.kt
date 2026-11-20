package com.HRGames.HorseBasketball

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class GameFlowTest {
    @get:Rule val ui = createAndroidComposeRule<MainActivity>()

    private fun back() = ui.runOnUiThread { ui.activity.onBackPressedDispatcher.onBackPressed() }

    @Test fun menuBackAndPausedRaceSurviveRecreation() {
        ui.runOnUiThread {
            val window = ui.activity.window.decorView
            assertTrue("The game should open in portrait", window.height > window.width)
        }
        ui.onNodeWithText("How to Play").performClick()
        ui.onNodeWithText("Shoot when the rim lights up.").assertExists()
        back()
        ui.onNodeWithText("Records").performClick()
        back()
        back()
        ui.onNodeWithText("Play").performClick()
        back()
        ui.onNodeWithText("Race paused").assertExists()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("Race paused").assertExists()
        back()
        ui.onNodeWithContentDescription("Pause").performClick()
        ui.onNodeWithText("Restart").performClick()
        ui.onNodeWithContentDescription("Pause").performClick()
        ui.onNodeWithText("Home").performClick()
        ui.onNodeWithText("How to Play").assertExists()
    }

    @Test fun resultsAndRecordsPersistOnce() {
        ui.onNodeWithText("Play").performClick()
        ui.runOnUiThread {
            val game = ViewModelProvider(ui.activity)[GameModel::class.java]
            val before = game.records.getInt("wins", 0)
            repeat(5000) {
                if (game.race.inRange) game.race.shoot()
                game.tick(STEP)
            }
            assertEquals(before + 1, game.records.getInt("wins", 0))
        }
        ui.onNodeWithText("1st Place!").assertExists()
        ui.activityRule.scenario.recreate()
        ui.onNodeWithText("1st Place!").assertExists()
        back()
        ui.onNodeWithText("Records").performClick()
        ui.onNodeWithText("1st").assertExists()
        back()
        ui.onNodeWithText("Play").assertExists()
    }

    @Test fun frameRateAndRestorationPreserveSimulation() {
        ui.runOnUiThread {
            val saved = SavedStateHandle()
            val slow = GameModel(ui.activity.application, saved)
            val fast = GameModel(ui.activity.application, SavedStateHandle())
            slow.start()
            fast.start()
            repeat(320) { slow.tick(STEP) }
            repeat(640) { fast.tick(STEP / 2) }
            assertEquals(slow.race.distance, fast.race.distance, 0.0)
            assertEquals(slow.race.clock, fast.race.clock, 0.0)
            slow.race.shoot()
            slow.tick(.1)
            slow.suspendRace()
            val restored = GameModel(ui.activity.application, saved)
            assertTrue(restored.paused)
            assertEquals(Screen.Race, restored.screen)
            assertEquals(slow.race.shot?.age, restored.race.shot?.age)
            val distance = restored.race.distance
            restored.tick(1.0)
            assertEquals(distance, restored.race.distance, 0.0)
        }
    }
}
