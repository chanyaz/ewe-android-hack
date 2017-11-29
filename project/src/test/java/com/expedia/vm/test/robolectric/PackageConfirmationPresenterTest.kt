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
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageConfirmationPresenterTest {

    lateinit var packagePresenter: PackagePresenter
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
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
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
    fun testConfirmationPresenterFromWebView() {
        setupMIDWebCheckout()

        val testObserver: TestSubscriber<AbstractItinDetailsResponse> = TestSubscriber.create()
        val makeItinResponseObserver = packagePresenter.makeNewItinResponseObserver()
        packagePresenter.confirmationPresenter.viewModel.itinDetailsResponseObservable.subscribe(testObserver)

        serviceRule.services!!.getTripDetails("package_trip_details", makeItinResponseObserver)
        testObserver.awaitValueCount(1, 10, TimeUnit.SECONDS)

        assertTrue(testObserver.valueCount == 1)
        val confirmationPresenter = packagePresenter.confirmationPresenter
        assertEquals("#7313989476663 sent to test@test.com", confirmationPresenter.itinNumber.text)
        assertEquals("Barcelona", confirmationPresenter.destination.text)
        assertEquals("Hotel Barcelona Universal", confirmationPresenter.destinationCard.title.text)
        assertEquals("Jan 25 - Feb 2, 1 guest", confirmationPresenter.destinationCard.subTitle.text)
        assertEquals("Flight to (BCN) Barcelona", confirmationPresenter.outboundFlightCard.title.text)
        assertEquals("Jan 24 at 18:00:00, 1", confirmationPresenter.outboundFlightCard.subTitle.text)
        assertEquals("Flight to (SFO) San Francisco", confirmationPresenter.inboundFlightCard.title.text)
        assertEquals("Feb 2 at 16:20:00, 1", confirmationPresenter.inboundFlightCard.subTitle.text)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testShowBookingSuccessDialogOnItinResponseError() {
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
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        AbacusTestUtils.bucketTestAndEnableFeature(activity, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        packagePresenter = LayoutInflater.from(activity).inflate(R.layout.package_activity, null) as PackagePresenter
    }
}
