package com.expedia.bookings.dagger;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import android.content.Context;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CollectionServices;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.utils.HMACInterceptor;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public final class LaunchModule {
	@Provides
	@LaunchScope
	HotelServices provideHotelServices(Context context, EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, HMACInterceptor hmacInterceptor,
		@Named("SatelliteInterceptor") Interceptor satelliteInterceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		final String satelliteEndpoint = endpointProvider.getSatelliteHotelEndpointUrl();

		List<Interceptor> satelliteInterceptors = new ArrayList<>();
		satelliteInterceptors.add(satelliteInterceptor);
		satelliteInterceptors.add(hmacInterceptor);

		boolean bucketed = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.HotelSatelliteSearch);
		return new HotelServices(endpoint, satelliteEndpoint, client, interceptor, satelliteInterceptors, bucketed,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LaunchScope
	CollectionServices provideCollectionServices(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CollectionServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
