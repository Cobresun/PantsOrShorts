package com.cobresun.brun.pantsorshorts.weather

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherDataStore @Inject constructor(private val dataStore: DataStore<Preferences>) {
    private val json = Json { encodeDefaults = true }

    private val TEMPERATURE_DATA = stringPreferencesKey("temperature_data")

    val temperatureDataFlow: Flow<TemperatureData> = dataStore.data
        .map { preferences ->
            preferences[TEMPERATURE_DATA]?.let { json.decodeFromString(it) } ?: TemperatureData()
        }

    suspend fun setTemperatureData(temperatureData: TemperatureData) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_DATA] =
                json.encodeToString(TemperatureData.serializer(), temperatureData)
        }
    }
}
