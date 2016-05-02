package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.data.LXState;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.LxServices;
import com.expedia.bookings.services.SuggestionServices;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@LXScope
	LxServices provideLxServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new LxServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	SuggestionServices provideLxSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getEssEndpointUrl();
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}
}
