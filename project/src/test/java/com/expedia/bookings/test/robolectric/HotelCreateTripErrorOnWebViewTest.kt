package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RoboTestHelper.assertVisible
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.vm.HotelWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelCreateTripErrorOnWebViewTest {

    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        setPOSWithWebCheckoutEnabled(true)
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        hotelPresenter.show(hotelPresenter.detailPresenter)
        selectHotelRoom()
    }

    private fun getDummyHotelSearchParams(): HotelSearchParams {
        return HotelSearchParams.Builder(activity.resources.getInteger(R.integer.calendar_max_days_hotel_stay),
                activity.resources.getInteger(R.integer.max_calendar_selectable_date_range_hotels_only))
                .destination(getDummySuggestion())
                .adults(2)
                .children(listOf(10, 10, 10))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1)).build() as HotelSearchParams
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCreateTripErrorProductExpiry() {
        (hotelPresenter.webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel.errorObservable.onNext(ApiError(ApiError.Code.HOTEL_PRODUCT_KEY_EXPIRY))
        assertVisible(hotelPresenter.errorPresenter)
        assertEquals("Search Again", hotelPresenter.errorPresenter.errorButton.text)
        assertEquals("We're really sorry, your hold on this room has expired", hotelPresenter.errorPresenter.errorText.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCreateTripErrorRoomUnavailable() {
        (hotelPresenter.webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel.errorObservable.onNext(ApiError(ApiError.Code.HOTEL_ROOM_UNAVAILABLE))
        assertVisible(hotelPresenter.errorPresenter)
        assertEquals("Select another room", hotelPresenter.errorPresenter.errorButton.text)
        assertEquals("This room is sold out.", hotelPresenter.errorPresenter.errorText.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testCreateTripErrorUnknownError() {
        (hotelPresenter.webCheckoutView.viewModel as HotelWebCheckoutViewViewModel).createTripViewModel.errorObservable.onNext(ApiError(ApiError.Code.UNKNOWN_ERROR))
        assertVisible(hotelPresenter.errorPresenter)
        assertEquals("Retry", hotelPresenter.errorPresenter.errorButton.text)
        assertEquals("Sorry, we could not connect to Expedia's servers.  Please try again later.", hotelPresenter.errorPresenter.errorText.text)
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