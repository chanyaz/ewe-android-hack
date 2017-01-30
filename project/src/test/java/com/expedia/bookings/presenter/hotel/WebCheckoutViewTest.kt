package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class))
class WebCheckoutViewTest {

    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity
    lateinit var webCheckoutViewObservable: TestSubscriber<Unit>

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        Ui.getApplication(activity).defaultHotelComponents()
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams()
        hotelPresenter.show(hotelPresenter.detailPresenter)
        webCheckoutViewObservable = TestSubscriber<Unit>()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fireCreateTripObservable.subscribe(webCheckoutViewObservable)

    }

    private fun getDummyHotelSearchParams(): HotelSearchParams {
        return HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.calendar_max_selectable_date_range))
                .destination(getDummySuggestion())
                .adults(2)
                .children(listOf(10, 10, 10))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams
    }

    @Test
    fun webCheckoutUsed() {
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(true)
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
    }

    @Test
    fun webCheckoutNotUsedOnUnsupportedPOS() {
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(false)
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    fun webCheckoutNotUsedWithFeatureToggleOff() {
        featureToggleWebCheckout(false)
        setPOSWithWebCheckoutEnabled(true)
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    fun webCheckoutNotUsedOnUnsupportedPOSAndFeatureToggleOff() {
        featureToggleWebCheckout(false)
        setPOSWithWebCheckoutEnabled(true)
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    fun webViewTripIDOnSuccessfulBooking() {
        val bookingTripIDSubscriber = TestSubscriber<String>()
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(true)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
        bookingTripIDSubscriber.assertValueCount(0)
        val tripID = "testing-for-confirmation"
        hotelPresenter.webCheckoutView.onWebPageStarted(hotelPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL + "?tripid=$tripID", null)
        bookingTripIDSubscriber.assertValueCount(1)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    private fun selectHotelRoom() {
        val hotelRoomResponse = HotelOffersResponse.HotelRoomResponse()
        hotelPresenter.hotelDetailViewModel.roomSelectedSubject.onNext(hotelRoomResponse)
    }

    private fun setPOSWithWebCheckoutEnabled(enable: Boolean) {
        val pointOfSale = if (enable) PointOfSaleId.INDIA else PointOfSaleId.UNITED_STATES
        SettingUtils.save(activity, "point_of_sale_key", pointOfSale.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun featureToggleWebCheckout(enable: Boolean) {
        SettingUtils.save(activity, R.string.preference_enable_3DS_checkout, enable)
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        return suggestion
    }
    
}