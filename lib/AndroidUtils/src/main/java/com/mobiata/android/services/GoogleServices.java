package com.mobiata.android.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;

import com.mobiata.android.BackgroundDownloader.DownloadListener;
import com.mobiata.android.Log;
import com.mobiata.android.net.AndroidHttpClient;
import com.mobiata.android.net.GeocoderResponseHandler;
import com.mobiata.android.util.NetUtils;

@SuppressWarnings("unchecked")
public class GoogleServices implements DownloadListener {

	private static final String USER_AGENT = "Mobiata/1.0";

	private Context mContext;

	private HttpRequestBase mGet;
	private boolean mCancellingDownload;

	public GoogleServices(Context context) {
		mContext = context;
	}

	//////////////////////////////////////////////////////////////////////////
	// Geocoder
	//
	// Documentation: http://code.google.com/apis/maps/documentation/geocoding/
	//

	public List<Address> geocode(String query) {
		List<BasicNameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("sensor", "true"));
		params.add(new BasicNameValuePair("address", query));

		addLanguage(params);

		return (List<Address>) doRequest("http://maps.googleapis.com/maps/api/geocode/json", params,
				new GeocoderResponseHandler());
	}

	//////////////////////////////////////////////////////////////////////////
	// Static maps
	//
	// Documentation: http://code.google.com/apis/maps/documentation/geocoding/
	//

	public enum MapType {
		ROADMAP("roadmap"),
		SATELLITE("satellite"),
		TERRAIN("terrain"),
		HYBRID("hybrid");

		private String mValue;

		private MapType(String value) {
			mValue = value;
		}

		protected String getValue() {
			return mValue;
		}
	}

	public static String getStaticMapUrl(int width, int height, int zoom, MapType mapType, double latitude,
			double longitude, String marker) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("center", latitude + "," + longitude));
		if (marker != null) {
			params.add(new BasicNameValuePair("markers", marker));
		}
		addStandardStaticMapParams(params, width, height, zoom, mapType);
		return getStaticMapUrl(params);
	}

	public static String getStaticPathMapUrl(int width, int height, MapType mapType, String path, List<String> markers) {
		List<String> paths = new ArrayList<String>();
		if (path != null) {
			paths.add(path);
		}
		return getStaticPathMapUrl(width, height, mapType, paths, markers);
	}

	public static String getStaticPathMapUrl(int width, int height, MapType mapType, List<String> paths,
			List<String> markers) {
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		for (String path : paths) {
			if (!TextUtils.isEmpty(path)) {
				params.add(new BasicNameValuePair("path", path));
			}
		}
		if (markers != null && markers.size() > 0) {
			for (String marker : markers) {
				params.add(new BasicNameValuePair("markers", marker));
			}
		}
		addStandardStaticPathMapParams(params, width, height, mapType);
		return getStaticMapUrl(params);
	}

	private static List<BasicNameValuePair> addStandardStaticPathMapParams(List<BasicNameValuePair> params,
			int width, int height, MapType mapType) {
		params.add(new BasicNameValuePair("sensor", "false"));
		params.add(new BasicNameValuePair("size", width + "x" + height));
		params.add(new BasicNameValuePair("maptype", mapType.getValue()));
		return params;
	}

	private static List<BasicNameValuePair> addStandardStaticMapParams(List<BasicNameValuePair> params, int width,
			int height, int zoom, MapType mapType) {
		params.add(new BasicNameValuePair("sensor", "true"));
		params.add(new BasicNameValuePair("size", width + "x" + height));
		params.add(new BasicNameValuePair("zoom", zoom + ""));
		params.add(new BasicNameValuePair("maptype", mapType.getValue()));
		return params;
	}

	private static String getStaticMapUrl(List<BasicNameValuePair> params) {
		HttpGet get = NetUtils.createHttpGet("https://maps.googleapis.com/maps/api/staticmap", params);
		return get.getURI().toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// Common code (among all the requests)

	private void addLanguage(List<BasicNameValuePair> params) {
		String language = Locale.getDefault().getLanguage();
		if (language != null && language.length() > 0) {
			params.add(new BasicNameValuePair("language", language));
		}
	}

	private Object doRequest(String url, List<BasicNameValuePair> params, ResponseHandler<?> responseHandler) {
		AndroidHttpClient client = AndroidHttpClient.newInstance(USER_AGENT, mContext);
		mGet = NetUtils.createHttpGet(url, params);
		AndroidHttpClient.modifyRequestToAcceptGzipResponse(mGet);
		Log.d("Google request: " + mGet.getURI());
		mCancellingDownload = false;
		try {
			return client.execute(mGet, responseHandler);
		}
		catch (IOException e) {
			if (mCancellingDownload) {
				Log.d("Google Service download cancelled", e);
			}
			else {
				Log.e("Error trying to get Google services", e);
			}
			return null;
		}
		finally {
			client.close();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// DownloadListener

	@Override
	public void onCancel() {
		Log.i("Cancelling download!");
		mCancellingDownload = true;
		if (mGet != null) {
			mGet.abort();
		}
	}
}
