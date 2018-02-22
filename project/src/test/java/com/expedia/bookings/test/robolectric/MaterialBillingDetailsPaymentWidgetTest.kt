package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatTextView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.extensions.getParentTextInputLayout
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.packages.MaterialBillingDetailsPaymentWidget
import com.expedia.testutils.AndroidAssert
import com.expedia.vm.PaymentViewModel
import org.joda.time.LocalDate
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class MaterialBillingDetailsPaymentWidgetTest {

    private lateinit var materialBillingDetailsPaymentWidget: MaterialBillingDetailsPaymentWidget
    private lateinit var activity: Activity

    @Before
    fun before() {
        Db.sharedInstance.clear()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        materialBillingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as MaterialBillingDetailsPaymentWidget
        materialBillingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
        materialBillingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        materialBillingDetailsPaymentWidget.viewmodel.emptyBillingInfo.onNext(Unit)
    }

    @Test
    fun testAccessibilityOnPaymentDetailScreen() {
        givenPackageTripWithVisaValidFormOfPayment()

        val expirationDate = materialBillingDetailsPaymentWidget.findViewById<View>(materialBillingDetailsPaymentWidget.creditCardNumber.nextFocusForwardId)
        Assert.assertEquals(expirationDate, materialBillingDetailsPaymentWidget.expirationDate)
        AndroidAssert.assertViewFocusabilityIsFalse(expirationDate)

        val cvvView = materialBillingDetailsPaymentWidget.findViewById<View>(expirationDate.nextFocusForwardId)
        Assert.assertEquals(cvvView, materialBillingDetailsPaymentWidget.creditCardCvv)

        val cardholderName = materialBillingDetailsPaymentWidget.findViewById<View>(cvvView.nextFocusForwardId)
        Assert.assertEquals(cardholderName, materialBillingDetailsPaymentWidget.creditCardName)

        val addressLine1 = materialBillingDetailsPaymentWidget.findViewById<View>(cardholderName.nextFocusForwardId)
        Assert.assertEquals(addressLine1, materialBillingDetailsPaymentWidget.addressLineOne)

        val city = materialBillingDetailsPaymentWidget.findViewById<View>(addressLine1.nextFocusForwardId)
        Assert.assertEquals(city, materialBillingDetailsPaymentWidget.addressCity)

        val state = materialBillingDetailsPaymentWidget.findViewById<View>(city.nextFocusForwardId)
        Assert.assertEquals(state, materialBillingDetailsPaymentWidget.addressState)

        val zip = materialBillingDetailsPaymentWidget.findViewById<View>(state.nextFocusForwardId)
        Assert.assertEquals(zip, materialBillingDetailsPaymentWidget.creditCardPostalCode)

        val country = materialBillingDetailsPaymentWidget.editCountryEditText as EditText
        AndroidAssert.assertViewFocusabilityIsFalse(country)
    }

    @Test
    fun testMaterialBillingAddressValidation() {
        val addressLayout = materialBillingDetailsPaymentWidget.addressLineOne.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(addressLayout, "Address line 1", "Address line 1")

        validateInvalidBillingInfo()
        assertErrorState(addressLayout, "Enter a valid billing address (using letters and numbers only)",
                "Address line 1, Error, Enter a valid billing address (using letters and numbers only)")

        materialBillingDetailsPaymentWidget.addressLineOne.setText("114 Sansome")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertValidState(addressLayout, "Address line 1", "Address line 1, 114 Sansome")
    }

    @Test
    fun testMaterialBillingCardValidation() {
        givenPackageTripWithVisaValidFormOfPayment()
        val creditCardLayout = materialBillingDetailsPaymentWidget.creditCardNumber.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(creditCardLayout, "Enter new Debit/Credit Card", "Enter new Debit/Credit Card")

        validateInvalidBillingInfo()
        assertErrorState(creditCardLayout, "Enter a valid card number", "Enter new Debit/Credit Card, Error, Enter a valid card number")

        materialBillingDetailsPaymentWidget.creditCardNumber.setText("4")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(creditCardLayout, "Enter a valid card number", "Enter new Debit/Credit Card, 4, Error, Enter a valid card number")

        materialBillingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertValidState(creditCardLayout, "Enter new Debit/Credit Card", "Enter new Debit/Credit Card, 4111111111111111")
    }

    @Test
    fun testMaterialBillingNameValidation() {
        val nameLayout = materialBillingDetailsPaymentWidget.creditCardName.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(nameLayout, "Cardholder name", "Cardholder name")

        validateInvalidBillingInfo()
        assertErrorState(nameLayout, "Enter name as it appears on the card", "Cardholder name, Error, Enter name as it appears on the card")

        materialBillingDetailsPaymentWidget.creditCardName.setText("Joe Bloggs")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertValidState(nameLayout, "Cardholder name", "Cardholder name, Joe Bloggs")
    }

    @Test
    fun testMaterialBillingExpirationValidation() {
        val expirationLayout = materialBillingDetailsPaymentWidget.expirationDate.getParentTextInputLayout()!!

        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(expirationLayout, "Expiration Date", " Expiration Date, Opens dialog")
        Assert.assertNotNull(expirationLayout.editText!!.compoundDrawables[2])

        validateInvalidBillingInfo()

        assertErrorState(expirationLayout, "Enter a valid month and year", "Expiration Date, Opens dialog, Error, Enter a valid month and year")
    }

    @Test
    fun testMaterialBillingStateValidation() {
        val stateLayout = materialBillingDetailsPaymentWidget.addressState.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        materialBillingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("USA")
        assertValidState(stateLayout, "State", "State")

        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        assertValidState(stateLayout, "State", "State")

        materialBillingDetailsPaymentWidget.sectionLocation.resetValidation()
        materialBillingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("CAN")
        assertValidState(stateLayout, "Province", "Province")

        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")
        assertValidState(stateLayout, "Province", "Province")

        materialBillingDetailsPaymentWidget.sectionLocation.resetValidation()
        materialBillingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("MEX")
        assertValidState(stateLayout, "County/State/Province (optional)", "County/State/Province (optional)")

        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("MEX")
        assertValidState(stateLayout, "County/State/Province (optional)", "County/State/Province (optional)")
    }

    @Test
    fun testMaterialBillingCvvValidation() {
        val cvvLayout = materialBillingDetailsPaymentWidget.creditCardCvv.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(cvvLayout, "CVV", "CVV")

        validateInvalidBillingInfo()
        assertErrorState(cvvLayout, "Enter a valid CVV number", "CVV, Error, Enter a valid CVV number")

        materialBillingDetailsPaymentWidget.creditCardCvv.setText("1")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(cvvLayout, "Enter a valid CVV number", "CVV, Error, Enter a valid CVV number")

        materialBillingDetailsPaymentWidget.creditCardCvv.setText("111")
        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertValidState(cvvLayout, "CVV", "CVV")
    }

    @Test
    fun testMaterialBillingZipValidationUsPos() {
        val postalLayout = materialBillingDetailsPaymentWidget.creditCardPostalCode.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        materialBillingDetailsPaymentWidget.sectionLocation.resetValidation(R.id.edit_address_postal_code, true)
        assertValidState(postalLayout, "Zip Code", "Zip Code")
        assertEquals(InputType.TYPE_CLASS_NUMBER, materialBillingDetailsPaymentWidget.creditCardPostalCode.inputType)

        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(postalLayout, "Enter a valid zip code", "Zip Code, Error, Enter a valid zip code")

        materialBillingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        materialBillingDetailsPaymentWidget.sectionLocation.resetValidation(R.id.edit_address_postal_code, true)
        materialBillingDetailsPaymentWidget.sectionLocation.updateMaterialPostalFields(PointOfSaleId.UNITED_STATES)
        assertValidState(postalLayout, "Zip Code", "Zip Code")
        assertEquals(InputType.TYPE_CLASS_NUMBER, materialBillingDetailsPaymentWidget.creditCardPostalCode.inputType)

        materialBillingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(postalLayout, "Enter a valid zip code", "Zip Code, Error, Enter a valid zip code")
    }

    @Test
    fun testMaterialBillingHiddenFieldsCleared() {
        materialBillingDetailsPaymentWidget.addressState.setText("CA")
        materialBillingDetailsPaymentWidget.creditCardPostalCode.setText("12345")
        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AL")
        assertTrue(materialBillingDetailsPaymentWidget.addressState.text.isEmpty())
        assertFalse(materialBillingDetailsPaymentWidget.creditCardPostalCode.text.isEmpty())
    }

    @Test
    fun testMaterialBillingHideProperFormFields() {
        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AL")
        assertFormFieldsHiddenProperly(View.GONE, View.VISIBLE)

        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AU")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.VISIBLE)

        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AF")
        assertFormFieldsHiddenProperly(View.GONE, View.GONE)

        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("HK")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.GONE)

        materialBillingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("US")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.VISIBLE)
    }

    @Test
    fun testMaterialBillingCountryDialog() {
        val countryLayout = materialBillingDetailsPaymentWidget.editCountryEditText.getParentTextInputLayout()!!
        val pointOfSale = PointOfSale.getPointOfSale().threeLetterCountryCode
        val position = materialBillingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(pointOfSale)
        val countryName = materialBillingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getItem(position)

        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(countryLayout, "Country", " Country, $countryName, Opens dialog")
        Assert.assertEquals(countryName, countryLayout.editText?.text.toString())
        Assert.assertNotNull(countryLayout.editText!!.compoundDrawables[2])

        materialBillingDetailsPaymentWidget.editCountryEditText.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        Assert.assertNotNull(testAlert)
        Assert.assertEquals("Billing Country", testAlert.title)
        testAlert.clickOnItem(0)

        Assert.assertEquals("Afghanistan", materialBillingDetailsPaymentWidget.editCountryEditText.text.toString())
        Assert.assertEquals("AFG", materialBillingDetailsPaymentWidget.sectionLocation.location.countryCode)
        assertValidState(countryLayout, "Country", " Country, Afghanistan, Opens dialog")
    }

    @Test
    fun testMaterialBillingCountryValidation() {
        val countryLayout = materialBillingDetailsPaymentWidget.editCountryEditText.getParentTextInputLayout()!!
        val testHasErrorSubscriber = TestObserver<Boolean>()
        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryErrorSubject.subscribe(testHasErrorSubscriber)
        val pointOfSale = PointOfSale.getPointOfSale().threeLetterCountryCode
        val position = materialBillingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(pointOfSale)
        val countryName = materialBillingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getItem(position)

        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        materialBillingDetailsPaymentWidget.showPaymentForm(false)

        assertValidState(countryLayout, "Country", " Country, $countryName, Opens dialog")
        Assert.assertEquals(true, testHasErrorSubscriber.values().isEmpty())
        Assert.assertEquals(countryName, countryLayout.editText?.text.toString())
        Assert.assertEquals(pointOfSale, materialBillingDetailsPaymentWidget.sectionLocation.location.countryCode)

        countryLayout.editText?.text = null
        materialBillingDetailsPaymentWidget.onDoneClicked()

        countryLayout.editText?.setText("")
        materialBillingDetailsPaymentWidget.sectionLocation.validateBillingCountrySubject.onNext(Unit)
        Assert.assertTrue(testHasErrorSubscriber.values()[testHasErrorSubscriber.values().lastIndex])
        assertErrorState(countryLayout, "Select a billing country", "Country, Opens dialog, Error, Select a billing country")
    }

    @Test
    fun testMaterialBillingMaskedCreditCardAlternatesVisibility() {
        val creditCardLayout = materialBillingDetailsPaymentWidget.creditCardNumber.getParentTextInputLayout()!!
        val maskedCreditLayout = materialBillingDetailsPaymentWidget.maskedCreditLayout as TextInputLayout

        assertInverseLayoutVisibility(visibleLayout = creditCardLayout, hiddenLayout = maskedCreditLayout)
        Assert.assertEquals("", materialBillingDetailsPaymentWidget.creditCardNumber.text.toString())

        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        materialBillingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        materialBillingDetailsPaymentWidget.showMaskedCreditCardNumber()

        assertInverseLayoutVisibility(visibleLayout = maskedCreditLayout, hiddenLayout = creditCardLayout)
        Assert.assertEquals("XXXX XXXX XXXX 1111", materialBillingDetailsPaymentWidget.maskedCreditCard.text.toString())

        materialBillingDetailsPaymentWidget.maskedCreditCard.cardNumberTextSubject.onNext("1")

        assertInverseLayoutVisibility(visibleLayout = creditCardLayout, hiddenLayout = maskedCreditLayout)
        Assert.assertEquals("1", materialBillingDetailsPaymentWidget.creditCardNumber.text.toString())

        materialBillingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        Assert.assertEquals("4111111111111111", materialBillingDetailsPaymentWidget.creditCardNumber.text.toString())
    }

    @Test
    fun testMaterialBillingZipValidationNonUsPos() {
        val postalLayout = materialBillingDetailsPaymentWidget.creditCardPostalCode.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()
        materialBillingDetailsPaymentWidget.sectionLocation.updateMaterialPostalFields(PointOfSaleId.IRELAND)
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        Assert.assertEquals(InputType.TYPE_CLASS_TEXT, materialBillingDetailsPaymentWidget.creditCardPostalCode.inputType)

        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("IRL")
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        Assert.assertEquals(InputType.TYPE_CLASS_TEXT, materialBillingDetailsPaymentWidget.creditCardPostalCode.inputType)

        materialBillingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        Assert.assertEquals(InputType.TYPE_CLASS_TEXT, materialBillingDetailsPaymentWidget.creditCardPostalCode.inputType)
    }

    @Test
    fun testMaterialBillingCityValidation() {
        val cityLayout = materialBillingDetailsPaymentWidget.addressCity.getParentTextInputLayout()!!
        materialBillingDetailsPaymentWidget.cardInfoContainer.performClick()

        assertValidState(cityLayout, "City", "City")

        validateInvalidBillingInfo()

        assertErrorState(cityLayout, "Enter a valid city", "City, Error, Enter a valid city")

        materialBillingDetailsPaymentWidget.addressCity.setText("San")
        materialBillingDetailsPaymentWidget.onDoneClicked()

        assertValidState(cityLayout, "City", "City, San")

        materialBillingDetailsPaymentWidget.addressCity.setText("")
        materialBillingDetailsPaymentWidget.onDoneClicked()

        assertErrorState(cityLayout, "Enter a valid city", "City, Error, Enter a valid city")
    }

    private fun givenPackageTripWithVisaValidFormOfPayment() {
        val packageCreateTripResponse = PackageCreateTripResponse()
        val visaFormOfPayment = ValidFormOfPayment()
        visaFormOfPayment.name = "Visa"
        packageCreateTripResponse.validFormsOfPayment = listOf(visaFormOfPayment)
        Db.getTripBucket().add(TripBucketItemPackages(packageCreateTripResponse))
    }

    private fun assertValidState(layout: TextInputLayout, hint: String?, accessbilityString: String) {
        Assert.assertFalse(layout.isErrorEnabled)
        assertNull(layout.error)
        Assert.assertEquals(hint, layout.hint)
        Assert.assertFalse(layout.isImportantForAccessibility)
        Assert.assertEquals(accessbilityString, (layout.editText as AccessibleEditText).getAccessibilityNodeInfo())
    }

    private fun validateInvalidBillingInfo() {
        val incompleteCCNumberInfo = BillingInfo(BillingDetailsTestUtils.getIncompleteCCBillingInfo())
        incompleteCCNumberInfo.location = Location()
        incompleteCCNumberInfo.expirationDate = LocalDate.now().minusMonths(1)
        incompleteCCNumberInfo.securityCode = ""
        incompleteCCNumberInfo.nameOnCard = ""
        materialBillingDetailsPaymentWidget.sectionLocation.bind(incompleteCCNumberInfo.location)
        materialBillingDetailsPaymentWidget.onDoneClicked()
    }

    private fun assertErrorState(layout: TextInputLayout, errorString: String, stringForAccessibility: String) {
        Assert.assertTrue(layout.isErrorEnabled)
        Assert.assertEquals(errorString, layout.error)
        Assert.assertFalse(layout.isImportantForAccessibility)
        Assert.assertEquals(stringForAccessibility, (layout.editText as AccessibleEditText).getAccessibilityNodeInfo())
        val errorTextView = layout.findViewById<AppCompatTextView>(R.id.textinput_error)
        Assert.assertFalse(errorTextView.isImportantForAccessibility)
    }

    private fun assertFormFieldsHiddenProperly(addressStateVisibility: Int, postalCodeVisiblity: Int) {
        Assert.assertTrue(materialBillingDetailsPaymentWidget.addressStateLayout.visibility == addressStateVisibility)
        Assert.assertTrue(materialBillingDetailsPaymentWidget.postalCodeLayout.visibility == postalCodeVisiblity)
    }

    private fun assertInverseLayoutVisibility(visibleLayout: TextInputLayout, hiddenLayout: TextInputLayout) {
        Assert.assertEquals(View.GONE, hiddenLayout.visibility)
        Assert.assertEquals(View.GONE, hiddenLayout.editText?.visibility)
        Assert.assertEquals(View.VISIBLE, visibleLayout.visibility)
        Assert.assertEquals(View.VISIBLE, visibleLayout.editText?.visibility)
    }
}
