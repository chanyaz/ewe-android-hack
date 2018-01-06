package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.HotelTravelerSummaryViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelTravelersSummaryViewModelTest {

    lateinit var summaryVM: HotelTravelerSummaryViewModel
    private var activity: Activity by Delegates.notNull()
    private var resources: Resources by Delegates.notNull()

    var expectedEmptyTitle = "Enter traveler details"
    var expectedSubTitleErrorMessage = ""

    val expectedEmptyIconStatus = ContactDetailsCompletenessStatus.DEFAULT
    val expectedIncompleteStatus = ContactDetailsCompletenessStatus.INCOMPLETE
    val expectedCompleteStatus = ContactDetailsCompletenessStatus.COMPLETE

    val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        Ui.getApplication(activity).defaultTravelerComponent()
        summaryVM = HotelTravelerSummaryViewModel(activity)
    }

    @After
    fun tearDown() {
        Db.sharedInstance.resetTravelers()
    }

    @Test
    fun testEmptyState() {
        assertTravelerSummaryCard(expectedTitle = expectedEmptyTitle,
                expectedSubtitle = "",
                expectedStatus = expectedEmptyIconStatus)
    }

    @Test
    fun testNullTravelerValidation() {
        assertFalse(summaryVM.isTravelerEmpty(null))
    }

    @Test
    fun testEmptyTravelerIsEmpty() {
        assertTrue(summaryVM.isTravelerEmpty(Traveler()))
    }

    @Test
    fun testCompletedTravelerIsEmpty() {
        assertFalse(summaryVM.isTravelerEmpty(mockTravelerProvider.getCompleteMockTraveler()))
    }

    @Test
    fun updateToEmptyTraveler() {
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertTravelerSummaryCard(expectedTitle = expectedEmptyTitle,
                expectedSubtitle = "",
                expectedStatus = expectedEmptyIconStatus)
    }

    @Test
    fun updateToIncompleteTravelerNoName() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertTravelerSummaryCard(expectedTitle = expectedEmptyTitle,
                expectedSubtitle = expectedSubTitleErrorMessage,
                expectedStatus = expectedIncompleteStatus)
    }

    @Test
    fun updateToIncompleteTravelerWithIncompleteName() {
        val mockTraveler = mockTravelerProvider.getIncompleteMockTravelerIncompleteName()

        mockTravelerProvider.updateDBWithMockTravelers(1, mockTraveler)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertTravelerSummaryCard(expectedTitle = expectedEmptyTitle,
                expectedSubtitle = "",
                expectedStatus = expectedIncompleteStatus)
    }

    @Test
    fun updateToIncompleteTravelerWithoutPhone() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTravelerWithoutPhone()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertTravelerSummaryCard(expectedTitle = mockTravelerProvider.testFullName,
                expectedSubtitle = expectedSubTitleErrorMessage,
                expectedStatus = expectedIncompleteStatus)
    }

    @Test
    fun updateToIncompleteTravelerWithoutEmail() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        mockTraveler.email = ""
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertTravelerSummaryCard(expectedTitle = mockTravelerProvider.testFullName,
                expectedSubtitle = expectedSubTitleErrorMessage,
                expectedStatus = expectedIncompleteStatus)
    }

    @Test
    fun updateToCompleteOneTraveler() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertTravelerSummaryCard(expectedTitle = mockTravelerProvider.testFullName,
                expectedSubtitle = mockTravelerProvider.testNumber,
                expectedStatus = expectedCompleteStatus)
    }

    @Test
    fun updateToIncompleteMultipleTravelersNoName() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertTravelerSummaryCard(expectedTitle = expectedEmptyTitle,
                expectedSubtitle = expectedSubTitleErrorMessage,
                expectedStatus = expectedIncompleteStatus)
    }

    @Test
    fun testPrepopulateSingleTraveler() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNameIsReversedBasedOnPos() {
        SettingUtils.save(activity, R.string.PointOfSaleKey, PointOfSaleId.JAPAN.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)

        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testReversedFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(mockTravelerProvider.testReversedFullName, summaryVM.titleObservable.value)
    }

    private fun updateDbTravelers(traveler: Traveler, name: String, numberOfTravelers: Int) {
        setFullNameForTraveler(traveler, name )
        mockTravelerProvider.updateDBWithMockTravelers(numberOfTravelers, traveler)
    }

    private fun setFullNameForTraveler(traveler: Traveler, name: String) {
        Mockito.`when`(traveler.fullName).thenReturn(mockTravelerProvider.testFullName)
        Mockito.`when`(traveler.fullNameBasedOnPos).thenReturn(name)
    }

    private fun assertTravelerSummaryCard(expectedTitle: String, expectedSubtitle: String, expectedStatus: ContactDetailsCompletenessStatus) {
        assertEquals(expectedTitle, summaryVM.getTitle())
        assertEquals(expectedSubtitle, summaryVM.getSubtitle())
        assertEquals(expectedStatus, summaryVM.iconStatusObservable.value)
    }

}
