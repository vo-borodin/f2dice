package com.voborodin.f2dice.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.voborodin.f2dice.types.BTDevice
import com.voborodin.f2dice.types.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

object PREFS {
    val THEME_KEY = stringPreferencesKey("THEME_KEY")
    val ROLE = stringPreferencesKey("ROLE")
    val CONNECTED_DEVICE_NAME = stringPreferencesKey("CONNECTED_DEVICE_NAME")
    val CONNECTED_DEVICE_ADDRESS = stringPreferencesKey("CONNECTED_DEVICE_ADDRESS")
}

class F2DiceDataStore private constructor(context: Context) {

    companion object {

        @Volatile
        private var INSTANCE: F2DiceDataStore? = null

        fun getInstance(context: Context): F2DiceDataStore {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = F2DiceDataStore(context)
                    INSTANCE = instance
                }
                return instance
            }
        }
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "DiceDS")
    private val dataStore = context.dataStore


    //theme prefs
    suspend fun setAppTheme(theme: String) {
        dataStore.edit { pref ->
            pref[PREFS.THEME_KEY] = theme
        }
    }

    fun getAppTheme(): Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { pref ->
            val theme = pref[PREFS.THEME_KEY] ?: "System Default"
            theme
        }

    suspend fun setConnectedDevice(device: BTDevice?) {
        dataStore.edit { pref ->
            if (device != null) {
                pref[PREFS.CONNECTED_DEVICE_NAME] = device.name ?: "UNKNOWN DEVICE"
                pref[PREFS.CONNECTED_DEVICE_ADDRESS] = device.address
            } else {
                pref.remove(PREFS.CONNECTED_DEVICE_NAME)
                pref.remove(PREFS.CONNECTED_DEVICE_ADDRESS)
            }
        }
    }

    fun getConnectedDevice(): Flow<BTDevice?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {pref ->
            val name = pref[PREFS.CONNECTED_DEVICE_NAME]
            val address = pref[PREFS.CONNECTED_DEVICE_ADDRESS] ?: ""

            if (address.isEmpty()) {
                null
            } else {
                val device = BTDevice(name, address)
                device
            }
        }

    suspend fun setRole(role: Role?) {
        dataStore.edit { pref ->
            if (role != null) {
                pref[PREFS.ROLE] = role.name
            } else {
                pref.remove(PREFS.ROLE)
            }
        }
    }

    fun getRole(): Flow<Role?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map {pref ->
            val name = pref[PREFS.ROLE]

            if (name.isNullOrEmpty()) {
                null
            } else {
                val role = enumValueOf<Role>(name)
                role
            }
        }
}