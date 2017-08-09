package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.traveler.FrequentFlyerAdapter
import com.expedia.bookings.widget.traveler.FrequentFlyerViewHolder
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class))
class FlightCheckoutPresenterTest {


    private var checkout: FlightCheckoutPresenter by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()

    @Before fun before() {
        Db.clear()
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        Db.setTravelers(listOf(getTravelerWithFrequentFlyerMemberships()))
    }

    @Test
    fun testPassportRequired() {
        setupCheckout()
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getPassportRequiredCreateTripResponse(true))
        passportRequiredSubscriber.assertValues(false, true)
    }

    @Test
    fun testPassportNotRequired() {
        setupCheckout()
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getPassportRequiredCreateTripResponse(false))
        passportRequiredSubscriber.assertValues(false, false)
    }

    @Test
    fun materialPaymentWidget() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        setupCheckout()

        assertNotNull(checkout.paymentWidget)
    }

    @Test
    fun materialPaymentWidgetColorsBackground() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        setupCheckout()
        checkout.paymentWidget.showPaymentForm(false)
        assertEquals(checkout.scrollView.background, ContextCompat.getDrawable(activity.applicationContext, R.color.white))
        checkout.paymentWidget.back()
        assertEquals(checkout.scrollView.background, ContextCompat.getDrawable(activity.applicationContext, R.color.gray1))
    }

    @Test
    fun testCreateTripDialogShows() {
        setupCheckout()
        checkout.flightCreateTripViewModel.showCreateTripDialogObservable.onNext(true)
        val shadowCreateTripDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(true, shadowCreateTripDialog.isShowing)
    }

    @Test
    fun testShowCreateTripDialogDoesNotCrashOnFalseValue() {
        setupCheckout()
        checkout.flightCreateTripViewModel.showCreateTripDialogObservable.onNext(true)
        val shadowCreateTripDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertTrue(shadowCreateTripDialog.isShowing)

        shadowCreateTripDialog.dismiss()
        assertFalse(shadowCreateTripDialog.isShowing)

        checkout.flightCreateTripViewModel.showCreateTripDialogObservable.onNext(false)
        val newShadowCreateTripDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertEquals(newShadowCreateTripDialog, shadowCreateTripDialog)
    }

    @Test
    fun testEnrolledFrequentFlyerProgramsPopulatesCardView() {
        setupCheckout(true)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(getPassportRequiredCreateTripResponse(false))
        checkout.travelersPresenter.showSelectOrEntryState()

        val frequentFlyerPlans = (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).frequentFlyerPlans
        assertNotNull(frequentFlyerPlans)
        val enrolledPlan = frequentFlyerPlans?.enrolledFrequentFlyerPlans!!.first()
        assertNotNull(enrolledPlan)

        assertEquals("AA", enrolledPlan.airlineCode)
        assertEquals("123", enrolledPlan.membershipNumber)
        assertEquals("American Airlines", enrolledPlan.frequentFlyerPlanName)

        val entryWidget = (checkout.travelersPresenter.travelerEntryWidget as FlightTravelerEntryWidget)
        entryWidget.frequentFlyerButton?.performClick()
        val adapter = (entryWidget.frequentFlyerRecycler?.adapter as FrequentFlyerAdapter)

        assertEquals(frequentFlyerPlans.enrolledFrequentFlyerPlans.first(), adapter.frequentFlyerPlans.enrolledFrequentFlyerPlans.first())

        val frequentFlyerView = adapter.onCreateViewHolder(entryWidget.frequentFlyerRecycler!!, 0) as FrequentFlyerViewHolder
        adapter.onBindViewHolder(frequentFlyerView, 0)

        assertEquals("American Airlines", frequentFlyerView.frequentFlyerNameTitle.text.toString())
        assertEquals("American Airlines", frequentFlyerView.frequentFlyerProgram.text.toString())
        assertEquals("123" ,frequentFlyerView.frequentFlyerNumberInput.text.toString())
    }

    private fun setupCheckout(isFrequentFlyerEnabled: Boolean = false) {
        if (isFrequentFlyerEnabled) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber, AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        }
        SettingUtils.save(RuntimeEnvironment.application, R.string.preference_enable_flights_frequent_flyer_number, isFrequentFlyerEnabled)
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_checkout_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        checkout = activity.findViewById(R.id.flight_checkout_presenter) as FlightCheckoutPresenter
    }

    private fun getPassportRequiredCreateTripResponse(passportRequired: Boolean): FlightCreateTripResponse? {
        val flightCreateTripResponse = FlightCreateTripResponse()
        val flightTripDetails = FlightTripDetails()

        val flightOffer = FlightTripDetails.FlightOffer()
        flightOffer.isPassportNeeded = passportRequired
        flightOffer.pricePerPassengerCategory = ArrayList<FlightTripDetails.PricePerPassengerCategory>()
        flightOffer.totalPrice = Money(9, "USD")
        flightTripDetails.offer = flightOffer
        flightTripDetails.legs = getFlightLegs()

        flightCreateTripResponse.details = flightTripDetails
        flightCreateTripResponse.newTrip = TripDetails("", "", "")
        flightCreateTripResponse.tealeafTransactionId = ""

        flightCreateTripResponse.frequentFlyerPlans = FlightCreateTripResponse.FrequentFlyerPlans()
        flightCreateTripResponse.frequentFlyerPlans.enrolledFrequentFlyerPlans = listOf(getFrequentFlyerPlans())
        flightCreateTripResponse.frequentFlyerPlans.allFrequentFlyerPlans = listOf(getFrequentFlyerPlans())

        return flightCreateTripResponse
    }

    private fun getFrequentFlyerPlans() : FrequentFlyerPlansTripResponse {
        val enrolledPlan = FrequentFlyerPlansTripResponse()
        enrolledPlan.airlineCode = "AA"
        enrolledPlan.frequentFlyerPlanName = "American Airlines"
        enrolledPlan.membershipNumber = "123"
        return enrolledPlan
    }

    private fun getFlightLegs() : List<FlightLeg> {
        val leg = FlightLeg()
        val segment = FlightLeg.FlightSegment()
        segment.airlineName = "American Airlines"
        segment.airlineCode = "AA"
        leg.segments = listOf(segment)
        return listOf(leg)
    }

    private fun  getTravelerWithFrequentFlyerMemberships(): Traveler? {
        val traveler = Traveler()
        traveler.addFrequentFlyerMembership(getTravelerFrequentFlyerMembership())
        return traveler
    }

    private fun getTravelerFrequentFlyerMembership() : TravelerFrequentFlyerMembership{
        val membership = TravelerFrequentFlyerMembership()
        membership.airlineCode = "AA"
        membership.membershipNumber = "123"
        membership.planCode = "123"
        return membership
    }

}
