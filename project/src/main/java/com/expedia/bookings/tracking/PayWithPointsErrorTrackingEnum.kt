package com.expedia.bookings.tracking

enum class PayWithPointsErrorTrackingEnum( val errorMessage: String) {
    UNKNOWN_ERROR("pwp:PointsConversionError_Unknown"),
    UNAUTHENTICATED_ACCESS("pwp:PointsConversionError_PointsConversionUnauthenticated"),
    TRIP_SERVICE_ERROR("pwp:PointsConversionError_TripServiceError"),
    AMOUNT_ENTERED_GREATER_THAN_TRIP_TOTAL("pwp:PointsConversionError_AmountEnteredGreaterThanTripTotal"),
    AMOUNT_ENTERED_GREATER_THAN_AVAILABLE("pwp:PointsConversionError_AmountEnteredGreaterThanAvailablePoints")
}
