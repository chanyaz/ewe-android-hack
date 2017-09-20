package com.expedia.bookings.dagger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Named;
import javax.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import android.content.Context;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.clientlog.ClientLog;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.model.PointOfSaleStateModel;
import com.expedia.bookings.notification.NotificationManager;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.PersistentCookieManagerV2;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.services.SatelliteServices;
import com.expedia.bookings.services.sos.SmartOfferService;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.utils.ClientLogConstants;
import com.expedia.bookings.utils.HMACUtil;
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
import okhttp3.Cache;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
		if (PointOfSale.getPointOfSale().shouldUseWebViewSyncCookieStore()) {
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
				Request.Builder request = chain.request().newBuilder();
				request.header("User-Agent", ServicesUtil.generateUserAgentString());
				if (!ExpediaBookingApp.isAutomation()) {
					request.addHeader("x-eb-client", ServicesUtil.generateXEbClientString(context));
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
				request.addHeader("Accept", "application/json");

				String mobVisId = AdvertisingIdUtils.getIDFA();
				if (Strings.isNotEmpty(mobVisId)) {
					request.addHeader("x-mobvisid", mobVisId);
				}

				String devLocation = ServicesUtil.generateXDevLocationString(context);
				if (Strings.isNotEmpty(devLocation)) {
					request.addHeader("x-dev-loc", devLocation);
				}

				request.url(url.build());
				Response response = chain.proceed(request.build());

				clientLog(request, response, context);
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

			ClientLogServices clientLogServices = Ui.getApplication(context).appComponent().clientLog();
			clientLogServices.log(responseLogBuilder.build());

		}
	}

	private String getPageName(Request request) {
		String pageName = request.url().encodedPath().replaceAll("/", "_");
		if (pageName.contains("flight_search") && AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightByotSearch)) {
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
	ClientLogServices provideClientLog(OkHttpClient client, EndpointProvider endpointProvider,
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
	@Named("HmacInterceptor")
	Interceptor provideHmacInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				Request.Builder request = chain.request().newBuilder();
				String xDate = HMACUtil.getXDate(DateTime.now(DateTimeZone.UTC));
				String salt = HMACUtil.generateSalt(16);
				request.addHeader("Authorization",
					HMACUtil.getAuthorization(context, chain.request().url(), chain.request().method(), xDate, salt));
				request.addHeader("x-date", xDate);
				request.addHeader("salt", salt);
				Response response = chain.proceed(request.build());
				return response;
			}
		};
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
	SearchLobToolbarCache provideSearchLobToolbarCache() {
		return new SearchLobToolbarCache();
	}

	@Provides
	@Singleton
	SatelliteServices provideSatelliteServices(EndpointProvider endpointProvider, OkHttpClient client,
		Interceptor interceptor, @Named("SatelliteInterceptor") Interceptor satelliteInterceptor,
		@Named("HmacInterceptor") Interceptor hmacInterceptor) {
		return new SatelliteServices(endpointProvider.getSatelliteEndpointUrl(), client, interceptor,
			satelliteInterceptor, hmacInterceptor,
			AndroidSchedulers.mainThread(), Schedulers.io());
	}

	@Provides
	@Singleton
	NotificationManager provideNotificationManager(Context context) {
		return new NotificationManager(context);
	}
}
