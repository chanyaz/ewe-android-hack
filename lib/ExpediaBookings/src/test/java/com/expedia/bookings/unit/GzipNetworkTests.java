package com.expedia.bookings.unit;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import retrofit.RestAdapter;
import retrofit.http.GET;
import okio.Buffer;

import static org.junit.Assert.assertEquals;

public class GzipNetworkTests {

	private static final String TEST_STRING = "{foo}";
	private static final String GZIP_TEST_STRING = string(gzip(TEST_STRING));

	private static byte[] gzip(String str) {
		byte[] bytes;
		try {
			bytes = str.getBytes("UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		try {
			ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
			OutputStream gzippedOut = new GZIPOutputStream(bytesOut);
			gzippedOut.write(bytes);
			gzippedOut.close();
			return bytesOut.toByteArray();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String string(byte[] bytes) {
		try {
			return new String(bytes, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Test
	public void testOkHttpHandlesGzip() throws Throwable {
		MockWebServer mockWebServer = new MockWebServer();
		Buffer gzipData = new Buffer().write(gzip(TEST_STRING));

		mockWebServer.enqueue(new MockResponse()
			// Don't decode
			.setBody(gzipData.clone()));

		mockWebServer.enqueue(new MockResponse()
			.setHeader("Content-Encoding", "gzip")
			.setBody(gzipData.clone()));

		mockWebServer.start();

		String endpoint = "http://localhost:" + mockWebServer.getPort();
		OkHttpClient client = new OkHttpClient();

		Request request = new Request.Builder()
			.url(endpoint)
			.build();

		Response response = client.newCall(request).execute();
		assertEquals(GZIP_TEST_STRING, response.body().string());

		response = client.newCall(request).execute();
		assertEquals(TEST_STRING, response.body().string());

		mockWebServer.shutdown();
	}

	@Test
	public void testRetrofitAddsGzipHeaders() throws Throwable {
		MockWebServer mockWebServer = new MockWebServer();
		mockWebServer.setDispatcher(new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
				assertEquals("gzip", request.getHeader("Accept-Encoding"));
				return new MockResponse().setBody("1");
			}
		});

		mockWebServer.start();

		String endpoint = "http://localhost:" + mockWebServer.getPort();

		RestAdapter adapter = new RestAdapter.Builder()
			.setEndpoint(endpoint)
				//.setLogLevel(RestAdapter.LogLevel.FULL)
			.build();

		LameApi api = adapter.create(LameApi.class);
		api.foo();
	}

	public interface LameApi {
		@GET("/foo")
		int foo();
	}
}
