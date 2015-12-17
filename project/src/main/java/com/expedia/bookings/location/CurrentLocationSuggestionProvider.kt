package com.expedia.bookings.location;

import android.location.Location;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;

import rx.Observable;

public class CurrentLocationSuggestionProvider(val suggestionServices: SuggestionServices, val locationObservable: Observable<Location>) {
	fun currentLocationSuggestion() : Observable<Suggestion> {
		return locationObservable.flatMap { location ->
                val latlong = "" + location.getLatitude() + "|" + location.getLongitude();

                this@CurrentLocationSuggestionProvider.suggestionServices
                    .getNearbyLxSuggestions(PointOfSale.getSuggestLocaleIdentifier(), latlong, PointOfSale.getPointOfSale().getSiteId())
                    .doOnNext { suggestions -> if (suggestions.size() < 1) throw ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS) }
                    .map { suggestions -> suggestions.get(0) }
            }
	}
}
