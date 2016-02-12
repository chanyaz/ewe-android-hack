package com.expedia.bookings.dagger;

import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

import com.expedia.account.server.ExpediaAccountApi;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.ClientLogServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.utils.ExpediaDebugUtil;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StethoShim;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.DebugUtils;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
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

	@Provides
	@Singleton
	RestAdapter.LogLevel provideLogLevel() {
		if (BuildConfig.DEBUG
			|| DebugUtils.isLogEnablerInstalled(context)
			|| ExpediaDebugUtil.isEBToolApkInstalled(context)) {
			return RestAdapter.LogLevel.FULL;
		}
		return RestAdapter.LogLevel.NONE;
	}

	@Provides
	@Singleton
	SSLContext provideSSLContext() {
		try {
			TrustManager[] easyTrustManager = new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws
						CertificateException {
						// So easy
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
						// So easy
					}

					public X509Certificate[] getAcceptedIssuers() {
						// So easy
						return null;
					}
				},
			};

			SSLContext socketContext = SSLContext.getInstance("TLS");
			socketContext.init(null, easyTrustManager, new java.security.SecureRandom());
			return socketContext;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static final String COOKIE_FILE_V4 = "cookies-4.dat";
	private static final String COOKIE_FILE_LATEST = COOKIE_FILE_V4;
	@Provides
	@Singleton
	PersistentCookieManager provideCookieManager(Context context) {
		File storage = context.getFileStreamPath(COOKIE_FILE_LATEST);
		PersistentCookieManager manager = new PersistentCookieManager(storage);
		return manager;
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(PersistentCookieManager cookieManager, SSLContext sslContext, Cache cache) {
		OkHttpClient client = new OkHttpClient();
		client.setCache(cache);

		client.setFollowSslRedirects(true);
		client.setCookieHandler(cookieManager);

		client.setConnectTimeout(10, TimeUnit.SECONDS);
		client.setReadTimeout(60L, TimeUnit.SECONDS);

		if (BuildConfig.DEBUG) {
			// We don't care about cert validity for debug builds
			client.setSslSocketFactory(sslContext.getSocketFactory());
			StethoShim.install(client);
		}

		return client;
	}

	@Provides
	@Singleton
	RequestInterceptor provideRequestInterceptor(final Context context, final EndpointProvider endpointProvider) {
		return new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addHeader("User-Agent", ServicesUtil.generateUserAgentString(context));
				if (!ExpediaBookingApp.isAutomation()) {
					request.addHeader("x-eb-client", ServicesUtil.generateXEbClientString(context));
				}
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(context));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (endpointProvider.requestRequiresSiteId()) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}

				boolean isV2HotelApiSearchEnabled =
					Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelSearchDomainV2);
				if (isV2HotelApiSearchEnabled) {
					request.addQueryParam("forceV2Search", "true");
				}

				request.addHeader("Accept", "application/json");
			}
		};
	}

	@Provides
	@Singleton
	EndpointProvider provideEndpointProvider(Context context) {
		try {
			String serverUrlPath = ProductFlavorFeatureConfiguration.getInstance().getServerEndpointsConfigurationPath();
			InputStream serverUrlStream = context.getAssets().open(serverUrlPath);
			return new EndpointProvider(context, serverUrlStream);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Provides
	@Singleton
	AbacusServices provideAbacus(OkHttpClient client, EndpointProvider endpointProvider, RequestInterceptor interceptor, RestAdapter.LogLevel loglevel) {
		final String endpoint = endpointProvider.getAbacusEndpointUrl();
		return new AbacusServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), loglevel);
	}

	@Provides
	@Singleton
	ClientLogServices provideClientLog(OkHttpClient client, EndpointProvider endpointProvider, RequestInterceptor interceptor, RestAdapter.LogLevel loglevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new ClientLogServices(endpoint, client, interceptor, AndroidSchedulers.mainThread(), Schedulers.io(), loglevel);
	}

	@Provides
	@Singleton
	ExpediaAccountApi provideExpediaAccountApi(OkHttpClient client, EndpointProvider endpointProvider, RestAdapter.LogLevel loglevel) {
		final String endpoint = endpointProvider.getE3EndpointUrl();
		return new RestAdapter.Builder()
			.setEndpoint(endpoint)
			.setLogLevel(loglevel)
			.setClient(new OkClient(client))
			.build().create(ExpediaAccountApi.class);
	}
}
