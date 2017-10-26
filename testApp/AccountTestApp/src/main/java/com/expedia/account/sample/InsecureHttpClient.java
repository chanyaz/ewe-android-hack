package com.expedia.account.sample;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * This is an OkHttpClient that will connect to any 'ol https server without
 * asking any questions.
 */
public class InsecureHttpClient {

	public static OkHttpClient newInstance() {
		OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
		try {
			TrustManager[] easyTrustManager = new TrustManager[] {
				new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType) throws
						CertificateException {
						// So easy
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType)
						throws CertificateException {
						// So easy
					}

					public X509Certificate[] getAcceptedIssuers() {
						// So easy
						return new X509Certificate[0];
					}
				},
			};

			SSLContext socketContext = SSLContext.getInstance("TLS");
			socketContext.init(null, easyTrustManager, new SecureRandom());
			builder.sslSocketFactory(socketContext.getSocketFactory());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}

		return builder.build();
	}

}
