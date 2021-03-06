package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class PackageModule {
	@Provides
	@PackageScope
	PackageServices providePackageServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new PackageServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@PackageScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(),
			logLevel);
	}
}

