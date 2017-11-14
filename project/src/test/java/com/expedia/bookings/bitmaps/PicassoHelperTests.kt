package com.expedia.bookings.bitmaps

import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PicassoHelperTests {

    @Test
    fun testThumborSmartURLFromSourceURL() {
        val originalURL = "https://a.travel-assets.com/findyours-php/viewfinder/images/res70/89000/89500-Little-Italy-Ny.jpg"
        val width = 400
        val height = 200
        val thumborSmartURL = PicassoHelper.generateSizedSmartCroppedUrl(originalURL, 400, 200)
        val expectedURL = PicassoHelper.THUMBOR_ENDPOINT + "/${width}x${height}/smart/filters:format(webp)/a.travel-assets.com/findyours-php/viewfinder/images/res70/89000/89500-Little-Italy-Ny.jpg"
        assertEquals(expectedURL, thumborSmartURL)
    }
}
