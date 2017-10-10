package com.mobiata.android.net;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;

/**
 * Important info: if the response is a JSONArray, it's wrapped as such:
 * 
 * {
 *   "response": [ ... ]
 * }
 * 
 */
public abstract class JsonResponseHandler<T> implements ResponseHandler<T> {

	// Don't log the response excessively; if we reach the max number of lines
	// give up (otherwise we might end up flushing all of the log by accident).
	private static final int MAX_LINES = 20;
	private static final Pattern PATTERN_NEWLINE = Pattern.compile("\n");

	public abstract T handleJson(JSONObject response);

	@Override
	public T handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
		if (response == null) {
			return null;
		}

		try {
			if (Log.isLoggingEnabled()) {
				StringBuilder httpInfo = new StringBuilder();
				httpInfo.append(response.getStatusLine().toString());
				for (Header header : response.getAllHeaders()) {
					httpInfo.append(" ");
					httpInfo.append(header.toString());
				}
				Log.v(httpInfo.toString());
			}

			String responseStr = NetUtils.toString(response.getEntity(), HTTP.UTF_8);
			if (Log.isLoggingEnabled()) {
				Log.v("Response length: " + responseStr.length());
			}

			// This is kind of ill-defined behavior right now; even though the response is
			// empty I'm going to return an empty object, so that the response handler
			// can use it.
			if (TextUtils.isEmpty(responseStr)) {
				return handleJson(new JSONObject());
			}

			if (Log.isLoggingEnabled()) {
				Log.v("Response length: " + responseStr.length());

				boolean output = true;
				Matcher m = PATTERN_NEWLINE.matcher(responseStr);
				int lineCount = 0;
				while (m.find()) {
					lineCount++;
					if (lineCount > MAX_LINES) {
						output = false;
						break;
					}
				}

				if (output) {
					Log.dump("Response: " + responseStr, Log.LEVEL_VERBOSE);
				}
				else {
					Log.dump("Response: " + responseStr.substring(0, m.start()), Log.LEVEL_VERBOSE);
					Log.v("<Response snipped for size>");
				}
			}

			// This is a hack to get around possible arrays
			JSONObject jsonResponse;
			if (responseStr.startsWith("[")) {
				jsonResponse = new JSONObject();
				jsonResponse.put("response", new JSONArray(responseStr));
			}
			else {
				jsonResponse = new JSONObject(responseStr);
			}

			return handleJson(jsonResponse);
		}
		catch (IOException e) {
			Log.e("Server request failed.", e);
		}
		catch (JSONException e) {
			Log.e("Could not parse server response.", e);
		}

		return null;
	}
}
