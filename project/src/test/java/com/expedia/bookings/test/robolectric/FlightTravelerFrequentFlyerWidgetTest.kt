package com.expedia.bookings.test.robolectric

import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightLeg.FlightSegment
import com.expedia.bookings.data.flights.FrequentFlyerCard
import com.expedia.bookings.data.flights.FrequentFlyerPlansTripResponse
import com.expedia.bookings.data.flights.TravelerFrequentFlyerMembership
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.bookings.widget.FrequentFlyerDialogAdapter
import com.expedia.bookings.widget.traveler.FrequentFlyerAdapter
import com.expedia.bookings.widget.traveler.FrequentFlyerViewHolder
import com.expedia.testutils.AndroidAssert.Companion.assertViewFocusabilityIsFalse
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.expedia.vm.traveler.FrequentFlyerProgramNumberViewModel
import io.reactivex.subjects.BehaviorSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.ArrayList
import java.util.LinkedHashMap
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
@Config(shadows = [(ShadowGCM::class), (ShadowUserManager::class), (ShadowAccountManagerEB::class)])

class FlightTravelerFrequentFlyerWidgetTest {
    private lateinit var widget: FlightTravelerEntryWidget
    private val context = RuntimeEnvironment.application

    private val testRegionName = "Chicago"
    private val testAirportCode = "ORD"
    val traveler = Traveler()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_flight_entry_widget, null) as FlightTravelerEntryWidget
        Db.sharedInstance.travelers.add(traveler)
        widget.viewModel = FlightTravelerEntryWidgetViewModel(activity, 0, BehaviorSubject.createDefault(false), TravelerCheckoutStatus.CLEAN)
    }

    @Test
    fun testFFNCardVisibility() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        widget.frequentFlyerButton.performClick()
        assertEquals(View.VISIBLE, widget.frequentFlyerRecycler.visibility)
    }

    @Test
    fun testCorrectFFNCardsDuplicateAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(false))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        assertEquals(2, widget.frequentFlyerRecycler.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNullAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(null)))
        assertEquals(0, widget.frequentFlyerRecycler.adapter?.itemCount)
    }

    @Test
    fun testCorrectFFNCardsNoAirlines() {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(0)))
        assertEquals(0, widget.frequentFlyerRecycler.adapter?.itemCount)
    }

    @Test
    fun testFFNDialogNoEnrolledPrograms() {
        val frequentFlyerPlansSubscriber = TestObserver.create<FlightCreateTripResponse.FrequentFlyerPlans>()
        val ffnAdapter = widget.frequentFlyerRecycler.adapter as FrequentFlyerAdapter
        ffnAdapter.viewModel.frequentFlyerPlans.subscribe(frequentFlyerPlansSubscriber)
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = false)
        assertNull(frequentFlyerPlansSubscriber.values()[0].enrolledFrequentFlyerPlans)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "", 0)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerDialogAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
    }

    @Test
    fun testFFNDialogEnrolledPrograms() {
        val frequentFlyerPlansSubscriber = TestObserver.create<FlightCreateTripResponse.FrequentFlyerPlans>()
        val ffnAdapter = widget.frequentFlyerRecycler.adapter as FrequentFlyerAdapter
        ffnAdapter.viewModel.frequentFlyerPlans.subscribe(frequentFlyerPlansSubscriber)
        Db.sharedInstance.travelers[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA", "AA-A1"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)

        frequentFlyerPlansSubscriber.assertValueCount(1)
        val plans = frequentFlyerPlansSubscriber.values()[0]
        assertEquals(1, plans.enrolledFrequentFlyerPlans.size)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "12345", 0)
        assertProgramNumberViewModel("AA-A1", "AA", "A1", frequentFlyerViewHolder.viewModel.frequentFlyerProgramNumberViewModel)

        val testDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertFrequentFlyerDialog(testDialog, frequentFlyerViewHolder.frequentFlyerDialogAdapter)

        testDialog.clickOnItem(1)
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Delta Airlines", "Alaska Airlines", "", 1)
        assertProgramNumberViewModel("DA-D1", "DA", "D1", frequentFlyerViewHolder.viewModel.frequentFlyerProgramNumberViewModel)
    }

    @Test
    fun testDifferentTravelerUpdatesEnrolledPrograms() {
        Db.sharedInstance.travelers[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA", "AA-A1"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)
        var frequentFlyerViewHolder = getViewHolderAndOpen()
        Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog()).dismiss()
        val oldEnrolledPlans = frequentFlyerViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans

        assertEnrolledPlans(oldEnrolledPlans, 1, "AA", "12345", "AA")

        val newTraveler = Traveler()
        newTraveler.addFrequentFlyerMembership(getNewFrequentFlyerMembership("UA", "98765", "UA", "UA-U1"))
        widget.viewModel.updateTraveler(newTraveler)
        openFrequentFlyerWidget()
        frequentFlyerViewHolder = getViewHolderAndOpen()
        val newEnrolledPlans = frequentFlyerViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans
        assertEnrolledPlans(newEnrolledPlans, 1, "UA", "98765", "UA")
    }

    @Test
    fun testNewSelectedTravelerClearsFFN() {
        val updateTravelerSubscriber = TestObserver.create<Traveler>()
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.updateTravelerObservable?.subscribe(updateTravelerSubscriber)
        Db.sharedInstance.travelers[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA", "AA-A1"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = true)
        updateTravelerSubscriber.assertValueCount(0)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog()).dismiss()
        assertFrequentFlyerViewHolderData(frequentFlyerViewHolder, "Alaska Airlines", "Alaska Airlines", "12345", 0)

        widget.onAddNewTravelerSelected()
        updateTravelerSubscriber.assertValueCount(1)
        openFrequentFlyerWidget()

        val updatedViewHolder = widget.frequentFlyerRecycler.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder

        assertFrequentFlyerViewHolderData(updatedViewHolder, "Alaska Airlines", "Alaska Airlines", "", 0)
        val newEnrolledPlans = updatedViewHolder.frequentFlyerDialogAdapter.enrolledFrequentFlyerPlans
        assertEnrolledPlans(newEnrolledPlans, 0, null, null, null)
    }

    @Test
    fun testViewHolderViewModelsMatch() {
        givenLegsAndFrequentFlyerPlans(false)
        val viewHolderViewModels = (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.viewHolderViewModels
        assertEquals(2, viewHolderViewModels.size)

        val firstVm = viewHolderViewModels.first()
        val firstViewHolder = widget.frequentFlyerRecycler.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder
        assertEquals(firstViewHolder.viewModel, firstVm)

        val secondVm = viewHolderViewModels.get(1)
        val secondViewHolder = widget.frequentFlyerRecycler.findViewHolderForAdapterPosition(1) as FrequentFlyerViewHolder
        assertEquals(secondViewHolder.viewModel, secondVm)
    }

    @Test
    fun testFrequentFlyerButtonHiddenWithoutValidPlans() {
        val frequentFlyerVisibilitySubscriber = TestObserver.create<Boolean>()
        val frequentFlyerCardsSubscriber = TestObserver.create<List<FrequentFlyerCard>>()
        setupVisibilityAndCardTestSubscribers(frequentFlyerVisibilitySubscriber, frequentFlyerCardsSubscriber, 1)

        frequentFlyerCardsSubscriber.assertNoValues()
        frequentFlyerVisibilitySubscriber.assertValue(false)
        assertEquals(View.GONE, widget.frequentFlyerButton.visibility)
        assertEquals(0, (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.viewHolderViewModels.size)
    }

    @Test
    fun testFrequentFlyerButtonVisibleWithOneValidAndOneInvalidPlan() {
        val frequentFlyerVisibilitySubscriber = TestObserver.create<Boolean>()
        val frequentFlyerCardsSubscriber = TestObserver.create<List<FrequentFlyerCard>>()
        setupVisibilityAndCardTestSubscribers(frequentFlyerVisibilitySubscriber, frequentFlyerCardsSubscriber, 2)

        frequentFlyerCardsSubscriber.assertValueCount(1)
        frequentFlyerVisibilitySubscriber.assertValue(true)
        assertEquals(View.VISIBLE, widget.frequentFlyerButton.visibility)
    }

    @Test
    fun testFrequentFlyerRecyclerVisibilityWithValidPlans() {
        val frequentFlyerVisibilitySubscriber = TestObserver.create<Boolean>()
        val frequentFlyerCardsSubscriber = TestObserver.create<List<FrequentFlyerCard>>()
        setupVisibilityAndCardTestSubscribers(frequentFlyerVisibilitySubscriber, frequentFlyerCardsSubscriber, 2)

        frequentFlyerCardsSubscriber.assertValueCount(1)
        frequentFlyerVisibilitySubscriber.assertValue(true)
        assertEquals(View.GONE, widget.frequentFlyerRecycler.visibility)

        openFrequentFlyerWidget()

        assertEquals(View.VISIBLE, widget.frequentFlyerRecycler.visibility)
        assertEquals(1, (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.viewHolderViewModels.size)
        assertEquals(1, (widget.frequentFlyerRecycler.adapter as FrequentFlyerAdapter).itemCount)
    }

    @Test
    fun testFrequentFlyerRecyclerVisibilityWithInvalidPlans() {
        val frequentFlyerVisibilitySubscriber = TestObserver.create<Boolean>()
        val frequentFlyerCardsSubscriber = TestObserver.create<List<FrequentFlyerCard>>()
        setupVisibilityAndCardTestSubscribers(frequentFlyerVisibilitySubscriber, frequentFlyerCardsSubscriber, 1)

        frequentFlyerCardsSubscriber.assertNoValues()
        frequentFlyerVisibilitySubscriber.assertValue(false)
        assertEquals(0, (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.viewHolderViewModels.size)
        assertEquals(0, (widget.frequentFlyerRecycler.adapter as FrequentFlyerAdapter).itemCount)
    }

    @Test
    fun testFrequentFlyerDoesntCrashSwitchingTravelersWhileClosed() {
        val updateTravelerSubscriber = TestObserver.create<Traveler>()
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel.updateTravelerObservable?.subscribe(updateTravelerSubscriber)
        Db.sharedInstance.travelers[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "12345", "AA", "AA-A1"))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(true))
        updateTravelerSubscriber.assertValueCount(0)

        (widget.viewModel as FlightTravelerEntryWidgetViewModel).updateTraveler(traveler)

        updateTravelerSubscriber.assertValueCount(1)
        assertEquals(traveler, (widget.frequentFlyerRecycler.adapter as FrequentFlyerAdapter).viewModel.traveler)
    }

    @Test
    fun testFrequentFlyerProgramFocusability() {
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = false)
        val viewHolder = widget.frequentFlyerRecycler.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder
        val frequentFlyerProgram = viewHolder.frequentFlyerProgram

        assertViewFocusabilityIsFalse(frequentFlyerProgram)
    }

    @Test
    fun testFFNDialogEnrolledProgramsHidesProgramsWithoutNumbers() {
        Db.sharedInstance.travelers[0].addFrequentFlyerMembership(getNewFrequentFlyerMembership("AA", "", "AA", "AA-A1"))
        givenLegsAndFrequentFlyerPlans(hasEnrolledPlans = false)

        val frequentFlyerViewHolder = getViewHolderAndOpen()
        assertEquals(0, frequentFlyerViewHolder.viewModel.enrolledPlans.size)
    }

    private fun setupVisibilityAndCardTestSubscribers(visibilitySubscriber: TestObserver<Boolean>,
                                                      cardsSubscriber: TestObserver<List<FrequentFlyerCard>>,
                                                      numOfSegments: Int) {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel
                .showFrequentFlyerObservable?.subscribe(visibilitySubscriber)
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerAdapterViewModel
                .frequentFlyerCardsObservable?.subscribe(cardsSubscriber)

        val mockFlightWithInvalidAirlines = buildMockFlight(numOfSegments)
        mockFlightWithInvalidAirlines.segments.first().airlineName = "INVALID AIRLINE NAME"
        mockFlightWithInvalidAirlines.segments.first().airlineCode = "INVALID AIRLINE CODE"
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(mockFlightWithInvalidAirlines))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(false))
    }

    private fun givenLegsAndFrequentFlyerPlans(hasEnrolledPlans: Boolean) {
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(listOf(buildMockFlight(3)))
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(getFrequentFlyerPlans(hasEnrolledPlans))
        openFrequentFlyerWidget()
    }

    private fun openFrequentFlyerWidget() {
        widget.frequentFlyerButton.performClick()
        widget.frequentFlyerRecycler.measure(0, 0)
        widget.frequentFlyerRecycler.layout(0, 0, 100, 10000)
    }

    private fun getViewHolderAndOpen(): FrequentFlyerViewHolder {
        val frequentFlyerViewHolder = widget.frequentFlyerRecycler.findViewHolderForAdapterPosition(0) as FrequentFlyerViewHolder
        frequentFlyerViewHolder.frequentFlyerProgram.performClick()
        return frequentFlyerViewHolder
    }

    private fun buildMockFlight(airlineListSize: Int?): FlightLeg {
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
            for (i in 0 until size) {
                flight.segments.add(FlightSegment())
                flight.segments[i].airlineName = airlineNames[i]
                flight.segments[i].airlineCode = "AA"
            }
        }
    }

    private fun getNewFrequentFlyerMembership(airlineCode: String, number: String, planCode: String, planID: String): TravelerFrequentFlyerMembership {
        val newMembership = TravelerFrequentFlyerMembership()
        newMembership.airlineCode = airlineCode
        newMembership.membershipNumber = number
        newMembership.planCode = planCode
        newMembership.frequentFlyerPlanID = planID
        return newMembership
    }

    private fun getFrequentFlyerPlans(hasEnrolledPlans: Boolean): FlightCreateTripResponse.FrequentFlyerPlans {
        val enrolledPlan = FlightCreateTripResponse.FrequentFlyerPlans ()
        enrolledPlan.allFrequentFlyerPlans = getAllFrequentFlyerPlans()
        enrolledPlan.enrolledFrequentFlyerPlans = if (hasEnrolledPlans) listOf(getFrequentFlyerTripResponse("AA", "Alaska Airlines", "123", "AA-A1", "A1")) else null
        return enrolledPlan
    }

    private fun getAllFrequentFlyerPlans(): List<FrequentFlyerPlansTripResponse> {
        return listOf(getFrequentFlyerTripResponse("AA", "Alaska Airlines", "", "AA-A1", "A1"),
                getFrequentFlyerTripResponse("DA", "Delta Airlines", "", "DA-D1", "D1"),
                getFrequentFlyerTripResponse("UA", "United Airlines", "", "UA-U1", "U1"))
    }

    private fun getFrequentFlyerTripResponse(airlineCode: String, planName: String, membershipNumber: String, planID: String, planCode: String): FrequentFlyerPlansTripResponse {
        val enrolledPlan = FrequentFlyerPlansTripResponse()
        enrolledPlan.airlineCode = airlineCode
        enrolledPlan.frequentFlyerPlanName = planName
        enrolledPlan.membershipNumber = membershipNumber
        enrolledPlan.frequentFlyerPlanID = planID
        enrolledPlan.frequentFlyerPlanCode = planCode
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

    private fun assertProgramNumberViewModel(planID: String, planKey: String, planCode: String, viewModel: FrequentFlyerProgramNumberViewModel) {
        assertEquals(planID, viewModel.airlineId)
        assertEquals(planKey, viewModel.airlineKey)
        assertEquals(planCode, viewModel.planCode)
    }
}
