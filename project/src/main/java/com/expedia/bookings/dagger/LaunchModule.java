package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.FeedsService;
import com.expedia.bookings.services.HotelServices;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class LaunchModule {
	@Provides
	@LaunchScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new HotelServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LaunchScope
	CollectionServices provideCollectionServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CollectionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LaunchScope
	FeedsService provideFeedsService(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new FeedsService(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

}
