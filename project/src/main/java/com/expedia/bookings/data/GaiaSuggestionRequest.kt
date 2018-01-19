package com.expedia.bookings.data

import android.location.Location

data class GaiaSuggestionRequest(val location: Location, val sortType: String, val lob: String,
                       val misForRealWorldEnabled: Boolean)
