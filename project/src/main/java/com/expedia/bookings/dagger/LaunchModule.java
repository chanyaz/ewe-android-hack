package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.SatelliteServices;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class LaunchModule {
	@Provides
	@LaunchScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new HotelServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LaunchScope
	CollectionServices provideCollectionServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CollectionServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	SatelliteServices provideSatelliteServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor,
		@Named("HmacInterceptor") Interceptor hmacInterceptor) {
		return new SatelliteServices(endpointProvider.getSatelliteEndpointUrl(), client,interceptor, hmacInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
