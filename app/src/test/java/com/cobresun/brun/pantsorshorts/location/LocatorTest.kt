package com.cobresun.brun.pantsorshorts.location

import android.location.Geocoder
import android.location.Location
import android.util.Log
import io.mockk.every
import io.mockk.mockkStatic
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class LocatorTest {
    @Mock
    private lateinit var geocoder: Geocoder

    @Mock
    private lateinit var mockLocation: Location

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        mockkStatic(Log::class)
        every { Log.v(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
    }

    @Test
    fun `getCityName() returns city name if one address is sent back`() {
        val address = mock(android.location.Address::class.java)
        whenever(address.locality).thenReturn("Calgary")
        whenever(address.subLocality).thenReturn("Kincora")

        whenever(geocoder.getFromLocation(mockLocation.latitude, mockLocation.longitude, 1)).thenReturn(listOf(address))

        val locator = Locator(geocoder)
        val result = locator.getCityName(mockLocation)

        assertEquals("Calgary", result)
    }

    @Test
    fun `getCityName() returns sublocality name if one address is sent back without city name but sublocality exists`() {
        val address = mock(android.location.Address::class.java)
        whenever(address.locality).thenReturn(null)
        whenever(address.subLocality).thenReturn("Brooklyn")

        whenever(geocoder.getFromLocation(mockLocation.latitude, mockLocation.longitude, 1)).thenReturn(listOf(address))

        val locator = Locator(geocoder)
        val result = locator.getCityName(mockLocation)

        assertEquals("Brooklyn", result)
    }

    @Test
    fun `getCityName() returns first address's city name if multiple addresses are sent back`() {
        val address1 = mock(android.location.Address::class.java)
        whenever(address1.locality).thenReturn("Calgary")
        whenever(address1.subLocality).thenReturn("Kincora")

        val address2 = mock(android.location.Address::class.java)
        whenever(address2.locality).thenReturn(null)
        whenever(address2.subLocality).thenReturn("Brooklyn")

        whenever(geocoder.getFromLocation(mockLocation.latitude, mockLocation.longitude, 1)).thenReturn(listOf(address1, address2))

        val locator = Locator(geocoder)
        val result = locator.getCityName(mockLocation)

        assertEquals("Calgary", result)
    }

    @Test
    fun `getCityName() returns null if geocoder returns no address`() {
        whenever(geocoder.getFromLocation(mockLocation.latitude, mockLocation.longitude, 1)).thenReturn(emptyList())

        val locator = Locator(geocoder)
        val result = locator.getCityName(mockLocation)

        assertEquals(null, result)
    }

    @Test
    fun `getCityName() returns null if address has null city and sublocality`() {
        val address = mock(android.location.Address::class.java)
        whenever(address.locality).thenReturn(null)
        whenever(address.subLocality).thenReturn(null)

        whenever(geocoder.getFromLocation(mockLocation.latitude, mockLocation.longitude, 1)).thenReturn(listOf(address))

        val locator = Locator(geocoder)
        val result = locator.getCityName(mockLocation)

        assertEquals(null, result)
    }
}
