package com.expedia.bookings.test.robolectric

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
import kotlin.test.assertEquals
import org.robolectric.shadows.ShadowAlertDialog
import rx.observers.TestSubscriber
import java.util.LinkedHashMap
import java.util.ArrayList
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
        Db.clear()
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
        val frequentFlyerPlansSubscriber = TestSubscriber.create<FlightCreateTripResponse.FrequentFlyerPlans>()
        val ffnAdapter = widget.frequentFlyerRecycler?.adapter as FrequentFlyerAdapter
        ffnAdapter.viewModel.frequentFlyerPlans.subscribe(frequentFlyerPlansSubscriber)
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = false)
        assertNull(frequentFlyerPlansSubscriber.onNextEvents[0].enrolledFrequentFlyerPlans)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "", 0)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerDialogAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
    }

    @Test
    fun testFFNDialogEnrolledPrograms() {
        val frequentFlyerPlansSubscriber = TestSubscriber.create<FlightCreateTripResponse.FrequentFlyerPlans>()
        val ffnAdapter = widget.frequentFlyerRecycler?.adapter as FrequentFlyerAdapter
        ffnAdapter.viewModel.frequentFlyerPlans.subscribe(frequentFlyerPlansSubscriber)
        Db.getTravelers()[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)

        frequentFlyerPlansSubscriber.assertValueCount(1)
        val plans = frequentFlyerPlansSubscriber.onNextEvents[0]
        assertEquals(1, plans.enrolledFrequentFlyerPlans.size)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "12345", 0)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerDialogAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
    }

    @Test
    fun testDifferentTravelerUpdatesEnrolledPrograms() {
        Db.getTravelers()[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)
        val frequentFlyerViewHolder = getViewHolderAndOpen()
        Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog()).dismiss()
        val oldEnrolledPlans = frequentFlyerViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans

        assertEnrolledPlans(oldEnrolledPlans, 1, "AA", "12345", "AA")

        val newTraveler = Traveler()
        newTraveler.addFrequentFlyerMembership(getNewFrequentFlyerMembership("UA", "98765", "UA"))
        frequentFlyerViewHolder.viewModel.updateTraveler(newTraveler)

        val newEnrolledPlans = frequentFlyerViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans
        assertEnrolledPlans(newEnrolledPlans, 1, "UA", "98765", "UA")
    }

    @Test
    fun testNewSelectedTravelerClearsFFN() {
        val updateTravelerSubscriber = TestSubscriber.create<Traveler>()
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel?.updateTravelerObservable?.subscribe(updateTravelerSubscriber)
        Db.getTravelers()[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)
        updateTravelerSubscriber.assertValueCount(0)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog()).dismiss()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "12345", 0)

        widget.onAddNewTravelerSelected()
        updateTravelerSubscriber.assertValueCount(1)

        val updatedViewHolder = widget.frequentFlyerRecycler?.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder

        assertFrequentFlyerViewHolderData(updatedViewHolder, "Alaska Airlines", "Alaska Airlines", "", 0)
        val newEnrolledPlans = updatedViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans
        assertEnrolledPlans(newEnrolledPlans, 0, null, null, null)
    }

    @Test
    fun testViewHolderViewModelsMatch() {
        givenLegsAndFrequentFlyerPlans(false)
        val viewHolderViewModels = (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel?.viewHolderViewModels
        assertEquals(2, viewHolderViewModels?.size)

        val firstVm = viewHolderViewModels?.first()
        val firstViewHolder = widget.frequentFlyerRecycler?.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder
        assertEquals(firstViewHolder.viewModel, firstVm)

        val secondVm = viewHolderViewModels?.get(1)
        val secondViewHolder = widget.frequentFlyerRecycler?.findViewHolderForAdapterPosition(1) as FrequentFlyerViewHolder
        assertEquals(secondViewHolder.viewModel, secondVm)
    }

    private fun givenLegsAndFrequentFlyerPlans(hasEnrolledPlans: Boolean) {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(hasEnrolledPlans))
        widget.frequentFlyerButton?.performClick()
        widget.frequentFlyerRecycler?.measure(0, 0);
        widget.frequentFlyerRecycler?.layout(0, 0, 100, 10000);
    }

    private fun getViewHolderAndOpen(): FrequentFlyerViewHolder {
        val frequentFlyerViewHolder = widget.frequentFlyerRecycler?.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder
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

    private fun getNewFrequentFlyerMembership(airlineCode: String, number: String, planCode: String) : TravelerFrequentFlyerMembership {
        val newMembership = TravelerFrequentFlyerMembership()
        newMembership.airlineCode = airlineCode
        newMembership.membershipNumber = number
        newMembership.planCode = planCode
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
        assertEquals(currentPosition, frequentFlyerViewHolder.frequentFlyerDialogAdapter.currentPosition)
    }

    private fun assertEnrolledPlans(oldEnrolledPlans: LinkedHashMap<String, FrequentFlyerPlansTripResponse>,
                                    size: Int,
                                    key: String?,
                                    number: String?,
                                    airlineCode: String?) {
        assertEquals(size, oldEnrolledPlans.size)
        assertEquals(key, oldEnrolledPlans.keys.firstOrNull())
        assertEquals(number, oldEnrolledPlans.values.firstOrNull()?.membershipNumber)
        assertEquals(airlineCode, oldEnrolledPlans.values.firstOrNull()?.airlineCode)
    }
}
