package com.expedia.bookings.location;

import android.content.Context
import android.location.Location
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.SuggestionResultType
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.services.SuggestionV4Services
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.ServicesUtil
import com.expedia.bookings.utils.SuggestionV4Utils
import rx.Observable

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
        if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_gaia_current_location_suggestion)) {
            return this@CurrentLocationSuggestionProvider.suggestionServices
                    .suggestNearbyGaia(location.latitude, location.longitude, "distance",
                            "lx", PointOfSale.getSuggestLocaleIdentifier(), PointOfSale.getPointOfSale().siteId)
                    .map { it ->
                        SuggestionV4Utils.convertToSuggestionV4(it)
                    }
        } else {
            val latlong = "" + location.latitude + "|" + location.longitude;
            val type = SuggestionResultType.CITY or SuggestionResultType.MULTI_CITY or SuggestionResultType.NEIGHBORHOOD;
            return this@CurrentLocationSuggestionProvider.suggestionServices.suggestNearbyV4(
                    PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().siteId,
                    ServicesUtil.generateClient(context), type, "d", "ACTIVITIES")
        }
    }
}
