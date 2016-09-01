package com.expedia.bookings.dagger;

import javax.inject.Singleton;

import android.content.Context;

import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.InsuranceServices;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.model.UserLoginStateChangedModel;
import dagger.Component;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Component(modules = {AppModule.class})
@Singleton
public interface AppComponent {
	void inject(ExpediaServices services);
	void inject(UserAccountRefresher userAccountRefresher);
	void inject(AbacusHelperUtils.CookiesReference cookiesReference);

	Context appContext();
	EndpointProvider endpointProvider();
	OkHttpClient okHttpClient();
	Interceptor requestInterceptor();
	AbacusServices abacus();
	ClientLogServices clientLog();
	InsuranceServices insurance();
	UserLoginStateChangedModel userLoginStateChangedModel();
}
