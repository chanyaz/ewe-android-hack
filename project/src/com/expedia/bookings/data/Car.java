package com.expedia.bookings.data;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Car implements JSONable {

	public static enum Category {
		MINI(R.string.car_category_mini),
		ECONOMY(R.string.car_category_economy),
		COMPACT(R.string.car_category_compact),
		MIDSIZE(R.string.car_category_midsize),
		STANDARD(R.string.car_category_standard),
		FULLSIZE(R.string.car_category_fullsize),
		PREMIUM(R.string.car_category_premium),
		LUXURY(R.string.car_category_luxury),
		SPECIAL(R.string.car_category_special),
		MINI_ELITE(R.string.car_category_mini_elite),
		ECONOMY_ELITE(R.string.car_category_economy_elite),
		COMPACT_ELITE(R.string.car_category_compact_elite),
		MIDSIZE_ELITE(R.string.car_category_midsize_elite),
		STANDARD_ELITE(R.string.car_category_standard_elite),
		FULLSIZE_ELITE(R.string.car_category_fullsize_elite),
		PREMIUM_ELITE(R.string.car_category_premium_elite),
		LUXURY_ELITE(R.string.car_category_luxury_elite),
		OVERSIZE(R.string.car_category_oversize);

		private int mResId;

		private Category(int resId) {
			mResId = resId;
		}

		public int getCategoryResId() {
			return mResId;
		}
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
			JodaUtils.putDateTimeInJson(obj, "pickupJodaDateTime", mPickUpDateTime);
			JSONUtils.putJSONable(obj, "pickupLocation", mPickUpLocation);
			JodaUtils.putDateTimeInJson(obj, "dropoffJodaDateTime", mDropOffDateTime);
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
		mPickUpDateTime = JodaUtils.getDateTimeFromJsonBackCompat(obj, "pickupJodaDateTime", "pickupDateTime");
		mPickUpLocation = JSONUtils.getJSONable(obj, "pickupLocation", Location.class);
		mPickUpDateTime = JodaUtils.getDateTimeFromJsonBackCompat(obj, "dropoffJodaDateTime", "dropoffDateTime");
		mDropOffLocation = JSONUtils.getJSONable(obj, "dropoffLocation", Location.class);

		mVendor = JSONUtils.getJSONable(obj, "vendor", CarVendor.class);

		mCategory = JSONUtils.getEnum(obj, "category", Category.class);
		mCategoryImage = JSONUtils.getJSONable(obj, "categoryImage", Media.class);

		mType = JSONUtils.getEnum(obj, "type", Type.class);

		return true;
	}

}
