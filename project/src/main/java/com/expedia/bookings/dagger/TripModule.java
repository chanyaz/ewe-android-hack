package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.ItinTripServices;
import com.expedia.bookings.services.RoomUpgradeOffersService;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class TripModule {
	@Provides
	@TripScope
	ItinTripServices provideTripServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ItinTripServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@TripScope
	RoomUpgradeOffersService provideRoomUpgradeOffersService(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new RoomUpgradeOffersService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
