package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class FlightModule {
	@Provides
	@FlightScope
	FlightServices provideFlightServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new FlightServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@FlightScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}

