package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.Db
import com.expedia.bookings.packages.presenter.PackageConfirmationPresenter
import com.expedia.bookings.packages.presenter.PackagePresenter
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.PackageTestUtil.Companion.getPackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.bookings.packages.vm.PackageWebCheckoutViewViewModel
import com.expedia.bookings.utils.TuneUtils
import com.expedia.bookings.utils.TuneUtilsTests
import com.expedia.vm.WebCheckoutViewViewModel
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackageConfirmationPresenterTest {

    lateinit var packagePresenter: PackagePresenter
    private val userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)
    private var confirmationPresenter: PackageConfirmationPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.trampoline(), "../lib/mocked/templates")
        @Rule get

    @Before
    fun setup() {
        Db.sharedInstance.clear()
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
        packagePresenter.itinTripServices = serviceRule.services!!
    }

    @Test
    fun testShouldShowCarsCrossSellButton() {
        setupCarsCrossSellButton(shouldShow = true)
        assertShouldShowCarsCrossSellButton(true)
    }

    @Test
    fun testShouldNotShowCarsCrossSellButton() {
        setupCarsCrossSellButton(shouldShow = false)
        assertShouldShowCarsCrossSellButton(false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDWebViewTripIDOnSuccessfulBooking() {
        setupMIDWebCheckout()

        val bookingTripIDSubscriber = TestObserver<String>()
        val fetchTripIDSubscriber = TestObserver<String>()
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fetchTripIDSubscriber)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock

        packagePresenter.bundlePresenter.show(packagePresenter.bundlePresenter.webCheckoutView)

        bookingTripIDSubscriber.assertValueCount(0)
        fetchTripIDSubscriber.assertValueCount(0)
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()
        val tripID = "mid_trip_details"

        packagePresenter.bundlePresenter.webCheckoutView.onWebPageStarted(packagePresenter.bundlePresenter.webCheckoutView.webView, "https://www.expedia.com/MultiItemBookingConfirmation" + "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fetchTripIDSubscriber.assertValueCount(1)
        fetchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDConfirmationPresenterFromWebView() {
        setupMIDWebCheckout()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        Db.setPackageParams(getPackageSearchParams())
        Db.setPackageSelectedOutboundFlight(PackageTestUtil.getPackageSelectedOutboundFlight())
        PackageTestUtil.setDbPackageSelectedHotel()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("mid_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        testObserver.assertValueCount(1)
        val confirmationPresenter = packagePresenter.confirmationPresenter
        assertEquals("#7316992699395 sent to seokev@gmail.com", confirmationPresenter.itinNumber.text)
        assertEquals("Los Angeles", confirmationPresenter.destination.text)
        assertEquals("Farmer's Daughter", confirmationPresenter.destinationCard.title.text)
        assertEquals("Mar 7 - Mar 8, 1 guest", confirmationPresenter.destinationCard.subTitle.text)
        assertEquals("Flight to (SNA) Orange County", confirmationPresenter.outboundFlightCard.title.text)
        assertEquals("Mar 7 at 09:00:00, 1 traveler", confirmationPresenter.outboundFlightCard.subTitle.text)
        assertEquals("Flight to (SFO) San Francisco", confirmationPresenter.inboundFlightCard.title.text)
        assertEquals("Mar 8 at 11:25:00, 1 traveler", confirmationPresenter.inboundFlightCard.subTitle.text)
        assertEquals("4462 Expedia Rewards Points", confirmationPresenter.expediaPoints.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDMultipleFlightsPopulatesConfirmationCorrectly() {
        setupMIDWebCheckout()

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("mid_multiple_flights_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        testObserver.assertValueCount(1)
        val confirmationPresenter = packagePresenter.confirmationPresenter
        assertEquals("Flight to (LGA) New York", confirmationPresenter.outboundFlightCard.title.text)
        assertEquals("Feb 22 at 23:15:00, 1 traveler", confirmationPresenter.outboundFlightCard.subTitle.text)
        assertEquals("Flight to (SFO) San Francisco", confirmationPresenter.inboundFlightCard.title.text)
        assertEquals("Feb 24 at 21:50:00, 1 traveler", confirmationPresenter.inboundFlightCard.subTitle.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTuneTrackedOnItinResponse() {
        setupMIDWebCheckout()
        val tune = TuneUtilsTests.TestTuneTrackingProviderImpl()
        TuneUtils.init(tune)

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("mid_multiple_flights_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        testObserver.assertValueCount(1)
        assertNotNull(tune.trackedEvent)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testShouldNotMaskScreenAfterWebCheckoutToNativeConfirmation() {
        setupMIDWebCheckout()

        val shouldMaskScreenTestObserver = TestObserver.create<Boolean>()
        packagePresenter.bundlePresenter.webCheckoutView.viewModel.showWebViewObservable.subscribe(shouldMaskScreenTestObserver)

        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock
        packagePresenter.bundlePresenter.show(packagePresenter.bundlePresenter.webCheckoutView)
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()
        val tripID = "mid_trip_details"
        packagePresenter.bundlePresenter.webCheckoutView.onWebPageStarted(packagePresenter.bundlePresenter.webCheckoutView.webView, "https://www.expedia.com/MultiItemBookingConfirmation" + "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()

        shouldMaskScreenTestObserver.assertValue(false)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDShowBookingSuccessDialogOnItinResponseError() {
        setupMIDWebCheckout()
        Db.setPackageParams(getPackageSearchParams())
        Db.setPackageSelectedOutboundFlight(PackageTestUtil.getPackageSelectedOutboundFlight())
        PackageTestUtil.setDbPackageSelectedHotel()

        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()

        serviceRule.services!!.getTripDetails("error_trip_response", makeItinResponseObserver)

        assertBookingSuccessDialogDisplayed()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDShowBookingSuccessDialogOnItinResponseContainingErrors() {
        setupMIDWebCheckout()
        Db.setPackageParams(getPackageSearchParams())
        Db.setPackageSelectedOutboundFlight(PackageTestUtil.getPackageSelectedOutboundFlight())
        PackageTestUtil.setDbPackageSelectedHotel()

        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()

        serviceRule.services!!.getTripDetails("error_trip_details_response", makeItinResponseObserver)

        assertBookingSuccessDialogDisplayed()
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDDontShowPointsOnConfirmation() {
        setupMIDWebCheckout()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())

        val testObserver: TestObserver<AbstractItinDetailsResponse> = TestObserver.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("mid_multiple_flights_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        testObserver.assertValueCount(1)
        val confirmationPresenter = packagePresenter.confirmationPresenter
        assertTrue(confirmationPresenter.expediaPoints.text.isEmpty())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTrackConfirmationViewItinClick() {
        val mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        packagePresenter.confirmationPresenter.viewItinButton.performClick()

        val controlEvar = mapOf(28 to "App.CKO.Confirm.ViewItinerary")
        OmnitureTestUtils.assertLinkTracked("Confirmation Trip Action", "App.CKO.Confirm.ViewItinerary", OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    fun assertShouldShowCarsCrossSellButton(show: Boolean) {
        if (show) assertTrue(confirmationPresenter.addCarLayout.visibility == View.VISIBLE)
        else assertTrue(confirmationPresenter.addCarLayout.visibility == View.GONE)
    }

    fun setupCarsCrossSellButton(shouldShow: Boolean = true) {
        PointOfSaleTestConfiguration.configurePointOfSale(activity, if (shouldShow) "MockSharedData/pos_with_car_cross_sell.json"
        else "MockSharedData/pos_with_no_car_cross_sell.json", false)
        confirmationPresenter = LayoutInflater.from(activity).inflate(com.expedia.bookings.R.layout.package_confirmation_stub, null) as PackageConfirmationPresenter
    }

    private fun assertBookingSuccessDialogDisplayed() {
        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertTrue(alertDialog.title.contains("Booking Successful!"))
        assertTrue(alertDialog.message.contains("Please check your email for the itinerary."))
    }

    fun setupMIDWebCheckout() {
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
        packagePresenter.showBundleOverView()
    }
}
