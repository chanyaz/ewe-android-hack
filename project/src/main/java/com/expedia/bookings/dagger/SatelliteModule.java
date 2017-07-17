package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.SatelliteScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.SatelliteServices;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import javax.inject.Singleton;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class SatelliteModule {
	@Provides
	SatelliteServices provideSatelliteServices(EndpointProvider endpointProvider, OkHttpClient client,
		@Named("HmacInterceptor") Interceptor hmacInterceptor) {
		return new SatelliteServices(endpointProvider.getSatelliteEndpointUrl(), client, hmacInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
