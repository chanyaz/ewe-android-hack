package com.expedia.bookings.data.cars;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.squareup.okhttp.OkHttpClient;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class CarDb {

	private static CarServices sCarServices;
	private static SuggestionServices sSuggestionServices;

	public static void injectEndpoint(Context context) {

		String e3endpoint = EndPoint.getE3EndpointUrl(context, true /*isSecure*/);
		sCarServices = generateCarServices(e3endpoint);

		String suggestEndpoint = EndPoint.getEssEndpointUrl(context, true /*isSecure*/);
		sSuggestionServices = generateCarSuggestionServices(suggestEndpoint);
	}

	private static SSLContext generateSSLContext() {
		try {
			SSLContext socketContext = SSLContext.getInstance("TLS");
			socketContext.init(null, sEasyTrustManager, new java.security.SecureRandom());
			return socketContext;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static OkHttpClient generateOkHttpClient() {
		OkHttpClient client = new OkHttpClient();
		if (BuildConfig.DEBUG) {
			client.setSslSocketFactory(generateSSLContext().getSocketFactory());
		}
		return client;
	}
	
	private static CarServices generateCarServices(String endpoint) {
		return new CarServices(endpoint, generateOkHttpClient(), AndroidSchedulers.mainThread(), Schedulers.io());
	}
	
	public static SuggestionServices generateCarSuggestionServices(String endpoint) {
		return new SuggestionServices(endpoint, generateOkHttpClient(), AndroidSchedulers.mainThread(), Schedulers.io());
	}

	public static CarServices getCarServices() {
		return sCarServices;
	}

	public static SuggestionServices getSuggestionServices() {
		return sSuggestionServices;
	}

	private static final TrustManager[] sEasyTrustManager = new TrustManager[] {
		new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
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
}
