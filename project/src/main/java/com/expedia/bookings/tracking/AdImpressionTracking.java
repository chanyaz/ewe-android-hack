package com.expedia.bookings.tracking;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.abacus.AbacusTest;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.Ui;

public class AdImpressionTracking {
	public static final String CONVERSION_URL = "ads/hooklogic";

	public static void trackAdClickOrImpression(final Context context, final String url,
		final List<BasicNameValuePair> query) {
		new AsyncTask<String, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(String... params) {
				ExpediaServices services = new ExpediaServices(context);
				boolean success = services.trackTravelAd(url, query);
				return success;
			}
		}.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
	}

	public static void trackAdClickOrImpressionWithTest(final Context context, final String url, final int testKey,
		final List<BasicNameValuePair> query) {
		if (url == null || url.trim().isEmpty()) {
			return;
		}
		AbacusTest abacusTest = Db.getAbacusResponse().testForKey(testKey);
		String analyticsToAppend = AbacusUtils.getAnalyticsString(abacusTest);
		String urlToUse = url;
		if (analyticsToAppend != null && !analyticsToAppend.isEmpty()) {
			urlToUse = AdImpressionTracking.appendUrlTestVersion(url, analyticsToAppend);
		}
		AdImpressionTracking.trackAdClickOrImpression(context, urlToUse, query);
	}

	private static String appendUrlTestVersion(String url, String analyticsToAppend) {
		String paramToAppend = "testVersionOverride=";
		int index = url.indexOf(paramToAppend);

		if (index == -1) {
			paramToAppend = "testVersion=";
			index = url.indexOf(paramToAppend);

			if (index == -1) {
				return url + "&testVersion=" + analyticsToAppend;
			}
		}

		index += paramToAppend.length();

		return new StringBuilder(url).insert(index, analyticsToAppend + "%2C").toString();
	}

	public static void trackAdConversion(final Context context, final String tripId) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("tripId", tripId));
		query.add(new BasicNameValuePair("clientid", ServicesUtil.generateClientId(context)));
		String endpoint = Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointUrl();
		String baseUrl = endpoint + CONVERSION_URL;
		trackAdClickOrImpression(context, baseUrl, query);
	}
}
