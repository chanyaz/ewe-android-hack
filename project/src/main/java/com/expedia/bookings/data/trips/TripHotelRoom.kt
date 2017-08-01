package com.expedia.bookings.data.trips

data class TripHotelRoom(
        var hotelConfirmationNumber: String,
        var roomType: String,
        var bookingStatus: String,
        var primaryOccupant: PrimaryOccupant,
        var occupantSelectedRoomOptions: OccupantSelectedRoomOptions,
        var otherOccupantInfo: OtherOccupantInfo,
        var amenities: List<String>,
        var amenityIds: List<Int>
)

data class PrimaryOccupant(
        var firstName: String,
        var fullName: String,
        var email: String,
        var phone: String
)

data class OtherOccupantInfo(
        var adultCount: Int,
        var childCount: Int,
        var infantCount: Int,
        var childAndInfantCount: Int,
        var maxGuestCount: Int,
        var childAndInfantAges: List<Int>
)

data class OccupantSelectedRoomOptions(
        var bedTypeName: String,
        var defaultBedTypeName: String,
        var smokingPreference: String,
        var specialRequest: String,
        var accessibilityOptions: List<String>,
        var hasExtraBedAdult: Boolean,
        var hasExtraBedChild: Boolean,
        var hasExtraBedInfant: Boolean,
        var isSmokingPreferenceSelected: Boolean,
        var isRoomOptionsAvailable: Boolean
)