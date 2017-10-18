package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.presenter.shared.KrazyglueHotelViewHolder
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.TextView
import com.mobiata.android.util.SettingUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class KrazyglueWidgetTest {

    @Test
    fun testVisibilityGONEWhenFeatureToggleONAndBucketingOFF() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, true)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingOFF() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, false)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingON() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, false)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleONAndBucketingONwithoutHotelResults() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        enableKrazyglueTest(activity)
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf())
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityVISIBLEWhenFeatureToggleONAndBucketingON() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        enableKrazyglueTest(activity)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.VISIBLE, krazyglueWidget.visibility)
    }

    @Test
    fun testViewModelHeaderText() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        enableKrazyglueTest(activity)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.cityObservable.onNext("Paris")
        assertEquals("Because you booked a flight, save on select hotels in Paris", krazyglueWidget.headerText.text)
    }

    @Test
    fun testFourHotelsInRecyclerView() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        enableKrazyglueTest(activity)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel(), KrazyglueResponse.KrazyglueHotel(), KrazyglueResponse.KrazyglueHotel(), KrazyglueResponse.KrazyglueHotel()))
        assertEquals(4, krazyglueWidget.hotelsRecyclerView.adapter.itemCount)
    }

    @Test
    fun testHotelBindsDataCorrectly() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val hotelView = LayoutInflater.from(activity).inflate(R.layout.krazyglue_hotel_view, null)

        val viewHolder = KrazyglueHotelViewHolder(hotelView)
        val hotel = KrazyglueResponse.KrazyglueHotel()
        hotel.hotelName = "San Francisco Hotel"
        viewHolder.bindData(hotel)

        assertEquals("San Francisco Hotel", hotelView.findViewById<TextView>(R.id.hotel_name_text_view).text)
    }


    private fun enableKrazyglueTest(activity: Activity?) {
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)
    }
}