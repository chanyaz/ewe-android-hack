package com.expedia.bookings.test.robolectric

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.ItinDetailsResponse
import com.expedia.bookings.presenter.lx.LXPresenter
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.tracking.OmnitureTracking.trackAppLXConfirmationFromTripsResponse
import com.expedia.bookings.utils.TuneUtils
import com.expedia.bookings.utils.TuneUtilsTests
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.LXConfirmationWidget
import com.google.gson.Gson
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.lx_base_layout.lx_base_presenter
import kotlinx.android.synthetic.main.lx_base_layout.confirmation
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.confirmation_image_view
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.title
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.location
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.tickets
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.date
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.email_text
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.confirmation_text
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.itin_number
import kotlinx.android.synthetic.main.widget_lx_confirmation.view.reservation_confirmation_text
import org.joda.time.LocalDate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog
import java.io.BufferedReader
import java.io.FileReader
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class LXConfirmationWidgetTest {

    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.trampoline(), "../lib/mocked/templates")
        @Rule get

    lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var confirmationWidget: LXConfirmationWidget
    lateinit var activity: Activity
    lateinit var presenter: LXPresenter

    @Before
    fun before() {
        val telephonyManager = RuntimeEnvironment.application.getSystemService(
                Context.TELEPHONY_SERVICE) as TelephonyManager
        val shadowTelephonyManager = shadowOf(telephonyManager)
        shadowTelephonyManager.networkOperatorName = "Test Operator"

        activity = Robolectric.buildActivity(Activity::class.java).create().start().resume().visible().get()
        activity.setTheme(R.style.V2_Theme_LX)
        Ui.getApplication(activity).defaultLXComponents()
        activity.setContentView(R.layout.lx_base_layout)
        presenter = activity.lx_base_presenter
        confirmationWidget = activity.confirmation
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY, MultiBrand.AIRASIAGO, MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS])
    fun testConfirmationWidgetViews() {
        mockConfirmationLXState(doCheckout = true)

        val confirmationImage = confirmationWidget.confirmation_image_view
        val title = confirmationWidget.title
        val location = confirmationWidget.location
        val tickets = confirmationWidget.tickets
        val date = confirmationWidget.date
        val email = confirmationWidget.email_text
        val confirmation = confirmationWidget.confirmation_text
        val itinNumber = confirmationWidget.itin_number
        val reservationConfirmation = confirmationWidget.reservation_confirmation_text

        val expectedConfirmationText = activity.resources.getString(R.string.lx_successful_checkout_email_label)
        val reservationConfirmationText = activity.resources.getString(
                R.string.lx_successful_checkout_reservation_label)

        assertNotNull(confirmationImage)
        assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours", title.text)
        assertEquals("New York, United States", location.text)
        assertEquals("3 Adults, 1 Child", tickets.text)
        assertEquals("Tue, Feb 24", date.text)
        assertEquals("coolguy@expedia.com", email.text)
        assertEquals("Itinerary #7666328719", itinNumber.text)
        assertEquals(expectedConfirmationText, confirmation.text)
        assertEquals(reservationConfirmationText, reservationConfirmation.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPopulateConfirmationViewsFromTripsCallWithEmail() {
        mockConfirmationLXState(doCheckout = false)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("lx_trip_details_with_email", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertEquals("Fri, Apr 13", confirmationWidget.date.text)
        assertEquals("tripscall@successful.com", confirmationWidget.email_text.text)
        assertEquals("Your tickets have been sent to", confirmationWidget.confirmation_text.text)
        assertEquals("Your reservation is confirmed!", confirmationWidget.reservation_confirmation_text.text)
        assertEquals("Itinerary #7344467767254", confirmationWidget.itin_number.text)

        assertNotNull(confirmationWidget.confirmation_image_view)
        assertEquals("New York Pass: Visit up to 80 Attractions, Museums & Tours", confirmationWidget.title.text)
        assertEquals("3 Adults, 1 Child", confirmationWidget.tickets.text)
        assertEquals("New York, United States", confirmationWidget.location.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testPopulateConfirmationViewsFromTripsCallWithoutEmail() {
        mockConfirmationLXState(doCheckout = false)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("lx_trip_details_without_email", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertEquals("The tickets have been sent to", confirmationWidget.confirmation_text.text)
        assertEquals("your email.", confirmationWidget.email_text.text)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShowBookingCompleteDialogAfterFailedTripsCall() {
        mockConfirmationLXState(doCheckout = false)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("failed trips call", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 2, TimeUnit.SECONDS)

        assertBookingSuccessDialogDisplayed()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testShowBookingCompleteDialogFromTripResponseWithErrors() {
        mockConfirmationLXState(doCheckout = false)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("error_trip_response", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 2, TimeUnit.SECONDS)

        assertBookingSuccessDialogDisplayed()
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testWebCKOToConfirmationOmnitureTracking() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        mockConfirmationLXState(doCheckout = false)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("lx_trip_details_without_email", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        val appState = "App.LX.Checkout.Confirmation"
        val expectedEvars = mapOf(2 to "D=c2", 30 to "LX:20150224-20150408:N", 18 to "App.LX.Checkout.Confirmation")
        val expectedProps = mapOf(2 to "local expert", 5 to "2015-02-24", 72 to "8104062917948")
        val expectedProducts = "LX;Merchant LX:183615;4;1795.00"
        val expectedEvents = "purchase"
        assertWebCKOToConfirmationTracking(appState, expectedEvars, expectedProps, expectedProducts, expectedEvents)
    }

    @Test
    @RunForBrands(brands = [MultiBrand.EXPEDIA])
    fun testWebCKOToConfirmationOmnitureTrackingForProduct() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val br = BufferedReader(FileReader("../lib/mocked/templates/api/trips/lx_trip_details_without_email.json"))
        val activityItinResponse = Gson().fromJson(br, ItinDetailsResponse::class.java)
        activityItinResponse.responseData!!.totalTripPrice!!.total = "1795,00"
        trackAppLXConfirmationFromTripsResponse(activityItinResponse, true, "1", 1, LocalDate.now(), LocalDate.now().plusDays(2))
        val expectedProducts = "LX;Merchant LX:1;1;1795.00"

        OmnitureTestUtils.assertStateTracked("App.LX-GT.Checkout.Confirmation", OmnitureMatchers.withProductsString(expectedProducts), mockAnalyticsProvider)
    }

    @Test
    fun testTuneTrackedOnNativeConfirmationFromWebview() {
        mockConfirmationLXState(doCheckout = false)
        val tune = TuneUtilsTests.TestTuneTrackingProviderImpl()
        TuneUtils.init(tune)
        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = presenter.makeNewItinResponseObserver()
        confirmationWidget.viewModel.itinDetailsResponseObservable.subscribe(testObserver)
        serviceRule.services!!.getTripDetails("lx_trip_details_without_email", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertNotNull(tune.trackedEvent)
    }

    private fun mockConfirmationLXState(doCheckout: Boolean) {
        LXStateTestUtil.searchParamsState()
        LXStateTestUtil.selectActivityState()
        LXStateTestUtil.offerSelected()
        if (doCheckout) LXStateTestUtil.checkoutSuccessState()
    }

    private fun assertBookingSuccessDialogDisplayed() {
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()

        val shadowOfAlertDialog = Shadows.shadowOf(alertDialog)
        val message = alertDialog.findViewById<View>(android.R.id.message) as TextView
        val okButton = alertDialog.findViewById<View>(android.R.id.button1) as Button

        assertEquals(true, alertDialog.isShowing)
        assertEquals("Booking Successful!", shadowOfAlertDialog.title)
        assertEquals("Please check your email for the itinerary.", message.text)
        assertEquals("OK", okButton.text)
    }

    private fun assertWebCKOToConfirmationTracking(appState: String, expectedEvars: Map<Int, String>, expectedProps: Map<Int, String>, expectedProducts: String, expectedEvents: String) {
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withProps(expectedProps), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withProductsString(expectedProducts), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked(appState, OmnitureMatchers.withEventsString(expectedEvents), mockAnalyticsProvider)
    }
}
