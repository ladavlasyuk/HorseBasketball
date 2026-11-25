package com.HRGames.HorseBasketball.courtside

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.courtsidePreferences by preferencesDataStore(name = "courtside_launch")

internal class LaunchPreferences(private val store: DataStore<Preferences>) {
    constructor(context: Context) : this(context.applicationContext.courtsidePreferences)
    private val passKey = stringPreferencesKey("courtside_pass")
    private val addressKey = stringPreferencesKey("courtside_address")

    suspend fun storedAddress(): String? {
        val values = store.data.first()
        return values[addressKey]?.takeIf { it.isNotBlank() }
    }

    suspend fun rememberTicket(ticket: WebTicket) {
        store.edit { values ->
            val credential = ticket.credential
            if (credential == null) values.remove(passKey) else values[passKey] = credential
            values[addressKey] = ticket.url
        }
    }
}
