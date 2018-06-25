package com.expedia.bookings.itin.tripstore.data

data class ItinCar(
        val uniqueID: String?,
        val carCategoryImageURL: String?,
        val carVendor: CarVendor?,
        val pickupTime: ItinTime?,
        val dropOffTime: ItinTime?,
        val dropOffLocation: CarLocation?,
        val pickupLocation: CarLocation?,
        val price: CarPrice?,
        val paymentModel: CarPaymentModel?,
        val carCategory: CarCategory?,
        val carType: CarType?,
        val carTypeLocalized: String?,
        val carTypeAttributes: CarTypeAttributes?
) : ItinLOB

data class CarVendor(
        val longName: String?,
        val localPhoneNumber: String?,
        val shortName: String?,
        val code: String?
)

data class CarLocation(
        val locationDescription: String?,
        val cityName: String?,
        val provinceStateName: String?,
        val addressLine1: String?,
        val postalCode: String?,
        val countryCode: String?,
        val latitude: Double?,
        val longitude: Double?,
        val locationCode: String?
)

data class CarPrice(
        val total: String?
)

data class CarTypeAttributes(
        val vehicleTypeLocalized: String?
)

enum class CarPaymentModel {
    AGENCY_COLLECT,
    MERCHANT_COLLECT
}

enum class CarCategory {
    Mini,
    Economy,
    Compact,
    Midsize,
    Standard,
    Fullsize,
    Premium,
    Luxury,
    Special,
    MiniElite,
    EconomyElite,
    CompactElite,
    MidsizeElite,
    StandardElite,
    FullsizeElite,
    PremiumElite,
    LuxuryElite,
    Oversize
}

enum class CarType {
    TwoDoorCar,
    ThreeDoorCar,
    FourDoorCar,
    Van,
    Wagon,
    Limousine,
    RecreationalVehicle,
    Convertible,
    SportsCar,
    SUV,
    PickupRegularCab,
    OpenAirAllTerrain,
    Special,
    CommercialVanTruck,
    PickupExtendedCab,
    SpecialOfferCar,
    Coupe,
    Monospace,
    Motorhome,
    TwoWheelVehicle,
    Roadster,
    Crossover
}
