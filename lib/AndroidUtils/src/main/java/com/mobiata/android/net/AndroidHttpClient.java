package com.mobiata.android.net;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;

/**
 * This is a wrapper class for AndroidHttpClient.  It used to do some fancy footwork with reflection
 * so that we could use a hacked up version of AndroidHttpClient prior to API 8.  But, since we're not
 * supporting anything older than API 8 anymore, it is now just a thin wrapper around the AndroidHttpClient
 * interface.  You really shouldn't be using this.
 *
 * @deprecated You should prefer using SDK classes directly, and probably prefer
 *             HttpUrlConnection (see http://android-developers.blogspot.com/2011/09/androids-http-clients.html)
 */
public class AndroidHttpClient implements HttpClient {

	private android.net.http.AndroidHttpClient mAndroidHttpClient;

	private AndroidHttpClient(android.net.http.AndroidHttpClient client) {
		mAndroidHttpClient = client;
	}

	/**
	 * Create a new HttpClient with reasonable defaults (which you can update).
	 *
	 * @param userAgent to report in your HTTP requests
	 * @param context to use for caching SSL sessions (may be null for no caching)
	 * @return AndroidHttpClient for you to use for all your requests.
	 */
	public static AndroidHttpClient newInstance(String userAgent, Context context) {
		return new AndroidHttpClient(android.net.http.AndroidHttpClient.newInstance(userAgent, context));
	}

	/**
	 * Create a new HttpClient with reasonable defaults (which you can update).
	 * @param userAgent to report in your HTTP requests.
	 * @return AndroidHttpClient for you to use for all your requests.
	 */
	public static AndroidHttpClient newInstance(String userAgent) {
		return newInstance(userAgent, null /* session cache */);
	}

	/**
	 * Modifies a request to indicate to the server that we would like a
	 * gzipped response.  (Uses the "Accept-Encoding" HTTP header.)
	 * @param request the request to modify
	 * @see #getUngzippedContent
	 */
	public static void modifyRequestToAcceptGzipResponse(HttpRequest request) {
		android.net.http.AndroidHttpClient.modifyRequestToAcceptGzipResponse(request);
	}

	/**
	 * Gets the input stream from a response entity.  If the entity is gzipped
	 * then this will get a stream over the uncompressed data.
	 *
	 * @param entity the entity whose content should be read
	 * @return the input stream to read from
	 * @throws IOException
	 */
	public static InputStream getUngzippedContent(HttpEntity entity) throws IOException {
		return android.net.http.AndroidHttpClient.getUngzippedContent(entity);
	}

	/**
	 * Release resources associated with this client.  You must call this,
	 * or significant resources (sockets and memory) may be leaked.
	 */
	public void close() {
		mAndroidHttpClient.close();
	}

	public HttpParams getParams() {
		return mAndroidHttpClient.getParams();
	}

	public ClientConnectionManager getConnectionManager() {
		return mAndroidHttpClient.getConnectionManager();
	}

	public HttpResponse execute(HttpUriRequest request) throws IOException {
		return mAndroidHttpClient.execute(request);
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
		return mAndroidHttpClient.execute(request, context);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
		return mAndroidHttpClient.execute(target, request);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
		return mAndroidHttpClient.execute(target, request, context);
	}

	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		return mAndroidHttpClient.execute(request, responseHandler);
	}

	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		return mAndroidHttpClient.execute(request, responseHandler, context);
	}

	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		return mAndroidHttpClient.execute(target, request, responseHandler);
	}

	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler,
			HttpContext context) throws IOException, ClientProtocolException {
		return mAndroidHttpClient.execute(target, request, responseHandler, context);
	}

}
