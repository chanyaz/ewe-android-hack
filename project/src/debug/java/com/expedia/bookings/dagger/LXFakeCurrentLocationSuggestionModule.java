package com.expedia.bookings.dagger;

import android.location.Location;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.location.CurrentLocationSuggestionProvider;
import com.expedia.bookings.services.SuggestionServices;

import dagger.Module;
import dagger.Provides;
import rx.Observable;
import rx.functions.Action1;

@Module
public class LXFakeCurrentLocationSuggestionModule {
	private ApiError error;
	private Location location;

	public LXFakeCurrentLocationSuggestionModule(ApiError error) {
		this.error = error;
	}

	public LXFakeCurrentLocationSuggestionModule(Location location) {
		this.location = location;
	}

	@Provides
	@LXScope
	Observable<Suggestion> provideMockedCurrentLocationSuggestionObservable(SuggestionServices service) {
		if (error != null) {
			return Observable.just(new Suggestion()).doOnNext(new Action1<Suggestion>() {
				@Override
				public void call(Suggestion suggestion) {
					throw error;
				}
			});
		}
		else {
			return new CurrentLocationSuggestionProvider(service, Observable.just(location)).currentLocationSuggestion();
		}
	}
}
