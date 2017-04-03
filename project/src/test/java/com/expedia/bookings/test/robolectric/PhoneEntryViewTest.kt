package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.Phone
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TravelerEditText
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowUserManager::class, ShadowAccountManagerEB::class, ShadowAlertDialog::class))

class PhoneEntryViewTest {

    private val appContext = RuntimeEnvironment.application.applicationContext
    private lateinit var themedContext: Activity
    private lateinit var widget: PhoneEntryView
    private val testCodeString = "355"
    private val testCountryName = "Albania"
    private val testPhoneNumber = "0987654321"

    @Before
    fun setup() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        themedContext = Robolectric.buildActivity(android.support.v4.app.FragmentActivity::class.java).create().get()
        themedContext.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(appContext).defaultTravelerComponent()
        Ui.getApplication(appContext).defaultFlightComponents()
    }

    @Test
    fun testCountryCodeChangesFromDialogSelection() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val editBoxForDialog = widget.findViewById(R.id.material_edit_phone_number_country_code) as EditText
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
        assertEquals("93", widget.viewModel.getTravelerPhone().countryCode)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testPointOfSaleCountryCodeUsedIfNoneProvided() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val editBoxForDialog = widget.findViewById(R.id.material_edit_phone_number_country_code) as EditText
        widget.viewModel = setupViewModelWithPhone()
        widget.viewModel.updatePhone(Phone())

        assertWidgetIsCorrect(editBoxForDialog, "1")
        assertEquals("1", widget.viewModel.getTravelerPhone().countryCode)
    }

    @Test
    fun testMaterialPhoneNumberReturnsEmptyIfNull() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()
        val newPhone = Phone()
        newPhone.number = null
        widget.viewModel.updatePhone(newPhone)

        assertEquals("", phoneNumberField.text.toString())
    }

    @Test
    fun testMaterialPhoneNumberNotFormattedIfUnderSixNumbers() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()
        val newPhone = Phone()

        newPhone.number = "0"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("0", phoneNumberField.text.toString())

        newPhone.number = "09"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("09", phoneNumberField.text.toString())

        newPhone.number = "098"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098", phoneNumberField.text.toString())

        newPhone.number = "0987"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("0987", phoneNumberField.text.toString())

        newPhone.number = "09876"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("09876", phoneNumberField.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialPhoneNumberFormattedIfOverFiveNumbers() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()
        val newPhone = Phone()

        newPhone.number = "098765"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-765", phoneNumberField.text.toString())

        newPhone.number = "0987654"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-7654", phoneNumberField.text.toString())

        newPhone.number = "09876543"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-765-43", phoneNumberField.text.toString())

        newPhone.number = "098765432"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-765-432", phoneNumberField.text.toString())

        newPhone.number = "0987654321"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-765-4321", phoneNumberField.text.toString())

        newPhone.number = "098765432101234"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("098-765-432101234", phoneNumberField.text.toString())

    }

    @Test
    fun testMaterialPhoneNumberNotFormattedWhenStartsWithOneUnderSixNumbers() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()
        val newPhone = Phone()

        newPhone.number = "1"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1", phoneNumberField.text.toString())

        newPhone.number = "12"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("12", phoneNumberField.text.toString())

        newPhone.number = "123"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("123", phoneNumberField.text.toString())

        newPhone.number = "1234"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1234", phoneNumberField.text.toString())

        newPhone.number = "12345"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("12345", phoneNumberField.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialPhoneNumberFormattedWhenStartsWithOneOverFiveNumbers() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()
        val newPhone = Phone()

        newPhone.number = "123456"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1-234-56", phoneNumberField.text.toString())

        newPhone.number = "1234567"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1-234-567", phoneNumberField.text.toString())

        newPhone.number = "12345678"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1-234-5678", phoneNumberField.text.toString())

        newPhone.number = "123456789"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1-234-567-89", phoneNumberField.text.toString())

        newPhone.number = "12345678901234"
        widget.viewModel.updatePhone(newPhone)
        assertEquals("1-234-567-8901234", phoneNumberField.text.toString())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testMaterialPhoneNumberNotFormattedIfNotUsPos() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)

        SettingUtils.save(appContext, R.string.PointOfSaleKey, PointOfSaleId.MEXICO.id.toString())
        PointOfSale.onPointOfSaleChanged(appContext)

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()

        assertEquals("0987654321", phoneNumberField.text.toString())
    }

    @Test
    fun testPhoneNumberNotFormattedIfNotMaterial() {

        widget = LayoutInflater.from(themedContext).inflate(R.layout.test_phone_entry_view, null) as PhoneEntryView
        val phoneNumberField = widget.findViewById(R.id.edit_phone_number) as TravelerEditText
        widget.viewModel = setupViewModelWithPhone()

        assertEquals("0987654321", phoneNumberField.text.toString())
    }

    private fun setupViewModelWithPhone() : TravelerPhoneViewModel{
        val vm = TravelerPhoneViewModel(appContext)
        val phone = Phone()
        phone.countryCode = testCodeString
        phone.countryName = testCountryName
        phone.number = testPhoneNumber
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