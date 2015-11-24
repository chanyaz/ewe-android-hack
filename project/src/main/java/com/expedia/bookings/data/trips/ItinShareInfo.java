package com.expedia.bookings.data.trips;

import org.json.JSONObject;

import android.text.TextUtils;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class ItinShareInfo implements JSONable {

	public interface ItinSharable {
		ItinShareInfo getShareInfo();

		boolean getSharingEnabled();
	}

	private String mSharableDetailsUrl;
	private String mShortSharableDetailsUrl;

	public ItinShareInfo() {

	}

	/**
	 * Returns the shortened sharable details url if available, otherwise the long form sharable details url
	 *
	 * @return
	 */
	public String getSharableUrl() {
		return TextUtils.isEmpty(mShortSharableDetailsUrl) ? mSharableDetailsUrl : mShortSharableDetailsUrl;
	}

	public boolean hasSharableUrl() {
		return !TextUtils.isEmpty(getSharableUrl());
	}

	/**
	 * Returns the long form sharable details url
	 *
	 * @return
	 */
	public String getSharableDetailsUrl() {
		return mSharableDetailsUrl;
	}

	/**
	 * Returns the long form sharable details url in the form suitable for fetching
	 * json data. E.g. /m/ is replaces with /api/ via the static convertSharableUrlToApiUrl method.
	 *
	 * @return share url suitable for fetching api data.
	 */
	public String getSharableDetailsApiUrl() {
		return convertSharableUrlToApiUrl(getSharableDetailsUrl());
	}

	public void setSharableDetailsUrl(String sharableDetailsUrl) {
		mSharableDetailsUrl = sharableDetailsUrl;
	}

	public boolean hasSharableDetailsUrl() {
		return !TextUtils.isEmpty(mSharableDetailsUrl);
	}

	/**
	 * Returns shortened sharable details url
	 *
	 * @return
	 */
	public String getShortSharableDetailsUrl() {
		return mShortSharableDetailsUrl;
	}

	public void setShortSharableDetailsUrl(String shortSharableDetailsUrl) {
		mShortSharableDetailsUrl = shortSharableDetailsUrl;
	}

	public boolean hasShortSharableDetailsUrl() {
		return !TextUtils.isEmpty(mShortSharableDetailsUrl);
	}

	//////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = new JSONObject();
		try {
			obj.putOpt("sharableDetailsURL", mSharableDetailsUrl);
			obj.putOpt("shortSharableDetailsURL", mShortSharableDetailsUrl);
		}
		catch (Exception ex) {
			Log.w("Error in toJson", ex);
		}
		return obj;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mSharableDetailsUrl = obj.optString("sharableDetailsURL");
		mShortSharableDetailsUrl = obj.optString("shortSharableDetailsURL");
		return true;
	}

	//////////////////////////////////////////////////////////////
	// Static

	/**
	 * Typically our sharable itin urls come in the mobile form (/m/),
	 * however, we often want the api version of the url (/api/).
	 * This method does the conversion.
	 *
	 * @param url - mobile version of the sharable itin url
	 * @return - api version of the url argument
	 */
	public static String convertSharableUrlToApiUrl(String url) {
		if (url == null) {
			return "";
		}
		return url.replace("/m/", "/api/");
	}
}
