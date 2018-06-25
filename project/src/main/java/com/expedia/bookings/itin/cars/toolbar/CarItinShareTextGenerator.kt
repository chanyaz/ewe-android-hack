package com.expedia.bookings.itin.cars.toolbar

import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.TripProducts
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.utils.IPOSInfoProvider
import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import com.expedia.bookings.itin.utils.StringSource

class CarItinShareTextGenerator(private val tripTitle: String, private val itinNumber: String, itinCar: ItinCar,
                                private val stringSource: StringSource,
                                private val posInfoProvider: IPOSInfoProvider) : ItinShareTextGenerator {

    private val vehicle = itinCar.carTypeAttributes?.vehicleTypeLocalized ?: ""
    private val pickupTime = itinCar.pickupTime?.localizedShortTime ?: ""
    private val pickupFullDate = itinCar.pickupTime?.localizedFullDate ?: ""
    private val dropOffTime = itinCar.dropOffTime?.localizedShortTime ?: ""
    private val dropOffFullDate = itinCar.dropOffTime?.localizedFullDate ?: ""
    private val pickupMediumDate = itinCar.pickupTime?.localizedMediumDate ?: ""
    private val dropOffMediumDate = itinCar.dropOffTime?.localizedMediumDate ?: ""
    private val vendor = itinCar.carVendor?.longName ?: ""
    private val localPhoneNumber = itinCar.carVendor?.localPhoneNumber ?: ""
    private val pickupAddress = itinCar.pickupLocation?.addressLine1 ?: ""
    private val pickupCity = itinCar.pickupLocation?.cityName ?: ""
    private val pickupState = itinCar.pickupLocation?.provinceStateName ?: ""
    private val pickupPostalCode = itinCar.pickupLocation?.postalCode ?: ""
    private val dropOffAddress = itinCar.dropOffLocation?.addressLine1 ?: ""
    private val dropOffCity = itinCar.dropOffLocation?.cityName ?: ""
    private val dropOffState = itinCar.dropOffLocation?.provinceStateName ?: ""
    private val dropOffPostalCode = itinCar.dropOffLocation?.postalCode ?: ""

    override fun getEmailSubject(): String {
        return stringSource.fetchWithPhrase(R.string.itin_car_share_email_subject_TEMPLATE, mapOf("reservation" to tripTitle))
    }

    override fun getEmailBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_car_share_email_body_TEMPLATE, mapOf("reservation" to tripTitle,
                "itin_number" to itinNumber, "vehicle_type" to vehicle, "pickup_date" to pickupFullDate,
                "pickup_time" to pickupTime, "drop_off_date" to dropOffFullDate, "drop_off_time" to dropOffTime,
                "phone_number" to localPhoneNumber, "vendor" to vendor, "pickup_address" to pickupAddress,
                "pickup_city" to pickupCity, "pickup_state" to pickupState, "pickup_postal_code" to pickupPostalCode,
                "drop_off_address" to dropOffAddress, "drop_off_city" to dropOffCity, "drop_off_state" to dropOffState,
                "drop_off_postal_code" to dropOffPostalCode, "brand" to BuildConfig.brand,
                "link" to posInfoProvider.getAppInfoURL()))
    }

    override fun getSmsBody(): String {
        return stringSource.fetchWithPhrase(R.string.itin_car_share_sms_body_TEMPLATE, mapOf("reservation" to tripTitle,
                "vehicle_type" to vehicle, "pickup_date" to pickupMediumDate, "pickup_time" to pickupTime,
                "drop_off_date" to dropOffMediumDate, "drop_off_time" to dropOffTime, "pickup_address" to pickupAddress,
                "pickup_city" to pickupCity, "pickup_state" to pickupState, "pickup_postal_code" to pickupPostalCode,
                "drop_off_address" to dropOffAddress, "drop_off_city" to dropOffCity, "drop_off_state" to dropOffState,
                "drop_off_postal_code" to dropOffPostalCode))
    }

    override fun getLOBTypeString(): String {
        return TripProducts.CAR.name.toLowerCase().capitalize()
    }
}
