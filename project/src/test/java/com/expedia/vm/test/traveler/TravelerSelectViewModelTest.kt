package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.validation.TravelerValidator
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.TravelerSelectViewModel
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import javax.inject.Inject
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class TravelerSelectViewModelTest {
    lateinit var travelerValidator: TravelerValidator
        @Inject set
    lateinit var selectVM: TestTravelerSelectViewModel
    private var activity: Activity by Delegates.notNull()
    private var resources: Resources by Delegates.notNull()

    var expectedEmptyTitle: String by Delegates.notNull()
    var expectedEmptyTitleChild: String by Delegates.notNull()
    var expectedEmptyTitleInfant: String by Delegates.notNull()
    var expectedEmptySubTitle = ""
    var expectedSubTitleErrorMessage: String by Delegates.notNull()
    var expectedEmptyFont = FontCache.Font.ROBOTO_REGULAR
    var expectedDefaultColor: Int by Delegates.notNull()
    var expectedDefaultFont = FontCache.Font.ROBOTO_MEDIUM
    var expectedErrorColor: Int by Delegates.notNull()

    val testIndex = 0

    val mockTravelerProvider = MockTravelerProvider()
    lateinit var testParams: PackageSearchParams

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        expectedEmptyTitle = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengerage", "Adult")
                .format().toString()
        expectedEmptyTitleChild = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengerage", "5 year old")
                .format().toString()
        expectedEmptyTitleInfant = Phrase.from(resources.getString(R.string.checkout_edit_traveler_TEMPLATE))
                .put("travelernumber", testIndex + 1)
                .put("passengerage", "1 year old")
                .format().toString()
        expectedDefaultColor = ContextCompat.getColor(activity, R.color.traveler_default_card_text_color)
        expectedErrorColor = ContextCompat.getColor(activity, R.color.traveler_incomplete_text_color)
        expectedSubTitleErrorMessage = "Enter missing traveler details"

        Ui.getApplication(activity).defaultTravelerComponent()
        testParams = setPackageParams()
    }

    @Test
    fun testAdultTitle() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testChildTitle() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, 5)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleChild, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectViewModel(activity, testIndex, 1)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectViewModel(activity, testIndex, 1)
        selectVM.updateStatus(TravelerCheckoutStatus.CLEAN)

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyEmptyTraveler() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyNamedTraveler() {
        val travelerWithName = Traveler()
        travelerWithName.fullName = mockTravelerProvider.testFullName

        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = travelerWithName
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value,
                "Color should be the default if name field is populated")
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyPhoneNoName() {
        val travelerWithPhone= Traveler()
        travelerWithPhone.phoneNumber = mockTravelerProvider.testNumber

        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = travelerWithPhone
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value,
                "Expected that no phone shows unless there is also a name")
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyPhoneAndName() {
        val traveler = Traveler()
        traveler.fullName = mockTravelerProvider.testFullName
        traveler.phoneNumber = mockTravelerProvider.testNumber

        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = traveler
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyValidTraveler() {
        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = mockTravelerProvider.getCompleteMockTraveler()
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(mockTravelerProvider.adultBirthDate.toString("MM/dd/yyyy"), selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyBirthday() {
        val traveler = Traveler()
        traveler.fullName = mockTravelerProvider.testFullName
        traveler.phoneNumber = mockTravelerProvider.testNumber
        traveler.gender = mockTravelerProvider.testGender

        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = traveler
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusCleanBirthdayDirtyBooking() {
        val traveler = Traveler()
        traveler.fullName = mockTravelerProvider.testFullName
        traveler.phoneNumber = mockTravelerProvider.testNumber
        traveler.gender = mockTravelerProvider.testGender

        selectVM = TestTravelerSelectViewModel(activity, testIndex, -1)
        selectVM.testTraveler = traveler
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    private fun setPackageParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(SuggestionV4())
                .destination(SuggestionV4())
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
        return packageParams
    }

    class TestTravelerSelectViewModel(context: Context, index: Int, age: Int) : TravelerSelectViewModel(context, index, age) {
        var testTraveler = Traveler()

        override fun getTraveler(): Traveler {
            return testTraveler
        }
    }
}