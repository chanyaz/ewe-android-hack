package com.expedia.bookings.dagger;

import com.expedia.bookings.data.LXState;
import com.expedia.bookings.dagger.tags.LXScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.LXServices;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@LXScope
	LXServices provideLXServices(EndpointProvider endpointProvider, OkHttpClient client, RequestInterceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl(true /*isSecure*/);
		return new LXServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	SuggestionServices provideLxSuggestionServices(EndpointProvider endpointProvider, OkHttpClient client) {
		final String endpoint = endpointProvider.getEssEndpointUrl(true /*isSecure*/);
		return new SuggestionServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@LXScope
	LXState provideLXState() {
		return new LXState();
	}
}
