package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.design.widget.TextInputLayout
import android.support.v7.widget.AppCompatTextView
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import butterknife.ButterKnife
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.user.User
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.showNewCreditCardExpiryFormField
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.extensions.getParentTextInputLayout
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.testutils.AndroidAssert.Companion.assertViewFocusabilityIsFalse
import com.expedia.vm.PaymentViewModel
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import java.util.ArrayList
import kotlin.test.assertNull

@RunWith(RxJavaTestImmediateSchedulerRunner::class)
@Config(shadows = [(ShadowGCM::class), (ShadowUserManager::class), (ShadowAccountManagerEB::class)])
class BillingDetailsPaymentWidgetTest {

    private lateinit var billingDetailsPaymentWidget: BillingDetailsPaymentWidget
    private lateinit var activity: Activity
    private var cardExpiry = DateTime.now().plusYears(1).toLocalDate()

    @Before
    fun before() {
        Db.sharedInstance.clear()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        billingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
    }

    @Test
    fun testCreditCardSecurityCodeWidget() {
        assertNotNull(billingDetailsPaymentWidget)
        ButterKnife.inject(activity)
        val securityCodeInput = billingDetailsPaymentWidget.findViewById<AccessibleEditText>(R.id.edit_creditcard_cvv)
        //test for accessibility content description
        securityCodeInput.getAccessibilityNodeInfo()
        assertEquals(securityCodeInput.contentDescription, "CVV")
        assertNotNull(securityCodeInput)
    }

    @Test
    fun testAccessibilityOnPaymentDetailScreen() {
        givenPackageTripWithVisaValidFormOfPayment()
        givenMaterialPaymentBillingWidget()

        val expirationDate = billingDetailsPaymentWidget.findViewById<View>(billingDetailsPaymentWidget.creditCardNumber.nextFocusForwardId)
        assertEquals(expirationDate, billingDetailsPaymentWidget.expirationDate)
        assertViewFocusabilityIsFalse(expirationDate)

        val cvvView = billingDetailsPaymentWidget.findViewById<View>(expirationDate.nextFocusForwardId)
        assertEquals(cvvView, billingDetailsPaymentWidget.creditCardCvv)

        val cardholderName = billingDetailsPaymentWidget.findViewById<View>(cvvView.nextFocusForwardId)
        assertEquals(cardholderName, billingDetailsPaymentWidget.creditCardName)

        val addressLine1 = billingDetailsPaymentWidget.findViewById<View>(cardholderName.nextFocusForwardId)
        assertEquals(addressLine1, billingDetailsPaymentWidget.addressLineOne)

        val city = billingDetailsPaymentWidget.findViewById<View>(addressLine1.nextFocusForwardId)
        assertEquals(city, billingDetailsPaymentWidget.addressCity)

        val state = billingDetailsPaymentWidget.findViewById<View>(city.nextFocusForwardId)
        assertEquals(state, billingDetailsPaymentWidget.addressState)

        val zip = billingDetailsPaymentWidget.findViewById<View>(state.nextFocusForwardId)
        assertEquals(zip, billingDetailsPaymentWidget.creditCardPostalCode)

        val country = billingDetailsPaymentWidget.editCountryEditText as EditText
        assertViewFocusabilityIsFalse(country)
    }

    @Test
    fun testNoTripValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)

        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123", activity)
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = cardExpiry
        info.securityCode = "1234"

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)

        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testFocusValidation() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)

        assertEquals(null, billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])
        assertEquals(R.drawable.material_dropdown, Shadows.shadowOf(billingDetailsPaymentWidget.expirationDate.compoundDrawables[2]).createdFromResId)
        assertEquals(null, billingDetailsPaymentWidget.creditCardCvv.compoundDrawables[2])
        assertEquals(null, billingDetailsPaymentWidget.creditCardName.compoundDrawables[2])

        billingDetailsPaymentWidget.creditCardNumber.requestFocus()
        billingDetailsPaymentWidget.expirationDate.requestFocus()
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)
        billingDetailsPaymentWidget.creditCardCvv.requestFocus()
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.expirationDate.compoundDrawables[2]).createdFromResId)
        billingDetailsPaymentWidget.addressLineOne.requestFocus()
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardCvv.compoundDrawables[2]).createdFromResId)
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testSavePromptDisplayed() {
        UserLoginTestUtil.setupUserAndMockLogin(getUserWithStoredCard())
        billingDetailsPaymentWidget.viewmodel.userLogin.onNext(true)
        completelyFillBillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.billingInfo.storedCard = StoredCreditCard()
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.showPaymentForm(false)

        billingDetailsPaymentWidget.onDoneClicked()
        val alertDialog = ShadowAlertDialog.getLatestAlertDialog()
        val okButton = alertDialog.findViewById<Button>(android.R.id.button1)
        val cancelButton = alertDialog.findViewById<Button>(android.R.id.button2)
        val message = alertDialog.findViewById<TextView>(android.R.id.message)
        assertEquals(true, alertDialog.isShowing)
        assertEquals("Save this payment method under your ${BuildConfig.brand} account to speed up future purchases?", message.text.toString())
        assertEquals("Save", okButton.text)
        assertEquals("No Thanks", cancelButton.text)
    }

    @Test
    fun testAmexSecurityCodeValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")

        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123", activity)
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = cardExpiry
        info.securityCode = "123"

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())

        info.securityCode = "1234"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testFlexPaymentValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        val info = BillingInfo()
        info.setNumberAndDetectType("4111111111111111", activity)
        info.nameOnCard = "Test CArd"
        info.expirationDate = cardExpiry
        info.securityCode = "123"

        val location = givenLocation()
        info.location = location

        givenTripResponse("Visa Debit")
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())

        givenTripResponse("Visa Credit")
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())

        givenTripResponse("Visa Electron")
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())

        givenTripResponse("Vis")
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testAddressLimit() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")

        billingDetailsPaymentWidget.addressLineOne.setText("12345678901234567890123456789012345678901234567890")
        assertEquals(40, billingDetailsPaymentWidget.addressLineOne.text.length)

        billingDetailsPaymentWidget.addressLineTwo.setText("12345678901234567890123456789012345678901234567890")
        assertEquals(40, billingDetailsPaymentWidget.addressLineTwo.text.length)
    }

    @Test
    fun testVisaSecurityCodeValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("Visa")

        val info = BillingInfo()
        info.setNumberAndDetectType("4284306858654528", activity)
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = cardExpiry
        info.securityCode = "1234"

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())

        info.securityCode = "123"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    @RunForBrands(brands = [(MultiBrand.EXPEDIA)])
    fun testIsAtLeastPartiallyFilled() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        var info = BillingInfo()
        var location = Location()

        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        info.setNumberAndDetectType("345104799171123", activity)
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        info.nameOnCard = "Expedia Chicago"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        info.securityCode = "1234"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        location.city = "San Francisco"
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        location = Location()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        location.postalCode = "60661"
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        location = Location()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        location.stateCode = "IL"
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())

        info = BillingInfo()
        location = Location()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        location.countryCode = "USA"
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
    }

    @Test
    fun testIsCompletelyFilled() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        completelyFillBillingInfo()
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
    }

    private fun completelyFillBillingInfo() {
        val info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())
        info.setNumberAndDetectType("345104799171123", activity)
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.nameOnCard = "Expedia Chicago"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.securityCode = "1234"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.expirationDate = LocalDate.now()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
    }

    @Test
    fun testSavedPaymentOffForRail() {
        UserLoginTestUtil.setupUserAndMockLogin(getUserWithStoredCard())
        // Make sure we meet all other requirements before asserting rail
        assertTrue(billingDetailsPaymentWidget.shouldShowPaymentOptions())

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        assertFalse("Error: Should not show payment options for rail!",
                billingDetailsPaymentWidget.shouldShowPaymentOptions())
    }

    @Test
    fun testCreditCardExclamationMark() {
        val incompleteCCNumberInfo = BillingInfo(getIncompleteCCBillingInfo())
        billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteCCNumberInfo)

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

        billingDetailsPaymentWidget.onDoneClicked()
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardNumber.setText("1234")
        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

        billingDetailsPaymentWidget.onDoneClicked()
        billingDetailsPaymentWidget.back()

        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)
    }

    @Test
    fun testNumberOfErrorsCorrect() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        val incompleteBillingInfo = BillingInfo(getIncompleteCCBillingInfo())
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        incompleteBillingInfo.location.stateCode = ""
        incompleteBillingInfo.location.postalCode = ""
        billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteBillingInfo.location)

        val numSectionLocationErrors = billingDetailsPaymentWidget.sectionLocation.numberOfInvalidFields
        val numSectionBillingErrors = billingDetailsPaymentWidget.sectionBillingInfo.numberOfInvalidFields
        val totalNumberOfErrors = numSectionBillingErrors.plus(numSectionLocationErrors)

        assertEquals(1, numSectionBillingErrors)
        assertEquals(2, numSectionLocationErrors)
        assertEquals(3, totalNumberOfErrors)
    }

    @Test
    fun testPostalCodeIsRequired() {
        val incompleteBillingInfo = getCompleteBillingInfo()
        incompleteBillingInfo.location.countryCode = "USA"
        incompleteBillingInfo.location.postalCode = ""

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteBillingInfo.location)

        assertFalse(billingDetailsPaymentWidget.sectionLocation.performValidation())

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        assertFalse(billingDetailsPaymentWidget.sectionLocation.performValidation())

        incompleteBillingInfo.location.postalCode = "12345"
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteBillingInfo.location)

        assertTrue(billingDetailsPaymentWidget.sectionLocation.performValidation())
    }

    @Test
    fun testMaterialBillingCardValidation() {
        givenPackageTripWithVisaValidFormOfPayment()
        givenMaterialPaymentBillingWidget()
        val creditCardLayout = billingDetailsPaymentWidget.creditCardNumber.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(creditCardLayout, "Enter new Debit/Credit Card", "Enter new Debit/Credit Card")

        validateInvalidBillingInfo()
        assertErrorState(creditCardLayout, "Enter a valid card number", "Enter new Debit/Credit Card, Error, Enter a valid card number")

        billingDetailsPaymentWidget.creditCardNumber.setText("4")
        billingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(creditCardLayout, "Enter a valid card number", "Enter new Debit/Credit Card, 4, Error, Enter a valid card number")

        billingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        billingDetailsPaymentWidget.onDoneClicked()
        assertValidState(creditCardLayout, "Enter new Debit/Credit Card", "Enter new Debit/Credit Card, 4111111111111111")
    }

    @Test
    fun testMaterialBillingExpirationValidation() {
        givenMaterialPaymentBillingWidget()
        val expirationLayout = billingDetailsPaymentWidget.expirationDate.getParentTextInputLayout()!!

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(expirationLayout, "Expiration Date", " Expiration Date, Opens dialog")
        assertNotNull(expirationLayout.editText!!.compoundDrawables[2])

        validateInvalidBillingInfo()

        assertErrorState(expirationLayout, "Enter a valid month and year", "Expiration Date, Opens dialog, Error, Enter a valid month and year")
    }

    @Test
    fun testMaterialBillingCvvValidation() {
        givenMaterialPaymentBillingWidget()
        val cvvLayout = billingDetailsPaymentWidget.creditCardCvv.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(cvvLayout, "CVV", "CVV")

        validateInvalidBillingInfo()
        assertErrorState(cvvLayout, "Enter a valid CVV number", "CVV, Error, Enter a valid CVV number")

        billingDetailsPaymentWidget.creditCardCvv.setText("1")
        billingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(cvvLayout, "Enter a valid CVV number", "CVV, Error, Enter a valid CVV number")

        billingDetailsPaymentWidget.creditCardCvv.setText("111")
        billingDetailsPaymentWidget.onDoneClicked()
        assertValidState(cvvLayout, "CVV", "CVV")
    }

    @Test
    fun testMaterialBillingNameValidation() {
        givenMaterialPaymentBillingWidget()
        val nameLayout = billingDetailsPaymentWidget.creditCardName.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(nameLayout, "Cardholder name", "Cardholder name")

        validateInvalidBillingInfo()
        assertErrorState(nameLayout, "Enter name as it appears on the card", "Cardholder name, Error, Enter name as it appears on the card")

        billingDetailsPaymentWidget.creditCardName.setText("Joe Bloggs")
        billingDetailsPaymentWidget.onDoneClicked()
        assertValidState(nameLayout, "Cardholder name", "Cardholder name, Joe Bloggs")
    }

    @Test
    fun testMaterialBillingAddressValidation() {
        givenMaterialPaymentBillingWidget()
        val addressLayout = billingDetailsPaymentWidget.addressLineOne.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(addressLayout, "Address line 1", "Address line 1")

        validateInvalidBillingInfo()
        assertErrorState(addressLayout, "Enter a valid billing address (using letters and numbers only)",
                "Address line 1, Error, Enter a valid billing address (using letters and numbers only)")

        billingDetailsPaymentWidget.addressLineOne.setText("114 Sansome")
        billingDetailsPaymentWidget.onDoneClicked()
        assertValidState(addressLayout, "Address line 1", "Address line 1, 114 Sansome")
    }

    @Test
    fun testMaterialBillingCityValidation() {
        givenMaterialPaymentBillingWidget()
        val cityLayout = billingDetailsPaymentWidget.addressCity.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(cityLayout, "City", "City")

        validateInvalidBillingInfo()
        assertErrorState(cityLayout, "Enter a valid city", "City, Error, Enter a valid city")

        billingDetailsPaymentWidget.addressCity.setText("San")
        billingDetailsPaymentWidget.onDoneClicked()
        assertValidState(cityLayout, "City", "City, San")

        billingDetailsPaymentWidget.addressCity.setText("")
        billingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(cityLayout, "Enter a valid city", "City, Error, Enter a valid city")
    }

    @Test
    fun testMaterialBillingStateValidation() {
        givenMaterialPaymentBillingWidget()
        val stateLayout = billingDetailsPaymentWidget.addressState.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("USA")
        assertValidState(stateLayout, "State", "State")

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        assertValidState(stateLayout, "State", "State")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("CAN")
        assertValidState(stateLayout, "Province", "Province")

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")
        assertValidState(stateLayout, "Province", "Province")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("MEX")
        assertValidState(stateLayout, "County/State/Province (optional)", "County/State/Province (optional)")

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("MEX")
        assertValidState(stateLayout, "County/State/Province (optional)", "County/State/Province (optional)")
    }

    @Test
    fun testMaterialBillingCountryValidation() {
        givenMaterialPaymentBillingWidget()
        val countryLayout = billingDetailsPaymentWidget.editCountryEditText?.getParentTextInputLayout()!!
        val testHasErrorSubscriber = TestObserver<Boolean>()
        billingDetailsPaymentWidget.sectionLocation.billingCountryErrorSubject.subscribe(testHasErrorSubscriber)
        val pointOfSale = PointOfSale.getPointOfSale().threeLetterCountryCode
        val position = billingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(pointOfSale)
        val countryName = billingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getItem(position)

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.showPaymentForm(false)

        assertValidState(countryLayout, "Country", " Country, $countryName, Opens dialog")
        assertEquals(true, testHasErrorSubscriber.values().isEmpty())
        assertEquals(countryName, countryLayout.editText?.text.toString())
        assertEquals(pointOfSale, billingDetailsPaymentWidget.sectionLocation.location.countryCode)

        countryLayout.editText?.text = null
        billingDetailsPaymentWidget.onDoneClicked()

        countryLayout.editText?.setText("")
        billingDetailsPaymentWidget.sectionLocation.validateBillingCountrySubject.onNext(Unit)
        assertTrue(testHasErrorSubscriber.values()[testHasErrorSubscriber.values().lastIndex])
        assertErrorState(countryLayout, "Select a billing country", "Country, Opens dialog, Error, Select a billing country")
    }

    @Test
    fun testMaterialBillingCountryDialog() {
        givenMaterialPaymentBillingWidget()
        val countryLayout = billingDetailsPaymentWidget.editCountryEditText?.getParentTextInputLayout()!!
        val pointOfSale = PointOfSale.getPointOfSale().threeLetterCountryCode
        val position = billingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getPositionByCountryThreeLetterCode(pointOfSale)
        val countryName = billingDetailsPaymentWidget.sectionLocation.materialCountryAdapter.getItem(position)

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(countryLayout, "Country", " Country, $countryName, Opens dialog")
        assertEquals(countryName, countryLayout.editText?.text.toString())
        assertNotNull(countryLayout.editText!!.compoundDrawables[2])

        billingDetailsPaymentWidget.editCountryEditText?.performClick()
        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertNotNull(testAlert)
        assertEquals("Billing Country", testAlert.title)
        testAlert.clickOnItem(0)

        assertEquals("Afghanistan", billingDetailsPaymentWidget.editCountryEditText?.text.toString())
        assertEquals("AFG", billingDetailsPaymentWidget.sectionLocation.location.countryCode)
        assertValidState(countryLayout, "Country", " Country, Afghanistan, Opens dialog")
    }

    @Test
    fun testMaterialBillingZipValidationUsPos() {
        givenMaterialPaymentBillingWidget()
        val postalLayout = billingDetailsPaymentWidget.creditCardPostalCode.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        billingDetailsPaymentWidget.sectionLocation.resetValidation(R.id.edit_address_postal_code, true)
        assertValidState(postalLayout, "Zip Code", "Zip Code")
        assertEquals(InputType.TYPE_CLASS_NUMBER, billingDetailsPaymentWidget.creditCardPostalCode.inputType)

        billingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(postalLayout, "Enter a valid zip code", "Zip Code, Error, Enter a valid zip code")

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        billingDetailsPaymentWidget.sectionLocation.resetValidation(R.id.edit_address_postal_code, true)
        billingDetailsPaymentWidget.sectionLocation.updateMaterialPostalFields(PointOfSaleId.UNITED_STATES)
        assertValidState(postalLayout, "Zip Code", "Zip Code")
        assertEquals(InputType.TYPE_CLASS_NUMBER, billingDetailsPaymentWidget.creditCardPostalCode.inputType)

        billingDetailsPaymentWidget.onDoneClicked()
        assertErrorState(postalLayout, "Enter a valid zip code", "Zip Code, Error, Enter a valid zip code")
    }

    @Test
    fun testMaterialBillingZipValidationNonUsPos() {
        givenMaterialPaymentBillingWidget()
        val postalLayout = billingDetailsPaymentWidget.creditCardPostalCode.getParentTextInputLayout()!!
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.sectionLocation.updateMaterialPostalFields(PointOfSaleId.IRELAND)
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        assertEquals(InputType.TYPE_CLASS_TEXT, billingDetailsPaymentWidget.creditCardPostalCode.inputType)

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("IRL")
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        assertEquals(InputType.TYPE_CLASS_TEXT, billingDetailsPaymentWidget.creditCardPostalCode.inputType)

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")
        assertValidState(postalLayout, "Postal Code", "Postal Code")
        assertEquals(InputType.TYPE_CLASS_TEXT, billingDetailsPaymentWidget.creditCardPostalCode.inputType)
    }

    @Test
    fun testMaterialBillingMaskedCreditCardAlternatesVisibility() {
        givenMaterialPaymentBillingWidget()
        val creditCardLayout = billingDetailsPaymentWidget.creditCardNumber.getParentTextInputLayout()!!
        val maskedCreditLayout = billingDetailsPaymentWidget.maskedCreditLayout as TextInputLayout

        assertInverseLayoutVisibility(visibleLayout = creditCardLayout, hiddenLayout = maskedCreditLayout)
        assertEquals("", billingDetailsPaymentWidget.creditCardNumber.text.toString())

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        billingDetailsPaymentWidget.showMaskedCreditCardNumber()

        assertInverseLayoutVisibility(visibleLayout = maskedCreditLayout, hiddenLayout = creditCardLayout)
        assertEquals("XXXX XXXX XXXX 1111", billingDetailsPaymentWidget.maskedCreditCard.text.toString())

        billingDetailsPaymentWidget.maskedCreditCard.cardNumberTextSubject.onNext("1")

        assertInverseLayoutVisibility(visibleLayout = creditCardLayout, hiddenLayout = maskedCreditLayout)
        assertEquals("1", billingDetailsPaymentWidget.creditCardNumber.text.toString())

        billingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        assertEquals("4111111111111111", billingDetailsPaymentWidget.creditCardNumber.text.toString())
    }

    @Test
    fun testMaterialBillingHideProperFormFields() {
        givenMaterialPaymentBillingWidget()

        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AL")
        assertFormFieldsHiddenProperly(View.GONE, View.VISIBLE)

        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AU")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.VISIBLE)

        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AF")
        assertFormFieldsHiddenProperly(View.GONE, View.GONE)

        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("HK")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.GONE)

        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("US")
        assertFormFieldsHiddenProperly(View.VISIBLE, View.VISIBLE)
    }

    @Test
    fun testMaterialBillingHiddenFieldsCleared() {
        givenMaterialPaymentBillingWidget()

        billingDetailsPaymentWidget.addressState.setText("CA")
        billingDetailsPaymentWidget.creditCardPostalCode.setText("12345")
        billingDetailsPaymentWidget.viewmodel.updateBillingCountryFields.onNext("AL")
        assertTrue(billingDetailsPaymentWidget.addressState.text.isEmpty())
        assertFalse(billingDetailsPaymentWidget.creditCardPostalCode.text.isEmpty())
    }

    @Test
    fun testShouldHideBillingAddressFields() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)
        assertBillingAddressSectionHidden(shouldHide = true)
    }

    @Test
    fun testShouldNotHideBillingAddressFields() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(false)
        assertBillingAddressSectionHidden(shouldHide = false)
    }

    @Test
    fun testCreateFakeAddress() {
        givenMaterialPaymentBillingWidget()
        val testCreateFakeAddressSubscriber = TestObserver.create<Unit>()
        val testPopulateFakeBillingAddressSubscriber = TestObserver.create<Location>()
        billingDetailsPaymentWidget.viewmodel.createFakeAddressObservable.subscribe(testCreateFakeAddressSubscriber)
        billingDetailsPaymentWidget.viewmodel.populateFakeBillingAddress.subscribe(testPopulateFakeBillingAddressSubscriber)
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        assertEquals(1, testCreateFakeAddressSubscriber.valueCount())
        assertEquals(1, testPopulateFakeBillingAddressSubscriber.valueCount())

        val fakeAddress = testPopulateFakeBillingAddressSubscriber.values()[0]
        assertValidFakeAddress(fakeAddress)
    }

    @Test
    fun testShouldNotCreateFakeAddress() {
        givenMaterialPaymentBillingWidget()
        val testCreateFakeAddressSubscriber = TestObserver.create<Unit>()
        billingDetailsPaymentWidget.viewmodel.createFakeAddressObservable.subscribe(testCreateFakeAddressSubscriber)
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(false)

        assertEquals(0, testCreateFakeAddressSubscriber.valueCount())
    }

    @Test
    fun testIsCompletelyFilledHiddenBillingAddress() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        billingDetailsPaymentWidget.creditCardNumber.setText("4444444444444442")
        billingDetailsPaymentWidget.creditCardName.setText("Hidden Billing")
        billingDetailsPaymentWidget.creditCardCvv.setText("111")
        billingDetailsPaymentWidget.expirationDate.setText(cardExpiry.toString())
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
    }

    @Test
    fun testShouldClearHiddenBillingAddress() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        billingDetailsPaymentWidget.viewmodel.clearHiddenBillingAddress.onNext(Unit)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
    }

    @Test
    fun testShouldNotClearBillingInformation() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        billingDetailsPaymentWidget.creditCardNumber.setText("4444444444444442")
        billingDetailsPaymentWidget.creditCardName.setText("Hidden Billing")
        billingDetailsPaymentWidget.creditCardCvv.setText("111")
        billingDetailsPaymentWidget.expirationDate.setText(cardExpiry.toString())

        billingDetailsPaymentWidget.viewmodel.clearHiddenBillingAddress.onNext(Unit)
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
    }

    @Test
    fun testAllowUnknownCardWithValidNumberValidation() {
        toggleAllowUnknownCardTypesABTest(true)
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")
        val completeBillingInfo = getIncompleteCCBillingInfo()
        // Set to an unknown card type with a valid number
        completeBillingInfo.number = "1234567812345670"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(completeBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(completeBillingInfo.location)

        assertTrue(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testDontAllowUnknownCardWithInvalidNumberValidation() {
        toggleAllowUnknownCardTypesABTest(true)
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")
        val completeBillingInfo = getIncompleteCCBillingInfo()
        // Set to an unknown card type with an invalid number
        completeBillingInfo.number = "1111111111111111"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(completeBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(completeBillingInfo.location)

        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testDontAllowUnknownCardValidation() {
        toggleAllowUnknownCardTypesABTest(false)
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")
        val completeBillingInfo = getIncompleteCCBillingInfo()
        // Set to an unknown card type with a valid number
        completeBillingInfo.number = "1234567812345670"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(completeBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(completeBillingInfo.location)

        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testExpiryDateABTestEnabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.CardExpiryDateFormField)
        assertTrue(showNewCreditCardExpiryFormField(activity))
    }

    @Test
    fun testExpiryDateABTestDefaultState() {
        assertFalse(showNewCreditCardExpiryFormField(activity))
    }

    @Test
    fun testExpiryDateABTestDisabled() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.CardExpiryDateFormField, AbacusVariant.CONTROL.value)
        assertFalse(showNewCreditCardExpiryFormField(activity))
    }

    private fun getUserWithStoredCard(): User {
        val user = User()
        user.addStoredCreditCard(getNewCard())
        val traveler = Traveler()
        traveler.email = "qa-ehcc@mobiata.com"
        user.primaryTraveler = traveler
        return user
    }

    private fun getNewCard(): StoredCreditCard {
        val card = StoredCreditCard()

        card.cardNumber = "4111111111111111"
        card.id = "stored-card-id"
        card.type = PaymentType.CARD_AMERICAN_EXPRESS
        card.description = "Visa 4111"
        card.setIsGoogleWallet(false)
        return card
    }

    private fun givenLocation(): Location {
        val location = Location()
        location.city = "San Francisco"
        location.countryCode = "USA"
        location.addStreetAddressLine("500 W Madison st")
        location.postalCode = "60661"
        location.stateCode = "IL"
        return location
    }

    private fun givenTripResponse(paymentName: String) {
        val response = PackageCreateTripResponse()
        val amexPayment = ValidFormOfPayment()
        amexPayment.name = paymentName
        val validFormsOfPayment = ArrayList<ValidFormOfPayment>()
        ValidFormOfPaymentUtils.addValidPayment(validFormsOfPayment, amexPayment)
        response.validFormsOfPayment = validFormsOfPayment
        val trip = TripBucketItemPackages(response)
        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)
        Db.getTripBucket().add(trip)
    }

    private fun getIncompleteCCBillingInfo(): BillingInfo {
        val location = getLocation()
        val billingInfo = BillingInfo()
        billingInfo.email = "qa-ehcc@mobiata.com"
        billingInfo.firstName = "JexperCC"
        billingInfo.lastName = "MobiataTestaverde"
        billingInfo.nameOnCard = "JexperCC MobiataTestaverde"
        //Incomplete number
        billingInfo.number = "411"
        billingInfo.expirationDate = LocalDate.now().plusYears(1)
        billingInfo.securityCode = "111"
        billingInfo.telephone = "4155555555"
        billingInfo.telephoneCountryCode = "1"
        billingInfo.location = location
        return billingInfo
    }

    private fun getLocation(): Location {
        val location = Location()
        location.city = "San Francisco"
        location.countryCode = "USA"
        location.description = "Cool description"
        location.addStreetAddressLine("114 Sansome St.")
        location.postalCode = "94109"
        location.stateCode = "CA"
        location.latitude = 37.7833
        location.longitude = 122.4167
        location.destinationId = "SF"
        return location
    }

    private fun getCompleteBillingInfo(): BillingInfo {
        val billingInfo = getIncompleteCCBillingInfo()
        billingInfo.setNumberAndDetectType("4111111111111111", activity)
        return billingInfo
    }

    private fun givenMaterialPaymentBillingWidget() {
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        billingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.viewmodel.emptyBillingInfo.onNext(Unit)
    }

    private fun assertValidState(layout: TextInputLayout, hint: String?, accessbilityString: String) {
        assertFalse(layout.isErrorEnabled)
        assertNull(layout.error)
        assertEquals(hint, layout.hint)
        assertFalse(layout.isImportantForAccessibility)
        assertEquals(accessbilityString, (layout.editText as AccessibleEditText).getAccessibilityNodeInfo())
    }

    private fun assertErrorState(layout: TextInputLayout, errorString: String, stringForAccessibility: String) {
        assertTrue(layout.isErrorEnabled)
        assertEquals(errorString, layout.error)
        assertFalse(layout.isImportantForAccessibility)
        assertEquals(stringForAccessibility, (layout.editText as AccessibleEditText).getAccessibilityNodeInfo())
        val errorTextView = layout.findViewById<AppCompatTextView>(R.id.textinput_error)
        assertFalse(errorTextView.isImportantForAccessibility)
    }

    private fun validateInvalidBillingInfo() {
        val incompleteCCNumberInfo = BillingInfo(getCompleteBillingInfo())
        incompleteCCNumberInfo.location = Location()
        incompleteCCNumberInfo.expirationDate = LocalDate.now().minusMonths(1)
        incompleteCCNumberInfo.securityCode = ""
        incompleteCCNumberInfo.nameOnCard = ""
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteCCNumberInfo.location)
        billingDetailsPaymentWidget.onDoneClicked()
    }

    private fun assertInverseLayoutVisibility(visibleLayout: TextInputLayout, hiddenLayout: TextInputLayout) {
        assertEquals(View.GONE, hiddenLayout.visibility)
        assertEquals(View.GONE, hiddenLayout.editText?.visibility)
        assertEquals(View.VISIBLE, visibleLayout.visibility)
        assertEquals(View.VISIBLE, visibleLayout.editText?.visibility)
    }

    private fun givenPackageTripWithVisaValidFormOfPayment() {
        val packageCreateTripResponse = PackageCreateTripResponse()
        val visaFormOfPayment = ValidFormOfPayment()
        visaFormOfPayment.name = "Visa"
        packageCreateTripResponse.validFormsOfPayment = listOf(visaFormOfPayment)
        Db.getTripBucket().add(TripBucketItemPackages(packageCreateTripResponse))
    }

    private fun assertFormFieldsHiddenProperly(addressStateVisibility: Int, postalCodeVisiblity: Int) {
        assertTrue(billingDetailsPaymentWidget.addressStateLayout?.visibility == addressStateVisibility)
        assertTrue(billingDetailsPaymentWidget.postalCodeLayout?.visibility == postalCodeVisiblity)
    }

    private fun assertBillingAddressSectionHidden(shouldHide: Boolean) {
        assertTrue(billingDetailsPaymentWidget.billingAddressTitle.visibility == if (shouldHide) View.GONE else View.VISIBLE)
        assertTrue(billingDetailsPaymentWidget.sectionLocation.visibility == if (shouldHide) View.GONE else View.VISIBLE)
    }

    private fun assertValidFakeAddress(fakeAddress: Location) {
        assertEquals("USA", fakeAddress.countryCode)
        assertEquals("Any street1", fakeAddress.streetAddressLine1)
        assertEquals("Any street2", fakeAddress.streetAddressLine2)
        assertEquals("Any city", fakeAddress.city)
        assertEquals("MA", fakeAddress.stateCode)
        assertEquals("12345", fakeAddress.postalCode)
    }

    private fun toggleAllowUnknownCardTypesABTest(toggleOn: Boolean) {
        if (toggleOn) {
            AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        } else {
            AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppAllowUnknownCardTypes)
        }
    }
}
