package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.location.CurrentLocationSuggestionProvider;
import com.expedia.bookings.services.SuggestionServices;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class LXCurrentLocationSuggestionModule {
	@Provides
	@LXScope
	Observable<Suggestion> provideCurrentLocationSuggestionObservable(SuggestionServices suggestionServices, Context context) {
		return new CurrentLocationSuggestionProvider(suggestionServices, CurrentLocationObservable.create(context)).currentLocationSuggestion();
	}
}
