package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Property implements JSONable {

	public static enum Amenity {
		// @formatter:off
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
		// @formatter:on

		private int flag;
		private int strId;

		private Amenity(int newFlag, int newStrId) {
			this.flag = newFlag;
			this.strId = newStrId;
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
	private String mLocalPhone;
	private String mTollFreePhone;

	// Text about the hotel
	private List<HotelTextSection> mOverviewText;
	private HotelTextSection mAmenitiesText;
	private HotelTextSection mPoliciesText;
	private HotelTextSection mFeesText;
	private HotelTextSection mMandatoryFeesText;
	private HotelTextSection mRenovationText;

	// These change based on when the user requests data
	private boolean mAvailable;
	private Distance mDistanceFromUser;
	private int mRoomsLeftAtThisRate;

	// Expedia specific detail
	private int mAmenityMask;
	private boolean mHasAmenitiesSet;
	private String mSupplierType; // E == merchant, S or W == GDS
	private Rate mLowestRate;
	private Money mHighestPriceFromSurvey;
	private boolean mIsLowestRateMobileExclusive = false;
	private boolean mIsLowestRateTonightOnly = false;
	private String mInfoSiteUrl;
	private String mTelephoneSalesNumber;
	private boolean mIsDesktopOverrideNumber;
	private boolean mIsVipAccess;

	// Hotel rating ranges from 0-5, in .5 intervals
	private double mHotelRating;

	// Expedia user reviews
	private int mTotalReviews;
	private int mTotalRecommendations;
	private double mAverageExpediaRating;

	// Convenient hotel search by name data
	private boolean mIsFromSearchByHotel = false;

	// Convenient itin data
	private String mItinRoomType;
	private String mItinBedType;

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

	public boolean isFromSearchByHotel() {
		return mIsFromSearchByHotel;
	}

	public void setIsFromSearchByHotel(boolean isFromSearchByHotel) {
		mIsFromSearchByHotel = isFromSearchByHotel;
	}

	public void setItinBedType(String bedType) {
		mItinBedType = bedType;
	}

	public String getItinBedType() {
		return mItinBedType;
	}

	public void setItinRoomType(String roomType) {
		mItinRoomType = roomType;
	}

	public String getItinRoomType() {
		return mItinRoomType;
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

	public boolean hasAnyAmenity(Amenity[] amenities) {
		if (amenities != null) {
			for (Amenity amenity : amenities) {
				if (hasAmenity(amenity)) {
					return true;
				}
			}
		}
		return false;
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

	public String getLocalPhone() {
		return mLocalPhone;
	}

	public void setLocalPhone(String localPhone) {
		mLocalPhone = localPhone;
	}

	public String getTollFreePhone() {
		return mTollFreePhone;
	}

	public void setTollFreePhone(String tollFreePhone) {
		mTollFreePhone = tollFreePhone;
	}

	public String getRelevantPhone() {
		if (TextUtils.isEmpty(mLocalPhone)) {
			return mTollFreePhone;
		}
		return mLocalPhone;
	}

	public List<HotelTextSection> getOverviewText() {
		return mOverviewText;
	}

	public void addOverviewText(HotelTextSection overviewText) {
		if (mOverviewText == null) {
			mOverviewText = new ArrayList<HotelTextSection>();
		}
		mOverviewText.add(overviewText);
	}

	public HotelTextSection getAmenitiesText() {
		return mAmenitiesText;
	}

	public void setAmenitiesText(HotelTextSection amenitiesText) {
		mAmenitiesText = amenitiesText;
	}

	public HotelTextSection getPoliciesText() {
		return mPoliciesText;
	}

	public void setPoliciesText(HotelTextSection policiesText) {
		mPoliciesText = policiesText;
	}

	public HotelTextSection getFeesText() {
		return mFeesText;
	}

	public void setFeesText(HotelTextSection feesText) {
		mFeesText = feesText;
	}

	public HotelTextSection getMandatoryFeesText() {
		return mMandatoryFeesText;
	}

	public void setMandatoryFeesText(HotelTextSection mandatoryFeesText) {
		mMandatoryFeesText = mandatoryFeesText;
	}

	public HotelTextSection getRenovationText() {
		return mRenovationText;
	}

	public void setRenovationText(HotelTextSection renovationText) {
		mRenovationText = renovationText;
	}

	public List<HotelTextSection> getAllHotelText() {
		List<HotelTextSection> sections = new ArrayList<HotelTextSection>();

		if (mOverviewText != null) {
			sections.addAll(mOverviewText);
		}

		addTextSection(sections, mAmenitiesText);
		addTextSection(sections, mPoliciesText);
		addTextSection(sections, mFeesText);
		addTextSection(sections, mMandatoryFeesText);
		addTextSection(sections, mRenovationText);

		return sections;
	}

	private void addTextSection(List<HotelTextSection> sections, HotelTextSection section) {
		if (section != null) {
			sections.add(section);
		}
	}

	/**
	 * This is a backwards-compatible method of getting all the hotel text.  It
	 * falls back to the old method if the new method is unavailable.
	 * 
	 * TODO: Delete this once hotel text sections are standard in all APIs
	 */
	public List<HotelTextSection> getAllHotelText(Context context) {
		List<HotelTextSection> sections = getAllHotelText();

		if (sections.size() == 0) {
			// Fallback to the old method of parsing the description
			// TODO: Remove once hotel text is in production
			HotelDescription.SectionStrings.initSectionStrings(context);
			HotelDescription hotelDescription = new HotelDescription(context);
			hotelDescription.parseDescription(mDescriptionText);
			sections = hotelDescription.getSections();
		}

		return sections;
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

	public int getPercentRecommended() {
		return Math.round(((float) mTotalRecommendations) / mTotalReviews * 100);
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

	public void setRoomsLeftAtThisRate(int num) {
		mRoomsLeftAtThisRate = num;
	}

	public int getRoomsLeftAtThisRate() {
		return mRoomsLeftAtThisRate;
	}

	// Only valid for Expedia
	public boolean isMerchant() {
		return TextUtils.equals(mSupplierType, "E") || TextUtils.equals(mSupplierType, "MERCHANT");
	}

	public boolean isHighlyRated() {
		return getAverageExpediaRating() >= HotelFilter.HIGH_USER_RATING;
	}

	public void setIsLowestRateMobileExclusive(boolean b) {
		mIsLowestRateMobileExclusive = b;
	}

	public boolean isLowestRateMobileExclusive() {
		return mIsLowestRateMobileExclusive;
	}

	public void setIsLowestRateTonightOnly(boolean b) {
		mIsLowestRateTonightOnly = b;
	}

	public boolean isLowestRateTonightOnly() {
		return mIsLowestRateTonightOnly;
	}

	public String getInfoSiteUrl() {
		return mInfoSiteUrl;
	}

	public void setInfoSiteUrl(String infoSiteUrl) {
		mInfoSiteUrl = infoSiteUrl;
	}

	public String getTelephoneSalesNumber() {
		return mTelephoneSalesNumber;
	}

	public void setTelephoneSalesNumber(String telephoneSalesNumber) {
		mTelephoneSalesNumber = telephoneSalesNumber;
	}

	public boolean isDesktopOverrideNumber() {
		return mIsDesktopOverrideNumber;
	}

	public void setIsDesktopOverrideNumber(boolean isDesktopOverride) {
		mIsDesktopOverrideNumber = isDesktopOverride;
	}

	public boolean isVipAccess() {
		return mIsVipAccess;
	}

	public void setIsVipAccess(boolean isVipAccess) {
		mIsVipAccess = isVipAccess;
	}

	public Money getHighestPriceFromSurvey() {
		return mHighestPriceFromSurvey;
	}

	public void setHighestPriceFromSurvey(Money highestPriceFromSurvey) {
		mHighestPriceFromSurvey = highestPriceFromSurvey;
	}

	// Updates a Property from another Property (currently, one returned via an HotelOffersResponse)
	public void updateFrom(Property property) {
		if (property.hasAmenitiesSet()) {
			this.setAmenityMask(property.getAmenityMask());
		}
		if (property.getDescriptionText() != null) {
			if (this.mDescriptionText == null
					|| this.mDescriptionText.length() < property.getDescriptionText().length()) {
				this.setDescriptionText(property.getDescriptionText());
			}
		}
		if (property.getMediaList() != null) {
			this.setMediaList(property.getMediaList());
		}

		// Just assume that the hotel offers will have better data than we have here
		mOverviewText = property.mOverviewText;
		mAmenitiesText = property.mAmenitiesText;
		mPoliciesText = property.mPoliciesText;
		mFeesText = property.mFeesText;
		mMandatoryFeesText = property.mMandatoryFeesText;
		mRenovationText = property.mRenovationText;
		mIsFromSearchByHotel = property.mIsFromSearchByHotel;

		// Only switch on with an update
		mIsVipAccess |= property.isVipAccess();
	}

	public Property clone() {
		Property property = new Property();
		JSONObject propertyJson = toJson();
		property.fromJson(propertyJson);
		return property;
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
			obj.putOpt("localPhone", mLocalPhone);
			obj.putOpt("tollFreePhone", mTollFreePhone);
			JSONUtils.putJSONableList(obj, "overviewText", mOverviewText);
			JSONUtils.putJSONable(obj, "amenitiesText", mAmenitiesText);
			JSONUtils.putJSONable(obj, "policiesText", mPoliciesText);
			JSONUtils.putJSONable(obj, "feesText", mFeesText);
			JSONUtils.putJSONable(obj, "mandatoryFeesText", mMandatoryFeesText);
			JSONUtils.putJSONable(obj, "renovationText", mRenovationText);
			obj.putOpt("available", mAvailable);
			JSONUtils.putJSONable(obj, "distanceFromUser", mDistanceFromUser);
			obj.putOpt("roomsLeft", mRoomsLeftAtThisRate);
			obj.putOpt("supplierType", mSupplierType);
			obj.putOpt("amenityMask", mAmenityMask);
			obj.putOpt("hotelRating", mHotelRating);
			obj.putOpt("totalReviews", mTotalReviews);
			obj.putOpt("totalRecommendations", mTotalRecommendations);
			obj.putOpt("averageExpediaRating", mAverageExpediaRating);
			obj.putOpt("isLowestRateMobileExclusive", mIsLowestRateMobileExclusive);
			obj.putOpt("isLowestRateTonightOnly", mIsLowestRateTonightOnly);
			obj.putOpt("infoSiteUrl", mInfoSiteUrl);
			obj.putOpt("telephoneSalesNumber", mTelephoneSalesNumber);
			obj.putOpt("isDesktopOverrideNumber", mIsDesktopOverrideNumber);
			obj.putOpt("isVipAccess", mIsVipAccess);
			JSONUtils.putJSONable(obj, "highestPriceFromSurvey", mHighestPriceFromSurvey);
			JSONUtils.putJSONable(obj, "lowestRate", mLowestRate);
			obj.putOpt("isFromSearchByHotel", mIsFromSearchByHotel);
			obj.putOpt("itinBedType", mItinBedType);
			obj.putOpt("itinRoomType", mItinRoomType);

			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert Property object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mName = obj.optString("name", null);
		mPropertyId = obj.optString("propertyId", null);
		mLocation = JSONUtils.getJSONable(obj, "location", Location.class);
		mDescriptionText = obj.optString("description", null);
		mThumbnail = JSONUtils.getJSONable(obj, "thumbnail", Media.class);
		mMedia = JSONUtils.getJSONableList(obj, "media", Media.class);
		mLocalPhone = obj.optString("localPhone", null);
		mTollFreePhone = obj.optString("tollFreePhone", null);
		mOverviewText = JSONUtils.getJSONableList(obj, "overviewText", HotelTextSection.class);
		mAmenitiesText = JSONUtils.getJSONable(obj, "amenitiesText", HotelTextSection.class);
		mPoliciesText = JSONUtils.getJSONable(obj, "policiesText", HotelTextSection.class);
		mFeesText = JSONUtils.getJSONable(obj, "feesText", HotelTextSection.class);
		mMandatoryFeesText = JSONUtils.getJSONable(obj, "mandatoryFeesText", HotelTextSection.class);
		mRenovationText = JSONUtils.getJSONable(obj, "renovationText", HotelTextSection.class);
		mAvailable = obj.optBoolean("available", false);
		mDistanceFromUser = JSONUtils.getJSONable(obj, "distanceFromUser", Distance.class);
		mRoomsLeftAtThisRate = obj.optInt("roomsLeft", 0);
		mSupplierType = obj.optString("supplierType", null);
		mHotelRating = obj.optDouble("hotelRating");
		mTotalReviews = obj.optInt("totalReviews", 0);
		mTotalRecommendations = obj.optInt("totalRecommendations", 0);
		mAverageExpediaRating = obj.optDouble("averageExpediaRating", 0);
		mAmenityMask = obj.optInt("amenityMask");
		mLowestRate = JSONUtils.getJSONable(obj, "lowestRate", Rate.class);
		mIsLowestRateMobileExclusive = obj.optBoolean("isLowestRateMobileExclusive");
		mIsLowestRateTonightOnly = obj.optBoolean("isLowestRateTonightOnly");
		mInfoSiteUrl = obj.optString("infoSiteUrl", null);
		mTelephoneSalesNumber = obj.optString("telephoneSalesNumber", null);
		mIsDesktopOverrideNumber = obj.optBoolean("isDesktopOverrideNumber", true);
		mIsVipAccess = obj.optBoolean("isVipAccess", false);
		mHighestPriceFromSurvey = JSONUtils.getJSONable(obj, "highestPriceFromSurvey", Money.class);
		mIsFromSearchByHotel = obj.optBoolean("isFromSearchByHotel", false);
		mItinRoomType = obj.optString("itinRoomType", null);
		mItinBedType = obj.optString("itinBedType", null);

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

			Money lowRate1 = lowestRate1.getDisplayPrice();
			Money lowRate2 = lowestRate2.getDisplayPrice();

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
			return lowRate1.getAmount().compareTo(lowRate2.getAmount());
		}
	};

	public static final Comparator<Property> DEALS_COMPARATOR = new Comparator<Property>() {
		@Override
		public int compare(Property leftProperty, Property rightProperty) {
			Rate left = leftProperty.getLowestRate();
			Rate right = rightProperty.getLowestRate();

			if (left.getDiscountPercent() == right.getDiscountPercent()) {
				return 0;
			}
			else if (left.getDiscountPercent() > right.getDiscountPercent()) {
				// We want to show larger percentage discounts first
				return -1;
			}
			else {
				return 1;
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
