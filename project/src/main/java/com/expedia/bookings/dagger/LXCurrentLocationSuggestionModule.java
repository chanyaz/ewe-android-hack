package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.SuggestionV4;
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
	Observable<SuggestionV4> provideCurrentLocationSuggestionObservable(SuggestionServices suggestionServices, Context context) {
		return new CurrentLocationSuggestionProvider(suggestionServices, CurrentLocationObservable.create(context), context).currentLocationSuggestion();
	}
}
