package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.CarScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class CarModule {
	@Provides
	@CarScope
	CarServices provideCarServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CarServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@CarScope
	SuggestionServices provideCarSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@CarScope
	SuggestionV4Services provideCarSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
