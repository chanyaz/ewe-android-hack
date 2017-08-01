package com.expedia.vm.test.traveler

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.FontCache
import com.expedia.bookings.utils.TravelerUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.ContactDetailsCompletenessStatus
import com.expedia.vm.traveler.TravelerSelectItemViewModel
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
class TravelerSelectItemViewModelTest {
    lateinit var selectVM: TestTravelerSelectItemViewModel
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
    val testAddTravelerIndex = 1

    val mockTravelerProvider = MockTravelerProvider()
    lateinit var testParams: PackageSearchParams

    @Before
    fun setUp() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        resources = activity.resources
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
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
        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testChildTitle() {
        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, 5, PassengerCategory.CHILD)
        selectVM.passportRequired.onNext(false) 

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleChild, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, 1, PassengerCategory.CHILD)
        selectVM.passportRequired.onNext(false)
        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, 1, PassengerCategory.CHILD)
        selectVM.passportRequired.onNext(false) 

        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitleInfant, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyEmptyTraveler() {
        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)
        assertEquals(ContactDetailsCompletenessStatus.DEFAULT, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
        assertEquals(expectedEmptySubTitle, selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedEmptyFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyNamedTraveler() {
        val travelerWithName = Traveler()
        travelerWithName.firstName = mockTravelerProvider.testFirstName
        travelerWithName.middleName = mockTravelerProvider.testMiddleName
        travelerWithName.lastName = mockTravelerProvider.testLastName

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
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

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.CHILD)
        selectVM.passportRequired.onNext(false) 
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

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
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
        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
        selectVM.testTraveler = mockTravelerProvider.getCompleteMockTraveler()
        Mockito.`when`((selectVM.testTraveler as Traveler).fullNameBasedOnPos)
                .thenReturn(mockTravelerProvider.testFullName)
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(mockTravelerProvider.adultBirthDate.toString("MM/dd/yyyy"), selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusDirtyValidTravelerNeedingPassport() {
        val selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(true) 
        selectVM.testTraveler = mockTravelerProvider.getCompleteMockTraveler()
        Mockito.`when`((selectVM.testTraveler as Traveler).fullNameBasedOnPos)
                .thenReturn(mockTravelerProvider.testFullName)
        mockTravelerProvider.addPassportToTraveler(selectVM.testTraveler!!)
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

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
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

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
        selectVM.testTraveler = traveler
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusNeedsPassportDirtyBooking() {
        val selectVM = TestTravelerSelectItemViewModel(activity, testIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(true) 
        selectVM.testTraveler = mockTravelerProvider.getCompleteMockTraveler()
        Mockito.`when`((selectVM.testTraveler as Traveler).fullNameBasedOnPos)
                .thenReturn(mockTravelerProvider.testFullName)
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(expectedSubTitleErrorMessage, selectVM.subtitleObservable.value)
        assertEquals(expectedErrorColor, selectVM.subtitleTextColorObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testUpdateStatusNoPhoneAddTravelers() {
        selectVM = TestTravelerSelectItemViewModel(activity, testAddTravelerIndex, -1, PassengerCategory.ADULT)
        selectVM.passportRequired.onNext(false) 
        selectVM.testTraveler = mockTravelerProvider.getCompleteMockTravelerWithoutPhone()
        Mockito.`when`((selectVM.testTraveler as Traveler).fullNameBasedOnPos)
                .thenReturn(mockTravelerProvider.testFullName)
        selectVM.travelerValidator.updateForNewSearch(testParams)
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)

        assertEquals(ContactDetailsCompletenessStatus.COMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(mockTravelerProvider.testFullName, selectVM.titleObservable.value)
        assertEquals(mockTravelerProvider.adultBirthDate.toString("MM/dd/yyyy"), selectVM.subtitleObservable.value)
        assertEquals(expectedDefaultFont, selectVM.titleFontObservable.value)
    }

    @Test
    fun testBucketedInfantTitle() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        updateTravelerAndViewModel(Traveler(), PassengerCategory.INFANT_IN_SEAT, 1)
        expectedEmptyTitle = getExpectedEmptyTitle(PassengerCategory.INFANT_IN_SEAT)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
    }

    @Test
    fun testBucketedChildTitle() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)
        updateTravelerAndViewModel(Traveler(), PassengerCategory.CHILD, 5)
        expectedEmptyTitle = getExpectedEmptyTitle(PassengerCategory.CHILD)

        assertEquals(ContactDetailsCompletenessStatus.INCOMPLETE, selectVM.iconStatusObservable.value)
        assertEquals(expectedEmptyTitle, selectVM.titleObservable.value)
    }

    private fun updateTravelerAndViewModel(traveler: Traveler, category: PassengerCategory, age: Int) {
        traveler.passengerCategory = category
        traveler.birthDate = LocalDate.now().plusYears(age)
        traveler.age = age

        selectVM = TestTravelerSelectItemViewModel(activity, testIndex, age, category)
        selectVM.passportRequired.onNext(false)
        selectVM.testTraveler = traveler
        selectVM.updateStatus(TravelerCheckoutStatus.DIRTY)
    }

    private fun getExpectedEmptyTitle(category: PassengerCategory) : String {
        val ageRangeString = Phrase.from(activity.getString(R.string.traveler_age_range_TEMPLATE))
                .put("category", category.getBucketedCategoryString(activity))
                .put("range", category.getBucketedAgeString(activity))
                .format().toString();
        return Phrase.from(resources.getString(R.string.checkout_traveler_title_TEMPLATE))
                .put("travelernumber", 1)
                .put("passengerycategory", ageRangeString)
                .format().toString()
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

    class TestTravelerSelectItemViewModel(context: Context, index: Int, age: Int, category: PassengerCategory) : TravelerSelectItemViewModel(context, index, age, category) {
        var testTraveler:Traveler? = null

        override fun getTraveler(): Traveler {
            if (testTraveler == null) testTraveler = Traveler()
            return testTraveler!!
        }

        fun updateStatus(travelerCheckoutStatus: TravelerCheckoutStatus) {
            currentStatusObservable.onNext(travelerCheckoutStatus)
            refreshStatusObservable.onNext(Unit)
        }
    }
}