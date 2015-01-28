package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collection;
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
	private String mDetailsUrl;
	private Integer mGuestsCount;
	private List<Traveler> mTravelers;
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

	public String getDetailsUrl() {
		return mDetailsUrl;
	}

	public void setDetailsUrl(String detailsUrl) {
		mDetailsUrl = detailsUrl;
	}

	public int getGuestCount() {
		return mGuestsCount;
	}

	public void setGuestCount(Integer guestCount) {
		mGuestsCount = guestCount;
	}

	public Traveler getTraveler(int index) {
		if (mTravelers != null) {
			return mTravelers.get(index);
		}

		return null;
	}

	public List<Traveler> getTravelers() {
		return mTravelers;
	}

	public void addTraveler(Traveler traveler) {
		if (mTravelers == null) {
			mTravelers = new ArrayList<Traveler>();
		}

		mTravelers.add(traveler);
	}

	public void addTravelers(Collection<? extends Traveler> travelers) {
		if (mTravelers == null) {
			mTravelers = new ArrayList<Traveler>();
		}

		mTravelers.addAll(travelers);
	}

	public void setTravelers(Collection<? extends Traveler> travelers) {
		mTravelers = new ArrayList<Traveler>(travelers);
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
			obj.put("detailsUrl", mDetailsUrl);
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
		mDetailsUrl = obj.optString("detailsUrl");
		mGuestsCount = obj.optInt("guestCount");
		mTravelers = JSONUtils.getJSONableList(obj, "travelers", Traveler.class);
		mVoucherPrintUrl = obj.optString("voucherPrintUrl");
		return true;
	}
}
