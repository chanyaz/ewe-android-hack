package com.expedia.bookings.hotel.widget

import android.content.Intent
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.hotel.activity.HotelGalleryGridActivity
import com.expedia.bookings.hotel.data.HotelGalleryAnalyticsData
import com.expedia.bookings.hotel.data.HotelGalleryConfig
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.test.OmnitureMatchers.Companion.withProps
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class HotelGalleryGridActivityTest {
    private var activity: HotelGalleryGridActivity by Delegates.notNull()
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private val galleryConfig = HotelGalleryConfig("Test Hotel", 4.6f, "a", true, 0)
    private val intentHotels = Intent().apply {
        putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig)
        putExtra(HotelExtras.GALLERY_ANALYTICS_DATA, HotelGalleryAnalyticsData(System.currentTimeMillis(), false))
    }
    private val intentPackages = Intent().apply {
        putExtra(HotelExtras.GALLERY_CONFIG, galleryConfig)
        putExtra(HotelExtras.GALLERY_ANALYTICS_DATA, HotelGalleryAnalyticsData(System.currentTimeMillis(), true))
    }

    @Before
    fun before() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testAnalyticsOnResume() {
        makeActivity(false)
        OmnitureTestUtils.assertStateTracked(
                "App.Hotels.Infosite.Gallery",
                Matchers.allOf(withProps(mapOf(2 to "hotels"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testAnalyticsOnResumePackages() {
        makeActivity(true)
        OmnitureTestUtils.assertStateTracked(
                "App.Package.Hotels.Infosite.Gallery",
                Matchers.allOf(withProps(mapOf(2 to "package:FH"))),
                mockAnalyticsProvider)
    }

    @Test
    fun testAnalyticsOnClick() {
        makeActivity(false)
        activity.adapter.selectedImagePosition.onNext(0)
        OmnitureTestUtils.assertLinkTracked("Image Gallery", "App.Hotels.IS.Gallery.OpenImage", mockAnalyticsProvider)
    }

    @Test
    fun testAnalyticsOnClickPackage() {
        makeActivity(true)
        activity.adapter.selectedImagePosition.onNext(0)
        OmnitureTestUtils.assertLinkTracked("Image Gallery", "App.Package.Hotels.IS.Gallery.OpenImage", mockAnalyticsProvider)
    }

    private fun makeActivity(fromPackages: Boolean): HotelGalleryGridActivity {
        activity = Robolectric.buildActivity(HotelGalleryGridActivity::class.java, if (fromPackages) intentPackages else intentHotels).create().resume().get()
        return activity
    }
}
