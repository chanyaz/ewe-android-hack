package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.RoomUpgradeOffersService;
import com.expedia.bookings.itin.ItinPageUsableTracking;
import com.expedia.bookings.services.TripsServices;
import com.expedia.bookings.services.TripsServicesInterface;
import com.expedia.vm.ItinPOSHeaderViewModel;

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
	ItinPOSHeaderViewModel provideItinPOSHeaderViewModel(PointOfSaleStateModel pointOfSaleStateModel) {
		return new ItinPOSHeaderViewModel(pointOfSaleStateModel);
	}

	@Provides
	@TripScope
	TripsServicesInterface provideTripServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new TripsServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@TripScope
	RoomUpgradeOffersService provideRoomUpgradeOffersService(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new RoomUpgradeOffersService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@TripScope
	ItinPageUsableTracking provideItinPageUsableTracking() {
		return new ItinPageUsableTracking();
	}
}
