package com.mobiata.android.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.mobiata.android.net.AndroidHttpClient;

public class NetUtils {
	/**
	 * Determines if we have network connectivity on the phone
	 * @param context the app context
	 * @return true if network is up and available, false otherwise
	 */
	public static boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return (ni != null && ni.isAvailable() && ni.isConnected());
	}

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi.isConnected();
	}

	/**
	 * Performs URL-formatting on this string (i.e., 
	 * converts " " to "+" and other special characters to their %XX representation).
	 * @param url
	 * @return
	 */
	public static String formatUrl(String url) {
		try {
			URL urlUrl = new URL(url);
			URI urlUri = new URI(urlUrl.getProtocol(), urlUrl.getUserInfo(), urlUrl.getHost(), urlUrl.getPort(),
					urlUrl.getPath(), urlUrl.getQuery(), urlUrl.getRef());
			return urlUri.toASCIIString();
		}
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static HttpGet createHttpGet(String url, List<BasicNameValuePair> params) {
		if (params != null && params.size() > 0) {
			String encodedParams = URLEncodedUtils.format(params, "UTF-8");
			if (!url.endsWith("?")) {
				url += "?";
			}
			return new HttpGet(url + encodedParams);
		}

		return new HttpGet(url);
	}

	/**
	 * Same as EntityUtils.toString(), except that it can handle GZIP compression
	 * as used by AndroidHttpClient. 
	 */
	public static String toString(final HttpEntity entity, final String defaultCharset) throws IOException,
			ParseException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		InputStream instream = AndroidHttpClient.getUngzippedContent(entity);
		if (instream == null) {
			return "";
		}
		if (entity.getContentLength() > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
		}
		int i = (int) entity.getContentLength();
		if (i < 0) {
			i = 4096;
		}
		String charset = EntityUtils.getContentCharSet(entity);
		if (charset == null) {
			charset = defaultCharset;
		}
		if (charset == null) {
			charset = HTTP.DEFAULT_CONTENT_CHARSET;
		}
		Reader reader = new InputStreamReader(instream, charset);
		CharArrayBuffer buffer = new CharArrayBuffer(i);
		try {
			char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
		}
		finally {
			reader.close();
		}
		return buffer.toString();
	}

	/**
	 * Same as EntityUtils.toString(), except that it can handle GZIP compression
	 * as used by AndroidHttpClient. 
	 */
	public static String toString(final HttpEntity entity) throws IOException, ParseException {
		return toString(entity, null);
	}

	/**
	 * Just a small debugging method.
	 */
	public static String getParamsForLogging(List<BasicNameValuePair> params) {
		if (params == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		for (BasicNameValuePair param : params) {
			sb.append(param.getName() + "=" + param.getValue() + "&");
		}
		return sb.toString();
	}
}
