package com.expedia.bookings.dagger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.inject.Named;
import javax.inject.Singleton;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.activity.SatelliteRemoteFeatureResolver;
import com.expedia.bookings.data.AppDatabase;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.clientlog.ClientLog;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.http.TravelGraphRequestInterceptor;
import com.expedia.bookings.itin.flight.common.FlightRegistrationHandler;
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils;
import com.expedia.bookings.itin.tripstore.utils.TripsJsonFileUtils;
import com.expedia.bookings.itin.utils.AbacusProvider;
import com.expedia.bookings.itin.utils.AbacusSource;
import com.expedia.bookings.itin.utils.StringProvider;
import com.expedia.bookings.itin.utils.StringSource;
import com.expedia.bookings.itin.utils.NotificationScheduler;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.notification.NotificationManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.PersistentCookieManagerV2;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.services.SatelliteServices;
import com.expedia.bookings.services.TNSServices;
import com.expedia.bookings.services.os.OfferService;
import com.expedia.bookings.services.sos.SmartOfferService;
import com.expedia.bookings.trace.util.ServerDebugTraceUtil;
import com.expedia.bookings.tracking.AppCreateTimeLogger;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.tracking.RouterToLaunchTimeLogger;
import com.expedia.bookings.tracking.RouterToOnboardingTimeLogger;
import com.expedia.bookings.tracking.RouterToSignInTimeLogger;
import com.expedia.bookings.utils.ClientLogConstants;
import com.expedia.bookings.utils.CookiesUtils;
import com.expedia.bookings.utils.HMACInterceptor;
import com.expedia.bookings.utils.OKHttpClientFactory;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.navigation.SearchLobToolbarCache;
import com.expedia.model.UserLoginStateChangedModel;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.NetUtils;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import kotlin.Pair;
import okhttp3.Cache;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.expedia.bookings.utils.Constants.APP_DATABASE_NAME;

@Module
public class AppModule {
	private final Context context;

	public AppModule(Context context) {
		this.context = context;
	}

	@Provides
	@Singleton
	Context provideContext() {
		return context;
	}

	@Provides
	@Singleton
	Cache provideOkHttpDiskCache(Context context) {
		final File directory = new File(context.getCacheDir(), "okhttp");
		if (!directory.exists()) {
			directory.mkdirs();
		}

		final long size = 50 * 1024 * 1024; // 50MB

		return new Cache(directory, size);
	}

	private static final String COOKIE_FILE_V5 = "cookies-5.dat";
	private static final String COOKIE_FILE_V4 = "cookies-4.dat";
	private static final String COOKIE_FILE_OLD = COOKIE_FILE_V4;
	private static final String COOKIE_FILE_LATEST = COOKIE_FILE_V5;

	@Provides
	@Singleton
	PointOfSaleStateModel providePointOfSaleStateModel() {
		return new PointOfSaleStateModel();
	}

	@Provides
	@Singleton
	PersistentCookiesCookieJar provideCookieManager(Context context) {
		PersistentCookiesCookieJar cookieManager;
		File oldStorage = context.getFileStreamPath(COOKIE_FILE_OLD);
		File storage = context.getFileStreamPath(COOKIE_FILE_LATEST);
		if (CookiesUtils.shouldUseNewCookiesMechanism(context)) {
			cookieManager = new PersistentCookieManagerV2(storage, oldStorage);
		}
		else {
			cookieManager = new PersistentCookieManager(storage, oldStorage);
		}
		return cookieManager;
	}

	@Provides
	@Singleton
	OKHttpClientFactory provideOkHttpClientFactory(Context context, PersistentCookiesCookieJar cookieManager,
												   Cache cache, final EndpointProvider endpointProvider) {
		return new OKHttpClientFactory(context, cookieManager, cache, endpointProvider);
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(OKHttpClientFactory okHttpClientFactory) {
		return okHttpClientFactory.getOkHttpClient(null);
	}

	@Provides
	@Singleton
	Interceptor provideRequestInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				HttpUrl.Builder url = chain.request().url().newBuilder();
				Request.Builder requestBuilder = chain.request().newBuilder();
				requestBuilder.header("User-Agent", ServicesUtil.generateUserAgentString());
				if (!ExpediaBookingApp.isAutomation()) {
					requestBuilder.addHeader("x-eb-client", ServicesUtil.generateXEbClientString(context));
				}
				url.addEncodedQueryParameter("clientid", ServicesUtil.generateClientId(context));
				url.addEncodedQueryParameter("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					url.addEncodedQueryParameter("langid", langid);
				}

				if (endpointProvider.requestRequiresSiteId()) {
					url.addEncodedQueryParameter("siteid", ServicesUtil.generateSiteId());
				}
				requestBuilder.addHeader("Accept", "application/json");

				String mobVisId = AdvertisingIdUtils.getIDFA();
				if (Strings.isNotEmpty(mobVisId)) {
					requestBuilder.addHeader("x-mobvisid", mobVisId);
				}

				String devLocation = ServicesUtil.generateXDevLocationString(context);
				if (Strings.isNotEmpty(devLocation)) {
					requestBuilder.addHeader("x-dev-loc", devLocation);
				}


				setupDebugTracingIfEnabled(requestBuilder);

				requestBuilder.url(url.build());
				Request request = requestBuilder.build();
				Response response = chain.proceed(request);

				captureDebugTracingIfEnabled(request, response);

				clientLog(requestBuilder, response, context);
				return response;
			}
		};
	}

	@Provides
	@Singleton
	@Named("SatelliteInterceptor")
	Interceptor provideSatelliteRequestInterceptor() {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				HttpUrl.Builder url = chain.request().url().newBuilder();
				Request.Builder request = chain.request().newBuilder();
				url.setEncodedQueryParameter("siteid", ServicesUtil.generateSiteId());
				request.url(url.build());

				return chain.proceed(request.build());
			}
		};
	}

	@Provides
	@Singleton
	@Named("ESSInterceptor")
	Interceptor provideESSInterceptor(final Context context) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				HttpUrl.Builder url = chain.request().url().newBuilder();
				Request.Builder request = chain.request().newBuilder();
				url.setEncodedQueryParameter("device", ServicesUtil.getDeviceType(context).getEssDeviceString());
				url.setEncodedQueryParameter("locale", PointOfSale.getSuggestLocaleIdentifier());
				url.setEncodedQueryParameter("siteid", ServicesUtil.generateSiteId());
				url.setEncodedQueryParameter("client", ServicesUtil.generateClient(context));
				request.url(url.build());

				return chain.proceed(request.build());
			}
		};
	}

	private void clientLog(Request.Builder request, Response response, Context context) {
		if (!request.build().url().toString().contains(ClientLogConstants.CLIENT_LOG_URL)) {
			long responseTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
			ClientLog.ResponseCLBuilder responseLogBuilder = new ClientLog.ResponseCLBuilder();

			responseLogBuilder.pageName(getPageName(request.build()));
			responseLogBuilder.eventName(
					NetUtils.isWifiConnected(context) ? ClientLogConstants.WIFI : ClientLogConstants.MOBILE_DATA);
			responseLogBuilder.deviceName(android.os.Build.MODEL);
			responseLogBuilder.responseTime(responseTime);

			IClientLogServices clientLogServices = Ui.getApplication(context).appComponent().clientLog();
			clientLogServices.log(responseLogBuilder.build());

		}
	}

	private String getPageName(Request request) {
		String pageName = request.url().encodedPath().replaceAll("/", "_");
		if (pageName.contains("flight_search") && AbacusFeatureConfigManager
				.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightByotSearch)) {
			FormBody body = (FormBody) request.body();

			for (int index = body.size(); index > 0; index--) {
				if (body.encodedName(index - 1).equals("ul")) {
					if (body.encodedValue(index - 1).equals("0")) {
						return pageName.concat("_outbound");
					}
					else {
						return pageName.concat("_inbound");
					}
				}
			}
		}
		return pageName;
	}

	@Provides
	@Singleton
	EndpointProvider provideEndpointProvider(Context context) {
		try {
			String serverUrlPath = ProductFlavorFeatureConfiguration.getInstance()
					.getServerEndpointsConfigurationPath();
			InputStream serverUrlStream = context.getAssets().open(serverUrlPath);
			return new EndpointProvider(context, serverUrlStream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Provides
	@Singleton
	AbacusServices provideAbacus(OkHttpClient client, EndpointProvider endpointProvider, Interceptor interceptor) {
		final String endpoint = endpointProvider.getAbacusEndpointUrl();
		return new AbacusServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	IClientLogServices provideClientLog(OkHttpClient client, EndpointProvider endpointProvider,
										Interceptor interceptor) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ClientLogServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	UserLoginStateChangedModel provideUserLoginStateChangedModel() {
		return new UserLoginStateChangedModel();
	}

	@Provides
	@Singleton
	AppStartupTimeLogger appStartupTimeLogger() {
		return new AppStartupTimeLogger();
	}

	@Provides
	@Singleton
	AppCreateTimeLogger appCreateTimeLogger() {
		return new AppCreateTimeLogger();
	}

	@Provides
	@Singleton
	RouterToOnboardingTimeLogger routerToOnboardingTimeLogger() {
		return new RouterToOnboardingTimeLogger();
	}

	@Provides
	@Singleton
	RouterToLaunchTimeLogger routerToLaunchTimeLogger() {
		return new RouterToLaunchTimeLogger();
	}

	@Provides
	@Singleton
	RouterToSignInTimeLogger routerToSignInTimeLogger() {
		return new RouterToSignInTimeLogger();
	}

	@Provides
	@Named("GaiaInterceptor")
	Interceptor provideGaiaRequestInterceptor(final Context context) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				Request.Builder request = chain.request().newBuilder();
				request.addHeader("key", ServicesUtil.getGaiaApiKey(context));
				Response response = chain.proceed(request.build());
				return response;
			}
		};
	}

	@Provides
	HMACInterceptor provideHmacInterceptor(final Context context) {
		return new HMACInterceptor(context.getResources().getString(R.string.exp_u), context.getResources().getString(R.string.exp_k));
	}

	@Provides
	UserAgentInterceptor provideUserAgentInterceptor() {
		return new UserAgentInterceptor();
	}

	@Provides
	@Named("TravelGraphInterceptor")
	Interceptor provideTravelGraphInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new TravelGraphRequestInterceptor(context, endpointProvider);
	}

	@Provides
	@Singleton
	SmartOfferService provideSmartOfferService(EndpointProvider endpointProvider, OkHttpClient client,
											   Interceptor interceptor) {
		final String endpoint = endpointProvider.getSmartOfferServiceEndpoint();
		return new SmartOfferService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	OfferService provideOfferService(EndpointProvider endpointProvider, OkHttpClient client,
											   Interceptor interceptor) {
		final String endpoint = endpointProvider.getOfferServiceEndpoint();
		return new OfferService(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	SearchLobToolbarCache provideSearchLobToolbarCache() {
		return new SearchLobToolbarCache();
	}

	@Provides
	@Singleton
	SatelliteServices provideSatelliteServices(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("SatelliteInterceptor") Interceptor satelliteInterceptor,
		HMACInterceptor hmacInterceptor) {
		return new SatelliteServices(endpointProvider.getSatelliteEndpointUrl(), client, interceptor,
				satelliteInterceptor, hmacInterceptor,
				AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	TNSServices provideTNSServices(EndpointProvider endpointProvider, OkHttpClient client,
								   HMACInterceptor hmacInterceptor,
								   UserAgentInterceptor userAgentInterceptor) {
		return new TNSServices(endpointProvider.getTNSEndpoint(), client, Arrays.asList(hmacInterceptor, userAgentInterceptor),
				Schedulers.io(), Schedulers.io());
	}

	@Provides
	@Singleton
	FlightRegistrationHandler provideFlightRegistrationService(TNSServices tnsServices,
															   UserStateManager userStateManager, Context context) {
		return new FlightRegistrationHandler(context, tnsServices, userStateManager.getUserSource());
	}

	@Provides
	@Singleton
	NotificationManager provideNotificationManager(Context context) {
		return new NotificationManager(context);
	}

	@Provides
	@Singleton
	ITripsJsonFileUtils provideTripsJsonFileUtils(Context context) {
		File tripsDirectory = context.getDir("TRIPS_JSON_STORE", Context.MODE_PRIVATE);
		return new TripsJsonFileUtils(tripsDirectory);
	}

	@Provides
	@Singleton
	StringSource provideStringSource(Context context) {
		return new StringProvider(context);
	}

	@Provides
	@Singleton
	AbacusSource provideAbacusSource(Context context) {
		return new AbacusProvider(context);
	}
	
	@Provides
	@Singleton
	NotificationScheduler provideNotificationScheduler(Context context, NotificationManager notificationManager,
		UserStateManager userStateManager, TNSServices tnsServices) {
		return new NotificationScheduler(context, notificationManager, userStateManager, tnsServices);
	}

	@Provides
	@Singleton
	SatelliteRemoteFeatureResolver satelliteRemoteFeatureResolver(Context context) {
		return new SatelliteRemoteFeatureResolver(context);
	}

	@Provides
	@Singleton
	AppDatabase provideAppDatabase(Context context) {
		return Room.databaseBuilder(context, AppDatabase.class, APP_DATABASE_NAME).build();
	}

	private void setupDebugTracingIfEnabled(Request.Builder requestBuilder) {
		if (ServerDebugTraceUtil.isDebugTracingAvailable()) {
			String serverDebugTraceToken = ServerDebugTraceUtil.getDebugTokenAndRefreshIfNeeded();
			if (serverDebugTraceToken != null) {
				requestBuilder.addHeader("x-debug-trace", serverDebugTraceToken);
			}
		}
	}

	private void captureDebugTracingIfEnabled(Request request, Response response) {
		if (ServerDebugTraceUtil.isDebugTracingAvailable()) {
			String requestUrl = request.url().toString();
			String traceId = null;
			if (response.header("Trace-ID") != null) {
				traceId = response.header("Trace-ID");
			}
			else if (response.header("activity-id") != null) {
				traceId = response.header("activity-id");
			}
			if (traceId != null) {
				ServerDebugTraceUtil.debugTraceData.add(new Pair(requestUrl, traceId));
			}
		}
	}
}
