package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.utils.GsonUtil;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Property implements JSONable {

	public enum Amenity {
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

		Amenity(int newFlag, int newStrId) {
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

	public enum RoomUpgradeType {
		NOT_CALLED_UPGRADE_API,
		NO_UPGRADE_OFFERS,
		HAS_UPGRADE_OFFERS
	}

	// Data about the hotel that generally does not change
	private String mName;
	private String mPropertyId;
	private Location mLocation;
	private HotelMedia mThumbnail;
	private List<HotelMedia> mHotelMedia = new ArrayList();
	private String mLocalPhone;
	private String mTollFreePhone;

	// Text about the hotel
	private List<HotelTextSection> mOverviewText;
	private HotelTextSection mAmenitiesText;
	private HotelTextSection mPoliciesText;
	private HotelTextSection mFeesText;
	private HotelTextSection mMandatoryFeesText;
	private HotelTextSection mRenovationText;
	private List<String> checkInPolicies = new ArrayList<>();

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
	private boolean mShowCircles = false;

	// Hotel rating ranges from 0-5, in .5 intervals
	private double mHotelRating;

	// Expedia user reviews
	private double mAverageExpediaRating;

	// Convenient hotel search by name data
	private boolean mIsFromSearchByHotel = false;

	// Convenient itin data
	private String mItinRoomType;
	private String mItinBedType;

	// Non-price promotions (NPP)
	private String mItinNonPricePromotionText;

	// ETP pay later offer associated with one or more rates
	private boolean mHasEtpOffer;

	// Travel Ad data
	private boolean mIsSponsored;
	private boolean mHasShownImpression;
	private String mClickTrackingUrl;
	private String mImpressionTrackingUrl;
	private String mOmnitureAdDisplayedUrl;
	private String mOmnitureAdClickedUrl;

	private boolean mIsETPHotel;

	private String mRoomCancelLink;
	private String mBookingChangeWebUrl;
	private String mRoomUpgradeWebViewUrl;
	private String mRoomUpgradeOffersApiUrl;
	private RoomUpgradeType mRoomUpgradeOfferType = RoomUpgradeType.NOT_CALLED_UPGRADE_API;

	public RoomUpgradeType getRoomUpgradeOfferType() {
		return mRoomUpgradeOfferType;
	}
	public void setRoomUpgradeOfferType(RoomUpgradeType hasRoomUpgradeOffers) {
		this.mRoomUpgradeOfferType = hasRoomUpgradeOffers;
	}
	public void setRoomCancelLink(String roomCancelLink) {
		this.mRoomCancelLink = roomCancelLink;
	}
	public void setBookingChangeWebUrl(String mBookingChangeWebUrl) {
		this.mBookingChangeWebUrl = mBookingChangeWebUrl;
	}
	public void setRoomUpgradeWebViewUrl(String roomUpgradeWebViewUrl) {
		this.mRoomUpgradeWebViewUrl = roomUpgradeWebViewUrl;
	}
	public void setRoomUpgradeOffersApiUrl(String mRoomUpgradeOffersApiUrl) {
		this.mRoomUpgradeOffersApiUrl = mRoomUpgradeOffersApiUrl;
	}

	public boolean isBookingChangeAvailable() {
		return Strings.isNotEmpty(mBookingChangeWebUrl);
	}

	public String getRoomCancelLink() {
		return mRoomCancelLink;
	}
	public String getBookingChangeWebUrl() {
		return mBookingChangeWebUrl;
	}
	public String getRoomUpgradeWebViewUrl() {
		return mRoomUpgradeWebViewUrl;
	}
	public String getRoomUpgradeOffersApiUrl() {
		return mRoomUpgradeOffersApiUrl;
	}

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

	public void setItinNonPricePromotionText(String itinNonPricePromotionText) {
		this.mItinNonPricePromotionText = itinNonPricePromotionText;
	}

	public String getItinNonPricePromotionText() {
		return mItinNonPricePromotionText;
	}

	public HotelMedia getThumbnail() {
		return mThumbnail;
	}

	public void setThumbnail(HotelMedia thumbnail) {
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

	public boolean hasAmenitiesSet() {
		return mHasAmenitiesSet;
	}

	public void addMedia(HotelMedia hotelMedia) {
		if (mHotelMedia == null) {
			mHotelMedia = new ArrayList<HotelMedia>();
		}
		mHotelMedia.add(hotelMedia);
	}

	public HotelMedia getMedia(int index) {
		if (mHotelMedia == null) {
			return null;
		}
		return mHotelMedia.get(index);
	}

	public List<HotelMedia> getMediaList() {
		if (mHotelMedia == null) {
			mHotelMedia = new ArrayList<HotelMedia>();
		}
		return mHotelMedia;
	}

	public void setMediaList(List<HotelMedia> theList) {
		this.mHotelMedia = theList;
	}

	public int getMediaCount() {
		if (mHotelMedia == null) {
			return 0;
		}
		return mHotelMedia.size();
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

	private void addTextSection(List<HotelTextSection> sections, HotelTextSection section) {
		if (section != null) {
			sections.add(section);
		}
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

	public double getAverageExpediaRating() {
		return mAverageExpediaRating;
	}

	/**
	 * The lowest rate from the hotel search API. When constructed via hotel search response, we have a
	 * lowest rate. When constructed via hotel search by name, there is no lowest rate.
	 *
	 * @return lowest rate as dictated by the cache used in the hotel search API
	 */
	@Nullable
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

	public String getInfoSiteUrl() {
		return mInfoSiteUrl;
	}

	public void setInfoSiteUrl(String infoSiteUrl) {
		mInfoSiteUrl = infoSiteUrl;
	}

	public boolean isVipAccess() {
		return mIsVipAccess;
	}

	public void setIsVipAccess(boolean isVipAccess) {
		mIsVipAccess = isVipAccess;
	}

	public void setCheckInPolicies(List<String> policies) {
		checkInPolicies = policies;
	}

	public List<String> getCheckInPolicies() {
		return checkInPolicies;
	}

	public boolean shouldShowCircles() {
		return mShowCircles;
	}

	public boolean isSponsored() {
		return mIsSponsored;
	}

	// Updates a Property from another Property (currently, one returned via an HotelOffersResponse)
	public void updateFrom(Property property) {
		if (property.hasAmenitiesSet()) {
			this.setAmenityMask(property.getAmenityMask());
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
		mHasEtpOffer = property.mHasEtpOffer;

		// Only switch on with an update
		mIsVipAccess |= property.isVipAccess();
		mShowCircles |= property.shouldShowCircles();
	}

	public void updateFrom(final Hotel offer) {
		mName = offer.localizedName;
		mPropertyId = offer.hotelId;

		mLocation = new Location();
		mLocation.addStreetAddressLine(offer.address);
		mLocation.setCity(offer.city);
		mLocation.setDescription(offer.locationDescription);
		mLocation.setStateCode(offer.stateProvinceCode);
		mLocation.setCountryCode(offer.countryCode);
		mLocation.setPostalCode(offer.postalCode);
		mLocation.setCountryName(offer.countryName);
		mLocation.setLatitude(offer.latitude);
		mLocation.setLongitude(offer.longitude);

		String thumbnailUrl = offer.largeThumbnailUrl;
		if (!TextUtils.isEmpty(thumbnailUrl)) {
			if (!thumbnailUrl.startsWith("http://")) {
				mThumbnail = new HotelMedia(Images.getMediaHost() + thumbnailUrl);
			}
			else {
				mThumbnail = new HotelMedia(thumbnailUrl);
			}
		}
		mAvailable = offer.isHotelAvailable;
		mDistanceFromUser = offer.distanceUnit.equals("Miles") ?
			new Distance(offer.proximityDistanceInMiles, Distance.DistanceUnit.MILES) :
			new Distance(offer.proximityDistanceInKiloMeters, Distance.DistanceUnit.KILOMETERS);
		mRoomsLeftAtThisRate = offer.roomsLeftAtThisRate;
		mSupplierType = offer.supplierType;
		mHotelRating = offer.hotelStarRating;
		mAverageExpediaRating = offer.hotelGuestRating;
		mLowestRate = new Rate();
		mLowestRate.setNumRoomsLeft(offer.roomsLeftAtThisRate);
		mLowestRate.setThumbnail(mThumbnail);
		mLowestRate.setIsPayLater(offer.isPaymentChoiceAvailable);
		mLowestRate.updateSearchRateFrom(offer.lowRateInfo);

		mHasEtpOffer = offer.isPaymentChoiceAvailable;
		mIsVipAccess = offer.isVipAccess;

		mIsSponsored = offer.isSponsoredListing;
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
			JSONUtils.putJSONable(obj, "thumbnail", mThumbnail);
			JSONUtils.putJSONableList(obj, "media", mHotelMedia);
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
			obj.putOpt("averageExpediaRating", mAverageExpediaRating);
			obj.putOpt("isLowestRateMobileExclusive", mIsLowestRateMobileExclusive);
			obj.putOpt("isLowestRateTonightOnly", mIsLowestRateTonightOnly);
			obj.putOpt("infoSiteUrl", mInfoSiteUrl);
			obj.putOpt("telephoneSalesNumber", mTelephoneSalesNumber);
			obj.putOpt("isDesktopOverrideNumber", mIsDesktopOverrideNumber);
			obj.putOpt("isVipAccess", mIsVipAccess);
			obj.putOpt("showCircles", mShowCircles);
			GsonUtil.putForJsonable(obj, "highestPriceFromSurvey", mHighestPriceFromSurvey);
			JSONUtils.putJSONable(obj, "lowestRate", mLowestRate);
			obj.putOpt("isFromSearchByHotel", mIsFromSearchByHotel);
			obj.putOpt("itinBedType", mItinBedType);
			obj.putOpt("itinRoomType", mItinRoomType);
			obj.putOpt("hasEtpOffer", mHasEtpOffer);
			obj.putOpt("isSponsored", mIsSponsored);
			obj.putOpt("clickTrackingUrl", mClickTrackingUrl);
			obj.putOpt("impressionTrackingUrl", mImpressionTrackingUrl);
			obj.putOpt("hasShownImpression", mHasShownImpression);
			obj.putOpt("omnitureAdClickedUrl", mOmnitureAdClickedUrl);
			obj.putOpt("omnitureAdDisplayedUrl", mOmnitureAdDisplayedUrl);
			obj.putOpt("isETPHotel", mIsETPHotel);
			obj.putOpt("mRoomCancelLink", mRoomCancelLink);
			obj.putOpt("mBookingChangeWebUrl", mBookingChangeWebUrl);
			obj.putOpt("mRoomUpgradeWebViewUrl", mRoomUpgradeWebViewUrl);
			obj.putOpt("mRoomUpgradeOffersApiUrl", mRoomUpgradeOffersApiUrl);
			obj.putOpt("mRoomUpgradeOfferType", mRoomUpgradeOfferType.name());
			JSONUtils.putStringList(obj, "checkInPolicies", checkInPolicies);
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
		mThumbnail = JSONUtils.getJSONable(obj, "thumbnail", HotelMedia.class);
		mHotelMedia = JSONUtils.getJSONableList(obj, "media", HotelMedia.class);
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
		mAverageExpediaRating = obj.optDouble("averageExpediaRating", 0);
		mAmenityMask = obj.optInt("amenityMask");
		mLowestRate = JSONUtils.getJSONable(obj, "lowestRate", Rate.class);
		mIsLowestRateMobileExclusive = obj.optBoolean("isLowestRateMobileExclusive");
		mIsLowestRateTonightOnly = obj.optBoolean("isLowestRateTonightOnly");
		mInfoSiteUrl = obj.optString("infoSiteUrl", null);
		mTelephoneSalesNumber = obj.optString("telephoneSalesNumber", null);
		mIsDesktopOverrideNumber = obj.optBoolean("isDesktopOverrideNumber", true);
		mIsVipAccess = obj.optBoolean("isVipAccess", false);
		mShowCircles = obj.optBoolean("showCircles", false);
		mHighestPriceFromSurvey = GsonUtil.getForJsonable(obj, "highestPriceFromSurvey", Money.class);
		mIsFromSearchByHotel = obj.optBoolean("isFromSearchByHotel", false);
		mItinRoomType = obj.optString("itinRoomType", null);
		mItinBedType = obj.optString("itinBedType", null);
		mHasEtpOffer = obj.optBoolean("hasEtpOffer", false);
		mIsSponsored = obj.optBoolean("isSponsored", false);
		mClickTrackingUrl = obj.optString("clickTrackingUrl", null);
		mImpressionTrackingUrl = obj.optString("impressionTrackingUrl", null);
		mHasShownImpression = obj.optBoolean("hasShownImpression", false);
		mOmnitureAdClickedUrl = obj.optString("omnitureAdClickedUrl", null);
		mOmnitureAdDisplayedUrl = obj.optString("omnitureAdDisplayedUrl", null);
		mIsETPHotel = obj.optBoolean("isETPHotel", false);
		mRoomCancelLink = obj.optString("mRoomCancelLink", "");
		mBookingChangeWebUrl = obj.optString("mBookingChangeWebUrl", "");
		mRoomUpgradeWebViewUrl = obj.optString("mRoomUpgradeWebViewUrl", "");
		mRoomUpgradeOffersApiUrl = obj.optString("mRoomUpgradeOffersApiUrl", "");
		mRoomUpgradeOfferType = RoomUpgradeType.valueOf(obj.optString("mRoomUpgradeOfferType", RoomUpgradeType.NOT_CALLED_UPGRADE_API.name()));
		List<String> policies = JSONUtils.getStringList(obj, "checkInPolicies");
		if (policies != null && policies.size() > 0) {
			checkInPolicies = policies;
		}
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
