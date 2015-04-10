package com.expedia.bookings.dagger;


import android.content.Context;

import com.expedia.bookings.data.LXState;
import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.location.CurrentLocationObservable;
import com.expedia.bookings.location.CurrentLocationSuggestionProvider;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@LXScope
	LXServices provideLXServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl(true /*isSecure*/);
		return new LXServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@LXScope
	SuggestionServices provideLxSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl(true /*isSecure*/);
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}

	@Provides
	@LXScope
	CurrentLocationSuggestionProvider provideCurrentLocationSuggestionProvider(SuggestionServices suggestionServices, Context context) {
		return new CurrentLocationSuggestionProvider(suggestionServices, CurrentLocationObservable.create(context));
	}
}
