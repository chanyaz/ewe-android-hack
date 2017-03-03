package com.expedia.bookings.dagger;

import android.content.Context;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.clientlog.ClientLog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.server.PersistentCookieManagerV2;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.tracking.AppStartupTimeLogger;
import com.expedia.bookings.utils.ClientLogConstants;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.FeatureToggleUtil;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StethoShim;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.TLSSocketFactory;
import com.expedia.bookings.utils.Ui;
import com.expedia.model.UserLoginStateChangedModel;
import com.google.android.gms.security.ProviderInstaller;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.util.AdvertisingIdUtils;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.util.SettingUtils;
import dagger.Module;
import dagger.Provides;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Cache;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
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
	X509TrustManager provideX509TrustManager() {
		return new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws
				CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};
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

	@Provides
	@Singleton
	HttpLoggingInterceptor.Level provideLogLevel() {
		if (BuildConfig.DEBUG
			|| DebugUtils.isLogEnablerInstalled(context)
			|| ExpediaDebugUtil.isEBToolApkInstalled(context)) {
			return HttpLoggingInterceptor.Level.BODY;
		}
		return HttpLoggingInterceptor.Level.NONE;
	}

	@Provides
	@Singleton
	SSLContext provideSSLContext(X509TrustManager x509TrustManager, boolean isModernTLSEnabled) {
		try {
			if (isModernTLSEnabled) {
				return SSLContext.getDefault();
			}
			else {
				TrustManager[] easyTrustManager = new TrustManager[] {
					x509TrustManager
				};

				SSLContext socketContext = SSLContext.getInstance("TLS");
				socketContext.init(null, easyTrustManager, new java.security.SecureRandom());
				return socketContext;

			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final String COOKIE_FILE_V5 = "cookies-5.dat";
	private static final String COOKIE_FILE_V4 = "cookies-4.dat";
	private static final String COOKIE_FILE_OLD = COOKIE_FILE_V4;
	private static final String COOKIE_FILE_LATEST = COOKIE_FILE_V5;

	@Provides
	@Singleton
	PersistentCookiesCookieJar provideCookieManager(Context context) {
		PersistentCookiesCookieJar cookieManager;
		File oldStorage = context.getFileStreamPath(COOKIE_FILE_OLD);
		File storage = context.getFileStreamPath(COOKIE_FILE_LATEST);
		if (FeatureToggleUtil.isFeatureEnabled(context, R.string.preference_enable_new_cookies)) {
			cookieManager = new PersistentCookieManagerV2(storage, oldStorage);
		}
		else {
			cookieManager = new PersistentCookieManager(storage, oldStorage);
		}
		return cookieManager;
	}

	@Provides
	@Singleton
	boolean provideIsModernTLSEnabled(EndpointProvider endpointProvider) {
		if (BuildConfig.RELEASE) {
			return true;
		}

		if (ExpediaBookingApp.isAutomation() || endpointProvider.getEndPoint() == EndPoint.MOCK_MODE
			|| endpointProvider.getEndPoint() == EndPoint.CUSTOM_SERVER) {
			return false;
		}

		return !SettingUtils
			.get(context, context.getString(R.string.preference_disable_modern_tls), false);
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(Context context, PersistentCookiesCookieJar cookieManager, Cache cache,
		HttpLoggingInterceptor.Level logLevel, SSLContext sslContext, boolean isModernTLSEnabled) {
		try {
			ProviderInstaller.installIfNeeded(context);
		}
		catch (Exception e) {
			// rely on the PlayServices checking code that runs when first activity starts
			// to guide the user through the recovery process
		}

		OkHttpClient.Builder client = new OkHttpClient().newBuilder();
		client.cache(cache);
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(logLevel);
		client.addInterceptor(logger);
		client.followRedirects(true);
		client.cookieJar(cookieManager);
		client.connectTimeout(10, TimeUnit.SECONDS);
		client.readTimeout(60L, TimeUnit.SECONDS);

		if (isModernTLSEnabled) {
			TLSSocketFactory socketFactory = new TLSSocketFactory(sslContext);
			client.sslSocketFactory(socketFactory);
			ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
				.tlsVersions(TlsVersion.TLS_1_2)
				.build();
			client.connectionSpecs(Collections.singletonList(spec));
		}
		else {
			client.sslSocketFactory(sslContext.getSocketFactory());
		}
		if (BuildConfig.DEBUG) {
			StethoShim.install(client);
		}

		return client.build();
	}

	@Provides
	@Singleton
	Interceptor provideRequestInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new Interceptor() {
			@Override
			public Response intercept(Interceptor.Chain chain) throws IOException {
				HttpUrl.Builder url = chain.request().url().newBuilder();
				Request.Builder request = chain.request().newBuilder();
				request.header("User-Agent", ServicesUtil.generateUserAgentString(context));
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

	private void clientLog(Request.Builder request, Response response, Context context) {
		if (!request.build().url().toString().contains(ClientLogConstants.CLIENT_LOG_URL)) {
			long responseTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
			ClientLog.ResponseCLBuilder responseLogBuilder = new ClientLog.ResponseCLBuilder();

			responseLogBuilder.pageName(getPageName(request.build()));
			responseLogBuilder.eventName(NetUtils.isWifiConnected(context) ? ClientLogConstants.WIFI : ClientLogConstants.MOBILE_DATA);
			responseLogBuilder.deviceName(android.os.Build.MODEL);
			responseLogBuilder.responseTime(responseTime);

			ClientLogServices clientLogServices = Ui.getApplication(context).appComponent().clientLog();
			clientLogServices.log(responseLogBuilder.build());

		}
	}

	private String getPageName(Request request) {
		String pageName = request.url().encodedPath().replaceAll("/", "_");
		if (pageName.contains("flight_search") && FeatureToggleUtil
			.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightByotSearch,
				R.string.preference_flight_byot)) {
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
}
