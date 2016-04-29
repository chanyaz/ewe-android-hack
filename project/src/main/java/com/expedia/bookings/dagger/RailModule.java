package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.RailScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.RailServices;

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

}

