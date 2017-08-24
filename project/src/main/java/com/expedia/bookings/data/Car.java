package com.expedia.bookings.data;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.utils.GsonUtil;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Car implements JSONable {

	public enum Category {
		MINI(R.string.car_category_mini, R.string.share_template_short_car_type_mini),
		ECONOMY(R.string.car_category_economy, R.string.share_template_short_car_type_economy),
		COMPACT(R.string.car_category_compact, R.string.share_template_short_car_type_compact),
		MIDSIZE(R.string.car_category_midsize, R.string.share_template_short_car_type_midsize),
		STANDARD(R.string.car_category_standard, R.string.share_template_short_car_type_standard),
		FULLSIZE(R.string.car_category_fullsize, R.string.share_template_short_car_type_fullsize),
		PREMIUM(R.string.car_category_premium, R.string.share_template_short_car_type_premium),
		LUXURY(R.string.car_category_luxury, R.string.share_template_short_car_type_luxury),
		SPECIAL(R.string.car_category_special, R.string.share_template_short_car_type_special),
		MINI_ELITE(R.string.car_category_mini_elite, R.string.share_template_short_car_type_mini_elite),
		ECONOMY_ELITE(R.string.car_category_economy_elite, R.string.share_template_short_car_type_economy_elite),
		COMPACT_ELITE(R.string.car_category_compact_elite, R.string.share_template_short_car_type_compact_elite),
		MIDSIZE_ELITE(R.string.car_category_midsize_elite, R.string.share_template_short_car_type_midsize_elite),
		STANDARD_ELITE(R.string.car_category_standard_elite, R.string.share_template_short_car_type_standard_elite),
		FULLSIZE_ELITE(R.string.car_category_fullsize_elite, R.string.share_template_short_car_type_fullsize_elite),
		PREMIUM_ELITE(R.string.car_category_premium_elite, R.string.share_template_short_car_type_premium_elite),
		LUXURY_ELITE(R.string.car_category_luxury_elite, R.string.share_template_short_car_type_luxury_elite),
		OVERSIZE(R.string.car_category_oversize, R.string.share_template_short_car_type_oversize);

		private final int mResId;
		private final int mShareId;

		Category(int resId, int shareId) {
			mResId = resId;
			mShareId = shareId;
		}

		public int getCategoryResId() {
			return mResId;
		}

		public int getShareMessageResId() {
			return mShareId;
		}
	}

	private String mId;

	private String mConfNumber;

	private Money mPrice;

	private DateTime mPickUpDateTime;
	private Location mPickUpLocation;

	private DateTime mDropOffDateTime;
	private Location mDropOffLocation;

	private CarVendor mVendor;

	private CarCategory mCategory;
	private HotelMedia mCategoryImage;

	private CarType mType;

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

	public CarCategory getCategory() {
		return mCategory;
	}

	public void setCategory(CarCategory category) {
		mCategory = category;
	}

	public HotelMedia getCategoryImage() {
		return mCategoryImage;
	}

	public void setCategoryImage(HotelMedia categoryImage) {
		mCategoryImage = categoryImage;
	}

	public CarType getType() {
		return mType;
	}

	public void setType(CarType type) {
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

			GsonUtil.putForJsonable(obj, "price", mPrice);
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

		mPrice = GsonUtil.getForJsonable(obj, "price", Money.class);
		mPickUpDateTime = JodaUtils.getDateTimeFromJsonBackCompat(obj, "pickupJodaDateTime", "pickupDateTime");
		mPickUpLocation = JSONUtils.getJSONable(obj, "pickupLocation", Location.class);
		mDropOffDateTime = JodaUtils.getDateTimeFromJsonBackCompat(obj, "dropoffJodaDateTime", "dropoffDateTime");
		mDropOffLocation = JSONUtils.getJSONable(obj, "dropoffLocation", Location.class);

		mVendor = JSONUtils.getJSONable(obj, "vendor", CarVendor.class);

		mCategory = JSONUtils.getEnum(obj, "category", CarCategory.class);
		mCategoryImage = JSONUtils.getJSONable(obj, "categoryImage", HotelMedia.class);

		mType = JSONUtils.getEnum(obj, "type", CarType.class);

		return true;
	}

}
