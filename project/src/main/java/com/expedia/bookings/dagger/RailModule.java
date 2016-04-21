package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.RailScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.RailServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class RailModule {

	@Provides
	@RailScope
	RailServices provideRailServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getRailEndpointUrl();
		return new RailServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@RailScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}

