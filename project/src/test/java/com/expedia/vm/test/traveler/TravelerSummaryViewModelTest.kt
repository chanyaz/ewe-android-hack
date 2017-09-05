package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.res.Resources
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.TravelerSummaryViewModel
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelerSummaryViewModelTest {
    lateinit var summaryVM: TravelerSummaryViewModel
    private var activity: Activity by Delegates.notNull()
    private var resources: Resources by Delegates.notNull()

    var expectedEmptyTitle: String by Delegates.notNull()
    var expectedEmptySubTitle: String by Delegates.notNull()
    val expectedEmptyIconStatus = ContactDetailsCompletenessStatus.DEFAULT
    val expectedIncompleteStatus = ContactDetailsCompletenessStatus.INCOMPLETE
    val expectedCompleteStatus = ContactDetailsCompletenessStatus.COMPLETE

    var expectedSubTitleErrorMessage: String by Delegates.notNull()

    val mockTravelerProvider = MockTravelerProvider()

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        expectedEmptyTitle = resources.getString(R.string.enter_traveler_details)
        expectedEmptySubTitle = resources.getString(R.string.enter_traveler_details)

        expectedSubTitleErrorMessage = "Enter missing traveler details"

        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        Ui.getApplication(activity).defaultTravelerComponent()
        summaryVM = TravelerSummaryViewModel(activity)
    }

    @Test
    fun emptyStateDefaultAfterInitWithTravelerInfoAbTest() {
        val expectedTitle = resources.getString(R.string.enter_traveler_details)
        val summaryVM = TravelerSummaryViewModel(activity)
        assertEquals(expectedTitle, summaryVM.getTitle())
        assertEquals("", summaryVM.getSubtitle())
    }

    @Test
    fun updateToIncompleteOneTravelerNoNameWithTravelerInfoAbTest() {
        val expectedEmptyTitle = resources.getString(R.string.enter_traveler_details)
        val summaryVM = TravelerSummaryViewModel(activity)

        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun emptyStateDefaultAfterInit() {
        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToEmptyOneTraveler() {
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteOneTravelerNoName() {
        mockTravelerProvider.updateDBWithMockTravelers(1, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteOneTravelerWithName() {
        val mockTraveler = Mockito.mock(Traveler::class.java)
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToCompleteOneTraveler() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(mockTravelerProvider.adultBirthDate.toString("MM/dd/yyyy"), summaryVM.subtitleObservable.value)
        assertEquals(expectedCompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun emptyStateDefaultMultipleTravelers() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.CLEAN)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(getAdditionalTravelersSubTitle(2), summaryVM.subtitleObservable.value)
        assertEquals(expectedEmptyIconStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteMultipleTravelersNoName() {
        mockTravelerProvider.updateDBWithMockTravelers(2, Traveler())
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(expectedEmptyTitle, summaryVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToIncompleteMultipleTravelersWithName() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 2)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
    }

    @Test
    fun updateToCompleteMultipleTraveler() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 2)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(getAdditionalTravelersSubTitle(2), summaryVM.subtitleObservable.value)
        assertEquals(expectedCompleteStatus, summaryVM.iconStatusObservable.value)
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
    fun testBirthdayNotDisplayedWhenDirty() {
        val mockTraveler = mockTravelerProvider.getCompleteMockTravelerExecptBirthday()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.DIRTY)

        assertEquals(mockTravelerProvider.testFullName, summaryVM.titleObservable.value)
        assertEquals(expectedIncompleteStatus, summaryVM.iconStatusObservable.value)
        assertEquals(expectedSubTitleErrorMessage, summaryVM.subtitleObservable.value)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNameIsReversedBasedOnPos() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        SettingUtils.save(activity, R.string.PointOfSaleKey, PointOfSaleId.JAPAN.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)

        val mockTraveler = mockTravelerProvider.getCompleteMockTraveler()
        updateDbTravelers(mockTraveler, mockTravelerProvider.testReversedFullName, 1)
        summaryVM.travelerStatusObserver.onNext(TravelerCheckoutStatus.COMPLETE)

        assertEquals(mockTravelerProvider.testReversedFullName, summaryVM.titleObservable.value)
    }

    private fun getAdditionalTravelersSubTitle(travelerCount: Int) : String {
        return Phrase.from(resources.getQuantityString(R.plurals.checkout_more_travelers_TEMPLATE, travelerCount - 1))
                .put("travelercount", travelerCount - 1).format().toString()
    }

    private fun updateDbTravelers(traveler: Traveler, name: String, numberOfTravelers: Int) {
        setFullNameForTraveler(traveler, name )
        mockTravelerProvider.updateDBWithMockTravelers(numberOfTravelers, traveler)
    }

    private fun setFullNameForTraveler(traveler: Traveler, name: String) {
        Mockito.`when`(traveler.fullName).thenReturn(mockTravelerProvider.testFullName)
        Mockito.`when`(traveler.fullNameBasedOnPos).thenReturn(name)
    }
}