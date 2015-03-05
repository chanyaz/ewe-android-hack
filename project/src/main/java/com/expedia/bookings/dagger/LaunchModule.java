package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LaunchScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.CollectionServices;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class LaunchModule {
	@Provides
	@LaunchScope
	HotelServices provideHotelServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl(true /*isSecure*/);
		return new HotelServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LaunchScope
	CollectionServices provideCollectionServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl(true /*isSecure*/);
		return new CollectionServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

}
