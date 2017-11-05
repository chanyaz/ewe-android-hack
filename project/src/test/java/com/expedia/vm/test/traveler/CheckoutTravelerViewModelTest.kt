package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CheckoutTravelerViewModelTest {
    val context = RuntimeEnvironment.application
    val mockTravelerProvider = MockTravelerProvider()
    lateinit var testViewModel: TravelersViewModel
    lateinit var searchParams: PackageSearchParams

    @Before
    fun setUp() {
        searchParams = setUpParams()
        Ui.getApplication(context).defaultTravelerComponent()
        testViewModel = FlightTravelersViewModel(context, LineOfBusiness.PACKAGES, false)
        testViewModel.travelerValidator.updateForNewSearch(searchParams)
    }

    @Test
    fun testMainTravelerMinAgeShow() {
        testViewModel = FlightTravelersViewModel(context, LineOfBusiness.PACKAGES, true)
        assertTrue(testViewModel.showMainTravelerMinAgeMessaging.value)
    }

    @Test
    fun testMainTravelerMinAgeHide() {
        testViewModel = FlightTravelersViewModel(context, LineOfBusiness.PACKAGES, false)
        assertFalse(testViewModel.showMainTravelerMinAgeMessaging.value)
    }

    @Test
    fun testEmptyListInvalid() {
        Db.setTravelers(emptyList())
        assertFalse(testViewModel.allTravelersValid())
    }

    @Test
    fun testInvalidTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        assertFalse(testViewModel.allTravelersValid())
    }

    @Test
    fun testMultipleInvalidTravelers() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        assertFalse(testViewModel.allTravelersValid())
    }

    @Test
    fun testValidTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(1, mockTravelerProvider.getCompleteMockTraveler())
        testViewModel.travelerValidator.updateForNewSearch(searchParams)
        assertTrue(testViewModel.allTravelersValid())
    }

    @Test
    fun testMultipleValidTravelers() {
        testViewModel.travelerValidator.updateForNewSearch(searchParams)
        mockTravelerProvider.updateDBWithMockTravelers(2, mockTravelerProvider.getCompleteMockTraveler())
        assertTrue(testViewModel.allTravelersValid())
    }

    @Test
    fun testEmptyTravelersOneValid() {
        mockTravelerProvider.addMockTravelerToDb(mockTravelerProvider.getCompleteMockTraveler())

        assertFalse(testViewModel.areTravelersEmpty())
    }

    @Test
    fun testEmptyTravelersMultipleValid() {
        mockTravelerProvider.updateDBWithMockTravelers(2, mockTravelerProvider.getCompleteMockTraveler())
        assertFalse(testViewModel.areTravelersEmpty())
    }

    @Test
    fun testEmptyTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        assertTrue(testViewModel.areTravelersEmpty())
    }

    @Test
    fun testEmptyTravelers() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        assertTrue(testViewModel.areTravelersEmpty())
    }

    @Test
    fun testValidation() {
        val testCompleteTraveler = mockTravelerProvider.getCompleteMockTraveler()
        val testAllTravelersComplete = TestSubscriber.create<List<Traveler>>()
        val testInvalidTravelers = TestSubscriber.create<Unit>()
        val testEmptyTravelers = TestSubscriber.create<Unit>()
        val testTravelerCompleteness = TestSubscriber.create<TravelerCheckoutStatus>()

        val expectedAllTravelersComplete = listOf(arrayListOf(testCompleteTraveler))
        val expectedEmptyTravelers = arrayListOf(Unit)
        val expectedInvalidTravelers = arrayListOf(Unit)
        val expectedTravelerCompleteness = arrayListOf(TravelerCheckoutStatus.CLEAN, TravelerCheckoutStatus.CLEAN, TravelerCheckoutStatus.DIRTY, TravelerCheckoutStatus.COMPLETE)

        testViewModel.allTravelersCompleteSubject.subscribe(testAllTravelersComplete)
        testViewModel.invalidTravelersSubject.subscribe(testInvalidTravelers)
        testViewModel.emptyTravelersSubject.subscribe(testEmptyTravelers)
        testViewModel.travelersCompletenessStatus.subscribe(testTravelerCompleteness)
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        testViewModel.refresh()

        mockTravelerProvider.updateDBWithMockTravelers(1, mockTravelerProvider.getInCompleteMockTraveler())
        testViewModel.refresh()

        mockTravelerProvider.updateDBWithMockTravelers(1, testCompleteTraveler)
        testViewModel.updateCompletionStatus()

        testAllTravelersComplete.assertReceivedOnNext(expectedAllTravelersComplete)
        testEmptyTravelers.assertReceivedOnNext(expectedEmptyTravelers)
        testInvalidTravelers.assertReceivedOnNext(expectedInvalidTravelers)
        testTravelerCompleteness.assertReceivedOnNext(expectedTravelerCompleteness)
    }

    private fun setUpParams() : PackageSearchParams {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(SuggestionV4())
                .destination(SuggestionV4())
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
        return packageParams
    }
}