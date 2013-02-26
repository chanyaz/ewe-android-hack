package com.expedia.bookings.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Car implements JSONable {

	public static enum Category {
		MINI,
		ECONOMY,
		COMPACT,
		MIDSIZE,
		STANDARD,
		FULLSIZE,
		PREMIUM,
		LUXURY,
		SPECIAL, 
		MINI_ELITE,
		ECONOMY_ELITE,
		COMPACT_ELITE,
		MIDSIZE_ELITE, 
		STANDARD_ELITE,
		FULLSIZE_ELITE,
		PREMIUM_ELITE, 
		LUXURY_ELITE,
		OVERSIZE;
	}

	public static enum Type {
		TWO_DOOR_CAR,
		THREE_DOOR_CAR,
		FOUR_DOOR_CAR, 
		VAN,
		WAGON,
		LIMOUSINE,
		RECREATIONAL_VEHICLE,
		CONVERTIBLE,
		SPORTS_CAR,
		SUV,
		PICKUP_REGULAR_CAB,
		OPEN_AIR_ALL_TERRAIN,
		SPECIAL,
		COMMERCIAL_VAN_TRUCK,
		PICKUP_EXTENDED_CAB,
		SPECIAL_OFFER_CAR,
		COUPE,
		MONOSPACE,
		MOTORHOME,
		TWO_WHEEL_VEHICLE,
		ROADSTER,
		CROSSOVER;
	}

	private String mId;

	private String mConfNumber;

	private Money mPrice;

	private DateTime mPickUpDateTime;
	private Location mPickUpLocation;

	private DateTime mDropOffDateTime;
	private Location mDropOffLocation;

	private CarVendor mVendor;

	private Category mCategory;
	private Media mCategoryImage;

	private Type mType;

	public String getId() {
		return mId;
	}

	public void setId(String id) {
		mId = id;
	}

	public String getConfNumber() {
		return mConfNumber;
	}

	public void setConfNumber(String confNumber) {
		mConfNumber = confNumber;
	}

	public Money getPrice() {
		return mPrice;
	}

	public void setPrice(Money price) {
		mPrice = price;
	}

	public DateTime getPickUpDateTime() {
		return mPickUpDateTime;
	}

	public void setPickUpDateTime(DateTime pickUpDateTime) {
		mPickUpDateTime = pickUpDateTime;
	}

	public Location getPickUpLocation() {
		return mPickUpLocation;
	}

	public void setPickUpLocation(Location pickUpLocation) {
		mPickUpLocation = pickUpLocation;
	}

	public DateTime getDropOffDateTime() {
		return mDropOffDateTime;
	}

	public void setDropOffDateTime(DateTime dropOffDateTime) {
		mDropOffDateTime = dropOffDateTime;
	}

	public Location getDropOffLocation() {
		return mDropOffLocation;
	}

	public void setDropOffLocation(Location dropOffLocation) {
		mDropOffLocation = dropOffLocation;
	}

	public CarVendor getVendor() {
		return mVendor;
	}

	public void setVendor(CarVendor vendor) {
		mVendor = vendor;
	}

	public Category getCategory() {
		return mCategory;
	}

	public void setCategory(Category category) {
		mCategory = category;
	}

	public Media getCategoryImage() {
		return mCategoryImage;
	}

	public void setCategoryImage(Media categoryImage) {
		mCategoryImage = categoryImage;
	}

	public Type getType() {
		return mType;
	}

	public void setType(Type type) {
		mType = type;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();

			obj.putOpt("id", mId);

			obj.putOpt("confNumber", mConfNumber);

			JSONUtils.putJSONable(obj, "price", mPrice);
			JSONUtils.putJSONable(obj, "pickupDateTime", mPickUpDateTime);
			JSONUtils.putJSONable(obj, "pickupLocation", mPickUpLocation);
			JSONUtils.putJSONable(obj, "dropoffDateTime", mDropOffDateTime);
			JSONUtils.putJSONable(obj, "dropoffLocation", mDropOffLocation);

			JSONUtils.putJSONable(obj, "vendor", mVendor);

			JSONUtils.putEnum(obj, "category", mCategory);
			JSONUtils.putJSONable(obj, "categoryImage", mCategoryImage);

			JSONUtils.putEnum(obj, "type", mType);

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		mId = obj.optString("id", null);

		mConfNumber = obj.optString("confNumber", null);

		mPrice = JSONUtils.getJSONable(obj, "price", Money.class);
		mPickUpDateTime = JSONUtils.getJSONable(obj, "pickupDateTime", DateTime.class);
		mPickUpLocation = JSONUtils.getJSONable(obj, "pickupLocation", Location.class);
		mDropOffDateTime = JSONUtils.getJSONable(obj, "dropoffDateTime", DateTime.class);
		mDropOffLocation = JSONUtils.getJSONable(obj, "dropoffLocation", Location.class);

		mVendor = JSONUtils.getJSONable(obj, "vendor", CarVendor.class);

		mCategory = JSONUtils.getEnum(obj, "category", Category.class);
		mCategoryImage = JSONUtils.getJSONable(obj, "categoryImage", Media.class);

		mType = JSONUtils.getEnum(obj, "type", Type.class);

		return true;
	}

}
