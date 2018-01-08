package com.expedia.vm.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.AbstractItinDetailsResponse
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.presenter.packages.PackageConfirmationPresenter
import com.expedia.bookings.presenter.packages.PackagePresenter
import com.expedia.bookings.services.ItinTripServices
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.PointOfSaleTestConfiguration
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.UserAccountRefresher
import com.expedia.vm.PackageWebCheckoutViewViewModel
import com.expedia.vm.WebCheckoutViewViewModel
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
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class PackageConfirmationPresenterTest {

    lateinit var packagePresenter: PackagePresenter
    private val userAccountRefresherMock = Mockito.mock(UserAccountRefresher::class.java)
    private var confirmationPresenter: PackageConfirmationPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    var serviceRule = ServicesRule(ItinTripServices::class.java, Schedulers.immediate(), "../lib/mocked/templates")
        @Rule get

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        AbacusTestUtils.unbucketTestAndDisableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
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

        val bookingTripIDSubscriber = TestSubscriber<String>()
        val fectchTripIDSubscriber = TestSubscriber<String>()
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as PackageWebCheckoutViewViewModel).bookedTripIDObservable.subscribe(bookingTripIDSubscriber)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).fetchItinObservable.subscribe(fectchTripIDSubscriber)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).userAccountRefresher = userAccountRefresherMock

        packagePresenter.bundlePresenter.show(packagePresenter.bundlePresenter.webCheckoutView)

        bookingTripIDSubscriber.assertValueCount(0)
        fectchTripIDSubscriber.assertValueCount(0)
        Mockito.verify(userAccountRefresherMock, Mockito.times(0)).forceAccountRefreshForWebView()
        val tripID = "mid_trip_details"

        packagePresenter.bundlePresenter.webCheckoutView.onWebPageStarted(packagePresenter.bundlePresenter.webCheckoutView.webView, "https://www.expedia.com/MultiItemBookingConfirmation" + "?tripid=$tripID", null)
        Mockito.verify(userAccountRefresherMock, Mockito.times(1)).forceAccountRefreshForWebView()
        bookingTripIDSubscriber.assertValueCount(1)
        (packagePresenter.bundlePresenter.webCheckoutView.viewModel as WebCheckoutViewViewModel).onUserAccountRefreshed()
        fectchTripIDSubscriber.assertValueCount(1)
        fectchTripIDSubscriber.assertValue(tripID)
        bookingTripIDSubscriber.assertValue(tripID)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDConfirmationPresenterFromWebView() {
        setupMIDWebCheckout()

        val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("mid_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertTrue(testObserver.valueCount == 1)
        val confirmationPresenter = packagePresenter.confirmationPresenter
        assertEquals("#7316992699395 sent to seokev@gmail.com", confirmationPresenter.itinNumber.text)
        assertEquals("Los Angeles", confirmationPresenter.destination.text)
        assertEquals("Farmer's Daughter", confirmationPresenter.destinationCard.title.text)
        assertEquals("Mar 7 - Mar 8, 1 guest", confirmationPresenter.destinationCard.subTitle.text)
        assertEquals("Flight to (SNA) Orange County", confirmationPresenter.outboundFlightCard.title.text)
        assertEquals("Mar 7 at 09:00:00, 1 traveler", confirmationPresenter.outboundFlightCard.subTitle.text)
        assertEquals("Flight to (SFO) San Francisco", confirmationPresenter.inboundFlightCard.title.text)
        assertEquals("Mar 8 at 11:25:00, 1 traveler", confirmationPresenter.inboundFlightCard.subTitle.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPointsShownFromMidConfirmation() {
        setupMIDWebCheckout()
        UserLoginTestUtil.setupUserAndMockLogin(UserLoginTestUtil.mockUser())
        val expediaRewards = "500"
        packagePresenter.expediaRewards = expediaRewards
        
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        serviceRule.services!!.getTripDetails("mid_trip_details", makeItinResponseObserver)

        assertEquals("$expediaRewards Expedia+ Points", packagePresenter.confirmationPresenter.expediaPoints.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMIDShowBookingSuccessDialogOnItinResponseError() {
        setupMIDWebCheckout()

        val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("error-response", makeItinResponseObserver)

        val alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertTrue(alertDialog.title.contains("Booking Successful!"))
        assertTrue(alertDialog.message.contains("Please check your email for the itinerary."))
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

    fun setupMIDWebCheckout() {
        Ui.getApplication(RuntimeEnvironment.application).defaultPackageComponents()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.package_activity)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
    }
}
