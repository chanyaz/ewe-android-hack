package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
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
    var userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        Ui.getApplication(activity).defaultHotelComponents()
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
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webCheckoutUsed() {
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webCheckoutNotUsedOnUnsupportedPOS() {
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(false)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webCheckoutNotUsedWithFeatureToggleOff() {
        featureToggleWebCheckout(false)
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webCheckoutNotUsedOnUnsupportedPOSAndFeatureToggleOff() {
        featureToggleWebCheckout(false)
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(0)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun webViewTripIDOnSuccessfulBooking() {
        val bookingTripIDSubscriber = TestSubscriber<String>()
        val fectchTripIDSubscriber = TestSubscriber<String>()
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(true)
        setUpTestToStartAtDetailsScreen()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fectchTripIDSubscriber)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        selectHotelRoom()
        webCheckoutViewObservable.assertValueCount(1)
        bookingTripIDSubscriber.assertValueCount(0)
        fectchTripIDSubscriber.assertValueCount(0)
        val tripID = "testing-for-confirmation"
        verify(userAccountRefresherMock, times(0)).forceAccountRefresh()

        hotelPresenter.webCheckoutView.onWebPageStarted(hotelPresenter.webCheckoutView.webView, PointOfSale.getPointOfSale().hotelsWebBookingConfirmationURL + "?tripid=$tripID", null)
        verify(userAccountRefresherMock, times(1)).forceAccountRefresh()
        bookingTripIDSubscriber.assertValueCount(1)
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fectchTripIDSubscriber.assertValueCount(1)
        fectchTripIDSubscriber.assertValue(tripID)
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

    private fun setUpTestToStartAtDetailsScreen(){
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams()
        hotelPresenter.show(hotelPresenter.detailPresenter)
        webCheckoutViewObservable = TestSubscriber<Unit>()
        (hotelPresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fireCreateTripObservable.subscribe(webCheckoutViewObservable)
    }
    
}