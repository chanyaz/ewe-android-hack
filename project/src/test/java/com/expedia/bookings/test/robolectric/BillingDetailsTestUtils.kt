package com.expedia.bookings.test.robolectric

import android.content.Context
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Location
import com.expedia.util.Optional
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

        @JvmStatic
        fun getBillingInfo(context: Context): Optional<BillingInfo> {
            val info = BillingInfo()
            info.email = "qa-ehcc@mobiata.com"
            info.firstName = "JexperCC"
            info.lastName = "MobiataTestaverde"
            info.nameOnCard = info.firstName + " " + info.lastName
            info.setNumberAndDetectType("4111111111111111", context)
            info.securityCode = "111"
            info.telephone = "4155555555"
            info.telephoneCountryCode = "1"
            info.expirationDate = LocalDate.now()

            val location = Location()
            location.streetAddress = arrayListOf("123 street", "apt 69")
            location.city = "city"
            location.stateCode = "CA"
            location.countryCode = "US"
            location.postalCode = "12334"
            info.location = location

            return Optional(info)
        }
    }
}
