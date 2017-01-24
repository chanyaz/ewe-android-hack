package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.dagger.tags.WeatherScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.FeedsService;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.WeatherServices;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class WeatherModule {
	@Provides
	@WeatherScope
	WeatherServices provideWeatherServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = "http://www.wunderground.com/";
		return new WeatherServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
