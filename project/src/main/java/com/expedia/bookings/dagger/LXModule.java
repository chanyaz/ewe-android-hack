package com.expedia.bookings.dagger;

import com.expedia.bookings.data.LXState;
import com.expedia.bookings.dagger.tags.LXScope;
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
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LXServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@LXScope
	SuggestionServices provideLxSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor requestInterceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, requestInterceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}
}
