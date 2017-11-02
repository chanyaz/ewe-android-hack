package com.expedia.bookings.dagger;

import android.content.Context;
import android.location.Location;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.location.CurrentLocationSuggestionProvider;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

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
	Observable<SuggestionV4> provideMockedCurrentLocationSuggestionObservable(SuggestionV4Services service, Context context) {
		if (error != null) {
			return Observable.just(new SuggestionV4()).doOnNext(new Consumer<SuggestionV4>() {
				@Override
				public void accept(SuggestionV4 suggestion) {
					throw error;
				}
			});
		}
		else {
			return new CurrentLocationSuggestionProvider(service, Observable.just(location), context).currentLocationSuggestion();
		}
	}
}
