package com.expedia.bookings.test.robolectric

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightLeg.FlightSegment
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.FrequentFlyerDialogAdapter
import com.expedia.bookings.widget.traveler.FrequentFlyerAdapter
import com.expedia.bookings.widget.traveler.FrequentFlyerViewHolder
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import rx.subjects.BehaviorSubject
import java.util.ArrayList
import kotlin.test.assertEquals
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightTravelerFrequentFlyerWidgetTest {
    private lateinit var widget: FlightTravelerEntryWidget
    private val context = RuntimeEnvironment.application

    val testRegionName = "Chicago"
    val testAirportCode = "ORD"
    val traveler = Traveler()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()


        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
        SettingUtils.save(activity, R.string.preference_enable_flights_frequent_flyer_number, true)
        widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_entry_widget, null) as FlightTravelerEntryWidget

        Db.getTravelers().add(traveler)
        widget.viewModel = FlightTravelerEntryWidgetViewModel(activity, 0, BehaviorSubject.create(false), TravelerCheckoutStatus.CLEAN)
    }

    @Test
    fun testFFNCardVisibility() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        widget.frequentFlyerButton?.performClick()
        assertEquals(View.VISIBLE, widget.frequentFlyerRecycler?.visibility)
    }

    @Test
    fun testCorrectFFNCardsDuplicateAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        assertEquals(2, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNullAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(null)))
        assertEquals(0, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNoAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(0)))
        assertEquals(0, widget.frequentFlyerRecycler?.adapter?.itemCount)
    }

    @Test
    fun testFFNDialogNoEnrolledPrograms() {
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = false)
        val ffnAdapter = widget.frequentFlyerRecycler?.adapter as FrequentFlyerAdapter
        assertNull(ffnAdapter.frequentFlyerPlans.enrolledFrequentFlyerPlans)

        val frequentFlyerViewHolder = getViewHolderAndOpen(ffnAdapter)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "", 0)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
    }

    @Test
    fun testFFNDialogEnrolledPrograms() {
        Db.getTravelers()[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership())
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)
        val ffnAdapter = widget.frequentFlyerRecycler?.adapter as FrequentFlyerAdapter
        assertEquals(1, ffnAdapter.frequentFlyerPlans.enrolledFrequentFlyerPlans.size)

        val frequentFlyerViewHolder = getViewHolderAndOpen(ffnAdapter)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "12345", 0)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
    }

    private fun givenLegsAndFrequentFlyerPlans(hasEnrolledPlans: Boolean) {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(hasEnrolledPlans))
        widget.frequentFlyerButton?.performClick()
    }

    private fun getViewHolderAndOpen(ffnAdapter: FrequentFlyerAdapter): FrequentFlyerViewHolder {
        val frequentFlyerViewHolder = ffnAdapter.onCreateViewHolder(widget.frequentFlyerRecycler as RecyclerView, 0) as FrequentFlyerViewHolder
        ffnAdapter.onBindViewHolder(frequentFlyerViewHolder, 0)
        frequentFlyerViewHolder.frequentFlyerProgram.performClick()
        return frequentFlyerViewHolder
    }

    private fun buildMockFlight(airlineListSize: Int?) : FlightLeg {
        val flight = Mockito.mock(FlightLeg::class.java)
        flight.destinationAirportCode = testAirportCode
        flight.destinationCity = testRegionName

        val airlineNames = listOf("Alaska Airlines", "Delta", "Alaska Airlines")

        createFlightSegments(flight, airlineNames, airlineListSize)
        return flight
    }

    private fun createFlightSegments(flight: FlightLeg, airlineNames: List<String>, size: Int?) {
        flight.segments = ArrayList<FlightSegment>()
        if (size != null) {
            for (i in 0 until size!!) {
                flight.segments.add(FlightSegment())
                flight.segments[i].airlineName = airlineNames[i]
                flight.segments[i].airlineCode = "AA"
            }
        }
    }

    private fun getNewFrequentFlyerMembership() : TravelerFrequentFlyerMembership {
        val newMembership = TravelerFrequentFlyerMembership()
        newMembership.airlineCode = "AA"
        newMembership.membershipNumber = "12345"
        newMembership.planCode = "AA"
        return newMembership
    }

    private fun getFrequentFlyerPlans(hasEnrolledPlans: Boolean) : FlightCreateTripResponse.FrequentFlyerPlans {
        val enrolledPlan = FlightCreateTripResponse.FrequentFlyerPlans ()
        enrolledPlan.allFrequentFlyerPlans = getAllFrequentFlyerPlans()
        enrolledPlan.enrolledFrequentFlyerPlans = if (hasEnrolledPlans) listOf(getFrequentFlyerTripResponse("AA", "Alaska Airlines", "123")) else null
        return enrolledPlan
    }

    private fun getAllFrequentFlyerPlans() : List<FrequentFlyerPlansTripResponse> {
        return listOf(getFrequentFlyerTripResponse("AA", "Alaska Airlines", ""),
                getFrequentFlyerTripResponse("DA", "Delta Airlines", ""),
                getFrequentFlyerTripResponse("UA", "United Airlines", ""))
    }

    private fun getFrequentFlyerTripResponse(airlineCode: String, planName: String, membershipNumber: String) : FrequentFlyerPlansTripResponse {
        val enrolledPlan = FrequentFlyerPlansTripResponse()
        enrolledPlan.airlineCode = airlineCode
        enrolledPlan.frequentFlyerPlanName = planName
        enrolledPlan.membershipNumber = membershipNumber
        return enrolledPlan
    }

    private fun assertFrequentFlyerDialog(testDialog: ShadowAlertDialog, frequentFlyerAdapter: FrequentFlyerDialogAdapter) {
        assertNotNull(testDialog)
        assertEquals(frequentFlyerAdapter, testDialog.adapter as FrequentFlyerDialogAdapter)
        assertEquals("Programs", testDialog.title)
        assertEquals(3, testDialog.adapter.count)
    }

    private fun assertFrequentFlyerViewHolderData(frequentFlyerViewHolder: FrequentFlyerViewHolder,
                                                  programName: String,
                                                  programTitle: String,
                                                  programNumber: String,
                                                  currentPosition: Int) {
        assertEquals(programNumber, frequentFlyerViewHolder.frequentFlyerNumberInput.text.toString())
        assertEquals(programName, frequentFlyerViewHolder.frequentFlyerProgram.text.toString())
        assertEquals(programTitle, frequentFlyerViewHolder.frequentFlyerNameTitle.text.toString())
        assertEquals(currentPosition, frequentFlyerViewHolder.frequentFlyerAdapter.currentPosition)
    }
}
