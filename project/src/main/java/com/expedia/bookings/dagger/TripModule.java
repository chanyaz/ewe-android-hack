package com.expedia.bookings.dagger;

import android.content.Context;

import com.expedia.bookings.dagger.tags.TripScope;
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil;
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils;
import com.expedia.bookings.itin.tripstore.utils.JsonToItinUtil;
import com.expedia.bookings.itin.utils.StringSource;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.notification.HotelNotificationGenerator;
import com.expedia.bookings.notification.INotificationManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.itin.common.ItinPageUsableTracking;
import com.expedia.bookings.services.TripShareUrlShortenService;
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface;
import com.expedia.bookings.services.TripsServices;
import com.expedia.bookings.services.TripsServicesInterface;
import com.expedia.vm.ItinPOSHeaderViewModel;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
		return new TripsServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), new CrashlyticsNonFatalLogger());
	}

	@Provides
	@TripScope
	TripShareUrlShortenServiceInterface provideTripShareUrlShortenService(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getShortlyEndpointUrl();
		return new TripShareUrlShortenService(endpoint, client, interceptor, AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	@Provides
	@TripScope
	ItinPageUsableTracking provideItinPageUsableTracking() {
		return new ItinPageUsableTracking();
	}

	@Provides
	@TripScope
	IJsonToItinUtil provideReadJsonUtil(ITripsJsonFileUtils tripsJsonFileUtils) {
		return new JsonToItinUtil(tripsJsonFileUtils);
	}

	@Provides
	@TripScope
	HotelNotificationGenerator provideHotelNotificationGenerator(Context context, StringSource stringSource, INotificationManager notificationManager) {
		return new HotelNotificationGenerator(context, stringSource, notificationManager);
	}
}
