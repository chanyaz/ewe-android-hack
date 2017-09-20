package com.expedia.bookings.dagger;

import javax.inject.Named;
import com.expedia.bookings.dagger.tags.CarScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public final class CarModule {
	@Provides
	@CarScope
	CarServices provideCarServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CarServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@CarScope
	SuggestionV4Services provideCarSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor, @Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
