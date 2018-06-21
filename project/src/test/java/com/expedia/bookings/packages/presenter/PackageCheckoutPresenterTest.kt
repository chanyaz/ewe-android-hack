package com.expedia.bookings.packages.presenter

import android.app.Activity
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.enums.TwoScreenOverviewState
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.robolectric.PackageTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.util.Optional
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCheckoutPresenterTest {
    private lateinit var presenter: PackageCheckoutPresenter
    private lateinit var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    val context: Context = RuntimeEnvironment.application

    private val PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE = "App.Package.Checkout.SlideToPurchase"

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        presenter = LayoutInflater.from(activity).inflate(R.layout.test_package_checkout_presenter,
                null) as PackageCheckoutPresenter
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testTrackCreateTripPriceChange() {
        presenter.trackCreateTripPriceChange(10)
        OmnitureTestUtils.assertLinkTracked("Rate Details View", "App.Package.RD.PriceChange", mockAnalyticsProvider)
    }

    @Test
    fun testTrackCheckoutPriceChange() {
        presenter.trackCheckoutPriceChange(10)
        OmnitureTestUtils.assertLinkTracked("Package Checkout", "App.Package.CKO.PriceChange", mockAnalyticsProvider)
    }

    @Test
    fun testHandleCheckoutPriceChange() {
        presenter.handleCheckoutPriceChange(FlightCreateTripResponse())
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun testSetupCreateTripViewModel() {
        val testSubscriber = TestObserver<Optional<TripResponse>>()
        val createTripResponse = FlightCreateTripResponse()
        presenter.getCreateTripViewModel().createTripResponseObservable.subscribe(testSubscriber)
        presenter.getCheckoutViewModel().checkoutPriceChangeObservable.onNext(createTripResponse)
        presenter.setupCreateTripViewModel(presenter.getCreateTripViewModel())
        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(createTripResponse, testSubscriber.values()[0].value)
    }

    @Test
    fun testOnUserLoggedIn() {
        val testSubscriber = TestObserver<TwoScreenOverviewState>()
        presenter.getCheckoutViewModel().bottomCheckoutContainerStateObservable.subscribe(testSubscriber)
        Db.setPackageParams(PackageTestUtil.getPackageSearchParams())
        presenter.onUserLoggedIn(Events.LoggedInSuccessful())
        testSubscriber.awaitTerminalEvent(200, TimeUnit.MILLISECONDS)
        testSubscriber.assertValueCount(1)
        assertEquals(TwoScreenOverviewState.CHECKOUT, testSubscriber.values()[0])
    }

    @Test
    fun testTrackShowSlideToPurchase() {
        presenter.trackShowSlideToPurchase()
        val controlEvar = mapOf(18 to PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE,
                37 to "Unknown")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testTrackShowSlideToPurchaseWithCardFeeFlexStatus() {
        val cardFeeFlexStatus = "flex"
        presenter.getCheckoutViewModel().cardFeeFlexStatus.onNext(cardFeeFlexStatus)
        presenter.trackShowSlideToPurchase()
        val controlEvar = mapOf(18 to PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE,
                37 to "Unknown",
                44 to cardFeeFlexStatus)
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testTrackShowSlideToPurchaseWithVisaPaymentType() {
        val billingInfo = BillingInfo()
        val storedCreditCard = StoredCreditCard()
        storedCreditCard.type = PaymentType.CARD_VISA
        billingInfo.storedCard = storedCreditCard
        Db.sharedInstance.setBillingInfo(billingInfo)
        presenter.trackShowSlideToPurchase()
        val controlEvar = mapOf(18 to PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE,
                37 to "Visa")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }

    @Test
    fun testTrackShowSlideToPurchaseWithAmexPaymentType() {
        val billingInfo = BillingInfo()
        billingInfo.number = "340000000000000"
        Db.sharedInstance.setBillingInfo(billingInfo)
        presenter.trackShowSlideToPurchase()
        val controlEvar = mapOf(18 to PACKAGES_CHECKOUT_SLIDE_TO_PURCHASE,
                37 to "AmericanExpress")
        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withEvars(controlEvar), mockAnalyticsProvider)
    }
}
