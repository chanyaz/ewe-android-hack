package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.server.EndpointProvider;
import com.squareup.okhttp.OkHttpClient;
import dagger.Component;
import retrofit.RequestInterceptor;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	EndpointProvider endpointProvider();
	OkHttpClient okHttpClient();
	RequestInterceptor requestInterceptor();
}
