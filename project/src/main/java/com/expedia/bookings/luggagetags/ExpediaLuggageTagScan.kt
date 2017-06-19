package com.expedia.bookings.luggagetags

import com.google.android.gms.maps.model.LatLng
import java.util.Date

data class ExpediaLuggageTagScan(val tagID: String,
                                 val scanDateTime: Date,
                                 val scannedUserID: String,
                                 val scannedLatLong: LatLng,
                                 val ipAddress: String)