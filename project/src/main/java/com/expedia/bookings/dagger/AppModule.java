package com.expedia.bookings.dagger;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.dagger.tags.E3Endpoint;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Strings;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import retrofit.RequestInterceptor;

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

	@Provides
	@Singleton
	OkHttpClient provideOkHttpClient(SSLContext sslContext) {
		OkHttpClient client = new OkHttpClient();
		if (BuildConfig.DEBUG) {
			client.setSslSocketFactory(sslContext.getSocketFactory());
		}
		return client;
	}

	@Provides
	@Singleton
	RequestInterceptor provideRequestInterceptor(final Context context) {
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

				if (EndPoint.requestRequiresSiteId(context)) {
					request.addEncodedQueryParam("siteid", ServicesUtil.generateSiteId());
				}
			}
		};
	}

	@Provides
	@Singleton
	@E3Endpoint
	String provideExpediaEndpoint(Context context) {
		return EndPoint.getE3EndpointUrl(context, true /*isSecure*/);
	}
}
