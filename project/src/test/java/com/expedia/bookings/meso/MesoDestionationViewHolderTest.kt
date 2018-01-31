package com.expedia.bookings.meso

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.ADMS_Measurement
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.meso.model.MesoDestinationAdResponse
import com.expedia.bookings.meso.vm.MesoDestinationViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class MesoDestionationViewHolderTest {

    lateinit var sut: MesoDestinationViewHolder
    lateinit var vm: MesoDestinationViewModel
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Before
    fun before() {
        createSystemUnderTest()
    }

    @Test
    fun testPerformClickGoToWebView() {
        val shadowActivity = Shadows.shadowOf(activity)
        sut.itemView.performClick()
        val startedIntent = shadowActivity.nextStartedActivity
        val extras = startedIntent.extras
        assertEquals(WebViewActivity::class.java.name, startedIntent.component.className)
        assertEquals("Las Vegas", extras.getString("ARG_TITLE"))
        assertEquals(ADMS_Measurement.getUrlWithVisitorData("https://viewfinder.expedia.com/features/vintage-las-vegas"), extras.getString("ARG_URL"))
    }

    private fun createSystemUnderTest() {
        val view = LayoutInflater.from(activity)
                .inflate(R.layout.meso_destination_launch_card, null)
        vm = MesoDestinationViewModel(activity)
        vm.mesoDestinationAdResponse = MesoDestinationAdResponse("Las Vegas", "Vintage Las Vegas", "Sponsored",
                "https://viewfinder.expedia.com/features/vintage-las-vegas", "https://a.travel-assets.com/dynamic_images/178276.jpg")
        sut = MesoDestinationViewHolder(view, vm)
        sut.bindData()
    }
}
