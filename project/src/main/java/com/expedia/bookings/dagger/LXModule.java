package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.dagger.tags.E3Endpoint;
import com.expedia.bookings.services.LXServices;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@Module
public class LXModule {
	@Provides
	@Singleton
	LXServices provideLXServices(@E3Endpoint String endpoint, OkHttpClient client, RequestInterceptor interceptor) {
		return new LXServices(endpoint, client, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
