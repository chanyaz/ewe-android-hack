package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import org.joda.time.LocalDate

class BillingDetailsTestUtils {
    companion object {
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

        fun getIncompleteCCBillingInfo(): BillingInfo {
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
    }
}
