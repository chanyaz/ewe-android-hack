package com.expedia.bookings.itin.common

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class GoogleMapsLiteMapViewTest {
    lateinit var sut: GoogleMapsLiteMapView
    val context = RuntimeEnvironment.application
    lateinit var mockDrawable: Drawable

    private val AIRPORT_ICON = R.drawable.flight_itin_map_airport_icon
    private val PIN_COLOR = R.color.itin_map_pin_color

    @Before
    fun setup() {
        sut = LayoutInflater.from(context).inflate(R.layout.test_google_maps_lite_widget, null) as GoogleMapsLiteMapView
        mockDrawable = mock(Drawable::class.java)
        `when`(mockDrawable.intrinsicWidth).thenReturn(1)
        `when`(mockDrawable.intrinsicHeight).thenReturn(1)
    }

    @Test
    fun bitmapDescriptorFromVectorNoColorPassed() {
        verify(mockDrawable, never()).setTint(anyInt())
        sut.bitmapFromVector(context = context, vectorResId = AIRPORT_ICON, vectorDrawable = mockDrawable)
        verify(mockDrawable, never()).setTint(anyInt())
    }

    @Test
    fun bitmapDescriptorFromVectorColorPassed() {
        verify(mockDrawable, never()).setTint(anyInt())
        sut.bitmapFromVector(context = context, vectorResId = AIRPORT_ICON, colorResId = PIN_COLOR, vectorDrawable = mockDrawable)
        verify(mockDrawable, times(1)).setTint(anyInt())
    }
}