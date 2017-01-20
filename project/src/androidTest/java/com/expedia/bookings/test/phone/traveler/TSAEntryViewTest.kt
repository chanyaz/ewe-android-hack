package com.expedia.bookings.test.phone.traveler

import android.support.design.widget.TextInputLayout
import android.support.test.InstrumentationRegistry
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
import com.mobiata.android.util.SettingUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariate.CONTROL.ordinal)
        SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, false)

        Ui.getApplication(InstrumentationRegistry.getTargetContext()).defaultTravelerComponent()
    }

    @Test
    @Throws(Throwable::class)
    fun testMaterialForm() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, true)

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
        SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, true)

        uiThreadTestRule.runOnUiThread {
            val tsaEntryView = LayoutInflater.from(activityTestRule.activity)
                    .inflate(R.layout.test_tsa_entry_view, null) as TSAEntryView

            assertTrue(tsaEntryView.materialFormTestEnabled)

            val genderEditText = tsaEntryView.findViewById(R.id.edit_gender_btn) as TravelerEditText
            assertNotNull(genderEditText)

            val tsaVM = TravelerTSAViewModel(Traveler(), activityTestRule.activity)

            tsaEntryView.viewModel = tsaVM
            tsaVM.genderViewModel.errorSubject.onNext(true)
            assertEquals((genderEditText.parent as TextInputLayout).error, "Select a gender")

            tsaVM.genderViewModel.errorSubject.onNext(false)
            assertEquals((genderEditText.parent as TextInputLayout).error, null)

        }
    }

    @Test
    fun birthDateErrorState() {
        tsaEntryView = activityTestRule.root as TSAEntryView
        val tsaVM = TravelerTSAViewModel(Traveler(), activityTestRule.activity)

        uiThreadTestRule.runOnUiThread {
            tsaEntryView.viewModel = tsaVM
            tsaVM.dateOfBirthViewModel.errorSubject.onNext(true)
        }

        //test for accessibility content description
        assertEquals(tsaEntryView.dateOfBirth.errorContDesc, "Error")

    }
}
