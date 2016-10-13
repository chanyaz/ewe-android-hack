package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import butterknife.ButterKnife
import com.expedia.bookings.R
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.flights.ValidFormOfPayment
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.trips.TripBucketItemPackages
import com.expedia.bookings.data.utils.ValidFormOfPaymentUtils
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.packages.BillingDetailsPaymentWidget
import com.expedia.vm.PaymentViewModel
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
import java.util.ArrayList
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class BillingDetailsPaymentWidgetTest {
    lateinit private var billingDetailsPaymentWidget: BillingDetailsPaymentWidget
    lateinit private var activity: Activity

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
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
        info.expirationDate = LocalDate(2017, 1, 1)
        info.securityCode = "1234"

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)

        assertFalse(billingDetailsPaymentWidget.sectionBillingInfo.performValidation())
    }

    @Test
    fun testAmexSecurityCodeValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("AmericanExpress")

        val info = BillingInfo()
        info.setNumberAndDetectType("345104799171123")
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = LocalDate(2017, 1, 1)
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

        billingDetailsPaymentWidget.addressLineOne.setText("891y398y2hifjsuiahssadbajbdjhasbdhjabsdhjbasjhbhjasdbhjasbdhjabsdhjbasjhdbjbasdhjasjhdbjhasbdhjbashjdbashjbdjhasbdhjbashjdbasjhbdjhbasdbhjasbdhjabsdjh")

        assertEquals(40, billingDetailsPaymentWidget.addressLineOne.text.length)
    }

    @Test
    fun testVisaSecurityCodeValidator() {
        billingDetailsPaymentWidget.viewmodel.lineOfBusiness.onNext(LineOfBusiness.PACKAGES)
        billingDetailsPaymentWidget.cardInfoContainer.performClick()

        givenTripResponse("Visa")

        val info = BillingInfo()
        info.setNumberAndDetectType("4284306858654528")
        info.nameOnCard = "Expedia Chicago"
        info.expirationDate = LocalDate(2017, 1, 1)
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

        val location = givenLocation()
        info.location = location
        billingDetailsPaymentWidget.sectionBillingInfo.bind(info)
        assertTrue(billingDetailsPaymentWidget.isCompletelyFilled())
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

	@Test
	fun testCreditCardExclamationMark() {
		val incompleteCCNumberInfo = BillingInfo(getIncompleteCCBillingInfo())
		billingDetailsPaymentWidget.sectionBillingInfo.bind(incompleteCCNumberInfo)

		billingDetailsPaymentWidget.cardInfoContainer.performClick()
		assertNull(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2])

		billingDetailsPaymentWidget.doneClicked.onNext(Unit)
		assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)

		billingDetailsPaymentWidget.back()

		billingDetailsPaymentWidget.cardInfoContainer.performClick()
		assertEquals(R.drawable.invalid, Shadows.shadowOf(billingDetailsPaymentWidget.creditCardNumber.compoundDrawables[2]).createdFromResId)
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

}
