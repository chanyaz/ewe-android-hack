package com.expedia.bookings.dagger;

import com.expedia.bookings.dagger.tags.PackageScope;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.FourSquareServices;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * Created by nbirla on 14/02/18.
 */

@Module
public final class FourSquareModule {
	@Provides
	@PackageScope
	FourSquareServices provideFourSquareServices(EndpointProvider endpointProvider, OkHttpClient client, Interceptor interceptor){
		final String endpoint = "https://api.foursquare.com";
		return new FourSquareServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
