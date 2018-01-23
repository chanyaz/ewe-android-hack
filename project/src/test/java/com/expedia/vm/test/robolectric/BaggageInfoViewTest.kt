package com.expedia.vm.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.BaggageInfoResponse
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.flights.BaggageInfoView
import com.expedia.vm.flights.BaggageInfoViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BaggageInfoViewTest {

    private var sut = BaggageInfoView(RoboTestHelper.getContext())
    lateinit var activity: Activity
    lateinit var vm: BaggageInfoViewModel

    @Before
    fun before() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        sut.baggageInfoParentContainer = LayoutInflater.from(activity).inflate(R.layout.baggage_info_parent, null) as LinearLayout
        vm = BaggageInfoViewModel(activity)
        sut.baggageInfoViewModel = vm
    }

    @Test
    fun baggageInfoHappyPathTest() {
        vm.makeBaggageInfoObserver().onNext(makeBaggageInfoResponse())
        var key: CharSequence
        var value: CharSequence

        var childLayout = sut.baggageInfoParentContainer.getChildAt(3) as LinearLayout
        key = childLayout.findViewById<TextView>(R.id.baggage_fee_key).text
        assertEquals("Airline", key)
        value = childLayout.findViewById<TextView>(R.id.baggage_fee_value).text
        assertEquals("JetBlue Airways", value)

        childLayout = sut.baggageInfoParentContainer.getChildAt(4) as LinearLayout
        key = childLayout.findViewById<TextView>(R.id.baggage_fee_key).text
        assertEquals("Carry-on Bag", key)
        value = childLayout.findViewById<TextView>(R.id.baggage_fee_value).text
        assertEquals("No fee", value)

        childLayout = sut.baggageInfoParentContainer.getChildAt(5) as LinearLayout
        key = childLayout.findViewById<TextView>(R.id.baggage_fee_key).text
        assertEquals("1st Checked Bag", key)
        value = childLayout.findViewById<TextView>(R.id.baggage_fee_value).text
        assertEquals("No fee up to 30 kg", value)
    }

    private fun makeBaggageInfoResponse(): BaggageInfoResponse {
        val baggageInfoResponse = BaggageInfoResponse()
        baggageInfoResponse.airlineName = "JetBlue Airways"
        var chargesList = ArrayList<HashMap<String, String>>()
        chargesList.add(hashMapOf("Carry-on Bag" to "No fee"))
        chargesList.add(hashMapOf("1st Checked Bag" to "No fee up to 30 kg"))
        baggageInfoResponse.charges = chargesList
        return baggageInfoResponse
    }
}
