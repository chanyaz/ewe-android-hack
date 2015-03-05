package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.dagger.tags.E3Endpoint;
import com.expedia.bookings.dagger.tags.SuggestEndpoint;
import com.squareup.okhttp.OkHttpClient;
import dagger.Component;
import retrofit.RequestInterceptor;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	@SuggestEndpoint String suggestEndpoint();
	@E3Endpoint String e3Endpoint();
	OkHttpClient okHttpClient();
	RequestInterceptor requestInterceptor();
}
