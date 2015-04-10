package com.expedia.bookings.location;

import java.util.List;

import android.location.Location;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.SuggestionServices;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class CurrentLocationSuggestionProvider {

	SuggestionServices suggestionServices;
	Observable<Location> locationObservable;

	public CurrentLocationSuggestionProvider(SuggestionServices suggestionServices, Observable<Location> locationObservable) {
		this.suggestionServices = suggestionServices;
		this.locationObservable = locationObservable;
	}

	public Observable<Suggestion> currentLocationSuggestion() {
		return locationObservable.flatMap(locationToSuggestions);
	}

	private final Func1<Location, Observable<Suggestion>> locationToSuggestions = new Func1<Location, Observable<Suggestion>>() {
		@Override
		public Observable<Suggestion> call(Location location) {
			String latlong = "" + location.getLatitude() + "|" + location.getLongitude();

			return CurrentLocationSuggestionProvider.this.suggestionServices
				.getNearbyLxSuggestions(PointOfSale.getSuggestLocaleIdentifier(), latlong,
					PointOfSale.getPointOfSale().getSiteId())
				.doOnNext(ENSURE_ATLEAST_ONE_SUGGESTION)
				.map(TAKE_FIRST_SUGGESTION);
		}
	};

	private static final Action1<List<Suggestion>> ENSURE_ATLEAST_ONE_SUGGESTION = new Action1<List<Suggestion>>() {
		@Override
		public void call(List<Suggestion> suggestions) {
			if (suggestions == null || suggestions.size() < 1) {
				throw new ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS);
			}
		}
	};

	private static final Func1<List<Suggestion>, Suggestion> TAKE_FIRST_SUGGESTION = new Func1<List<Suggestion>, Suggestion>() {
		@Override
		public Suggestion call(List<Suggestion> suggestions) {
			return suggestions.get(0);
		}
	};
}
