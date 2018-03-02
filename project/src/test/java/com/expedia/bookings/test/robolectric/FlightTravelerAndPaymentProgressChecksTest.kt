package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripBucketItemFlightV2
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.BaseTwoScreenOverviewPresenter
import com.expedia.bookings.presenter.flight.FlightOverviewPresenter
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightCreateTripResponse
import com.expedia.bookings.test.robolectric.FlightTestUtil.Companion.getFlightSearchParams
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.PaymentTestUtils.Companion.setUpCompletePayment
import com.expedia.bookings.utils.TravelerTestUtils.Companion.completeTraveler
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightTravelerAndPaymentProgressChecksTest {

    private var activity: FragmentActivity by Delegates.notNull()
    lateinit var overviewPresenter: FlightOverviewPresenter
    lateinit var travelerValidator: TravelerValidator

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultFlightComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        setupOverviewPresenter()
        overviewPresenter.show(BaseTwoScreenOverviewPresenter.BundleDefault())
        overviewPresenter.checkoutButton.performClick()
        overviewPresenter.show(overviewPresenter.getCheckoutPresenter())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutButtonClickTriggersIncompleteTravelerStatus() {
        val travelerStatusTestObserver = TestObserver<TravelerCheckoutStatus>()
        overviewPresenter.getCheckoutPresenter().travelerSummaryCard.viewModel.travelerStatusObserver.subscribe(travelerStatusTestObserver)

        overviewPresenter.checkoutButton.performClick()

        travelerStatusTestObserver.assertValueCount(2)
        travelerStatusTestObserver.assertValues(TravelerCheckoutStatus.CLEAN, TravelerCheckoutStatus.DIRTY)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutButtonClickTriggersIncompletePaymentWidgetStatus() {
        val paymentWidgetStatusObserver = TestObserver<ContactDetailsCompletenessStatus>()
        overviewPresenter.getCheckoutPresenter().paymentWidget.viewmodel.statusUpdate.subscribe(paymentWidgetStatusObserver)

        overviewPresenter.checkoutButton.performClick()

        paymentWidgetStatusObserver.assertValueCount(1)
        paymentWidgetStatusObserver.assertValue(ContactDetailsCompletenessStatus.INCOMPLETE)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutButtonClickDoesntTriggerIncompleteTravelerStatus() {
        completeTraveler(Db.sharedInstance.travelers[0])
        completeTraveler(Db.sharedInstance.travelers[1])

        val travelerStatusTestObserver = TestObserver<TravelerCheckoutStatus>()
        overviewPresenter.getCheckoutPresenter().travelerSummaryCard.viewModel.travelerStatusObserver.subscribe(travelerStatusTestObserver)

        overviewPresenter.checkoutButton.performClick()

        travelerStatusTestObserver.assertValueCount(1)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPackageCheckoutButtonClickDoesntTriggerIncompletePaymentWidgetStatus() {
        setUpCompletePayment(activity)

        val paymentWidgetStatusObserver = TestObserver<ContactDetailsCompletenessStatus>()
        overviewPresenter.getCheckoutPresenter().paymentWidget.viewmodel.statusUpdate.subscribe(paymentWidgetStatusObserver)

        overviewPresenter.checkoutButton.performClick()

        paymentWidgetStatusObserver.assertValueCount(0)
    }

    private fun setupOverviewPresenter() {
        Ui.getApplication(activity).defaultTravelerComponent()
        Ui.getApplication(activity).defaultFlightComponents()
        travelerValidator = Ui.getApplication(RuntimeEnvironment.application).travelerComponent().travelerValidator()
        setupDb()
        travelerValidator.updateForNewSearch(Db.getFlightSearchParams())
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_overview_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java, styledIntent).create().visible().get()
        overviewPresenter = LayoutInflater.from(activity).inflate(R.layout.flight_overview_stub, null) as FlightOverviewPresenter
        overviewPresenter.getCheckoutPresenter().getPaymentWidgetViewModel().lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
    }

    private fun setupDb() {
        Db.setFlightSearchParams(getFlightSearchParams(true))
        Db.sharedInstance.travelers = arrayListOf(Traveler(), Traveler())
        val flightTripItem = TripBucketItemFlightV2(getFlightCreateTripResponse())
        Db.getTripBucket().add(flightTripItem)
    }
}
