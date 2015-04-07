package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Activity implements JSONable {

	private String mId;

	private Money mPrice;
	private String mTitle;
	private Integer mGuestsCount;
	private List<Traveler> mTravelers = new ArrayList<>();
	private String mVoucherPrintUrl;

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

	public int getGuestCount() {
		return mGuestsCount;
	}

	public void setGuestCount(Integer guestCount) {
		mGuestsCount = guestCount;
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
			GsonUtil.putForJsonable(obj, "price", mPrice);
			obj.putOpt("title", mTitle);
			obj.put("guestCount", mGuestsCount);
			JSONUtils.putJSONableList(obj, "travelers", mTravelers);
			obj.put("voucherPrintUrl", mVoucherPrintUrl);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optString("id", null);
		mPrice = GsonUtil.getForJsonable(obj, "price", Money.class);
		mTitle = obj.optString("title", mTitle);
		mGuestsCount = obj.optInt("guestCount");
		mTravelers = JSONUtils.getJSONableList(obj, "travelers", Traveler.class);
		mVoucherPrintUrl = obj.optString("voucherPrintUrl");
		return true;
	}
}
