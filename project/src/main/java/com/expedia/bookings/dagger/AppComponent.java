package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.squareup.okhttp.OkHttpClient;
import dagger.Component;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	void inject(ExpediaServices services);

	EndpointProvider endpointProvider();
	OkHttpClient okHttpClient();
	RequestInterceptor requestInterceptor();
	PersistentCookieManager persistentCookieManager();
	RestAdapter.LogLevel logLevel();
	AbacusServices abacus();
}
