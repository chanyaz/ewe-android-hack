package com.expedia.bookings.dagger;

import javax.inject.Named;
import javax.inject.Singleton;

import org.jetbrains.annotations.NotNull;

import android.content.Context;

import com.expedia.account.util.NetworkConnectivity;
import com.expedia.bookings.activity.AccountLibActivity;
import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.activity.SatelliteRemoteFeatureResolver;
import com.expedia.bookings.data.AppDatabase;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.fragment.AccountSettingsFragment;
import com.expedia.bookings.hotel.util.HotelGalleryManager;
import com.expedia.bookings.itin.flight.common.FlightRegistrationHandler;
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil;
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils;
import com.expedia.bookings.itin.utils.AbacusSource;
import com.expedia.bookings.itin.utils.IToaster;
import com.expedia.bookings.itin.utils.NotificationScheduler;
import com.expedia.bookings.itin.utils.StringSource;
import com.expedia.bookings.launch.displaylogic.LaunchListStateManager;
import com.expedia.bookings.launch.activity.PhoneLaunchActivity;
import com.expedia.bookings.launch.widget.LaunchListWidget;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.notification.GCMIntentServiceV2;
import com.expedia.bookings.notification.INotificationManager;
import com.expedia.bookings.onboarding.activity.OnboardingActivity;
import com.expedia.bookings.presenter.trips.AddGuestItinWidget;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.services.SatelliteServices;
import com.expedia.bookings.services.SuggestionV4Services;
import com.expedia.bookings.services.TNSServices;
import com.expedia.bookings.services.os.OfferService;
import com.expedia.bookings.services.sos.SmartOfferService;
import com.expedia.bookings.tracking.AppCreateTimeLogger;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.utils.AbacusHelperUtils;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.utils.navigation.SearchLobToolbarCache;
import com.expedia.model.UserLoginStateChangedModel;

import dagger.Component;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

@Component(modules = { AppModule.class, UserModule.class, GalleryModule.class })
@Singleton
public interface AppComponent {
	void inject(ExpediaServices services);

	void inject(UserAccountRefresher userAccountRefresher);

	void inject(PhoneLaunchActivity activity);

	void inject(AbacusHelperUtils.CookiesReference cookiesReference);

	void inject(LaunchListWidget launchListWidget);

	void inject(AddGuestItinWidget addGuestItinWidget);

	void inject(AccountSettingsFragment accountSettingsFragment);

	void inject(OnboardingActivity onboardingActivity);

	void inject(RouterActivity routerActivity);

	void inject(AccountLibActivity accountLibActivity);

	void inject(GCMIntentServiceV2 gcmIntentServiceV2);

	PointOfSaleStateModel pointOfSaleStateModel();

	@NotNull Context appContext();

	EndpointProvider endpointProvider();

	OkHttpClient okHttpClient();

	Interceptor requestInterceptor();

	@Named("GaiaInterceptor")
	Interceptor gaiaRequestInterceptor();

	@Named("ESSInterceptor")
	Interceptor essRequestInterceptor();

	@Named("SatelliteInterceptor")
	Interceptor satelliteRequestInterceptor();

	AbacusServices abacus();

	IClientLogServices clientLog();

	UserLoginStateChangedModel userLoginStateChangedModel();

	AppStartupTimeLogger appStartupTimeLogger();

	AppCreateTimeLogger appCreateTimeLogger();

	SmartOfferService smartOfferService();

	OfferService offerService();

	UserStateManager userStateManager();

	NetworkConnectivity networkConnectivity();

	LaunchListStateManager launchListStateManager();

	SearchLobToolbarCache searchLobToolbarCache();

	INotificationManager notificationManager();

	SatelliteServices satelliteServices();
	HotelGalleryManager hotelGalleryManager();

	TNSServices tnsService();
	FlightRegistrationHandler flightRegistrationService();

	@Named("TripDetailsFileUtil")
	ITripsJsonFileUtils tripJsonFileUtils();
	NotificationScheduler notificationScheduler();
	StringSource stringProvider();
	AbacusSource abacusProvider();
	IJsonToItinUtil jsonUtilProvider();

	SatelliteRemoteFeatureResolver satelliteRemoteFeatureResolver();

	IToaster toaster();

	AppDatabase provideAppDatabase();

	SuggestionV4Services suggestionsService();
}
