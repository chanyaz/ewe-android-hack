package com.expedia.bookings.test.unit.traveler

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.testutils.RobolectricPlaygroundRule
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(RobolectricRunner::class)
class TSAEntryViewTest {
    private lateinit var tsaEntryView: TSAEntryView

    @Rule
    @JvmField
    var activityTestRule = RobolectricPlaygroundRule(R.layout.test_tsa_entry_view, R.style.V2_Theme_Packages)

    @Before
    fun before() {
        tsaEntryView = activityTestRule.findRoot()!!
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialForm() {
        val textInputLayout = tsaEntryView.findViewById<View>(R.id.edit_birth_date_text_layout_btn)
        assertNotNull(textInputLayout)
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialGenderFocusability() {
        val genderField = tsaEntryView.genderEditText as TravelerEditText
        assertViewFocusabilityIsFalse(genderField)
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialDateOfBirthFocusability() {
        val dateOfBirthField = tsaEntryView.dateOfBirth
        assertViewFocusabilityIsFalse(dateOfBirthField)
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormGender() {
        val genderEditText = tsaEntryView.findViewById<View>(R.id.edit_gender_btn) as TravelerEditText
        assertNotNull(genderEditText)

        val tsaVM = TravelerTSAViewModel(Traveler(), activityTestRule.activity)

        tsaEntryView.viewModel = tsaVM
        tsaVM.genderViewModel.errorSubject.onNext(true)
        assertEquals((genderEditText.parent.parent as TextInputLayout).error, "Select a gender")

        tsaVM.genderViewModel.errorSubject.onNext(false)
        assertEquals((genderEditText.parent.parent as TextInputLayout).error, null)
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormInvalidGenderOptions() {
        val testTraveler = Traveler()
        testTraveler.gender = null
        tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

        assertNull(tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

        testTraveler.gender = Traveler.Gender.OTHER
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertEquals(Traveler.Gender.OTHER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

        testTraveler.gender = Traveler.Gender.GENDER
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertEquals(Traveler.Gender.GENDER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormValidGenderOptions() {
        val testTraveler = Traveler()
        testTraveler.gender = Traveler.Gender.MALE
        tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

        assertEquals(Traveler.Gender.MALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertTrue(tsaEntryView.isValidGender())
        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())

        testTraveler.gender = Traveler.Gender.FEMALE
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertEquals(Traveler.Gender.FEMALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
        assertTrue(tsaEntryView.isValidGender())
    }

    @Test
    fun testInvalidGenderOptions() {
        val testTraveler = Traveler()
        testTraveler.gender = null
        tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

        assertNull(tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

        // OTHER gets sent from desktop if user doesn't select a gender
        testTraveler.gender = Traveler.Gender.OTHER
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertEquals(Traveler.Gender.OTHER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

        testTraveler.gender = Traveler.Gender.GENDER
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertEquals(Traveler.Gender.GENDER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())
    }

    @Test
    fun testDateOfBirth() {
        val testTraveler = Traveler()
        testTraveler.gender = null
        tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

        val birthDateTextButton: EditText? = tsaEntryView.findViewById(R.id.edit_birth_date_text_btn)
        assertNotNull(birthDateTextButton)
        birthDateTextButton?.setText("ㅎ")
        birthDateTextButton?.setText("ㅎabkja")
    }

    @Test
    fun testValidGenderOptions() {
        val testTraveler = Traveler()
        testTraveler.gender = Traveler.Gender.MALE
        tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
        assertEquals(Traveler.Gender.MALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)

        testTraveler.gender = Traveler.Gender.FEMALE
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
        assertEquals(Traveler.Gender.FEMALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
    }

    @Test
    fun birthDateErrorState() {
        val dateOfBirthView = tsaEntryView.findViewById<View>(R.id.edit_birth_date_text_btn) as TravelerEditText
        val tsaVM = TravelerTSAViewModel(Traveler(), activityTestRule.activity)
        tsaEntryView.viewModel = tsaVM
        tsaVM.dateOfBirthViewModel.errorSubject.onNext(true)

        assertEquals("Enter valid date of birth", (dateOfBirthView.parent.parent as TextInputLayout).error)
    }

    private fun assertViewFocusabilityIsFalse(view: View) {
        assertFalse(view.isFocusable)
        assertFalse(view.isFocusableInTouchMode)
    }
}
