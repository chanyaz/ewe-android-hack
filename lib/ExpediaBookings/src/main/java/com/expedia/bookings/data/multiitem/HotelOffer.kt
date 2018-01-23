package com.expedia.bookings.data.multiitem

data class HotelOffer(
        val id: String,
        val name: String,
        val englishName: String,
        val shortDescription: String,
        val regionId: Long,
        val address: Address?,
        val starRating: Double,
        val averageReview: Float,
        val reviewCount: Int,
        val recommendationPercentage: Float,
        val thumbnailUrl: String,
        val roomRatePlanDescription: String,
        val roomLongDescription: String,
        val ratePlanCode: String,
        val roomTypeCode: String,
        val vip: Boolean,
        val neighborhood: String,
        val roomAmenities: List<Amenity>,
        val bedTypes: List<Amenity>,
        val roomsLeft: Int,
        val referenceBasePrice: Price,
        val referenceTaxesAndFees: Price,
        val referenceTotalPrice: Price,
        val checkInDate: String,
        val checkOutDate: String,
        val nights: Int,
        val avgReferencePricePerNight: Price,
        val rateRuleId: Long,
        val promotion: Promotion?,
        val inventoryType: String,
        val mandatoryFees: MandatoryFees?,
        val memberDeal: Boolean,
        val sourceTypeRestricted: Boolean,
        val sameDayDRR: Boolean
)
