package com.cobresun.brun.pantsorshorts.weather

import com.cobresun.brun.pantsorshorts.weather.api.WeatherAPIService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class WeatherRepositoryTest {
    @Mock
    private lateinit var mockApiService: WeatherAPIService

    @Mock
    private lateinit var mockWeatherResponse: WeatherResponse

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `getWeather() should return a WeatherResponse if inputs are correct`() = runBlocking {
        whenever(mockApiService.getWeatherResponse("apiKey", 0.0, 0.0)).thenReturn(mockWeatherResponse)

        val weatherRepository = WeatherRepository(apiService = mockApiService, apiKey = "apiKey")
        val result = weatherRepository.getWeather(0.0, 0.0)

        assertEquals(mockWeatherResponse, result)
    }

    @Test
    fun `getWeather() should return null if apiKey is incorrect`() = runBlocking {
        whenever(mockApiService.getWeatherResponse("apiKey", 0.0, 0.0)).thenReturn(mockWeatherResponse)

        val weatherRepository = WeatherRepository(apiService = mockApiService, apiKey = "wrongApiKey")
        val result = weatherRepository.getWeather(0.0, 0.0)

        assertEquals(null, result)
    }
}
