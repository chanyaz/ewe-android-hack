package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Activity implements JSONable {

	private String mId;

	private String mTitle;
	private Integer mGuestsCount;
	private List<Traveler> mTravelers = new ArrayList<>();
	private String mVoucherPrintUrl;
	private String mImageUrl;

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(String title) {
		mTitle = title;
	}

	public int getGuestCount() {
		return mGuestsCount;
	}

	public String getImageUrl() {
		return mImageUrl;
	}

	public void setGuestCount(Integer guestCount) {
		mGuestsCount = guestCount;
	}

	public void setImageUrl(String imageUrl) {
		mImageUrl = imageUrl;
	}

	public List<Traveler> getTravelers() {
		return mTravelers;
	}

	public void addTraveler(Traveler traveler) {
		mTravelers.add(traveler);
	}

	public String getVoucherPrintUrl() {
		return mVoucherPrintUrl;
	}

	public void setVoucherPrintUrl(String argName) {
		mVoucherPrintUrl = argName;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("id", mId);
			obj.putOpt("title", mTitle);
			obj.put("guestCount", mGuestsCount);
			JSONUtils.putJSONableList(obj, "travelers", mTravelers);
			obj.put("voucherPrintUrl", mVoucherPrintUrl);
			obj.put("imageUrl", mImageUrl);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optString("id", null);
		mTitle = obj.optString("title", mTitle);
		mGuestsCount = obj.optInt("guestCount");
		mTravelers = JSONUtils.getJSONableList(obj, "travelers", Traveler.class);
		mVoucherPrintUrl = obj.optString("voucherPrintUrl");
		mImageUrl = obj.optString("imageUrl");
		return true;
	}
}
