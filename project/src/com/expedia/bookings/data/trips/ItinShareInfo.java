package com.expedia.bookings.data.trips;

import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONable;

public class ItinShareInfo implements JSONable {

	public interface ItinSharable {
		public ItinShareInfo getShareInfo();

		public boolean getSharingEnabled();
	}

	private String mSharableDetailsUrl;
	private String mShortSharableDetailsUrl;

	public ItinShareInfo() {

	}

	/**
	 * Returns the shortened sharable details url if available, otherwise the long form sharable details url
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
	 * @return
	 */
	public String getSharableDetailsUrl() {
		return mSharableDetailsUrl;
	}

	public void setSharableDetailsUrl(String sharableDetailsUrl) {
		mSharableDetailsUrl = sharableDetailsUrl;
	}

	public boolean hasSharableDetailsUrl() {
		return !TextUtils.isEmpty(mSharableDetailsUrl);
	}

	/**
	 * Returns shortened sharable details url
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
}
