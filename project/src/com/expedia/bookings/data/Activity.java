package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Activity implements JSONable {

	private String mId;

	private Money mPrice;
	private String mTitle;
	private String mDetailsUrl;
	private Integer mGuestsCount;
	private List<Traveler> mTravlers;

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
		if (mTravlers != null) {
			return mTravlers.get(index);
		}

		return null;
	}

	public List<Traveler> getTravlers() {
		return mTravlers;
	}

	public void addTravler(Traveler traveler) {
		if (mTravlers == null) {
			mTravlers = new ArrayList<Traveler>();
		}

		mTravlers.add(traveler);
	}

	public void addTravlers(Collection<? extends Traveler> travelers) {
		if (mTravlers == null) {
			mTravlers = new ArrayList<Traveler>();
		}

		mTravlers.addAll(travelers);
	}

	public void setTravlers(Collection<? extends Traveler> travelers) {
		mTravlers = new ArrayList<Traveler>(travelers);
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
		return true;
	}
}
