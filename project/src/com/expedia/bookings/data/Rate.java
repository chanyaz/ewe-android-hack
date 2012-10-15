package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Rate implements JSONable {

	// Anything outside the range [0, 100] is invalid. So let's use 200.
	public static final double UNSET_DISCOUNT_PERCENT = 200;

	// The types of display rates
	public enum UserPriceType {
		RATE_FOR_WHOLE_STAY_WITH_TAXES,
		PER_NIGHT_RATE_NO_TAXES,
		UNKNOWN;
	}

	// Common fields between HotelPal and StayHIP
	private String mRatePlanCode; // In Expedia, this is just rateCode
	private String mRatePlanName;
	private String mRoomDescription;
	private String mRoomLongDescription;
	private List<RateBreakdown> mRateBreakdown;
	private boolean mRateChange;
	private Money mDailyAmountBeforeTax; // HP equiv: bookableRate
	private Money mTotalAmountBeforeTax; // HP equiv: totalRateAmount
	private Money mTotalAmountAfterTax; // HP equiv: totalCostPerRoom

	// HotelPal unique fields
	private int mRateType;
	private String mRateKey;
	private String mRoomTypeCode;
	private Money mTaxesAndFeesPerRoom;

	// Surcharges
	private Money mTotalSurcharge; // The total fees for the rate (NOT per night)
	private Money mExtraGuestFee;
	private Money mTotalMandatoryFees; // "bait & switch" fees
	private Money mTotalPriceWithMandatoryFees;
	private Money mTotalPriceAdjustments;

	// Display prices
	private UserPriceType mUserPriceType;
	private Money mPriceToShowUsers;
	private Money mStrikethroughPriceToShowUsers;

	// StayHIP unique fields
	private String mBookingCode;
	private String mRoomTypeName;

	// Expedia-specific fields
	private Money mAverageRate; // The average rate, post-sale
	private Money mAverageBaseRate; // The average rate, without sale discounts
	private double mDiscountPercent = UNSET_DISCOUNT_PERCENT; // Discount percent, as reported by E3 (i.e. 15.0)
	private int mNumberOfNights;
	private String mPromoDescription;
	private int mNumRoomsLeft;
	private List<String> mValueAdds = new ArrayList<String>();
	private boolean mHasFreeCancellation = false;
	private boolean mNonRefundable = false;

	// For Expedia, RateRules are provided with with availability response
	private RateRules mRateRules;

	// These are computed rates, based on the user's current locale.  They should
	// not be saved, but instead computed on demand (since locale can change).
	private Money mMandatoryFeesBaseRate = null;
	private Money mMandatoryFeesRate = null;

	/*
	 * This enum represents the different bed types
	 * that can be returned from EAN. Note that 
	 * some bed types have multiple ids pointing to them.
	 * The mappings were picked up from the following documentation:
	 * http://developer.ean.com/general_info/BedTypes 
	 *
	 */
	public static enum BedTypeId {

		/*
		 * King bed types in order of priority
		 */
		ONE_KING_BED(new String[] { "KG", "4", "14" }),
		TWO_KING_BEDS(new String[] { "2KG", "22" }),
		THREE_KING_BEDS(new String[] { "56" }),
		FOUR_KING_BEDS(new String[] { "59" }),
		ONE_KING_ONE_SOFA(new String[] { "67" }),

		/*
		 * Queen bed types in order of priority
		 */
		ONE_QUEEN_BED(new String[] { "QN", "3", "15" }),
		TWO_QUEEN_BEDS(new String[] { "2QN", "7", "23" }),
		THREE_QUEEN_BEDS(new String[] { "57" }),
		FOUR_QUEEN_BEDS(new String[] { "60" }),
		ONE_QUEEN_ONE_SOFA(new String[] { "68" }),

		/*
		 * Double beds in order of priority
		 */
		ONE_DOUBLE_BED(new String[] { "DD", "2", "13" }),
		TWO_DOUBLE_BEDS(new String[] { "2DD", "6", "21" }),
		ONE_DOUBLE_ONE_SINGLE(new String[] { "63" }),
		ONE_DOUBLE_TWO_SINGLES(new String[] { "66" }),

		/*
		 * Twin beds in order of priority
		 */
		ONE_TWIN_BED(new String[] { "TW", "18" }),
		TWO_TWIN_BEDS(new String[] { "2TW", "5", "25" }),
		THREE_TWIN_BEDS(new String[] { "30" }),
		FOUR_TWIN_BEDS(new String[] { "34" }),

		/*
		 * Full beds in order of priority
		 */
		ONE_FULL_BED(new String[] { "46" }),
		TWO_FULL_BEDS(new String[] { "47" }),

		/*
		 * Single beds in order of priority
		 */
		ONE_SINGLE_BED(new String[] { "42" }),
		TWO_SINGLE_BEDS(new String[] { "43" }),
		THREE_SINGLE_BEDS(new String[] { "44" }),
		FOUR_SINGLE_BEDS(new String[] { "45" }),

		/*
		 * Remaining beds in order of priority
		 */
		ONE_BED(new String[] { "40" }),
		TWO_BEDS(new String[] { "41" }),
		ONE_TRUNDLE_BED(new String[] { "48" }),
		ONE_MURPHY_BED(new String[] { "49" }),
		ONE_BUNK_BED(new String[] { "50" }),
		ONE_SLEEPER_SOFA(new String[] { "51" }),
		TWO_SLEEPER_SOFAS(new String[] { "52" }),
		THREE_SLEEPER_SOFAS(new String[] { "53" }),
		JAPENESE_FUTON(new String[] { "54" }),
		THREE_BEDS(new String[] { "55" }),
		FOUR_BEDS(new String[] { "58" }),

		/*
		 * Handles all unknown bed type cases
		 */
		UNKNOWN(new String[] {});

		private Set<String> mIds;

		private BedTypeId(String[] ids) {
			mIds = new HashSet<String>();
			for (String id : ids) {
				mIds.add(id);
			}
		}

		public static BedTypeId fromStringId(String id) {
			for (BedTypeId bedTypeId : values()) {
				if (bedTypeId.mIds.contains(id)) {
					return bedTypeId;
				}
			}
			return UNKNOWN;
		}
	}

	public static class BedType {
		public BedTypeId bedTypeId;
		public String bedTypeDescription;

		public BedType(BedTypeId id, String description) {
			bedTypeId = id;
			bedTypeDescription = description;
		}
	}

	private Set<BedType> mBedTypes;

	public String getRatePlanCode() {
		return mRatePlanCode;
	}

	public void setRatePlanCode(String ratePlanCode) {
		this.mRatePlanCode = ratePlanCode;
	}

	public Set<BedType> getBedTypes() {
		return mBedTypes;
	}

	public void addBedType(String bedTypeId, String bedTypeDescription) {
		if (mBedTypes == null) {
			mBedTypes = new HashSet<Rate.BedType>();
		}
		mBedTypes.add(new BedType(BedTypeId.fromStringId(bedTypeId), bedTypeDescription));
	}

	public List<RateBreakdown> getRateBreakdownList() {
		return mRateBreakdown;
	}

	public void addRateBreakdown(RateBreakdown rateBreakdown) {
		if (mRateBreakdown == null) {
			mRateBreakdown = new ArrayList<RateBreakdown>();
		}
		mRateBreakdown.add(rateBreakdown);
	}

	public boolean rateChanges() {
		return mRateChange;
	}

	public void setRateChange(boolean rateChange) {
		this.mRateChange = rateChange;
	}

	public String getRoomDescription() {
		return mRoomDescription;
	}

	public void setRoomDescription(String roomDescription) {
		this.mRoomDescription = roomDescription;
	}

	public String getRoomLongDescription() {
		return mRoomLongDescription;
	}

	public void setRoomLongDescription(String roomLongDescription) {
		this.mRoomLongDescription = roomLongDescription;
	}

	public Money getDailyAmountBeforeTax() {
		return mDailyAmountBeforeTax;
	}

	public void setDailyAmountBeforeTax(Money dailyAmountBeforeTax) {
		this.mDailyAmountBeforeTax = dailyAmountBeforeTax;
	}

	public Money getTotalAmountBeforeTax() {
		return mTotalAmountBeforeTax;
	}

	public void setTotalAmountBeforeTax(Money totalAmountBeforeTax) {
		this.mTotalAmountBeforeTax = totalAmountBeforeTax;
	}

	public Money getTotalAmountAfterTax() {
		return mTotalAmountAfterTax;
	}

	public void setTotalAmountAfterTax(Money totalAmountAfterTax) {
		this.mTotalAmountAfterTax = totalAmountAfterTax;
	}

	public Money getTaxesAndFeesPerRoom() {
		return mTaxesAndFeesPerRoom;
	}

	public void setTaxesAndFeesPerRoom(Money taxesAndFeesPerRoom) {
		this.mTaxesAndFeesPerRoom = taxesAndFeesPerRoom;
	}

	public Money getExtraGuestFee() {
		return mExtraGuestFee;
	}

	public void setExtraGuestFee(Money extraGuestFee) {
		this.mExtraGuestFee = extraGuestFee;
	}

	public int getRateType() {
		return mRateType;
	}

	public void setRateType(int rateType) {
		this.mRateType = rateType;
	}

	public String getRateKey() {
		return mRateKey;
	}

	public void setRateKey(String rateKey) {
		this.mRateKey = rateKey;
	}

	public String getRoomTypeCode() {
		return mRoomTypeCode;
	}

	public void setRoomTypeCode(String roomTypeCode) {
		this.mRoomTypeCode = roomTypeCode;
	}

	public String getBookingCode() {
		return mBookingCode;
	}

	public void setBookingCode(String bookingCode) {
		this.mBookingCode = bookingCode;
	}

	public String getRoomTypeName() {
		return mRoomTypeName;
	}

	public void setRoomTypeName(String roomTypeName) {
		this.mRoomTypeName = roomTypeName;
	}

	public String getRatePlanName() {
		return mRatePlanName;
	}

	public void setRatePlanName(String ratePlanName) {
		this.mRatePlanName = ratePlanName;
	}

	public String getPromoDescription() {
		return mPromoDescription;
	}

	public void setPromoDescription(String promoDescription) {
		mPromoDescription = promoDescription;
	}

	public Money getAverageRate() {
		return mAverageRate;
	}

	public void setAverageRate(Money averageRate) {
		mAverageRate = averageRate;
	}

	public Money getAverageBaseRate() {
		return mAverageBaseRate;
	}

	public void setAverageBaseRate(Money averageBaseRate) {
		mAverageBaseRate = averageBaseRate;
	}

	/**
	 * @return the savings between the base rate and the sale rate, in the range [0, 100]. 0 if no sale.
	 */
	public double getDiscountPercent() {
		if (mDiscountPercent <= 100 && mDiscountPercent >= 0) {
			return mDiscountPercent;
		}
		// Alternate/old method of calculating savings percent.
		else if (mAverageRate != null && mAverageBaseRate != null) {
			double baseRate = mAverageBaseRate.getAmount().doubleValue();
			double saleRate = mAverageRate.getAmount().doubleValue();
			if (baseRate > saleRate) {
				return 100 * (1 - (saleRate / baseRate));
			}
		}
		return 0;
	}

	public void setDiscountPercent(double discountPercent) {
		mDiscountPercent = Math.abs(discountPercent);
	}

	public Money getTotalSurcharge() {
		return mTotalSurcharge;
	}

	public void setTotalSurcharge(Money surcharge) {
		mTotalSurcharge = surcharge;
	}

	public Money getTotalMandatoryFees() {
		return mTotalMandatoryFees;
	}

	public void setTotalMandatoryFees(Money totalMandatoryFees) {
		mTotalMandatoryFees = totalMandatoryFees;
	}

	public Money getTotalPriceWithMandatoryFees() {
		return mTotalPriceWithMandatoryFees;
	}

	public void setTotalPriceWithMandatoryFees(Money totalPriceWithMandatoryFees) {
		mTotalPriceWithMandatoryFees = totalPriceWithMandatoryFees;
	}

	public Money getTotalPriceAdjustments() {
		return mTotalPriceAdjustments;
	}

	public void setTotalPriceAdjustments(Money totalPriceAdjustments) {
		mTotalPriceAdjustments = totalPriceAdjustments;
	}

	public void setUserPriceType(String userPriceType) {
		if ("RateForWholeStayWithTaxes".equals(userPriceType)) {
			mUserPriceType = UserPriceType.RATE_FOR_WHOLE_STAY_WITH_TAXES;
		}
		else if ("PerNightRateNoTaxes".equals(userPriceType)) {
			mUserPriceType = UserPriceType.PER_NIGHT_RATE_NO_TAXES;
		}
		else {
			mUserPriceType = UserPriceType.UNKNOWN;
		}
	}

	public void setUserPriceType(UserPriceType userPriceType) {
		mUserPriceType = userPriceType;
	}

	public UserPriceType getUserPriceType() {
		if (mUserPriceType == null) {
			return UserPriceType.UNKNOWN;
		}
		return mUserPriceType;
	}

	public void setPriceToShowUsers(Money m) {
		mPriceToShowUsers = m;
	}

	public void setStrikethroughPriceToShowUsers(Money m) {
		mStrikethroughPriceToShowUsers = m;
	}

	public int getNumberOfNights() {
		return mNumberOfNights;
	}

	public void setNumberOfNights(int numberOfNights) {
		mNumberOfNights = numberOfNights;
	}

	// #10905 - If the property's sale is <1%, we don't consider it on sale.
	public boolean isOnSale() {
		return getDiscountPercent() >= 1;
	}

	// 9.5% or higher will be rounded to 10% when the percent is displayed as an integer.
	public boolean isSaleTenPercentOrBetter() {
		return getDiscountPercent() >= 9.5;
	}

	public int getNumRoomsLeft() {
		return mNumRoomsLeft;
	}

	public void setNumRoomsLeft(int numRoomsLeft) {
		mNumRoomsLeft = numRoomsLeft;
	}

	public void addValueAdd(String valueAdd) {
		mValueAdds.add(valueAdd);
	}

	public int getValueAddCount() {
		return mValueAdds.size();
	}

	public List<String> getValueAdds() {
		return mValueAdds;
	}

	public void setRateRules(RateRules rateRules) {
		mRateRules = rateRules;
	}

	public RateRules getRateRules() {
		return mRateRules;
	}

	/**
	 * Returns the qualifier on what the rate means - e.g., is it per night?  Average per night?  Total?
	 * 
	 * The qualifier is returned as a resource id (which links to the qualifier in question)
	 * 
	 * @return the qualifier for this rate
	 */
	public int getQualifier(boolean shortVersion) {
		if (!showInclusivePrices()) {
			List<RateBreakdown> rateBreakdown = getRateBreakdownList();
			if (rateBreakdown == null) {
				// If rateBreakdown is null, we assume that this is a per/night hotel
				return (shortVersion) ? R.string.per_night : R.string.rate_per_night;
			}
			else if (rateBreakdown.size() > 1) {
				if (rateChanges()) {
					return R.string.rate_avg_per_night;
				}
				else {
					return (shortVersion) ? R.string.per_night : R.string.rate_per_night;
				}
			}
		}

		// Indicates this is a total and has no qualifier
		return 0;
	}

	public int getQualifier() {
		return getQualifier(false);
	}

	public void setHasFreeCancellation(boolean b) {
		mHasFreeCancellation = b;
	}

	public boolean hasFreeCancellation() {
		return mHasFreeCancellation;
	}

	public void setNonRefundable(boolean b) {
		mNonRefundable = b;
	}

	public boolean isNonRefundable() {
		return mNonRefundable;
	}

	private boolean mIsMobileExclusive = false;

	public void setMobileExlusivity(boolean bool) {
		mIsMobileExclusive = bool;
	}

	public boolean isMobileExclusive() {
		return mIsMobileExclusive;
	}

	public boolean showInclusivePrices() {
		return LocaleUtils.doesPointOfSaleHaveInclusivePricing();
	}

	public boolean showMandatoryFees() {
		return LocaleUtils.shouldDisplayMandatoryFees();
	}

	private Money getMandatoryBaseRate() {
		if (mMandatoryFeesBaseRate == null) {
			mMandatoryFeesBaseRate = new Money(mStrikethroughPriceToShowUsers);
			mMandatoryFeesBaseRate.add(mTotalMandatoryFees);
		}
		return mMandatoryFeesBaseRate;
	}

	public Money getDisplayBaseRate() {
		if (showMandatoryFees()) {
			return getMandatoryBaseRate();
		}
		else if (mStrikethroughPriceToShowUsers != null) {
			return mStrikethroughPriceToShowUsers;
		}
		else {
			return mAverageBaseRate;
		}
	}

	public Money getDisplayRate() {
		if (showMandatoryFees() && mTotalPriceWithMandatoryFees != null) {
			return mTotalPriceWithMandatoryFees;
		}
		else if (mPriceToShowUsers != null) {
			return mPriceToShowUsers;
		}
		else {
			return mAverageRate;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// JSON Stuff

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("ratePlanCode", mRatePlanCode);
			obj.putOpt("ratePlanName", mRatePlanName);
			obj.putOpt("roomDescription", mRoomDescription);
			obj.putOpt("roomLongDescription", mRoomLongDescription);

			JSONUtils.putJSONableList(obj, "rateBreakdown", mRateBreakdown);
			obj.putOpt("rateChange", mRateChange);

			JSONUtils.putJSONable(obj, "dailyAmountBeforeTax", mDailyAmountBeforeTax);
			JSONUtils.putJSONable(obj, "totalAmountBeforeTax", mTotalAmountBeforeTax);
			JSONUtils.putJSONable(obj, "totalAmountAfterTax", mTotalAmountAfterTax);

			obj.putOpt("rateType", mRateType);
			obj.putOpt("rateKey", mRateKey);
			obj.putOpt("roomTypeCode", mRoomTypeCode);
			JSONUtils.putJSONable(obj, "taxesAndFeesPerRoom", mTaxesAndFeesPerRoom);
			JSONUtils.putJSONable(obj, "extraGuestFee", mExtraGuestFee);
			obj.putOpt("bookingCode", mBookingCode);
			obj.putOpt("roomTypeName", mRoomTypeName);

			obj.putOpt("promoDescription", mPromoDescription);
			JSONUtils.putJSONable(obj, "averageRate", mAverageRate);
			JSONUtils.putJSONable(obj, "averageBaseRate", mAverageBaseRate);
			obj.put("discountPercent", mDiscountPercent);
			JSONUtils.putJSONable(obj, "totalSurcharge", mTotalSurcharge);
			JSONUtils.putJSONable(obj, "totalMandatoryFees", mTotalMandatoryFees);
			JSONUtils.putJSONable(obj, "totalPriceWithMandatoryFees", mTotalPriceWithMandatoryFees);
			obj.putOpt("userPriceType", getUserPriceType().ordinal());
			JSONUtils.putJSONable(obj, "priceToShowUsers", mPriceToShowUsers);
			JSONUtils.putJSONable(obj, "strikethroughPriceToShowUsers", mStrikethroughPriceToShowUsers);
			obj.putOpt("numberOfNights", mNumberOfNights);
			obj.putOpt("numRoomsLeft", mNumRoomsLeft);
			obj.putOpt("hasFreeCancellation", mHasFreeCancellation);
			obj.putOpt("nonRefundable", mNonRefundable);
			JSONUtils.putStringList(obj, "valueAdds", mValueAdds);

			JSONUtils.putJSONable(obj, "rateRules", mRateRules);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert RateBreakdown object to JSON.", e);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean fromJson(JSONObject obj) {
		mRatePlanCode = obj.optString("ratePlanCode", null);
		mRatePlanName = obj.optString("ratePlanName", null);
		mRoomDescription = obj.optString("roomDescription", null);
		mRoomLongDescription = obj.optString("roomLongDescription", null);

		mRateBreakdown = (List<RateBreakdown>) JSONUtils.getJSONableList(obj, "rateBreakdown", RateBreakdown.class);
		mRateChange = obj.optBoolean("rateChange", false);
		mHasFreeCancellation = obj.optBoolean("hasFreeCancellation", false);
		mNonRefundable = obj.optBoolean("nonRefundable", false);

		mDailyAmountBeforeTax = (Money) JSONUtils.getJSONable(obj, "dailyAmountBeforeTax", Money.class);
		mTotalAmountBeforeTax = (Money) JSONUtils.getJSONable(obj, "totalAmountBeforeTax", Money.class);
		mTotalAmountAfterTax = (Money) JSONUtils.getJSONable(obj, "totalAmountAfterTax", Money.class);

		mRateType = obj.optInt("rateType");
		mRateKey = obj.optString("rateKey", null);
		mRoomTypeCode = obj.optString("roomTypeCode", null);

		mTaxesAndFeesPerRoom = (Money) JSONUtils.getJSONable(obj, "taxesAndFeesPerRoom", Money.class);
		mExtraGuestFee = (Money) JSONUtils.getJSONable(obj, "extraGuestFee", Money.class);

		mBookingCode = obj.optString("bookingCode", null);
		mRoomTypeName = obj.optString("roomTypeName", null);

		mPromoDescription = obj.optString("promoDescription", null);
		mAverageRate = (Money) JSONUtils.getJSONable(obj, "averageRate", Money.class);
		mAverageBaseRate = (Money) JSONUtils.getJSONable(obj, "averageBaseRate", Money.class);
		mDiscountPercent = obj.optDouble("discountPercent", UNSET_DISCOUNT_PERCENT);
		mTotalSurcharge = (Money) JSONUtils.getJSONable(obj, "totalSurcharge", Money.class);
		if (mTotalSurcharge == null) {
			// Try surcharge from EAN
			mTotalSurcharge = (Money) JSONUtils.getJSONable(obj, "surcharge", Money.class);
		}
		mTotalMandatoryFees = (Money) JSONUtils.getJSONable(obj, "totalMandatoryFees", Money.class);
		mTotalPriceWithMandatoryFees = (Money) JSONUtils.getJSONable(obj, "totalPriceWithMandatoryFees", Money.class);
		mUserPriceType = UserPriceType.values()[obj.optInt("userPriceType", UserPriceType.UNKNOWN.ordinal())];
		mPriceToShowUsers = (Money) JSONUtils.getJSONable(obj, "priceToShowUsers", Money.class);
		mStrikethroughPriceToShowUsers = (Money) JSONUtils.getJSONable(obj, "strikethroughPriceToShowUsers",
				Money.class);
		mNumberOfNights = obj.optInt("numberOfNights", 0);
		mNumRoomsLeft = obj.optInt("numRoomsLeft", 0);
		mValueAdds = JSONUtils.getStringList(obj, "valueAdds");

		mRateRules = (RateRules) JSONUtils.getJSONable(obj, "rateRules", RateRules.class);

		return true;
	}

	@Override
	public boolean equals(Object o) {
		// This assumes that rate key is always available
		if (o instanceof Rate) {
			Rate other = (Rate) o;
			return getRateKey().equals(other.getRateKey());
		}
		return false;
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
}
