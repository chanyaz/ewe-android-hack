package com.expedia.bookings.test.phone.traveler

import android.support.design.widget.TextInputLayout
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.UiThreadTestRule
import android.support.test.runner.AndroidJUnit4
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.espresso.AbacusTestUtils
import com.expedia.bookings.test.rules.PlaygroundRule
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.vm.traveler.TravelerTSAViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.properties.Delegates

@RunWith(AndroidJUnit4::class)
class TSAEntryViewTest {
    var tsaEntryView: TSAEntryView by Delegates.notNull()

    @Rule @JvmField
    var uiThreadTestRule = UiThreadTestRule()

    @Rule @JvmField
    var activityTestRule = PlaygroundRule(R.layout.test_tsa_entry_view, R.style.V2_Theme_Packages)

    @Before
    fun setUp() {
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal)

        Ui.getApplication(InstrumentationRegistry.getTargetContext()).defaultTravelerComponent()
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialForm() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        uiThreadTestRule.runOnUiThread {
            val tsaEntryView = LayoutInflater.from(activityTestRule.activity)
                    .inflate(R.layout.test_tsa_entry_view, null) as TSAEntryView

            assertTrue(tsaEntryView.materialFormTestEnabled)
            val textInputLayout = tsaEntryView.findViewById(R.id.edit_birth_date_text_layout_btn) as TextInputLayout
            assertNotNull(textInputLayout)

        }
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormGender() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        uiThreadTestRule.runOnUiThread {
            val tsaEntryView = LayoutInflater.from(activityTestRule.activity)
                    .inflate(R.layout.test_tsa_entry_view, null) as TSAEntryView

            assertTrue(tsaEntryView.materialFormTestEnabled)

            val genderEditText = tsaEntryView.findViewById(R.id.edit_gender_btn) as TravelerEditText
            assertNotNull(genderEditText)

            val tsaVM = TravelerTSAViewModel(Traveler(), activityTestRule.activity)

            tsaEntryView.viewModel = tsaVM
            tsaVM.genderViewModel.errorSubject.onNext(true)
            assertEquals((genderEditText.parent.parent as TextInputLayout).error, "Select a gender")

            tsaVM.genderViewModel.errorSubject.onNext(false)
            assertEquals((genderEditText.parent.parent as TextInputLayout).error, null)
        }
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormInvalidGenderOptions() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        uiThreadTestRule.runOnUiThread {
            val tsaEntryView = LayoutInflater.from(activityTestRule.activity)
                    .inflate(R.layout.test_tsa_entry_view, null) as TSAEntryView

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
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialFormValidGenderOptions() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        uiThreadTestRule.runOnUiThread {
            val tsaEntryView = LayoutInflater.from(activityTestRule.activity)
                    .inflate(R.layout.test_tsa_entry_view, null) as TSAEntryView
            val testTraveler = Traveler()
            testTraveler.gender = Traveler.Gender.MALE
            tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)

            assertEquals(Traveler.Gender.MALE,tsaEntryView.viewModel.genderViewModel.genderSubject.value)
            assertTrue(tsaEntryView.isValidGender())
            assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())

            testTraveler.gender = Traveler.Gender.FEMALE
            tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

            assertEquals(Traveler.Gender.FEMALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
            assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
            assertTrue(tsaEntryView.isValidGender())
        }
    }

    @Test
    fun testInvalidGenderOptions() {
        tsaEntryView = activityTestRule.root as TSAEntryView
        val testTraveler = Traveler()
        testTraveler.gender = null
        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)
        }


        assertNull(tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

//        OTHER gets sent from desktop if user doesn't select a gender
        testTraveler.gender = Traveler.Gender.OTHER
        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)
        }

        assertEquals(Traveler.Gender.OTHER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())

        testTraveler.gender = Traveler.Gender.GENDER
        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)
        }

        assertEquals(Traveler.Gender.GENDER, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
        assertFalse(tsaEntryView.viewModel.genderViewModel.isValid())
        assertFalse(tsaEntryView.isValidGender())
    }

    @Test
    fun testDateOfBirth() {
        tsaEntryView = activityTestRule.root as TSAEntryView
        val testTraveler = Traveler()
        testTraveler.gender = null
        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)
        }
        Espresso.onView(ViewMatchers.withId(R.id.edit_birth_date_text_btn)).perform(ViewActions.replaceText("ㅎ"))
        Espresso.onView(ViewMatchers.withId(R.id.edit_birth_date_text_btn)).perform(ViewActions.replaceText("ㅎabkja"))
    }

    @Test
    fun testValidGenderOptions() {
        tsaEntryView = activityTestRule.root as TSAEntryView
        val testTraveler = Traveler()
        testTraveler.gender = Traveler.Gender.MALE
        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel = TravelerTSAViewModel(testTraveler, activityTestRule.activity)
        }

        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
        assertEquals(Traveler.Gender.MALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)

        testTraveler.gender = Traveler.Gender.FEMALE
        tsaEntryView.viewModel.genderViewModel.updateTravelerGender(testTraveler)

        assertTrue(tsaEntryView.viewModel.genderViewModel.isValid())
        assertEquals(Traveler.Gender.FEMALE, tsaEntryView.viewModel.genderViewModel.genderSubject.value)
    }

    @Test
    fun birthDateErrorState() {
        tsaEntryView = activityTestRule.root as TSAEntryView

        //test for accessibility content description
        assertEquals("Enter valid date of birth", tsaEntryView.dateOfBirth.errorContDesc)
    }

    @Test
    fun genderErrorState() {
        tsaEntryView = activityTestRule.root as TSAEntryView

        //test for accessibility content description
        assertEquals("Select a gender", tsaEntryView.genderSpinner?.errorMessage)
    }
}
