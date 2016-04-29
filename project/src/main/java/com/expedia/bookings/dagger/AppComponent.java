package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import android.content.Context;

import com.expedia.account.server.ExpediaAccountApi;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.model.UserLoginStateChangedModel;

import dagger.Component;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	void inject(ExpediaServices services);
	void inject(UserAccountRefresher userAccountRefresher);

	Context appContext();
	EndpointProvider endpointProvider();
	OkHttpClient okHttpClient();
	Interceptor requestInterceptor();
	PersistentCookieManager persistentCookieManager();
	HttpLoggingInterceptor.Level logLevel();
	AbacusServices abacus();
	ClientLogServices clientLog();
	ExpediaAccountApi accountApi();
	UserLoginStateChangedModel userLoginStateChangedModel();

}
