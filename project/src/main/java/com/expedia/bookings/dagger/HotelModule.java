package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.HotelScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.ReviewsServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class HotelModule {
	@Provides
	@HotelScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new HotelServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	SuggestionV4Services provideHotelSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor, RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionV4Services(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}

	@Provides
	@HotelScope
	ReviewsServices provideHotelReviewsServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor,
		RestAdapter.LogLevel logLevel) {
		final String endpoint = endpointProvider.getReviewsEndpointUrl();
		return new ReviewsServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), logLevel);
	}
}
