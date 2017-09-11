package com.expedia.bookings.dagger;

import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.CardFeeService;

import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Module
public class FeesModule {

	@Provides
	@Reusable
	CardFeeService provideCardFeeService(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new CardFeeService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
