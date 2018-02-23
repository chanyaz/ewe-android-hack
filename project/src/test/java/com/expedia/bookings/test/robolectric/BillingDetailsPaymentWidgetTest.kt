package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
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

        val expirationDate = billingDetailsPaymentWidget.findViewById<View>(billingDetailsPaymentWidget.creditCardNumber.nextFocusForwardId)
        assertEquals(expirationDate, billingDetailsPaymentWidget.expirationDate)

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
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        Db.getTripBucket().clear(LineOfBusiness.RAILS)

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
        val incompleteCCNumberInfo = BillingInfo(BillingDetailsTestUtils.getIncompleteCCBillingInfo())
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
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        val incompleteBillingInfo = BillingInfo(BillingDetailsTestUtils.getIncompleteCCBillingInfo())
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
    fun testMaterialBillingExpirationValidation() {
        setupPaymentBillingWidget()
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        assertEquals(null, billingDetailsPaymentWidget.expirationDate.compoundDrawables[2])

        validateInvalidBillingInfo()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.expirationDate.compoundDrawables[2]).createdFromResId)
    }

    @Test
    fun testMaterialBillingCvvValidation() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.RAILS)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        validateInvalidBillingInfo()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardCvv.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardCvv.setText("1")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardCvv.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardCvv.setText("111")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(null, billingDetailsPaymentWidget.creditCardCvv.compoundDrawables[2])
    }

    @Test
    fun testMaterialBillingNameValidation() {
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        validateInvalidBillingInfo()
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardName.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardName.setText("Joe Bloggs")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(null, billingDetailsPaymentWidget.creditCardName.compoundDrawables[2])
    }

    @Test
    fun testMaterialBillingAddressValidation() {
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        validateInvalidBillingInfo()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.addressLineOne.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.addressLineOne.setText("114 Sansome")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(null, billingDetailsPaymentWidget.addressLineOne.compoundDrawables[2])
    }

    @Test
    fun testMaterialBillingCityValidation() {
        setupPaymentBillingWidget()
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        assertEquals(null, billingDetailsPaymentWidget.addressCity.compoundDrawables[2])

        validateInvalidBillingInfo()
        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.addressCity.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.addressCity.setText("San")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(null, billingDetailsPaymentWidget.addressCity.compoundDrawables[2])

        billingDetailsPaymentWidget.addressCity.setText("")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.addressCity.compoundDrawables[2]).createdFromResId)
    }

    @Test
    fun testMaterialBillingStateValidation() {
        billingDetailsPaymentWidget.cardInfoContainer.performClick()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("USA")

        assertEquals("State", billingDetailsPaymentWidget.addressState.hint)

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("USA")

        assertEquals("State", billingDetailsPaymentWidget.addressState.hint)

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("CAN")

        assertEquals("Province", billingDetailsPaymentWidget.addressState.hint)

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("CAN")

        assertEquals("Province", billingDetailsPaymentWidget.addressState.hint)

        billingDetailsPaymentWidget.sectionLocation.resetValidation()
        billingDetailsPaymentWidget.sectionLocation.updateStateFieldBasedOnBillingCountry("MEX")

        assertEquals("County/State/Province (optional)", billingDetailsPaymentWidget.addressState.hint)

        billingDetailsPaymentWidget.sectionLocation.billingCountryCodeSubject.onNext("MEX")

        assertEquals("County/State/Province (optional)", billingDetailsPaymentWidget.addressState.hint)
    }

    @Test
    fun testShouldHideBillingAddressFields() {
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)
        assertBillingAddressSectionHidden(shouldHide = true)
    }

    @Test
    fun testShouldNotHideBillingAddressFields() {
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(false)
        assertBillingAddressSectionHidden(shouldHide = false)
    }

    @Test
    fun testCreateFakeAddress() {
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
        val testCreateFakeAddressSubscriber = TestObserver.create<Unit>()
        billingDetailsPaymentWidget.viewmodel.createFakeAddressObservable.subscribe(testCreateFakeAddressSubscriber)
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(false)

        assertEquals(0, testCreateFakeAddressSubscriber.valueCount())
    }

    @Test
    fun testIsCompletelyFilledHiddenBillingAddress() {
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        billingDetailsPaymentWidget.creditCardNumber.setText("4444444444444442")
        billingDetailsPaymentWidget.creditCardName.setText("Hidden Billing")
        billingDetailsPaymentWidget.creditCardCvv.setText("111")
        billingDetailsPaymentWidget.expirationDate.setText(cardExpiry.toString())
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
    }

    @Test
    fun testShouldClearHiddenBillingAddress() {
        setupPaymentBillingWidget()
        billingDetailsPaymentWidget.viewmodel.removeBillingAddressForApac.onNext(true)

        billingDetailsPaymentWidget.viewmodel.clearHiddenBillingAddress.onNext(Unit)
        assertFalse(billingDetailsPaymentWidget.isAtLeastPartiallyFilled())
    }

    @Test
    fun testShouldNotClearBillingInformation() {
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
        val completeBillingInfo = BillingDetailsTestUtils.getIncompleteCCBillingInfo()
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
        val completeBillingInfo = BillingDetailsTestUtils.getIncompleteCCBillingInfo()
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
        val completeBillingInfo = BillingDetailsTestUtils.getIncompleteCCBillingInfo()
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

    @Test
    fun testMaterialBillingCardValidation() {
        givenPackageTripWithVisaValidFormOfPayment()
        setupPaymentBillingWidget()
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

        validateInvalidBillingInfo()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardNumber.setText("4")
        billingDetailsPaymentWidget.onDoneClicked()

        assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)

        billingDetailsPaymentWidget.creditCardNumber.setText("4111111111111111")
        billingDetailsPaymentWidget.onDoneClicked()

        assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])
    }

    fun testVisibilityOfExpiryTextViewWithABTestOn() {
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.CardExpiryDateFormField)
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget

        assertEquals(View.GONE, billingDetailsPaymentWidget.oldCreditExpiryTextLayout?.visibility)
        assertEquals(View.VISIBLE, billingDetailsPaymentWidget.newCreditCardExpiryTextLayout?.visibility)
    }

    @Test
    fun testVisibilityOfExpiryTxtViewWithAbTestOff() {
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.CardExpiryDateFormField, AbacusVariant.CONTROL.value)
        billingDetailsPaymentWidget = LayoutInflater.from(activity).inflate(R.layout.material_billing_details_payment_widget, null) as BillingDetailsPaymentWidget

        assertEquals(View.VISIBLE, billingDetailsPaymentWidget.expirationDate.visibility)
        assertEquals(View.GONE, billingDetailsPaymentWidget.newCreditCardExpiryTextLayout?.visibility)
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

    private fun getCompleteBillingInfo(): BillingInfo {
        val billingInfo = BillingDetailsTestUtils.getIncompleteCCBillingInfo()
        billingInfo.setNumberAndDetectType("4111111111111111", activity)
        return billingInfo
    }

    private fun setupPaymentBillingWidget() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.viewmodel.emptyBillingInfo.onNext(Unit)
    }

    private fun validateInvalidBillingInfo() {
        val incompleteCCNumberInfo = BillingInfo(BillingDetailsTestUtils.getIncompleteCCBillingInfo())
        incompleteCCNumberInfo.location = Location()
        incompleteCCNumberInfo.expirationDate = LocalDate.now().minusMonths(1)
        incompleteCCNumberInfo.securityCode = ""
        incompleteCCNumberInfo.nameOnCard = ""
        billingDetailsPaymentWidget.sectionLocation.bind(incompleteCCNumberInfo.location)
        billingDetailsPaymentWidget.onDoneClicked()
    }

    private fun givenPackageTripWithVisaValidFormOfPayment() {
        val packageCreateTripResponse = PackageCreateTripResponse()
        val visaFormOfPayment = ValidFormOfPayment()
        visaFormOfPayment.name = "Visa"
        packageCreateTripResponse.validFormsOfPayment = listOf(visaFormOfPayment)
        Db.getTripBucket().add(TripBucketItemPackages(packageCreateTripResponse))
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
