package com.HRGames.HorseBasketball

import java.io.Serializable
import kotlin.math.abs
import kotlin.math.min

internal const val TRACK = 600.0
internal const val STEP = 1.0 / 60.0
internal const val FLIGHT = 0.80
internal const val RIM_RADIUS = 1.75

internal enum class Screen { Home, Rules, Race, Results, Records }
internal enum class Call { None, Basket, Miss }

internal data class Shot(val origin: Double, val reach: Double, val target: Double, var age: Double = 0.0) : Serializable {
    val position get() = origin + reach * age / FLIGHT
    val height: Double get() {
        val u = age / FLIGHT
        return 75 * u + 205 * u * (1 - u)
    }
}

internal class Race : Serializable {
    var countdown = 3.0
    var distance = 0.0
    var elapsed = 0.0
    var clock = 60.0
    var speed = 9.0
    var peak = speed
    var boost = 0.0
    var nextHoop = 35.0
    var baskets = 0
    var attempts = 0
    var call = Call.None
    var callTime = 0.0
    var shot: Shot? = null
    var ended = false
    val finished get() = distance >= TRACK
    val place get() = 1 + listOf(10.0, 11.0).count { elapsed * it > distance + 0.000001 }
    val reach get() = 12.0 + speed * 0.45
    val inRange get() = nextHoop < TRACK && abs(distance + reach - nextHoop) <= RIM_RADIUS

    fun shoot() {
        if (ended || countdown > 0 || shot != null || nextHoop >= TRACK || nextHoop - distance > 35.0) return
        shot = Shot(distance, reach, nextHoop)
        nextHoop += 55.0
        attempts++
    }

    private fun resolve(hit: Boolean) {
        call = if (hit) Call.Basket else Call.Miss
        callTime = 1.05
        boost = if (hit) 3.0 else 0.0
        speed = if (hit) min(18.0, speed + 2.5) else maxOf(5.0, speed - 2.0)
        peak = maxOf(peak, speed)
        if (hit) baskets++
    }

    fun step() {
        if (ended) return
        if (countdown > 0.0) {
            countdown = (countdown - STEP).coerceAtLeast(0.0)
            if (countdown < 1e-8) countdown = 0.0
            return
        }
        var left = STEP
        while (left > 1e-9 && !ended) {
            val activeShot = shot
            val rate = if (boost > 0.0) 0.5 else 1.0
            val toFinish = (TRACK - distance) / speed
            val toTimeout = clock / rate
            val toRim = if (activeShot != null && activeShot.age < FLIGHT) FLIGHT - activeShot.age else Double.POSITIVE_INFINITY
            val toPass = if (nextHoop < TRACK) (nextHoop - distance).coerceAtLeast(0.0) / speed else Double.POSITIVE_INFINITY
            val dt = minOf(left, toFinish, toTimeout, toRim, toPass, if (boost > 0) boost else left)
            elapsed += dt
            distance += speed * dt
            clock = (clock - rate * dt).coerceAtLeast(0.0)
            boost = (boost - dt).coerceAtLeast(0.0)
            callTime = (callTime - dt).coerceAtLeast(0.0)
            if (activeShot != null) activeShot.age += dt
            left -= dt
            if (dt == toFinish || dt == toTimeout) {
                if (toFinish <= toTimeout + 1e-10) distance = TRACK
                ended = true
            } else if (dt == toRim && activeShot != null) {
                activeShot.age = FLIGHT
                resolve(abs(activeShot.position - activeShot.target) <= RIM_RADIUS)
            } else if (dt == toPass) {
                attempts++
                nextHoop += 55.0
                resolve(false)
            }
            if (activeShot != null && activeShot.age >= FLIGHT + 0.24) shot = null
        }
    }
}
