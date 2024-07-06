package com.cobresun.brun.pantsorshorts

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cobresun.brun.pantsorshorts.weather.TemperatureUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    private val USER_THRESHOLD = intPreferencesKey("user_threshold")

    val temperatureUnitFlow: Flow<TemperatureUnit> = dataStore.data
        .map { preferences ->
            TemperatureUnit.valueOf(preferences[TEMPERATURE_UNIT] ?: TemperatureUnit.CELSIUS.name)
        }

    suspend fun toggleTemperatureUnit() {
        dataStore.edit { preferences ->
            val current = preferences[TEMPERATURE_UNIT] ?: TemperatureUnit.CELSIUS.name
            val new: String = when (current) {
                TemperatureUnit.CELSIUS.name -> TemperatureUnit.FAHRENHEIT.name
                TemperatureUnit.FAHRENHEIT.name -> TemperatureUnit.CELSIUS.name
                else -> TemperatureUnit.CELSIUS.name
            }

            preferences[TEMPERATURE_UNIT] = new
        }
    }

    val userThresholdFlow: Flow<Int> = dataStore.data
        .map { preferences ->
            preferences[USER_THRESHOLD] ?: 21
        }

    suspend fun setUserThreshold(userThreshold: Int) {
        dataStore.edit { preferences ->
            preferences[USER_THRESHOLD] = userThreshold
        }
    }
}
