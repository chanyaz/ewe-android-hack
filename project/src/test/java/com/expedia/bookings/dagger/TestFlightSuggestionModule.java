package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.FlightScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.services.TestSuggestionV4Services;
import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
class TestFlightSuggestionModule {
	@Provides
	@FlightScope
	SuggestionV4Services provideSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, Interceptor essRequestInterceptor, Interceptor gaiaRequestInterceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new TestSuggestionV4Services(essEndpoint, gaiaEndpoint, client,
			interceptor, essRequestInterceptor, gaiaRequestInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
