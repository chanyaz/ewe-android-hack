package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.design.widget.TextInputLayout
import android.view.LayoutInflater
import butterknife.ButterKnife
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.User
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.vm.PaymentViewModel
import com.mobiata.android.util.SettingUtils
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

@RunWith(RobolectricRunner::class)
@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
class BillingDetailsPaymentWidgetTest {
    lateinit private var billingDetailsPaymentWidget: BillingDetailsPaymentWidget
    lateinit private var activity: Activity
    private var cardExpiry = DateTime.now().plusYears(1).toLocalDate()

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms)
        SettingUtils.save(activity.applicationContext, R.string.preference_universal_checkout_material_forms, false)
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        billingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
    }

    @Test
    fun testCreditCardSecurityCodeWidget() {
        assertNotNull(billingDetailsPaymentWidget)
        ButterKnife.inject(activity)
        val securityCodeInput = billingDetailsPaymentWidget.findViewById(R.id.edit_creditcard_cvv) as AccessibleEditText
        //test for accessibility content description
        securityCodeInput.getAccessibilityNodeInfo()
        assertEquals(securityCodeInput.contentDescription, " CVV")
        assertNotNull(securityCodeInput)
    }

    @Test
    fun testNoTripValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)

        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123")
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
    fun testAmexSecurityCodeValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")

        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123")
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
        info.setNumberAndDetectType("4284306858654528")
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
    fun testSecureCheckoutDisabled() {
        assertFalse("All Hotel A/B tests must be disabled for packages",
                billingDetailsPaymentWidget.isSecureToolbarBucketed())
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testIsAtLeastPartiallyFilled() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        var info = BillingInfo()
        var location = Location()

        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
        info.setNumberAndDetectType("345104799171123")
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

        val info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())
        info.setNumberAndDetectType("345104799171123")
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
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
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
    fun testChangeOfBillingCountryStateRequirement() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        Db.getTripBucket().clear(LineOfBusiness.PACKAGES)

        val info = BillingInfo()
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.setNumberAndDetectType("345104799171123")
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = cardExpiry
        info.securityCode = "1234"

        val location = givenLocation()
        info.location = location
        info.location.countryCode = "SWE"
        info.location.stateCode = ""
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)

        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())

        info.location.countryCode = "CAN"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.location.countryCode = "USA"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertFalse(billingDetailsPaymentWidget.isCompletelyFilled())

        info.location.stateCode = "CA"
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
    }

    @Test
    fun testCreditCardExclamationMark() {
        val incompleteCCNumberInfo = BillingInfo(getIncompleteCCBillingInfo())
        billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteCCNumberInfo)

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardNumber.setText("1234")
        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        billingDetailsPaymentWidget.back()

        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)
    }

    @Test
    fun testNumberOfErrorsCorrect() {
        val incompleteBillingInfo = BillingInfo(getIncompleteCCBillingInfo())
        incompleteBillingInfo.location.stateCode = ""
        incompleteBillingInfo.location.postalCode = ""
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
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
    fun testPostalCodeNotRequired() {
//        reference ExpediaPaymentPostalCodeOptionalCountries for list of countries not requiring postal code
        val incompleteBillingInfo = getCompleteBillingInfo()
        incompleteBillingInfo.location.countryCode = "SYR"
        incompleteBillingInfo.location.postalCode = ""

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteBillingInfo)
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteBillingInfo.location)

        assertTrue(billingDetailsPaymentWidget.sectionLocation.performValidation())

        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)

        assertTrue(billingDetailsPaymentWidget.sectionLocation.performValidation())
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
        givenMaterialPaymentBillingWidget()
        val creditCardLayout = billingDetailsPaymentWidget.creditCardNumber.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(creditCardLayout, "Enter new Debit/Credit Card")

        validateInvalidBillingInfo()
        assertErrorState(creditCardLayout, "Enter a valid card number")
    }

    @Test
    fun testMaterialBillingExpirationValidation() {
        givenMaterialPaymentBillingWidget()
        val expirationLayout = billingDetailsPaymentWidget.expirationDate.parent as TextInputLayout

        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(expirationLayout, "Expiration Date")

        validateInvalidBillingInfo()

        assertErrorState(expirationLayout, "Enter a valid month and year")
    }

    @Test
    fun testMaterialBillingCvvValidation() {
        givenMaterialPaymentBillingWidget()
        val cvvLayout = billingDetailsPaymentWidget.creditCardCvv.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(cvvLayout, "CVV")

        validateInvalidBillingInfo()
        assertErrorState(cvvLayout, "Enter a valid CVV number")
    }

    @Test
    fun testMaterialBillingNameValidation() {
        givenMaterialPaymentBillingWidget()
        val nameLayout = billingDetailsPaymentWidget.creditCardName.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(nameLayout, "Cardholder name")

        validateInvalidBillingInfo()
        assertErrorState(nameLayout, "Enter name as it appears on the card")
    }

    @Test
    fun testMaterialBillingAddressValidation() {
        givenMaterialPaymentBillingWidget()
        val addressLayout = billingDetailsPaymentWidget.addressLineOne.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(addressLayout, "Address line 1")

        validateInvalidBillingInfo()
        assertErrorState(addressLayout, "Enter a valid billing address (using letters and numbers only)")
    }

    @Test
    fun testMaterialBillingCityValidation() {
        givenMaterialPaymentBillingWidget()
        val cityLayout = billingDetailsPaymentWidget.addressCity.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(cityLayout, "City")

        validateInvalidBillingInfo()
        assertErrorState(cityLayout, "Enter a valid city")
    }

    @Test
    fun testMaterialBillingStateValidation() {
        givenMaterialPaymentBillingWidget()
        val stateLayout = billingDetailsPaymentWidget.addressState.parent as TextInputLayout
//        Only US & CA POS require state
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("MEX")
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(stateLayout, "County/State/Province")

        validateInvalidBillingInfo()
        assertValidState(stateLayout, "County/State/Province")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        assertValidState(stateLayout, "State")

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        assertErrorState(stateLayout, "Enter a valid state")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")
        assertValidState(stateLayout, "Province")

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        assertErrorState(stateLayout, "Enter a valid province")
    }

    @Test
    fun testMaterialBillingCountryValidation() {
        givenMaterialPaymentBillingWidget()
        val countryLayout = billingDetailsPaymentWidget.editCountryEditText?.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(countryLayout, "Country")

        validateInvalidBillingInfo()
        assertErrorState(countryLayout, "Select a billing country")
    }

    @Test
    fun testMaterialBillingCountryDialog(){
        givenMaterialPaymentBillingWidget()
        val countryLayout = billingDetailsPaymentWidget.editCountryEditText?.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(countryLayout, "Country")
        assertTrue(billingDetailsPaymentWidget.editCountryEditText?.text.isNullOrBlank())

        billingDetailsPaymentWidget.editCountryEditText?.performClick()

        val testAlert = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog())
        assertNotNull(testAlert)
        assertEquals("Billing Country", testAlert.title)
        testAlert.clickOnItem(0)

        assertEquals("Afghanistan", billingDetailsPaymentWidget.editCountryEditText?.text.toString())
        assertValidState(countryLayout, "Country")
    }

    @Test
    fun testMaterialBillingZipValidation() {
        givenMaterialPaymentBillingWidget()
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        val postalLayout = billingDetailsPaymentWidget.creditCardPostalCode.parent as TextInputLayout
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertValidState(postalLayout, "Zip Code")

        validateInvalidBillingInfo()
        assertErrorState(postalLayout, "Enter a valid zip code")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.FLIGHTS_V2)
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")
        assertValidState(postalLayout, "Zip")

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        assertErrorState(postalLayout, "Enter a valid zip code")

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("FRA")
        assertValidState(postalLayout, "Postal Code")

        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
        assertErrorState(postalLayout, "Enter a valid postal code")
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
        billingInfo.setNumberAndDetectType("4111111111111111")
        return billingInfo
    }

    private fun givenMaterialPaymentBillingWidget() {
        SettingUtils.save(activity, R.string.preference_universal_checkout_material_forms, true)
        SettingUtils.save(activity, R.string.PointOfSaleKey, PointOfSaleId.UNITED_STATES.id.toString())
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        billingDetailsPaymentWidget.viewmodel = PaymentViewModel(activity)
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
    }

    private fun assertValidState(layout: TextInputLayout, hint: String?) {
        assertFalse(layout.isErrorEnabled)
        assertNull(layout.error)
        assertEquals(hint, layout.hint)
    }

    private fun assertErrorState(layout: TextInputLayout, errorString: String) {
        assertTrue(layout.isErrorEnabled)
        assertEquals(errorString, layout.error)
    }

    private fun validateInvalidBillingInfo() {
        val incompleteCCNumberInfo = BillingInfo(getCompleteBillingInfo())
        incompleteCCNumberInfo.location = Location()
        incompleteCCNumberInfo.expirationDate = LocalDate.now().minusMonths(1)
        incompleteCCNumberInfo.securityCode = ""
        incompleteCCNumberInfo.nameOnCard = ""
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteCCNumberInfo.location)
        billingDetailsPaymentWidget.doneClicked.onNext(Unit)
    }
}
