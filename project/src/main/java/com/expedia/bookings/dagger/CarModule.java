package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.CarScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class CarModule {
	@Provides
	@CarScope
	CarServices provideCarServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl(true /*isSecure*/);
		return new CarServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@CarScope
	SuggestionServices provideCarSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getEssEndpointUrl(true /*isSecure*/);
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
