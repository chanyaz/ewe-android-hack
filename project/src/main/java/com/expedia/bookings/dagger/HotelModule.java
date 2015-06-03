package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class HotelModule {

	@Provides
	@HotelScope
	SuggestionServices provideHotelSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}
}

