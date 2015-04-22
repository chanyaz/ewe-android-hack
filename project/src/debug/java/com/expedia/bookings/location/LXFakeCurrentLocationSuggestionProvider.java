package com.expedia.bookings.location;

import java.util.List;

import android.location.Location;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.enums.LxCurrentLocationSearchErrorTestMode;
import com.expedia.bookings.services.SuggestionServices;

import rx.Observable;
import rx.functions.Action1;

public class LXFakeCurrentLocationSuggestionProvider extends CurrentLocationSuggestionProvider {

	LxCurrentLocationSearchErrorTestMode currentLocationSearchErrorTestMode;

	public LXFakeCurrentLocationSuggestionProvider(SuggestionServices suggestionServices, Observable<Location> locationObservable) {
		super(suggestionServices, locationObservable);
	}

	public void setTestMode(LxCurrentLocationSearchErrorTestMode currentLocationSearchErrorTestMode) {
		this.currentLocationSearchErrorTestMode = currentLocationSearchErrorTestMode;
	}

	@Override
	public Observable<Suggestion> currentLocationSuggestion() {
		switch (currentLocationSearchErrorTestMode) {
		case NO_CURRENT_LOCATION:
			return locationObservable.flatMap(new LocationToSuggestions(ENSURE_ATLEAST_ONE_SUGGESTION));
		case NO_SUGGESTIONS:
			return Observable.just(locationWithoutSuggestions()).flatMap(new LocationToSuggestions(THROW_SUGGESTIONS_NO_RESULTS));
		case NO_LX_ACTIVITIES:
			return Observable.just(locationWithNoActivities()).flatMap(new LocationToSuggestions(ENSURE_ATLEAST_ONE_SUGGESTION));
		}

		return null;
	}

	private Location locationWithNoActivities() {
		Location locationWithNoActivities = new Location("Jalandhar");
		locationWithNoActivities.setLatitude(31.32);
		locationWithNoActivities.setLongitude(75.57);
		return locationWithNoActivities;
	}

	private Location locationWithoutSuggestions() {
		Location locationWithoutSuggestions = new Location("PacificOcean");
		locationWithoutSuggestions.setLatitude(45);
		locationWithoutSuggestions.setLongitude(-150);
		return locationWithoutSuggestions;
	}

	private static final Action1<List<Suggestion>> THROW_SUGGESTIONS_NO_RESULTS = new Action1<List<Suggestion>>() {
		@Override
		public void call(List<Suggestion> suggestions) {
			throw new ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS);
		}
	};
}
