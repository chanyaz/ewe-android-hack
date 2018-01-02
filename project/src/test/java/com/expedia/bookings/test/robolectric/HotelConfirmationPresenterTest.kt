package com.expedia.bookings.presenter.hotel

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.HotelPresenterTestUtil.Companion.getDummyHotelSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import rx.subjects.TestSubject
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class HotelConfirmationPresenterTest {
    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.immediate(), "../lib/mocked/templates")
        @Rule get


    lateinit var hotelPresenter: HotelPresenter
    lateinit var activity: Activity

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Hotels_Default)
        Ui.getApplication(activity).defaultHotelComponents()
        setPOSWithWebCheckoutEnabled(true)
        hotelPresenter = LayoutInflater.from(activity).inflate(R.layout.activity_hotel, null) as HotelPresenter
        hotelPresenter.hotelSearchParams = getDummyHotelSearchParams(activity)
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        hotelPresenter.show(hotelPresenter.detailPresenter)
        selectHotelRoom()
    }

    @Test
    fun testConfirmationScreenPopulatedByItinsCall() {
        val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
        val makeItinResponseObserver = hotelPresenter.makeNewItinResponseObserver()
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("web_view_hotel_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)
        assertEquals("CitiGarden Hotel", hotelPresenter.confirmationPresenter.hotelNameTextView.text)
        assertEquals("Feb 10 – 11, 2017", hotelPresenter.confirmationPresenter.checkInOutDateTextView.text)
        assertEquals("245 S Airport Blvd", hotelPresenter.confirmationPresenter.addressL1TextView.text)
        assertEquals("South San Francisco, CA", hotelPresenter.confirmationPresenter.addressL2TextView.text)
        assertEquals("Itinerary #7241053124635", hotelPresenter.confirmationPresenter.itinNumberTextView.text)
        assertEquals("abhithaparian@gmail.com", hotelPresenter.confirmationPresenter.sendToEmailTextView.text)
    }

    @Test
    fun testDialogDisplayedOnItinsCallFailure() {
        val makeItinResponseObserver = hotelPresenter.makeNewItinResponseObserver()
        makeItinResponseObserver.onError(Throwable())
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val shadowOfAlertDialog = Shadows.shadowOf(alertDialog)

        val message = alertDialog.findViewById<View>(android.R.id.message) as TextView
        val okButton = alertDialog.findViewById<View>(android.R.id.button1) as Button

        assertEquals(true, alertDialog.isShowing)
        assertEquals("Booking Successful!", shadowOfAlertDialog.title)
        assertEquals("Please check your email for the itinerary.", message.text)
        assertEquals("OK", okButton.text)
    }

    @Test
    fun testHotelConfirmationObservable() {
        var confirmationDetailsAndUISet = false
        val testDetailsSetSubscriber = TestSubscriber<Boolean>()
        val testUISetSubscriber = TestSubscriber<Boolean>()
        setDummyCheckoutResponse()
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationDetailsSetObservable.subscribe(testDetailsSetSubscriber)
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationUISetObservable.subscribe(testUISetSubscriber)

        TestSubject.combineLatest(hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationDetailsSetObservable, hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationUISetObservable, {
            detailsSet, UISet ->
                if (detailsSet && UISet) {
                    confirmationDetailsAndUISet = true
                }
        }).subscribe()

        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationDetailsSetObservable.onNext(true)
        assertTrue(!confirmationDetailsAndUISet)

        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelConfirmationUISetObservable.onNext(true)
        assertTrue(confirmationDetailsAndUISet)
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

    private fun getDummyHotelCheckoutResponse(): HotelCheckoutResponse {
        val hotelCheckoutResponse = HotelCheckoutResponse()
        hotelCheckoutResponse.checkoutResponse = HotelCheckoutResponse.CheckoutResponse()
        hotelCheckoutResponse.checkoutResponse.bookingResponse = HotelCheckoutResponse.BookingResponse()
        hotelCheckoutResponse.checkoutResponse.productResponse = HotelCheckoutResponse.ProductResponse()
        setDummyHotelProductResponse(hotelCheckoutResponse.checkoutResponse.productResponse)
        hotelCheckoutResponse.currencyCode = "US"
        hotelCheckoutResponse.orderId = "123"
        hotelCheckoutResponse.totalCharges = "4.98"
        return hotelCheckoutResponse
    }

    private fun setDummyHotelProductResponse(productResponse: HotelCheckoutResponse.ProductResponse) {
        productResponse.hotelCity = "Los Angelos"
        productResponse.hotelStateProvince = "CA"
        productResponse.hotelCountry = "USA"
        productResponse.checkInDate = "2015-02-22"
        productResponse.checkOutDate = "2019-02-22"
    }

    private fun setDummyCheckoutResponse() {
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.hotelCheckoutResponseObservable.onNext(getDummyHotelCheckoutResponse())
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.percentagePaidWithPointsObservable.onNext(44)
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.totalAppliedRewardCurrencyObservable.onNext("0")
        hotelPresenter.confirmationPresenter.hotelConfirmationViewModel.couponCodeObservable.onNext("4")
    }
}
