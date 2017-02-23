package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowResourcesEB
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelConfirmationPresenterTest {
    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.immediate(), "../lib/mocked/templates")
        @Rule get


    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Control)
        Ui.getApplication(activity).defaultHotelComponents()
        featureToggleWebCheckout(true)
        setPOSWithWebCheckoutEnabled(true)
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        hotelPresenter.show(hotelPresenter.detailPresenter)
        selectHotelRoom()
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
    fun testConfirmationScreenPopulatedByItinsCall() {
        val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
        val makeItinResponseObserver = hotelPresenter.makeNewItinResponseObserver()
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("web_view_hotel_trip_details", makeItinResponseObserver)
        testObserver.awaitTerminalEvent(10, TimeUnit.SECONDS)
        assertEquals("CitiGarden Hotel", hotelPresenter.confirmationPresenter.hotelNameTextView.text)
        assertEquals("Feb 10 – 11, 2017", hotelPresenter.confirmationPresenter.checkInOutDateTextView.text)
        assertEquals("245 S Airport Blvd", hotelPresenter.confirmationPresenter.addressL1TextView.text)
        assertEquals("South San Francisco, CA", hotelPresenter.confirmationPresenter.addressL2TextView.text)
        assertEquals("Itinerary #7241053124635", hotelPresenter.confirmationPresenter.itinNumberTextView.text)
        assertEquals("abhithaparian@gmail.com", hotelPresenter.confirmationPresenter.sendToEmailTextView.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testDialogDisplayedOnItinsCallFailure() {
        val makeItinResponseObserver = hotelPresenter.makeNewItinResponseObserver()
        makeItinResponseObserver.onError(Throwable())
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfAlertDialog = Shadows.shadowOf(alertDialog)

        val message = alertDialog.findViewById(android.R.id.message) as TextView
        val okButton = alertDialog.findViewById(android.R.id.button1) as Button

        assertEquals(true, alertDialog.isShowing)
        assertEquals("Booking Successful!", shadowOfAlertDialog.title)
        assertEquals("Please check your email for the itinerary.", message.text)
        assertEquals("OK", okButton.text)
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