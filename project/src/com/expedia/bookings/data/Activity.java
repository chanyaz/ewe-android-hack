package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Activity implements JSONable {

	private String mId;

	private Money mPrice;

	private String mTitle;
	private String mDetailsUrl;

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public Money getPrice() {
		return mPrice;
	}

	public void setPrice(Money price) {
		mPrice = price;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public String getDetailsUrl() {
		return mDetailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		mDetailsUrl = detailsUrl;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("id", mId);
			JSONUtils.putJSONable(obj, "price", mPrice);
			obj.putOpt("title", mTitle);
			obj.putOpt("detailsUrl", mDetailsUrl);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optString("id", null);
		mPrice = JSONUtils.getJSONable(obj, "price", Money.class);
		mTitle = obj.optString("title", mTitle);
		mDetailsUrl = obj.optString("detailsUrl", null);
		return true;
	}
}
