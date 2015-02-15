package com.expedia.bookings.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.expedia.bookings.BuildConfig;
import com.squareup.okhttp.OkHttpClient;

public class DbUtils {

	public static OkHttpClient generateOkHttpClient() {
		OkHttpClient client = new OkHttpClient();
		if (BuildConfig.DEBUG) {
			client.setSslSocketFactory(generateSSLContext().getSocketFactory());
		}
		return client;
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
