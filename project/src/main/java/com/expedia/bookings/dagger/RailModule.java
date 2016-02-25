package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.RailScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.RailServices;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class RailModule {

	@Provides
	@RailScope
	RailServices provideRailServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getRailEndpointUrl();
		return new RailServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

}

