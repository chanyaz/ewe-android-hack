package com.expedia.bookings.dagger;

import java.io.File;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.EndpointProvider;
import com.expedia.bookings.services.AbacusServices;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StethoShim;
import com.expedia.bookings.utils.Strings;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;
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

	private static final String COOKIE_FILE_V2 = "cookies-2.dat";
	private static final String COOKIE_FILE_V3 = "cookies-3.dat";
	@Provides
	@Singleton
	PersistentCookieManager provideCookieManager(Context context) {
		File storageV3 = context.getFileStreamPath(COOKIE_FILE_V3);
		PersistentCookieManager manager = new PersistentCookieManager(storageV3);

		// REMOVE THIS once people upgrade
		File storageV2 = context.getFileStreamPath(COOKIE_FILE_V2);
		PersistentCookieManager.fillWithOldCookies(manager, storageV2);

		return manager;
	}

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(PersistentCookieManager cookieManager, SSLContext sslContext, Cache cache) {
		OkHttpClient client = new OkHttpClient();
		client.setCache(cache);

		client.setFollowSslRedirects(true);
		client.setCookieHandler(cookieManager);

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
				request.addEncodedQueryParam("clientid", ServicesUtil.generateClientId(context));
				request.addEncodedQueryParam("sourceType", ServicesUtil.generateSourceType());

				String langid = ServicesUtil.generateLangId();
				if (Strings.isNotEmpty(langid)) {
					request.addEncodedQueryParam("langid", langid);
				}

				if (endpointProvider.requestRequiresSiteId()) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
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
	AbacusServices provideAbacus(OkHttpClient client) {
		String abacusEndpoint = BuildConfig.DEBUG ? AbacusServices.DEV : AbacusServices.PRODUCTION;
		return new AbacusServices(client, abacusEndpoint, AndroidSchedulers.mainThread(), Schedulers.io());
	}
}
