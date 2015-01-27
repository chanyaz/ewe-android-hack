package com.expedia.bookings.data.cars;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.expedia.bookings.services.CarServices;
import com.expedia.bookings.services.SuggestionServices;
import com.mobiata.android.Log;
import com.squareup.okhttp.OkHttpClient;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class CarDb {

	public static CarSearchParams searchParams = new CarSearchParams();

	public static CarSearch carSearch = new CarSearch();

	public static void setSearchParams(CarSearchParams carSearchParams) {
		searchParams = carSearchParams.clone();
	}

	private static CarServices sCarServices;
	private static SuggestionServices sSuggestionServices;

	public static void setServicesEndpoint(String endpoint, boolean isRelease) {
		OkHttpClient okHttpClient = new OkHttpClient();
		if (!isRelease) {
			SSLContext socketContext = null;
			try {
				socketContext = SSLContext.getInstance("TLS");
				socketContext.init(null, sEasyTrustManager, new java.security.SecureRandom());
				okHttpClient.setSslSocketFactory(socketContext.getSocketFactory());
			}
			catch (NoSuchAlgorithmException | KeyManagementException e) {
				Log.w("Something sad happened during manipulation of SSL", e);
			}
		}
		sCarServices = new CarServices(endpoint, okHttpClient, AndroidSchedulers.mainThread(), Schedulers.io());
	}

	public static CarServices getCarServices() {
		return sCarServices;
	}

	public static SuggestionServices getSuggestionServices() {
		if (sSuggestionServices == null) {
			OkHttpClient okHttpClient = new OkHttpClient();
			sSuggestionServices = new SuggestionServices(okHttpClient,  AndroidSchedulers.mainThread(), Schedulers.io());
		}
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
