package com.expedia.bookings.widget.packages

import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripDetails
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.presenter.flight.FlightCheckoutPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.TravelerSelectItem
import com.expedia.util.Optional
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getPassportRequiredCreateTripResponse(true)))
        passportRequiredSubscriber.assertValues(false, true)
    }

    @Test
    fun testPassportRequiredObservables() {
        setupCheckout()
        Db.setTravelers(listOf(Traveler(), Traveler()))
        checkout.travelersPresenter.resetTravelers()
        val travelerPickerWidget = checkout.travelersPresenter.travelerPickerWidget
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        val travelerPickerPassportSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        (travelerPickerWidget.viewModel).passportRequired.subscribe(travelerPickerPassportSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getPassportRequiredCreateTripResponse(true)))

        passportRequiredSubscriber.assertValues(false, true)
        travelerPickerPassportSubscriber.assertValues(false, true)

        travelerPickerWidget.show()
        ((travelerPickerWidget.findViewById<View>(R.id.main_traveler_container) as LinearLayout)
                .getChildAt(0) as TravelerSelectItem)
                .performClick()
        val selectedTraveler = travelerPickerWidget.viewModel.selectedTravelerSubject.value

        assertTrue(selectedTraveler.passportRequired.value)
        assertFalse(selectedTraveler.travelerValidator.isValidForFlightBooking(selectedTraveler.getTraveler(), 0, selectedTraveler.passportRequired.value))
        assertTrue((checkout.travelersPresenter.travelerEntryWidget.viewModel as FlightTravelerEntryWidgetViewModel).showPassportCountryObservable.value)
    }

    @Test
    fun testPassportNotRequired() {
        setupCheckout()
        val passportRequiredSubscriber = TestSubscriber<Boolean>()
        (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).passportRequired.subscribe(passportRequiredSubscriber)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getPassportRequiredCreateTripResponse(false)))
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
    fun testEnrolledFrequentFlyerPrograms() {
        setupCheckout(true)
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(getPassportRequiredCreateTripResponse(false)))
        checkout.travelersPresenter.showSelectOrEntryState()
        val frequentFlyerPlans = (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).frequentFlyerPlans

        assertEquals(1, frequentFlyerPlans?.enrolledFrequentFlyerPlans!!.size)
        val firstEnrolledPlan = frequentFlyerPlans.enrolledFrequentFlyerPlans!!.first()
        assertEquals("AA", firstEnrolledPlan.airlineCode)
        assertEquals("American Airlines", firstEnrolledPlan.frequentFlyerPlanName)
        assertEquals("123", firstEnrolledPlan.membershipNumber)
    }

    @Test
    fun testAllFrequentFlyerPrograms() {
        setupCheckout(true)
        val tripResponseWithoutEnrolledFFPlans = getPassportRequiredCreateTripResponse(false)
        tripResponseWithoutEnrolledFFPlans?.frequentFlyerPlans?.enrolledFrequentFlyerPlans = null
        checkout.flightCreateTripViewModel.createTripResponseObservable.onNext(Optional(tripResponseWithoutEnrolledFFPlans))
        checkout.travelersPresenter.showSelectOrEntryState()

        val frequentFlyerPlans = (checkout.travelersPresenter.viewModel as FlightTravelersViewModel).frequentFlyerPlans

        assertEquals(3, frequentFlyerPlans?.allFrequentFlyerPlans!!.size)
        val firstPlan = frequentFlyerPlans.allFrequentFlyerPlans!!.first()
        assertEquals("AA", firstPlan.airlineCode)
        assertEquals("American Airlines", firstPlan.frequentFlyerPlanName)
        assertEquals(null, firstPlan.membershipNumber)
    }

    private fun setupCheckout(isFrequentFlyerEnabled: Boolean = false) {
        if (isFrequentFlyerEnabled) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber, AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        }
        SettingUtils.save(RuntimeEnvironment.application, R.string.preference_enable_flights_frequent_flyer_number, isFrequentFlyerEnabled)
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.flight_checkout_test)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
        checkout = activity.findViewById<View>(R.id.flight_checkout_presenter) as FlightCheckoutPresenter
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
        flightCreateTripResponse.frequentFlyerPlans.enrolledFrequentFlyerPlans = listOf(getFrequentFlyerPlans(true))
        flightCreateTripResponse.frequentFlyerPlans.allFrequentFlyerPlans = listOf(getFrequentFlyerPlans(), getFrequentFlyerPlans(), getFrequentFlyerPlans())

        return flightCreateTripResponse
    }

    private fun getFrequentFlyerPlans(hasMembership: Boolean = false) : FrequentFlyerPlansTripResponse {
        val enrolledPlan = FrequentFlyerPlansTripResponse()
        enrolledPlan.airlineCode = "AA"
        enrolledPlan.frequentFlyerPlanName = "American Airlines"
        enrolledPlan.membershipNumber = if (hasMembership) "123" else null
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
