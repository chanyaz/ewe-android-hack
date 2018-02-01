package com.expedia.bookings.data.trips;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.json.JSONUtils;

public class TripHotel extends TripComponent {

	private Property mProperty;
	private String mCheckInTime;
	private String mCheckOutTime;
	private int mGuests;
	private Set<String> mConfirmationNumbers;
	private String mSharableDetailsUrl;
	private Traveler mPrimaryTraveler; // Used in sharedItin.
	private List<TripHotelRoom> mRooms = new ArrayList<>();
	private List<String> changeAndCancelRules = new ArrayList<>();
	private TripAction action;

	public TripHotel() {
		super(Type.HOTEL);
	}

	public Property getProperty() {
		return mProperty;
	}

	public void setProperty(Property property) {
		mProperty = property;
	}

	public String getCheckInTime() {
		return mCheckInTime;
	}

	public void setCheckInTime(String checkInTime) {
		mCheckInTime = checkInTime;
	}

	public String getCheckOutTime() {
		return mCheckOutTime;
	}

	public void setCheckOutTime(String checkOutTime) {
		mCheckOutTime = checkOutTime;
	}

	public int getGuests() {
		return mGuests;
	}

	public void setGuests(int guests) {
		mGuests = guests;
	}

	public Set<String> getConfirmationNumbers() {
		return mConfirmationNumbers;
	}

	public void addConfirmationNumber(String confirmationNumber) {
		if (mConfirmationNumbers == null) {
			mConfirmationNumbers = new LinkedHashSet<String>();
		}

		mConfirmationNumbers.add(confirmationNumber);
	}

	public void setConfirmationNumbers(Set<String> confirmationNumbers) {
		mConfirmationNumbers = confirmationNumbers;
	}

	public Traveler getPrimaryTraveler() {
		return mPrimaryTraveler;
	}

	public void setPrimaryTraveler(Traveler primaryTraveler) {
		this.mPrimaryTraveler = primaryTraveler;
	}

	public void addRoom(TripHotelRoom room) {
		getRooms().add(room);
	}

	public List<TripHotelRoom> getRooms() {
		return mRooms;
	}

	public void setChangeAndCancelRules(List<String> rules) {
		changeAndCancelRules = rules;
	}

	public List<String> getChangeAndCancelRules() {
		return changeAndCancelRules;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONable(obj, "property", mProperty);
			obj.put("guests", mGuests);
			obj.putOpt("checkInTime", mCheckInTime);
			obj.putOpt("checkOutTime", mCheckOutTime);
			JSONUtils.putStringList(obj, "confNumbers", mConfirmationNumbers);
			obj.putOpt("sharableItemDetailURL", mSharableDetailsUrl);
			JSONUtils.putJSONable(obj, "primaryTraveler", mPrimaryTraveler);
			GsonUtil.putListForJsonable(obj, "rooms", mRooms);
			JSONUtils.putStringList(obj, "changeAndCancelRules", changeAndCancelRules);
			JSONUtils.putJSONable(obj, "action", action);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mProperty = JSONUtils.getJSONable(obj, "property", Property.class);
		mGuests = obj.optInt("guests");
		mCheckInTime = obj.optString("checkInTime", null);
		mCheckOutTime = obj.optString("checkOutTime", null);
		mSharableDetailsUrl = obj.optString("sharableItemDetailURL");
		mPrimaryTraveler = JSONUtils.getJSONable(obj, "primaryTraveler", Traveler.class);
		List<TripHotelRoom> rooms = GsonUtil.getListForJsonable(obj, "rooms", TripHotelRoom.Companion.getGsonTypeToken());
		mRooms = (rooms != null) ? rooms : new ArrayList<TripHotelRoom>();

		List<String> confNumbers = JSONUtils.getStringList(obj, "confNumbers");
		if (confNumbers != null && confNumbers.size() > 0) {
			mConfirmationNumbers = new LinkedHashSet<String>();
			mConfirmationNumbers.addAll(confNumbers);
		}

		List<String> rules = JSONUtils.getStringList(obj, "changeAndCancelRules");
		if (rules != null && rules.size() > 0) {
			changeAndCancelRules = rules;
		}
		action = GsonUtil.getForJsonable(obj, "action", TripAction.class);

		return true;
	}

	public TripAction getAction() {
		return action;
	}

	public void setAction(TripAction action) {
		this.action = action;
	}
}
