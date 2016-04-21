package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.CarScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class CarModule {
	@Provides
	@CarScope
	CarServices provideCarServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CarServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@CarScope
	SuggestionServices provideCarSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor requestInterceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, requestInterceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}
}
