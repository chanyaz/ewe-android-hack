package com.expedia.bookings.location;

import android.content.Context
import android.location.Location;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.utils.ServicesUtil

import rx.Observable;

class CurrentLocationSuggestionProvider(val suggestionServices: SuggestionServices, val locationObservable: Observable<Location>, val context: Context) {
	fun currentLocationSuggestion() : Observable<SuggestionV4> {
		return locationObservable.flatMap { location ->
                val latlong = "" + location.getLatitude() + "|" + location.getLongitude();

                this@CurrentLocationSuggestionProvider.suggestionServices
                    .getNearbyLxSuggestions(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().getSiteId(), ServicesUtil.generateClientId(context))
                    .doOnNext { suggestions -> if (suggestions.size < 1) throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS) }
                    .map { suggestions -> suggestions.get(0) }
            }
	}
}
