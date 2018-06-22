package com.expedia.bookings.itin.flight.common

import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.itin.tripstore.data.CarCategory
import com.expedia.bookings.itin.tripstore.data.CarPaymentModel
import com.expedia.bookings.itin.tripstore.data.CarType
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinCar
import com.expedia.bookings.itin.tripstore.data.ItinHotel
import com.expedia.bookings.itin.tripstore.data.ItinLx
import com.expedia.bookings.itin.tripstore.extensions.firstCar
import com.expedia.bookings.itin.tripstore.extensions.firstHotel
import com.expedia.bookings.itin.tripstore.extensions.firstLx
import com.expedia.bookings.itin.tripstore.extensions.tripEndDate
import com.expedia.bookings.itin.tripstore.extensions.tripStartDate
import com.expedia.bookings.utils.JodaUtils
import org.joda.time.DateTime
import org.joda.time.Days

object ItinOmnitureUtils {

    enum class LOB {
        HOTEL,
        LX,
        CAR
    }

    //These methods are for Flights using ItinCardData
    @JvmStatic
    fun createOmnitureTrackingValues(itinCardData: ItinCardDataFlight): HashMap<String, String?> {
        val duration = calculateTripDuration(itinCardData)
        val daysUntilTrip = calculateDaysUntilTripStart(itinCardData)
        val orderAndTripNumbers = buildOrderNumberAndItinNumberString(itinCardData)
        val tripStartDate = JodaUtils.format(itinCardData.tripStartDate, "yyyy-MM-dd")
        val tripEndDate = JodaUtils.format(itinCardData.tripEndDate, "yyyy-MM-dd")
        val productString = buildFlightProductString(itinCardData)
        val valueMap = HashMap<String, String?>()
        valueMap.put("duration", duration)
        valueMap.put("daysUntilTrip", daysUntilTrip)
        valueMap.put("orderAndTripNumbers", orderAndTripNumbers)
        valueMap.put("tripStartDate", tripStartDate)
        valueMap.put("tripEndDate", tripEndDate)
        valueMap.put("productString", productString)

        return valueMap
    }

    fun calculateTripDuration(trip: ItinCardDataFlight): String? {
        if (trip.tripStartDate != null && trip.tripEndDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            val tripEndDate = trip.tripEndDate.withTimeAtStartOfDay().plusHours(2)
            if (tripStartDate == tripEndDate) {
                return "0.0"
            } else {
                return (Days.daysBetween(tripStartDate, tripEndDate).days + 1).toString()
            }
        } else {
            return null
        }
    }

    fun calculateDaysUntilTripStart(trip: ItinCardDataFlight): String? {
        val now = DateTime.now()
        if (trip.tripStartDate != null) {
            val tripStartDate = trip.tripStartDate.withTimeAtStartOfDay()
            var daysUntil = Days.daysBetween(now, tripStartDate).days
            if (daysUntil >= 1) {
                daysUntil += 1 //this accounts for today
                return daysUntil.toString()
            } else {
                return "0.0"
            }
        } else {
            return null
        }
    }

    fun buildOrderNumberAndItinNumberString(trip: ItinCardDataFlight): String {
        val travelRecordLocator = trip.orderNumber ?: "NA"
        val itinNumber = trip.tripNumber ?: "NA"
        val orderItinstring = StringBuilder()
        orderItinstring.append(travelRecordLocator)
                .append("|")
                .append(itinNumber)

        return orderItinstring.toString()
    }

    fun buildFlightProductString(trip: ItinCardDataFlight): String {
        val tripType = getTripType(trip)
        val airlineCode = trip.flightLeg.firstAirlineCode ?: ""
        val productString = StringBuilder()
        productString.append(";Flight:")
                .append(airlineCode)
                .append(":")
                .append(tripType)
                .append(";;")
        return productString.toString()
    }

    fun getTripType(trip: ItinCardDataFlight): String {
        val numLegs = trip.legCount
        if (!trip.isSplitTicket) {
            when (numLegs) {
                1 -> return "OW"
                2 -> return "RT"
                else -> return "MD"
            }
        } else {
            return "ST"
        }
    }

    //These methods are for other LOBs using new objects rather than ItinCardData

    @JvmStatic
    fun createOmnitureTrackingValuesNew(trip: Itin, lob: LOB, currentDate: DateTime = DateTime.now()): HashMap<String, String?> {
        val duration = calculateTripDurationNew(trip, lob)
        val daysUntilTrip = calculateDaysUntilTripStartNew(trip, currentDate)
        val orderAndTripNumbers = buildOrderNumberAndItinNumberStringNew(trip)
        val tripStartDate = JodaUtils.format(trip.tripStartDate(), "yyyy-MM-dd")
        val tripEndDate = JodaUtils.format(trip.tripEndDate(), "yyyy-MM-dd")
        val productString = buildLOBProductString(trip, lob)
        val valueMap = HashMap<String, String?>()
        valueMap.put("duration", duration)
        valueMap.put("daysUntilTrip", daysUntilTrip)
        valueMap.put("orderAndTripNumbers", orderAndTripNumbers)
        valueMap.put("tripStartDate", tripStartDate)
        valueMap.put("tripEndDate", tripEndDate)
        valueMap.put("productString", productString)

        return valueMap
    }

    fun calculateTripDurationNew(trip: Itin, lob: LOB): String {
        var duration = ""
        if (lob == LOB.HOTEL) {
            trip.firstHotel()?.let { hotel ->
                duration = hotel.numberOfNights!!
            }
        } else {
            val tripStartDate = trip.tripStartDate()
            val tripEndDate = trip.tripEndDate()
            if (tripStartDate != null && tripEndDate != null) {
                val tripFirstDay = tripStartDate.withTimeAtStartOfDay()
                val tripLastDay = tripEndDate.withTimeAtStartOfDay().plusHours(2)
                if (tripFirstDay == tripLastDay) {
                    duration = "0.0"
                } else {
                    duration = (Days.daysBetween(tripFirstDay, tripLastDay).days + 1).toString()
                }
            }
        }
        return duration
    }

    fun calculateDaysUntilTripStartNew(trip: Itin, currentDate: DateTime): String? {
        val startDate = trip.tripStartDate()
        if (startDate != null) {
            val tripStartDate = startDate.withTimeAtStartOfDay()
            var daysUntil = Days.daysBetween(currentDate, tripStartDate).days
            if (daysUntil >= 1) {
                daysUntil += 1 //this accounts for today
                return daysUntil.toString()
            } else {
                return "0.0"
            }
        } else {
            return null
        }
    }

    fun buildOrderNumberAndItinNumberStringNew(trip: Itin): String {
        val travelRecordLocator = trip.orderNumber ?: "NA"
        val itinNumber = trip.tripNumber ?: "NA"
        val orderItinstring = StringBuilder()
        orderItinstring.append(travelRecordLocator)
                .append("|")
                .append(itinNumber)

        return orderItinstring.toString()
    }

    fun carProductInfo(trip: ItinCar?): String {
        val vendorCode = trip?.carVendor?.code
        val carCatergoryCharacter = getCarCategoryCharacter(trip?.carCategory)
        val carTypeCharacter = getCarTypeCharacter(trip?.carType)
        val sippCode = StringBuilder()
        sippCode.append(carCatergoryCharacter).append(carTypeCharacter)

        val carProductString = StringBuilder()
        carProductString.append(vendorCode)
                .append(":")
                .append(sippCode)

        return carProductString.toString()
    }

    fun getCarCategoryCharacter(carCategory: CarCategory?): String {
        when (carCategory) {
            CarCategory.Mini -> return "M"
            CarCategory.Economy -> return "E"
            CarCategory.Compact -> return "C"
            CarCategory.Midsize -> return "I"
            CarCategory.Standard -> return "S"
            CarCategory.Fullsize -> return "F"
            CarCategory.Premium -> return "P"
            CarCategory.Luxury -> return "L"
            CarCategory.Special -> return "X"
            CarCategory.MiniElite -> return "N"
            CarCategory.EconomyElite -> return "H"
            CarCategory.CompactElite -> return "D"
            CarCategory.MidsizeElite -> return "J"
            CarCategory.StandardElite -> return "R"
            CarCategory.FullsizeElite -> return "G"
            CarCategory.PremiumElite -> return "U"
            CarCategory.LuxuryElite -> return "W"
            CarCategory.Oversize -> return "O"
        }
        return ""
    }

    fun getCarTypeCharacter(carType: CarType?): String {
        when (carType) {
            CarType.TwoDoorCar -> return "C"
            CarType.ThreeDoorCar -> return "B"
            CarType.FourDoorCar -> return "D"
            CarType.Van -> return "V"
            CarType.Wagon -> return "W"
            CarType.Limousine -> return "L"
            CarType.RecreationalVehicle -> return "R"
            CarType.Convertible -> return "T"
            CarType.SportsCar -> return "S"
            CarType.SUV -> return "F"
            CarType.PickupRegularCab -> return "P"
            CarType.OpenAirAllTerrain -> return "J"
            CarType.Special -> return "X"
            CarType.CommercialVanTruck -> return "K"
            CarType.PickupExtendedCab -> return "Q"
            CarType.SpecialOfferCar -> return "Z"
            CarType.Coupe -> return "E"
            CarType.Monospace -> return "M"
            CarType.Motorhome -> return "H"
            CarType.TwoWheelVehicle -> return "Y"
            CarType.Roadster -> return "N"
            CarType.Crossover -> return "G"
        }
        return ""
    }

    fun carPaymentModel(paymentModel: CarPaymentModel?): String {
        when (paymentModel) {
            CarPaymentModel.AGENCY_COLLECT -> return "Agency"
            CarPaymentModel.MERCHANT_COLLECT -> return "Merchant"
        }
        return ""
    }

    private data class BuildProductObject(val productLOBType: String, val productId: String, val numberOfUnits: String, val totalPrice: String, val bonusMaterial: String = "")

    fun buildLOBProductString(trip: Itin, lob: LOB): String {
        val productObject: BuildProductObject
        when (lob) {
            LOB.HOTEL -> {
                val hotel = trip.firstHotel()
                productObject = if (hotel != null) {
                    hotelProductBuilder(hotel)
                } else {
                    BuildProductObject("", "", "", "")
                }
            }
            LOB.LX -> {
                val lx = trip.firstLx()
                productObject = if (lx != null) {
                    lxProductBuilder(lx)
                } else {
                    BuildProductObject("", "", "", "")
                }
            }
            LOB.CAR -> {
                val car = trip.firstCar()
                productObject = if (car != null) {
                    carProductBuilder(car, trip)
                } else {
                    BuildProductObject("", "", "", "")
                }
            }
        }
        val productString = StringBuilder()
        productString.append(productObject.productLOBType)
                .append(productObject.productId)
                .append(";")
                .append(productObject.numberOfUnits)
                .append(";")
                .append(productObject.totalPrice)
                .append(productObject.bonusMaterial)

        return productString.toString()
    }

    private fun hotelProductBuilder(hotel: ItinHotel): BuildProductObject {
        val productLOBType = ";Hotel:"
        val productId = hotel.hotelId ?: ""
        val numberOfUnits = hotel.numberOfNights ?: ""
        val totalPrice = hotel.totalPriceDetails?.total ?: ""
        return BuildProductObject(productLOBType = productLOBType, productId = productId, numberOfUnits = numberOfUnits, totalPrice = totalPrice)
    }

    private fun lxProductBuilder(lx: ItinLx): BuildProductObject {
        val productLOBType = ";LX:"
        val productId = lx.activityId ?: ""
        val numberOfUnits = lx.travelerCount ?: ""
        val totalPrice = lx.price?.total ?: ""
        return BuildProductObject(productLOBType = productLOBType, productId = productId, numberOfUnits = numberOfUnits, totalPrice = totalPrice)
    }

    private fun carProductBuilder(car: ItinCar, trip: Itin): BuildProductObject {
        val productLOBType = ";CAR:"
        val productId = carProductInfo(car)
        val numberOfUnits = calculateTripDurationNew(trip, ItinOmnitureUtils.LOB.CAR)
        val totalPrice = car.price?.total ?: ""
        val stringBuilder = StringBuilder()
        val eVar30 = carPaymentModel(car.paymentModel)
        val carPickUpLocation = car.pickupLocation?.locationCode ?: ""
        val carDropOffLocation = car.dropOffLocation?.locationCode ?: ""
        val carPickUpDate = JodaUtils.format(trip.tripStartDate(), "yyyyMMdd")
        val carDropOffDate = JodaUtils.format(trip.tripEndDate(), "yyyyMMdd")
        stringBuilder.append(";;")
                .append("eVar30=")
                .append(eVar30)
                .append(":CAR:")
                .append(carPickUpLocation)
                .append("-")
                .append(carDropOffLocation)
                .append(":")
                .append(carPickUpDate)
                .append("-")
                .append(carDropOffDate)
        return BuildProductObject(productLOBType = productLOBType, productId = productId, numberOfUnits = numberOfUnits, totalPrice = totalPrice, bonusMaterial = stringBuilder.toString())
    }
}
