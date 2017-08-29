package com.expedia.bookings.test.robolectric

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.enums.PassengerCategory
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Ui
import com.expedia.vm.packages.PackageCheckoutViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class PackageCheckoutViewModelTest {
    var testViewModel: PackageCheckoutViewModel by Delegates.notNull()
    var activity : Activity by Delegates.notNull()

    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    private var LOTS_MORE: Long = 100

    @Before
    fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        Ui.getApplication(activity).defaultPackageComponents()
        testViewModel = PackageCheckoutViewModel(activity.application, serviceRule.services!!)
    }

    @Test
    fun testCheckoutPriceChange() {
        val testSubscriber = TestSubscriber.create<TripResponse>()
        testViewModel.checkoutPriceChangeObservable.subscribe(testSubscriber)

        testViewModel.builder.tripId("12312")
        testViewModel.builder.expectedTotalFare("133")
        testViewModel.builder.expectedFareCurrencyCode("USD")
        testViewModel.builder.bedType("123")

        val params = PackageCheckoutParams(getBillingInfo("errorcheckoutpricechange"),
                arrayListOf(getTraveler()), "", "", "", "", "123", true)
        testViewModel.checkoutParams.onNext(params)

        testSubscriber.requestMore(LOTS_MORE)
        testSubscriber.awaitTerminalEvent(5, TimeUnit.SECONDS)

        testSubscriber.assertValueCount(1)
        val packageCheckoutResponse = testSubscriber.onNextEvents[0] as PackageCheckoutResponse
        assertEquals("$464.64", packageCheckoutResponse.oldPackageDetails.pricing.packageTotal.formattedPrice)
        assertEquals("$787.00", packageCheckoutResponse.packageDetails.pricing.packageTotal.formattedPrice)
    }

    fun getBillingInfo(file: String): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "asdasd"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111")
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf(file)
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "malcolm"
        traveler.lastName = "nguyen"
        traveler.fullName = "malcolm nguyen"
        traveler.email = "malcolmnguyen@gmail.com"
        traveler.gender = Traveler.Gender.MALE
        traveler.phoneNumber = "9163355329"
        traveler.phoneCountryCode = "1"
        traveler.passengerCategory = PassengerCategory.ADULT
        traveler.birthDate = LocalDate.now().minusYears(18)
        traveler.primaryPassportCountry = "usa"
        traveler.assistance = Traveler.AssistanceType.BLIND_WITH_GUIDE_DOG

        return traveler
    }
}
