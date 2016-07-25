package com.expedia.bookings.data

import android.content.Context
import android.widget.ImageView
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class LXMediaTest {
    fun getContext(): Context {
        return RuntimeEnvironment.application
    }

    @Test
    fun loadImageTest() {
        val lxMedia = LXMedia(ArrayList<String>(), "ImageCaption")
        val imageView = ImageView(getContext(), null)
        lxMedia.loadImage(imageView, null, 0)
        assertEquals("ImageCaption", imageView.contentDescription)
    }

    @Test
    fun loadImageWhenNullCaptionTest() {
        val lxMedia = LXMedia(ArrayList<String>(), null)
        val imageView = ImageView(getContext(), null)
        lxMedia.loadImage(imageView, null, 0)
        assertNull(imageView.contentDescription)
    }

    @Test
    fun loadErrorImageTest() {
        val lxMedia = LXMedia(ArrayList<String>(), "ImageCaption")
        val imageView = ImageView(getContext(), null)
        lxMedia.loadErrorImage(imageView, null, 0)
        assertEquals("ImageCaption", imageView.contentDescription)
    }
}