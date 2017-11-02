package com.expedia.bookings.unit;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.junit.Test;

import com.expedia.bookings.data.BaseApiResponse;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory ;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import io.reactivex.Observable;

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

		Retrofit adapter = new Retrofit.Builder()
			.baseUrl(endpoint)
			.client(new OkHttpClient())
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.create())
			.build();

		LameApi api = adapter.create(LameApi.class);
		api.foo();
	}

	public interface LameApi {
		@GET("/foo")
		Observable<BaseApiResponse> foo();
	}
}
