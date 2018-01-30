package com.expedia.bookings.location

import android.content.Context
import android.location.Location
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.GaiaSuggestionRequest
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.SuggestionV4Utils
import io.reactivex.Observable

class CurrentLocationSuggestionProvider(val suggestionServices: SuggestionV4Services, val locationObservable: Observable<Location>, val context: Context) {
    fun currentLocationSuggestion(): Observable<SuggestionV4> {
        return locationObservable.flatMap { location ->
            getNearBysuggestions(location)
                    .doOnNext { suggestions -> if (suggestions.size < 1) throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS) }
                    .map { suggestions ->
                        suggestions[0]
                    }
        }
    }

    fun getNearBysuggestions(location: Location): Observable<MutableList<SuggestionV4>> {
        val request = GaiaSuggestionRequest(location.latitude, location.longitude, "distance", "lx",
                PointOfSale.getSuggestLocaleIdentifier(), PointOfSale.getPointOfSale().siteId,
                misForRealWorldEnabled = false)
        return this@CurrentLocationSuggestionProvider.suggestionServices
                .suggestNearbyGaia(request)
                .map { it ->
                    SuggestionV4Utils.convertToSuggestionV4(it)
                }
    }
}
