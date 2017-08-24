package com.expedia.bookings.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import android.text.TextUtils;

import com.expedia.bookings.data.GsonResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.SuggestionV2TypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobiata.android.Log;
import okhttp3.Response;

public class AutoJsonResponseHandler<T> implements ResponseHandler<GsonResponse<T>> {

	private final Class<T> mType;

	public AutoJsonResponseHandler(Class<T> type) {
		mType = type;
	}

	@Override
	public GsonResponse<T> handleResponse(Response response) throws IOException {
		if (response == null) {
			return null;
		}

		try {
			if (Log.isLoggingEnabled()) {
				StringBuilder httpInfo = new StringBuilder();
				httpInfo.append("HTTP " + response.code());
				httpInfo.append("\n");
				httpInfo.append(response.headers().toString());
				Log.v(httpInfo.toString());
			}

			if (response.code() != 200) {
				return null;
			}

			String contentEncoding = response.headers().get("Content-Encoding");
			InputStream is;
			if (!TextUtils.isEmpty(contentEncoding) && "gzip".equalsIgnoreCase(contentEncoding)) {
				is = new GZIPInputStream(response.body().byteStream());
			}
			else {
				is = response.body().byteStream();
			}

			Gson gson = new GsonBuilder() //
			     .registerTypeAdapter(SuggestionV2.class, new SuggestionV2TypeAdapter()) //
			     .create();

			InputStreamReader isr = new InputStreamReader(is);
			T obj = gson.fromJson(isr, mType);
			response.body().close();
			return new GsonResponse(obj);
		}
		catch (IOException e) {
			Log.e("Server request failed.", e);
		}
		catch (Exception e) {
			Log.e("Could not parse server response.", e);
		}

		return null;
	}
}
