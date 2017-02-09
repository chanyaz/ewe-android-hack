package com.expedia.bookings.test.robolectric

import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.vm.traveler.TravelerPhoneViewModel
import com.mobiata.android.util.SettingUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowResourcesEB
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowResourcesEB::class, ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowAlertDialog::class))

class PhoneEntryViewTest {

    private val context = RuntimeEnvironment.application.applicationContext
    private lateinit var widget: PhoneEntryView
    private val testCodeString = "355"
    private val testCountryName = "Albania"

    @Before
    fun setup() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        SettingUtils.save(context, R.string.preference_universal_checkout_material_forms, false)

        val activity = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(context).defaultTravelerComponent()
        Ui.getApplication(context).defaultFlightComponents()
    }

    @Test
    fun testCountryCodeChangesFromDialogSelection() {
        SettingUtils.save(context, R.string.preference_universal_checkout_material_forms, true)
        widget = LayoutInflater.from(context).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val editBoxForDialog = widget.findViewById(R.id.edit_phone_number_country_code_button) as EditText
        widget.viewModel = setupViewModelWithPhone()

        assertWidgetIsCorrect(editBoxForDialog, testCodeString)

        editBoxForDialog.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertDialogIsCorrect(testAlert,
                expectedTitle = "Country",
                numOfItems = 242,
                position = widget.phoneAdapter.currentPosition)

        testAlert.clickOnItem(0)
        assertWidgetIsCorrect(editBoxForDialog, "93")
    }

    @Test
    fun testPointOfSaleCountryCodeUsedIfNoneProvided() {
        SettingUtils.save(context, R.string.preference_universal_checkout_material_forms, true)
        widget = LayoutInflater.from(context).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val editBoxForDialog = widget.findViewById(R.id.edit_phone_number_country_code_button) as EditText
        widget.viewModel = setupViewModelWithPhone()
        widget.viewModel.updatePhone(Phone())

        assertWidgetIsCorrect(editBoxForDialog, "1")
    }

    private fun setupViewModelWithPhone() : TravelerPhoneViewModel{
        val vm = TravelerPhoneViewModel(context)
        val phone = Phone()
        phone.countryCode = testCodeString
        phone.countryName = testCountryName
        vm.updatePhone(phone)

        return vm
    }

    private fun assertWidgetIsCorrect(editBoxForDialog: EditText, countryCode: String) {
        assertEquals(View.VISIBLE, editBoxForDialog.visibility)
        assertEquals(countryCode, widget.viewModel.phoneCountryCodeSubject.value)
        assertEquals("+$countryCode", editBoxForDialog.text.toString())
    }

    private fun assertDialogIsCorrect(testAlertDialog: ShadowAlertDialog, expectedTitle: String, numOfItems: Int, position: Int) {
        assertNotNull(testAlertDialog)
        assertEquals(expectedTitle, testAlertDialog.title)
        assertEquals(numOfItems, testAlertDialog.items.size)
        assertEquals(position, widget.phoneAdapter.getPositionFromName(widget.viewModel.phoneCountryNameSubject.value))
    }
}