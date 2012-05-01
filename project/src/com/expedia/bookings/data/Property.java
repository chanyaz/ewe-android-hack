package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Property implements JSONable {

	public static enum Amenity {
		BUSINESS_CENTER(0x0000001, R.string.AmenityBusinessCenter),
		FITNESS_CENTER(0x0000002, R.string.AmenityFitnessCenter),
		HOT_TUB(0x0000004, R.string.AmenityHotTub),
		INTERNET(0x0000008, R.string.AmenityInternet),
		KIDS_ACTIVITIES(0x0000010, R.string.AmenityKidsActivities),
		KITCHEN(0x0000020, R.string.AmenityKitchen),
		PETS_ALLOWED(0x0000040, R.string.AmenityPetsAllowed),
		POOL(0x0000080, R.string.AmenityPool),
		RESTAURANT(0x0000100, R.string.AmenityRestaurant),
		SPA(0x0000200, R.string.AmenitySpa),
		WHIRLPOOL_BATH(0x0000400, R.string.AmenityWhirlpoolBath),
		BREAKFAST(0x0000800, R.string.AmenityBreakfast),
		BABYSITTING(0x0001000, R.string.AmenityBabysitting),
		JACUZZI(0x0002000, R.string.AmenityJacuzzi),
		PARKING(0x0004000, R.string.AmenityParking),
		ROOM_SERVICE(0x0008000, R.string.AmenityRoomService),
		ACCESSIBLE_PATHS(0x0010000, R.string.AmenityAccessiblePaths),
		ACCESSIBLE_BATHROOM(0x0020000, R.string.AmenityAccessibleBathroom),
		ROLL_IN_SHOWER(0x0040000, R.string.AmenityRollInShower),
		HANDICAPPED_PARKING(0x0080000, R.string.AmenityHandicappedParking),
		IN_ROOM_ACCESSIBILITY(0x0100000, R.string.AmenityInRoomAccessibility),
		DEAF_ACCESSIBILITY_EQUIPMENT(0x0200000, R.string.AmenityDeafAccessibilityEquipment),
		BRAILLE_SIGNAGE(0x0400000, R.string.AmenityBrailleSignage),
		FREE_AIRPORT_SHUTTLE(0x0800000, R.string.AmenityFreeAirportShuttle),
		POOL_INDOOR(0x1000000, R.string.AmenityPoolIndoor),
		POOL_OUTDOOR(0x2000000, R.string.AmenityPoolOutdoor),
		EXTENDED_PARKING(0x4000000, R.string.AmenityExtendedParking),
		FREE_PARKING(0x8000000, R.string.AmenityFreeParking);

		private int flag;
		private int strId;

		private Amenity(int flag, int strId) {
			this.flag = flag;
			this.strId = strId;
		}

		public boolean inMask(int amenityMask) {
			return (amenityMask & flag) != 0;
		}

		public int getFlag() {
			return flag;
		}

		public int getStrId() {
			return strId;
		}
	}

	// Data about the hotel that generally does not change
	private String mName;
	private String mPropertyId;
	private Location mLocation;
	private String mDescriptionText;
	private Media mThumbnail;
	private List<Media> mMedia;

	// These change based on when the user requests data
	private boolean mAvailable;
	private Distance mDistanceFromUser;

	// Expedia specific detail
	private int mAmenityMask;
	private boolean mHasAmenitiesSet;
	private String mSupplierType; // E == merchant, S or W == GDS
	private Rate mLowestRate;

	// Hotel rating ranges from 0-5, in .5 intervals
	private double mHotelRating;

	// Expedia user reviews
	private int mTotalReviews;
	private int mTotalRecommendations;
	private double mAverageExpediaRating;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getPropertyId() {
		return mPropertyId;
	}

	public void setPropertyId(String propertyId) {
		this.mPropertyId = propertyId;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(Location location) {
		this.mLocation = location;
	}

	public String getDescriptionText() {
		return mDescriptionText;
	}

	public void setDescriptionText(String descriptionText) {
		this.mDescriptionText = descriptionText;
	}

	public boolean hasDescriptionText() {
		return this.mDescriptionText != null && this.mDescriptionText.length() > 0;
	}

	public Media getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(Media thumbnail) {
		this.mThumbnail = thumbnail;
	}

	public int getAmenityMask() {
		return mAmenityMask;
	}

	public void setAmenityMask(int amenityMask) {
		mHasAmenitiesSet = true;
		mAmenityMask = amenityMask;
	}

	public boolean hasAmenity(Amenity amenity) {
		return amenity.inMask(mAmenityMask);
	}

	public boolean hasAmenities() {
		return mAmenityMask != 0;
	}

	public boolean hasAmenitiesSet() {
		return mHasAmenitiesSet;
	}

	public void addMedia(Media media) {
		if (mMedia == null) {
			mMedia = new ArrayList<Media>();
		}
		mMedia.add(media);
	}

	public Media getMedia(int index) {
		if (mMedia == null) {
			return null;
		}
		return mMedia.get(index);
	}

	public List<Media> getMediaList() {
		return mMedia;
	}

	public void setMediaList(List<Media> theList) {
		this.mMedia = theList;
	}

	public int getMediaCount() {
		if (mMedia == null) {
			return 0;
		}
		return mMedia.size();
	}

	public boolean isAvailable() {
		return mAvailable;
	}

	public void setAvailable(boolean available) {
		this.mAvailable = available;
	}

	public Distance getDistanceFromUser() {
		return mDistanceFromUser;
	}

	public void setDistanceFromUser(Distance distanceFromUser) {
		this.mDistanceFromUser = distanceFromUser;
	}

	public String getSupplierType() {
		return mSupplierType;
	}

	public void setSupplierType(String supplierType) {
		mSupplierType = supplierType;
	}

	public void setHotelRating(double hotelRating) {
		this.mHotelRating = hotelRating;
	}

	public double getHotelRating() {
		return mHotelRating;
	}

	public void setTotalReviews(int totalReviews) {
		mTotalReviews = totalReviews;
	}

	public int getTotalReviews() {
		return mTotalReviews;
	}

	public void setTotalRecommendations(int totalRecommendations) {
		mTotalRecommendations = totalRecommendations;
	}

	public int getTotalRecommendations() {
		return mTotalRecommendations;
	}

	public void setAverageExpediaRating(double averageExpediaRating) {
		mAverageExpediaRating = averageExpediaRating;
	}

	public double getAverageExpediaRating() {
		return mAverageExpediaRating;
	}

	public boolean hasExpediaReviews() {
		return mPropertyId != null && mTotalReviews != 0;
	}

	public void setLowestRate(Rate rate) {
		mLowestRate = rate;
	}

	public Rate getLowestRate() {
		return mLowestRate;
	}

	// Only valid for Expedia
	public boolean isMerchant() {
		return mSupplierType != null && (mSupplierType.equals("E") || mSupplierType.equals("MERCHANT"));
	}

	public boolean isHighlyRated() {
		return getAverageExpediaRating() >= Filter.HIGH_USER_RATING;
	}

	// Updates a Property from another Property (currently, one returned via an AvailabilityResponse)
	public void updateFrom(Property property) {
		if (property.hasAmenitiesSet()) {
			this.setAmenityMask(property.getAmenityMask());
		}
		if (property.getDescriptionText() != null) {
			if (this.mDescriptionText == null || this.mDescriptionText.length() < property.getDescriptionText().length()) {
				this.setDescriptionText(property.getDescriptionText());
			}
		}
		if (property.getMediaList() != null) {
			this.setMediaList(property.getMediaList());
		}
	}

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("name", mName);
			obj.putOpt("propertyId", mPropertyId);
			JSONUtils.putJSONable(obj, "location", mLocation);
			obj.putOpt("description", mDescriptionText);
			JSONUtils.putJSONable(obj, "thumbnail", mThumbnail);
			JSONUtils.putJSONableList(obj, "media", mMedia);
			obj.putOpt("available", mAvailable);
			JSONUtils.putJSONable(obj, "distanceFromUser", mDistanceFromUser);
			obj.putOpt("supplierType", mSupplierType);
			obj.putOpt("amenityMask", mAmenityMask);
			obj.putOpt("hotelRating", mHotelRating);
			obj.putOpt("totalReviews", mTotalReviews);
			obj.putOpt("totalRecommendations", mTotalRecommendations);
			obj.putOpt("averageExpediaRating", mAverageExpediaRating);

			JSONUtils.putJSONable(obj, "lowestRate", mLowestRate);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Property object to JSON.", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mPropertyId = obj.optString("propertyId", null);
		mLocation = (Location) JSONUtils.getJSONable(obj, "location", Location.class);
		mDescriptionText = obj.optString("description", null);
		mThumbnail = (Media) JSONUtils.getJSONable(obj, "thumbnail", Media.class);
		mMedia = (List<Media>) JSONUtils.getJSONableList(obj, "media", Media.class);
		mAvailable = obj.optBoolean("available", false);
		mDistanceFromUser = (Distance) JSONUtils.getJSONable(obj, "distanceFromUser", Distance.class);
		mSupplierType = obj.optString("supplierType", null);
		mHotelRating = obj.optDouble("hotelRating");
		mTotalReviews = obj.optInt("totalReviews", 0);
		mTotalRecommendations = obj.optInt("totalRecommendations", 0);
		mAverageExpediaRating = obj.optDouble("averageExpediaRating", 0);
		mAmenityMask = obj.optInt("amenityMask");
		mLowestRate = (Rate) JSONUtils.getJSONable(obj, "lowestRate", Rate.class);

		return true;
	}

	@Override
	public String toString() {
		JSONObject obj = toJson();
		try {
			return obj.toString(2);
		}
		catch (JSONException e) {
			return obj.toString();
		}
	}

	/**
	 * Can be used to compare two properties based on lowest rate
	 */
	public static final Comparator<Property> PRICE_COMPARATOR = new Comparator<Property>() {
		@Override
		public int compare(Property property1, Property property2) {
			// Use lowestRate if available; otherwise fall back on lowRate.
			Rate lowestRate1 = property1.getLowestRate();
			Rate lowestRate2 = property2.getLowestRate();

			Money lowRate1 = lowestRate1.getDisplayRate();
			Money lowRate2 = lowestRate2.getDisplayRate();

			// Check that we have rates to compare first
			if (lowRate1 == null && lowRate2 == null) {
				return NAME_COMPARATOR.compare(property1, property2);
			}
			else if (lowRate1 == null) {
				return -1;
			}
			else if (lowRate2 == null) {
				return 1;
			}

			// Compare rates
			double amount1 = lowRate1.getAmount();
			double amount2 = lowRate2.getAmount();
			if (amount1 == amount2) {
				return NAME_COMPARATOR.compare(property1, property2);
			}
			else if (amount1 > amount2) {
				return 1;
			}
			else {
				return -1;
			}
		}
	};

	public static final Comparator<Property> NAME_COMPARATOR = new Comparator<Property>() {
		@Override
		public int compare(Property property1, Property property2) {
			return property1.getName().compareTo(property2.getName());
		}
	};

	// It's worth noting that this comparator is DESCENDING, in that the highest-rated
	// properties end up at the top of the list.
	public static final Comparator<Property> RATING_COMPARATOR = new Comparator<Property>() {
		@Override
		public int compare(Property property1, Property property2) {
			double rating1 = property1.getAverageExpediaRating();
			double rating2 = property2.getAverageExpediaRating();
			if (rating1 == rating2) {
				return PRICE_COMPARATOR.compare(property1, property2);
			}
			else if (rating1 > rating2) {
				return -1;
			}
			else {
				return 1;
			}
		}
	};

	public static final Comparator<Property> DISTANCE_COMPARATOR = new Comparator<Property>() {
		@Override
		public int compare(Property property1, Property property2) {
			Distance distance1 = property1.getDistanceFromUser();
			Distance distance2 = property2.getDistanceFromUser();

			if (distance1 == null && distance2 == null) {
				return PRICE_COMPARATOR.compare(property1, property2);
			}
			else if (distance1 == null) {
				return -1;
			}
			else if (distance2 == null) {
				return 1;
			}

			int cmp = distance1.compareTo(distance2);
			if (cmp == 0) {
				return NAME_COMPARATOR.compare(property1, property2);
			}
			else {
				return cmp;
			}
		}
	};
}
