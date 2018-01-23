package com.expedia.bookings.test

import android.content.Intent
import com.expedia.bookings.data.HotelMedia
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.ui.GalleryActivity
import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)

class GalleryActivityTest {
    private lateinit var galleryActivity: GalleryActivity

    @Before
    fun before() {
        val media1 = HotelMedia("https://hotelphoto1.com", "Hotel1 Description")
        val media2 = HotelMedia("https://hotelphoto2.com", "Hotel2 Description")
        val media3 = HotelMedia("https://hotelphoto3.com", "Hotel3 Description")
        val mediaList = listOf(media1, media2, media3)

        val i = Intent(RuntimeEnvironment.application, GalleryActivity::class.java)
        val gson = GsonBuilder().create()
        val json = gson.toJson(mediaList)
        i.putExtra("Urls", json)
        i.putExtra("Position", 0 )
        i.putExtra("Name", "Happy Hotel" )
        i.putExtra("Rating", 5f )

        galleryActivity = Robolectric.buildActivity(GalleryActivity::class.java, i).create().get()
    }

    @Test
    fun testGalleryItems() {
        assertEquals(3, galleryActivity.gallery.adapter.itemCount)
        assertEquals("Hotel1 Description", galleryActivity.mediaList.elementAt(0).mDescription)
        assertEquals("https://hotelphotob.jpg", galleryActivity.mediaList.elementAt(2).originalUrl)
    }
}
