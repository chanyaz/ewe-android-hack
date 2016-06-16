package com.expedia.vm.test.traveler

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.vm.traveler.CheckoutTravelerViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class CheckoutTravelerViewModelTest {
    val mockTravelerProvider = MockTravelerProvider()
    lateinit var testViewModel: CheckoutTravelerViewModel
    lateinit var searchParams: PackageSearchParams

    @Before
    fun setUp() {
        searchParams = setUpParams()
        var context = RuntimeEnvironment.application
        Ui.getApplication(context).defaultTravelerComponent()
        testViewModel = CheckoutTravelerViewModel(context)
    }

    @Test
    fun testEmptyListInvalid() {
        Db.setTravelers(emptyList())
        assertFalse(testViewModel.validateTravelersComplete())
    }

    @Test
    fun testInvalidTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        assertFalse(testViewModel.validateTravelersComplete())
    }

    @Test
    fun testMultipleInvalidTravelers() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        assertFalse(testViewModel.validateTravelersComplete())
    }

    @Test
    fun testValidTraveler() {
        mockTravelerProvider.updateDBWithMockTravelers(1, mockTravelerProvider.getCompleteMockTraveler())
        testViewModel.travelerValidator.updateForNewSearch(searchParams)
        assertTrue(testViewModel.validateTravelersComplete())
    }

    @Test
    fun testMultipleValidTravelers() {
        testViewModel.travelerValidator.updateForNewSearch(searchParams)
        mockTravelerProvider.updateDBWithMockTravelers(2, mockTravelerProvider.getCompleteMockTraveler())
        assertTrue(testViewModel.validateTravelersComplete())
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