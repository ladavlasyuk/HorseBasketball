package com.HRGames.HorseBasketball.courtside

import android.app.Activity
import android.content.Intent
import android.os.Build
import com.HRGames.HorseBasketball.R

internal fun Activity.showWebsite(pageUrl: String) {
    val browserIntent = Intent(this, CourtsideWebActivity::class.java).apply {
        putExtra(CourtsideWebActivity.EXTRA_START_ADDRESS, pageUrl)
    }
    startActivity(browserIntent)
    animateWebTransition(this, closing = false)
    finish()
    animateWebTransition(this, closing = true)
}

@Suppress("DEPRECATION")
private fun animateWebTransition(activity: Activity, closing: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val type = if (closing) {
            Activity.OVERRIDE_TRANSITION_CLOSE
        } else {
            Activity.OVERRIDE_TRANSITION_OPEN
        }
        activity.overrideActivityTransition(
            type,
            R.anim.courtside_fade_in,
            R.anim.courtside_fade_out
        )
    } else {
        activity.overridePendingTransition(R.anim.courtside_fade_in, R.anim.courtside_fade_out)
    }
}
