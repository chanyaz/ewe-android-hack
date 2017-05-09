package com.expedia.bookings.test.robolectric

import android.support.v4.app.FragmentActivity
import android.view.ViewStub
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.activity.PlaygroundActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.enums.TravelerCheckoutStatus
import com.expedia.bookings.presenter.packages.FlightTravelersPresenter
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.FlightTravelerEntryWidget
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel
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
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))

class FlightTravelerEntryWidgetTest {

    private var widget: FlightTravelerEntryWidget by Delegates.notNull()
    private var testVM: FlightTravelerEntryWidgetViewModel by Delegates.notNull()
    private var activity: FragmentActivity by Delegates.notNull()
    private var travelerPresenter: FlightTravelersPresenter by Delegates.notNull()
    private var traveler: Traveler by Delegates.notNull()

    @Before
    fun setUp() {
        Ui.getApplication(RuntimeEnvironment.application).defaultTravelerComponent()
        Ui.getApplication(RuntimeEnvironment.application).defaultFlightComponents()
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
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal)
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
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal)
    }

    @Test
    fun testNumberOfErrorsIsCorrectPrimaryTraveler() {
        givenMaterialForm(true)
        setupViewModel(0, true)
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
        assertFalse((widget.viewModel as FlightTravelerEntryWidgetViewModel).passportValidSubject.value)
        assertEquals(4, widget.getNumberOfInvalidFields())
    }

    @Test
    fun testNumberOfErrorIsCorrectForNonPrimaryTraveler() {
        givenMaterialForm(true)
        setupViewModel(1, showPassport = false)
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
        widget.onTravelerChosen(traveler)

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

    private fun givenMaterialForm(isMaterialForm: Boolean) {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        val viewStub = activity.findViewById(R.id.traveler_presenter_stub) as ViewStub
        travelerPresenter = viewStub.inflate() as FlightTravelersPresenter
        widget = travelerPresenter.travelerEntryWidget as FlightTravelerEntryWidget
        traveler = Traveler()
        Db.getTravelers().add(traveler)
        Db.getTravelers().add(traveler)
    }

    private fun setupViewModel(travelerIndex: Int, showPassport: Boolean) {
        val showPassportCountryObservable = BehaviorSubject.create<Boolean>()
        showPassportCountryObservable.onNext(showPassport)

        testVM = FlightTravelerEntryWidgetViewModel(activity.applicationContext, travelerIndex, showPassportCountryObservable,
                TravelerCheckoutStatus.CLEAN)

        widget.viewModel = testVM
        assertTrue(widget.materialFormTestEnabled)
    }
}
