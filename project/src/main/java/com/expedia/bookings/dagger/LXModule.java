package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.services.SuggestionServices;
import com.expedia.bookings.services.SuggestionV4Services;

import dagger.Module;
import dagger.Provides;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@LXScope
	LxServices provideLxServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LxServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	SuggestionV4Services provideLXSuggestionV4Services(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor) {
		final String essEndpoint = endpointProvider.getEssEndpointUrl();
		final String gaiaEndpoint = endpointProvider.getGaiaEndpointUrl();
		return new SuggestionV4Services(essEndpoint, gaiaEndpoint, client, interceptor, AndroidSchedulers.mainThread(),
			Schedulers.io());
	}

	@Provides
	@LXScope
	SuggestionServices provideLxSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}
}
