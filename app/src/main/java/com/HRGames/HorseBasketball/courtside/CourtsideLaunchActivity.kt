package com.HRGames.HorseBasketball.courtside

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.HRGames.HorseBasketball.MainActivity

class CourtsideLaunchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = LaunchPreferences(this)
        setContent {
            CourtsideLobby(
                settings = settings,
                showWebsite = ::showWebsite,
                openGame = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )
        }
    }
}
