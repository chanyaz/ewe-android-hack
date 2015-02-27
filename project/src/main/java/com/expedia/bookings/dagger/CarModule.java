package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.dagger.tags.E3Endpoint;
import com.expedia.bookings.dagger.tags.SuggestEndpoint;
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
	@Singleton
	CarServices provideCarServices(@E3Endpoint String endpoint, OkHttpClient client, RequestInterceptor interceptor) {
		return new CarServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	SuggestionServices provideCarSuggestionServices(@SuggestEndpoint String endpoint, OkHttpClient client) {
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
