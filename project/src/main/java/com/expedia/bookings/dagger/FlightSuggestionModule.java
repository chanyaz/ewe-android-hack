package com.expedia.bookings.dagger;

import javax.inject.Named;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public class FlightSuggestionModule {
	@Provides
	@FlightScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("ESSInterceptor") Interceptor essRequestInterceptor,
		@Named("GaiaInterceptor") Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
