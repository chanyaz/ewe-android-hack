package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.TravelerSelectViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelerSelectViewModelTest {
    lateinit var selectVM: TestTravelerSelectViewModel
    private var activity: Activity by Delegates.notNull()
    private var resources: Resources by Delegates.notNull()

    var expectedEmptyTitle: String by Delegates.notNull()
    var expectedEmptyTitleChild: String by Delegates.notNull()
    var expectedEmptyTitleInfant: String by Delegates.notNull()
    var expectedEmptySubTitle = ""
    var expectedEmptyFont = FontCache.Font.ROBOTO_REGULAR
    var expectedDefaultColor: Int by Delegates.notNull()
    var expectedDefaultFont = FontCache.Font.ROBOTO_MEDIUM
    var expectedErrorColor: Int by Delegates.notNull()

    val testName = "Oscar Grouch"
    val testNumber = "7732025862"
    val testIndex = 0

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        expectedEmptyTitle = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengercategory", "Adult")
                .format().toString()
        expectedEmptyTitleChild = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengercategory", "Child")
                .format().toString()
        expectedEmptyTitleInfant = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengercategory", "Infant")
                .format().toString()
        expectedDefaultColor = ContextCompat.getColor(activity, R.color.traveler_default_card_text_color)
        expectedErrorColor = ContextCompat.getColor(activity, R.color.traveler_incomplete_text_color)
        setPackageParams()
    }

    @Test
    fun testAdultTitle() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testChildTitle() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.CHILD)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleChild, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.INFANT_IN_SEAT)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.INFANT_IN_LAP)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyEmptyTraveler() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyNamedTraveler() {
        val travelerWithName = Traveler()
        travelerWithName.fullName = testName

        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.testTraveler = travelerWithName
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(testName, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value,
                "Color should be the default if name field is populated")
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyPhoneNoName() {
        val travelerWithPhone= Traveler()
        travelerWithPhone.phoneNumber = testNumber

        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.testTraveler = travelerWithPhone
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value,
                "Expected that no phone shows unless there is also a name")
        assertEquals(expectedErrorColor, selectVM.textColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyPhoneAndName() {
        val traveler = Traveler()
        traveler.fullName = testName
        traveler.phoneNumber = testNumber

        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.testTraveler = traveler
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(testName, selectVM.titleObservable.value)
        assertEquals(testNumber, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyValidTraveler() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, PassengerCategory.ADULT)
        selectVM.testTraveler = getCompleteTraveler()
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(testName, selectVM.titleObservable.value)
        assertEquals(testNumber, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.textColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    private fun getCompleteTraveler(): Traveler {
        val mockPhone = Mockito.mock(Phone::class.java)
        Mockito.`when`(mockPhone.number).thenReturn(testNumber)

        val mockName = Mockito.mock(TravelerName::class.java)
        Mockito.`when`(mockName.firstName).thenReturn("Oscar")
        Mockito.`when`(mockName.lastName).thenReturn("Grouch")

        val mockTraveler = Mockito.mock(Traveler::class.java)
        Mockito.`when`(mockTraveler.name).thenReturn(mockName)
        Mockito.`when`(mockTraveler.fullName).thenReturn(testName)
        Mockito.`when`(mockTraveler.primaryPhoneNumber).thenReturn(mockPhone)
        Mockito.`when`(mockTraveler.phoneNumber).thenReturn(testNumber)
        Mockito.`when`(mockTraveler.birthDate).thenReturn(LocalDate.now().minusYears(18))
        Mockito.`when`(mockTraveler.getPassengerCategory(Mockito.any<PackageSearchParams>()))
                .thenReturn(PassengerCategory.ADULT)

        return mockTraveler
    }

    private fun setPackageParams() {
        val packageParams = PackageSearchParams.Builder(12)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .departure(SuggestionV4()).arrival(SuggestionV4())
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
    }

    class TestTravelerSelectViewModel(context: Context, index: Int, category: PassengerCategory) : TravelerSelectViewModel(context, index, category) {
        var testTraveler = Traveler()

        override fun getTraveler(): Traveler {
            return testTraveler
        }
    }
}