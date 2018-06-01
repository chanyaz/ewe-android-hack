package com.expedia.bookings.itin.common

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.tripstore.data.ItinLOB
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricRunner::class)
class ItinImageWidgetTest {
    private lateinit var mockVM: MockViewModel
    lateinit var sut: ItinImageWidget<ItinLOB>

    @Before
    @Suppress("UNCHECKED_CAST")
    fun setup() {
        val context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_itin_image_widget, null) as ItinImageWidget<ItinLOB>
        mockVM = MockViewModel()
        sut.setupViewModel(mockVM)
    }

    @Test
    fun imageUrlSubjectSubscriptionTest() {
        val prevDrawable = sut.itinImage.drawable
        val url = "https://s3.amazonaws.com/mediavault.le/expedia-static/e4b486117f4dc3b1bb630825aee074e8ce0ea707.jpeg"
        mockVM.imageUrlSubject.onNext(url)
        assertNotEquals(prevDrawable, sut.itinImage.drawable)
    }

    @Test
    fun imageUrlSubjectSubscriptionEmptyTest() {
        val prevDrawable = sut.itinImage.drawable
        val url = ""
        mockVM.imageUrlSubject.onNext(url)
        assertEquals(prevDrawable, sut.itinImage.drawable)
    }

    @Test
    fun nameSubjectSubscriptionTest() {
        assertEquals(View.GONE, sut.itinName.visibility)
        assertEquals("", sut.itinName.text)

        mockVM.nameSubject.onNext("how you doing?")

        assertEquals(View.VISIBLE, sut.itinName.visibility)
        assertEquals("how you doing?", sut.itinName.text)
    }

    private class MockViewModel : ItinImageViewModel<ItinLOB>() {
        override val itinLOBObserver: LiveDataObserver<ItinLOB> = LiveDataObserver { }
    }
}
