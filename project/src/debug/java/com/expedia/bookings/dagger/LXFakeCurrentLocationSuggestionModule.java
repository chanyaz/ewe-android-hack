package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.enums.LxCurrentLocationSearchErrorTestMode;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.location.LXFakeCurrentLocationSuggestionProvider;
import com.expedia.bookings.services.SuggestionServices;

import dagger.Module;
import dagger.Provides;
import rx.Observable;

@Module
public class LXFakeCurrentLocationSuggestionModule {
	private final LxCurrentLocationSearchErrorTestMode lxCurrentLocationSearchErrorTestMode;

	public LXFakeCurrentLocationSuggestionModule(LxCurrentLocationSearchErrorTestMode lxCurrentLocationSearchErrorTestMode) {
		this.lxCurrentLocationSearchErrorTestMode = lxCurrentLocationSearchErrorTestMode;
	}

	@Provides
	@LXScope
	Observable<Suggestion> provideCurrentLocationSuggestionObservable(SuggestionServices suggestionServices, Context context) {
		LXFakeCurrentLocationSuggestionProvider fakeCurrentLocationSuggestionProvider = new LXFakeCurrentLocationSuggestionProvider(suggestionServices, CurrentLocationObservable.create(context));
		fakeCurrentLocationSuggestionProvider.setTestMode(lxCurrentLocationSearchErrorTestMode);
		return fakeCurrentLocationSuggestionProvider.currentLocationSuggestion();
	}
}
