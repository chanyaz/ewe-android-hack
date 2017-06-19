package com.expedia.bookings.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import android.content.Context;

import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.fragment.AccountSettingsFragment;
import com.expedia.bookings.launch.activity.NewPhoneLaunchActivity;
import com.expedia.bookings.launch.widget.LaunchListWidget;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.presenter.trips.AddGuestItinWidget;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.TranslateServices;
import com.expedia.bookings.services.sos.SmartOfferService;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.model.UserLoginStateChangedModel;

import dagger.Component;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Component(modules = { AppModule.class, UserModule.class })
@Singleton
public interface AppComponent {
	void inject(ExpediaServices services);

	void inject(UserAccountRefresher userAccountRefresher);

	void inject(NewPhoneLaunchActivity activity);

	void inject(AbacusHelperUtils.CookiesReference cookiesReference);

	void inject(LaunchListWidget launchListWidget);

	void inject(AddGuestItinWidget addGuestItinWidget);

	void inject(AccountSettingsFragment accountSettingsFragment);

	PointOfSaleStateModel pointOfSaleStateModel();

	Context appContext();

	EndpointProvider endpointProvider();

	OkHttpClient okHttpClient();

	Interceptor requestInterceptor();

	@Named("GaiaInterceptor")
	Interceptor gaiaRequestInterceptor();

	AbacusServices abacus();

	ClientLogServices clientLog();

	UserLoginStateChangedModel userLoginStateChangedModel();

	AppStartupTimeLogger appStartupTimeLogger();

	SmartOfferService smartOfferService();

	UserStateManager userStateManager();
}
