package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.View
import android.view.ViewStub
import android.widget.Button
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TravelerName
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
import com.mobiata.android.util.SettingUtils
import junit.framework.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates.notNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightTravelerEntryWidgetTest {

    private var widget: FlightTravelerEntryWidget by notNull()
    private var testVM: FlightTravelerEntryWidgetViewModel by notNull()
    private var activity: FragmentActivity by notNull()
    private var travelerPresenter: FlightTravelersPresenter by notNull()
    private var traveler: Traveler by notNull()

    @Before
    fun setUp() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
        AbacusTestUtils.resetABTests()
        SettingUtils.save(RuntimeEnvironment.application, R.string.preference_enable_flights_frequent_flyer_number, false)
        val intent = PlaygroundActivity.createIntent(RuntimeEnvironment.application, R.layout.test_traveler_presenter)
        val styledIntent = PlaygroundActivity.addTheme(intent, R.style.V2_Theme_Packages)
        activity = Robolectric.buildActivity(PlaygroundActivity::class.java).withIntent(styledIntent).create().visible().get()
    }

    @Test
    fun testMaterialPassportCountryIsNotCountryCode() {
        givenMaterialForm(true)
        setupViewModel(0, true)

        val editBoxForDialog = widget.findViewById(R.id.passport_country_btn) as EditText
        editBoxForDialog.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        testAlert.clickOnItem(1)
        val countryName = widget.passportCountryEditBox.text.toString()
        assertEquals(countryName, widget.passportCountryEditBox.text.toString())

        widget.viewModel = testVM

        assertEquals(countryName, widget.passportCountryEditBox.text.toString())
    }

    @Test
    fun testInvalidPassportCountrySetsEmptyString() {
        givenMaterialForm(true)
        setupViewModel(0, showPassport = true)

        (widget.viewModel as FlightTravelerEntryWidgetViewModel).passportCountrySubject.onNext("2KA9$%@!@")
        assertEquals("", widget.passportCountryEditBox.text.toString())
    }

    @Test
    fun testPassportErrorMessage() {
        givenMaterialForm(false)

        assertEquals("Select a passport country", widget.passportCountrySpinner.errorMessage)
    }

    @Test
    fun testEmailErrorMessage() {
        givenMaterialForm(false)

        assertEquals("Enter a valid email address", widget.emailEntryView.emailAddress.errorContDesc)
    }

    @Test
    @Config(qualifiers="fr")
    fun testMaterialGenderNonEnglishLanguage() {
        givenMaterialForm(true)
        setupViewModel(0, true)
        widget.tsaEntryView.genderEditText?.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        testAlert.clickOnItem(0)
        val expectedGender = widget.tsaEntryView.viewModel.genderViewModel.genderSubject.value

        assertEquals(Traveler.Gender.MALE, expectedGender)
    }

    @Test
    fun testNumberOfErrorsIsCorrectPrimaryTraveler() {
        givenMaterialForm(true)
        setupViewModel(0, true)
        traveler = Traveler()
        widget.onTravelerChosen(traveler)
        widget.viewModel.validate()

        assertFalse(widget.nameEntryView.viewModel.firstNameViewModel.isValid())
        assertFalse(widget.nameEntryView.viewModel.lastNameViewModel.isValid())
        assertFalse(widget.emailEntryView.viewModel.validate())
        assertFalse(widget.phoneEntryView.viewModel.validate())
        assertFalse(widget.tsaEntryView.viewModel.genderViewModel.validate())
        assertFalse(widget.tsaEntryView.dateOfBirth.viewModel.validate())
        assertFalse((widget.viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.value)
        assertEquals(7, widget.getNumberOfInvalidFields())

        traveler.email = "fake@gmail.com"
        widget.onTravelerChosen(traveler)
        assertTrue(widget.emailEntryView.viewModel.validate())
        assertEquals(6, widget.getNumberOfInvalidFields())

        traveler.phoneNumber = "1234567890"
        widget.onTravelerChosen(traveler)
        assertTrue(widget.phoneEntryView.viewModel.validate())
        assertEquals(5, widget.getNumberOfInvalidFields())

        traveler.primaryPassportCountry = "USA"
        widget.onTravelerChosen(traveler)
        assertTrue((widget.viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.value)
        assertEquals(1, (widget.viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.values.size)
        assertEquals(4, widget.getNumberOfInvalidFields())
    }

    @Test
    fun testNumberOfErrorIsCorrectForNonPrimaryTraveler() {
        givenMaterialForm(true)
        setupViewModel(1, showPassport = false)
        traveler = Traveler()
        widget.onTravelerChosen(traveler)
        assertEquals(4, widget.getNumberOfInvalidFields())

        traveler.firstName = "primary"
        widget.onTravelerChosen(traveler)
        assertEquals(3, widget.getNumberOfInvalidFields())

        traveler.lastName = "traveler"
        widget.onTravelerChosen(traveler)
        assertEquals(2, widget.getNumberOfInvalidFields())

        traveler.gender = Traveler.Gender.MALE
        widget.onTravelerChosen(traveler)
        assertEquals(1, widget.getNumberOfInvalidFields())

        traveler.gender = Traveler.Gender.GENDER
        widget.onTravelerChosen(traveler)
        assertEquals(2, widget.getNumberOfInvalidFields())
    }

    @Test
    fun testEmailEntryView() {
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.onTravelerChosen(traveler)

        traveler.email = ""
        widget.onTravelerChosen(traveler)
        assertFalse(widget.emailEntryView.viewModel.validate())
        assertEquals("", widget.emailEntryView.emailAddress.text.toString())

        traveler.email = null
        widget.onTravelerChosen(traveler)
        assertFalse(widget.emailEntryView.viewModel.validate())
        assertEquals("", widget.emailEntryView.emailAddress.text.toString())

        traveler.email = "fake@gmail.com"
        widget.onTravelerChosen(traveler)
        assertTrue(widget.emailEntryView.viewModel.validate())
        assertEquals("fake@gmail.com", widget.emailEntryView.emailAddress.text.toString())
    }

    @Test
    fun testMaterialOnAddNewTravelerValidation() {
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.onTravelerChosen(Traveler())

        widget.nameEntryView.viewModel.validate()
        widget.emailEntryView.viewModel.validate()
        widget.phoneEntryView.viewModel.validate()

        assertFalse(widget.nameEntryView.firstName.valid)
        assertFalse(widget.nameEntryView.lastName.valid)
        assertFalse(widget.emailEntryView.emailAddress.valid)
        assertFalse(widget.phoneEntryView.phoneNumber.valid)

        widget.onAddNewTravelerSelected()
        assertTrue(widget.nameEntryView.firstName.valid)
        assertTrue(widget.nameEntryView.lastName.valid)
        assertTrue(widget.emailEntryView.emailAddress.valid)
        assertTrue(widget.phoneEntryView.phoneNumber.valid)
    }

    @Test
    fun testAdvancedOptionsContentDescription() {
        givenMaterialForm(true)
        val expandAdvancedOptionsContDesc = widget.context.resources.getString(R.string.expand_advanced_button_cont_desc)
        val collapseAdvancedOptionsContDesc = widget.context.resources.getString(R.string.collapse_advanced_button_cont_desc)
        assertEquals(expandAdvancedOptionsContDesc, widget.advancedOptionsText.contentDescription)

        widget.advancedButton.callOnClick()
        assertEquals(collapseAdvancedOptionsContDesc, widget.advancedOptionsText.contentDescription)

        widget.advancedButton.callOnClick()
        assertEquals(expandAdvancedOptionsContDesc, widget.advancedOptionsText.contentDescription)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardShowEmailAndPhoneDefaultLayout() {
        setPOS(PointOfSaleId.UNITED_STATES)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(true)
        widget.viewModel.showPhoneNumberObservable.onNext(true)

        assertEquals(R.id.edit_email_address, widget.nameEntryView.lastName.nextFocusForwardId)
        assertEquals(R.id.edit_phone_number, widget.emailEntryView.emailAddress.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardShowEmailNoPhoneDefaultLayout() {
        setPOS(PointOfSaleId.UNITED_STATES)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(true)
        widget.viewModel.showPhoneNumberObservable.onNext(false)

        assertEquals(R.id.edit_email_address, widget.nameEntryView.lastName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardShowPhoneNoEmailDefaultLayout() {
        setPOS(PointOfSaleId.UNITED_STATES)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(false)
        widget.viewModel.showPhoneNumberObservable.onNext(true)

        assertEquals(R.id.edit_phone_number, widget.nameEntryView.lastName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardNoPhoneNoEmailDefaultLayout() {
        setPOS(PointOfSaleId.UNITED_STATES)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(false)
        widget.viewModel.showPhoneNumberObservable.onNext(false)

        assertEquals(R.id.edit_birth_date_text_btn, widget.nameEntryView.lastName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardShowEmailAndPhoneReversedLayout() {
        setPOS(PointOfSaleId.JAPAN)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(true)
        widget.viewModel.showPhoneNumberObservable.onNext(true)

        assertEquals(R.id.edit_email_address, widget.nameEntryView.firstName.nextFocusForwardId)
        assertEquals(R.id.edit_phone_number, widget.emailEntryView.emailAddress.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardNoEmailShowPhonePhoneReversedLayout() {
        setPOS(PointOfSaleId.JAPAN)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(false)
        widget.viewModel.showPhoneNumberObservable.onNext(true)

        assertEquals(R.id.edit_phone_number, widget.nameEntryView.firstName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardShowEmailNoPhoneReversedLayout() {
        setPOS(PointOfSaleId.JAPAN)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(true)
        widget.viewModel.showPhoneNumberObservable.onNext(false)

        assertEquals(R.id.edit_email_address, widget.nameEntryView.firstName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testNextFocusForwardNoPhoneNoEmailReversedLayout() {
        setPOS(PointOfSaleId.JAPAN)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.viewModel.showEmailObservable.onNext(false)
        widget.viewModel.showPhoneNumberObservable.onNext(false)

        assertEquals(R.id.edit_birth_date_text_btn, widget.nameEntryView.firstName.nextFocusForwardId)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTravelerButtonHasReversedName() {
        setPOS(PointOfSaleId.JAPAN)
        givenMaterialForm(true)
        setupViewModel(0, false)
        widget.resetStoredTravelerSelection()

        val travelerButton = travelerPresenter.travelerEntryWidget.travelerButton.findViewById(R.id.select_traveler_button) as Button
        assertEquals(traveler.reversedFullName, travelerButton.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testTravelerButtonHasDefaultNameOrder() {
        setPOS(PointOfSaleId.UNITED_STATES)
        givenMaterialForm(false)
        setupViewModel(0, false)
        widget.resetStoredTravelerSelection()

        val travelerButton = travelerPresenter.travelerEntryWidget.travelerButton.findViewById(R.id.select_traveler_button) as Button
        assertEquals(traveler.fullName, travelerButton.text.toString())
    }

    @Test
    fun testGetTravelerReturnsTravelerIfEmpty() {
        Db.clear()
        givenMaterialForm(true)
        assertEquals(2, Db.getTravelers().size)
        assertEquals(12345, Db.getTravelers()[0].tuid)

        Db.resetTravelers()
        assertEquals(2, Db.getTravelers().size)

        setupViewModel(0, false)

        val testTraveler = testVM.getTraveler()
        assertNotNull(testTraveler)
        assertEquals(Traveler().tuid, testTraveler.tuid)
    }

    @Test
    fun testFrequentFlyerNumberWidgetHiddenWithoutLegsOrPlans() {
        givenMaterialForm(true, isFFNEnabled = true)
        setupViewModel(0, false)
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).flightLegsObservable.onNext(null)
        (widget.viewModel as FlightTravelerEntryWidgetViewModel).frequentFlyerPlans.onNext(null)

        assertEquals(View.GONE, widget.frequentFlyerButton?.visibility)
    }

    private fun givenMaterialForm(isMaterialForm: Boolean, isFFNEnabled: Boolean = false) {
        if (isFFNEnabled) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.EBAndroidAppFlightFrequentFlyerNumber)
            SettingUtils.save(RuntimeEnvironment.application, R.string.preference_enable_flights_frequent_flyer_number, true)
        } else if (isMaterialForm) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        }

        val viewStub = activity.findViewById(R.id.traveler_presenter_stub) as ViewStub
        travelerPresenter = viewStub.inflate() as FlightTravelersPresenter
        widget = travelerPresenter.travelerEntryWidget as FlightTravelerEntryWidget
        traveler = Traveler()
        setTravelerName()
        traveler.tuid = 12345
        Db.getTravelers().add(traveler)
        Db.getTravelers().add(traveler)
    }

    private fun setupViewModel(travelerIndex: Int, showPassport: Boolean) {
        val showPassportCountryObservable = BehaviorSubject.create<Boolean>()
        showPassportCountryObservable.onNext(showPassport)

        testVM = FlightTravelerEntryWidgetViewModel(activity.applicationContext, travelerIndex, showPassportCountryObservable,
                TravelerCheckoutStatus.CLEAN)

        widget.viewModel = testVM
    }

    private fun setPOS(pos: PointOfSaleId) {
        SettingUtils.save(activity, R.string.PointOfSaleKey, pos.id.toString())
        PointOfSale.onPointOfSaleChanged(activity)
    }

    private fun setTravelerName() : TravelerName {
        val name = TravelerName()
        traveler.firstName = "Oscar"
        traveler.middleName = "The"
        traveler.lastName = "Grouch"
        name.firstName = traveler.firstName
        name.middleName = traveler.middleName
        name.lastName = traveler.lastName
        return name
    }
}
