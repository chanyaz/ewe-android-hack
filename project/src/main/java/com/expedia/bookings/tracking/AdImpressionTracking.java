package com.expedia.bookings.tracking;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.os.AsyncTask;

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

	public static void trackAdConversion(final Context context, final String tripId) {
		List<BasicNameValuePair> query = new ArrayList<BasicNameValuePair>();
		query.add(new BasicNameValuePair("tripId", tripId));
		query.add(new BasicNameValuePair("clientid", ServicesUtil.generateClientId(context)));
		String endpoint = Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointUrl();
		String baseUrl = endpoint + CONVERSION_URL;
		trackAdClickOrImpression(context, baseUrl, query);
	}
}
