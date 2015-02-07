package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BedType.BedTypeId;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.GsonUtil;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
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

	// Which price to display to users
	public enum CheckoutPriceType {
		TOTAL,
		TOTAL_WITH_MANDATORY_FEES
	}

	// Tax type to display
	public enum TaxStatusType {
		NONE,
		INCLUDED,
		UNKNOWN,
		ESTIMATED
	}

	// Common fields between HotelPal and StayHIP
	private String mRatePlanCode; // In Expedia, this is just rateCode
	private String mRatePlanName;
	private String mRoomDescription;
	private String mRoomLongDescription;
	private List<RateBreakdown> mRateBreakdown;
	private boolean mRateChange;
	private Money mNightlyRateTotal;
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
	private Money mTotalMandatoryFees;
	private Money mTotalPriceWithMandatoryFees;
	private Money mTotalPriceAdjustments;
	private Money mDepositAmount;

	// Display prices
	private UserPriceType mUserPriceType;
	private CheckoutPriceType mCheckoutPriceType;
	private Money mPriceToShowUsers;
	private Money mDepositToShowUsers;
	private Money mStrikethroughPriceToShowUsers;

	// StayHIP unique fields
	private String mBookingCode;
	private String mRoomTypeName;

	// Expedia-specific fields
	private Money mAverageRate; // The average rate, post-sale
	private Money mAverageBaseRate; // The average rate, without sale discounts
	private double mDiscountPercent = UNSET_DISCOUNT_PERCENT; // Discount percent, as reported by E3 (i.e. 15.0)
	private boolean mIsMobileExclusive = false;
	private int mNumberOfNights;
	private String mPromoDescription;
	private int mNumRoomsLeft;
	private List<String> mValueAdds = new ArrayList<String>();
	private boolean mHasFreeCancellation = false;
	private DateTime mFreeCancellationWindowDate;
	private boolean mNonRefundable = false;
	private boolean mShowResortFees = false;
	private boolean mResortFeeInclusion = false;
	private boolean mDepositRequired = false;

	private TaxStatusType mTaxStatusType;

	private Set<BedType> mBedTypes;

	// For Expedia, RateRules are provided with with availability response
	private RateRules mRateRules;

	// #1266: There's sometimes thumbnail associated with the rate (of the specific room)
	private Media mThumbnail;

	// Air Attach - is this rate discounted as the result of a flight booking?
	private boolean mAirAttached;

	// ETP: is there a pay later offer associated with this rate?
	private Rate mEtpRate;

	//ETP: is this rate a pay later rate?
	private boolean mIsPayLater;

	// These are computed rates, based on the user's current locale.  They should
	// not be saved, but instead computed on demand (since locale can change).
	private Money mMandatoryFeesBaseRate = null;

	public String getRatePlanCode() {
		return mRatePlanCode;
	}

	public void setRatePlanCode(String ratePlanCode) {
		this.mRatePlanCode = ratePlanCode;
	}

	public Set<BedType> getBedTypes() {
		return mBedTypes;
	}

	public String getFormattedBedNames() {
		ArrayList<String> bedNames = new ArrayList<String>();

		if (mBedTypes != null) {
			for (BedType bed : mBedTypes) {
				bedNames.add(bed.getBedTypeDescription());
			}
		}

		return Strings.joinWithoutEmpties(", ", bedNames);
	}

	public void addBedType(String bedTypeId, String bedTypeDescription) {
		if (mBedTypes == null) {
			mBedTypes = new HashSet<BedType>();
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

	public Money getNightlyRateTotal() {
		return mNightlyRateTotal;
	}

	public void setNightlyRateTotal(Money nightlyRateTotal) {
		mNightlyRateTotal = nightlyRateTotal;
	}

	/**
	 * @return the total amount to be paid, minus mandatory fees; useful if you're trying to get at what the user
	 * 		is going to pay *right now* for the rate
	 */
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

	public Money getDepositAmount() {
		return mDepositAmount;
	}

	public void setDepositAmount(Money depositAmount) {
		mDepositAmount = depositAmount;
		mDepositRequired = !depositAmount.isZero();
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

	/**
	 * @return the discount that this rate represents (i.e., you applied a coupon and this is how much you saved)
	 */
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

	public void setCheckoutPriceType(String checkoutPriceType) {
		if ("totalPriceWithMandatoryFees".equals(checkoutPriceType)) {
			mCheckoutPriceType = CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES;
		}
		else {
			// Default to total; value would be "total" otherwise
			mCheckoutPriceType = CheckoutPriceType.TOTAL;
		}
	}

	public void setCheckoutPriceType(CheckoutPriceType checkoutPriceType) {
		mCheckoutPriceType = checkoutPriceType;
	}

	public CheckoutPriceType getCheckoutPriceType() {
		return mCheckoutPriceType;
	}

	public void setPriceToShowUsers(Money m) {
		mPriceToShowUsers = m;
	}

	public void setDepositToShowUsers(Money m) {
		mDepositToShowUsers = m;
		mDepositRequired = !m.isZero();
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
		return ((getDiscountPercent() >= 1) && (getDisplayPrice().compareTo(getDisplayBasePrice()) < 0));
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

	public void setValueAdds(List<String> valueAdds) {
		if (valueAdds == null) {
			mValueAdds = new ArrayList<String>();
		}
		else {
			mValueAdds = valueAdds;
		}
	}

	public void setRateRules(RateRules rateRules) {
		mRateRules = rateRules;
	}

	public RateRules getRateRules() {
		return mRateRules;
	}

	public void setThumbnail(Media thumbnail) {
		mThumbnail = thumbnail;
	}

	public Media getThumbnail() {
		return mThumbnail;
	}

	public boolean isAirAttached() {
		return mAirAttached && PointOfSale.getPointOfSale().shouldShowAirAttach();
	}

	public void setAirAttached(boolean isAirAttached) {
		mAirAttached = isAirAttached;
	}

	/**
	 * Returns the qualifier on what the rate means - e.g., is it per night?  Average per night?  Total?
	 *
	 * The qualifier is returned as a resource id (which links to the qualifier in question)
	 *
	 * @return the qualifier for this rate
	 */
	public int getQualifier(boolean shortVersion) {
		if (mUserPriceType == UserPriceType.PER_NIGHT_RATE_NO_TAXES) {
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

	public void setFreeCancellationWindowDate(DateTime date) {
		mFreeCancellationWindowDate = date;
	}

	public DateTime getFreeCancellationWindowDate() {
		return mFreeCancellationWindowDate;
	}

	public void setNonRefundable(boolean b) {
		mNonRefundable = b;
	}

	public boolean isNonRefundable() {
		return mNonRefundable;
	}

	public boolean shouldShowFreeCancellation() {
		return !isNonRefundable() && hasFreeCancellation();
	}

	public boolean showResortFeesMessaging() {
		return mShowResortFees;
	}

	public void setShowResortFeesMessaging(boolean showResortFees) {
		mShowResortFees = showResortFees;
	}

	public boolean resortFeeInclusion() {
		return mResortFeeInclusion;
	}

	public void setResortFeesInclusion(boolean resortFeeInclusion) {
		mResortFeeInclusion = resortFeeInclusion;
	}

	public boolean depositRequired() {
		return mDepositRequired;
	}

	public void setMobileExlusivity(boolean bool) {
		mIsMobileExclusive = bool;
	}

	public boolean isMobileExclusive() {
		return mIsMobileExclusive;
	}

	public int compareForPriceChange(Rate other) {
		return getTotalPriceWithMandatoryFees().compareToTheWholeValue(other.getTotalPriceWithMandatoryFees());
	}

	public int compareTo(Rate other) {
		return getDisplayPrice().compareTo(other.getTotalAmountAfterTax());
	}

	public TaxStatusType getTaxStatusType() {
		return mTaxStatusType;
	}

	public void setTaxStatusType(String taxStatusType) {
		if ("ESTIMATED".equals(taxStatusType)) {
			mTaxStatusType = TaxStatusType.ESTIMATED;
		}
		else if ("UNKNOWN".equals(taxStatusType)) {
			mTaxStatusType = TaxStatusType.UNKNOWN;
		}
		else if ("INCLUDED".equals(taxStatusType)) {
			mTaxStatusType = TaxStatusType.INCLUDED;
		}
		else if ("NONE".equals(taxStatusType)) {
			mTaxStatusType = TaxStatusType.NONE;
		}
	}

	public Rate getEtpRate() {
		return mEtpRate;
	}

	public void addEtpOffer(Rate etpRate) {
		mEtpRate = etpRate;
	}

	//Tells us if this rate is paylater from the create trip response
	public void setIsPayLater(boolean value) {
		mIsPayLater = value;
	}

	public boolean isPayLater() {
		return mIsPayLater;
	}
	//////////////////////////////////////////////////////////////////////////
	// Prices to show users
	//
	// Unless you're targeting a specific part of the cost (like surcharges),
	// you should be using one of these methods to show the user the price.

	/**
	 * @return the *actual* rate to show users
	 */
	public Money getDisplayPrice() {
		return mPriceToShowUsers;
	}

	public Money getDisplayDeposit() {
		return mDepositToShowUsers;
	}
	/**
	 * @return the *base* (aka, pre-discount) rate we should show users
	 */
	public Money getDisplayBasePrice() {
		return mStrikethroughPriceToShowUsers;
	}

	/**
	 * @return the *checkout* rate we should show users (should == price to show users, but always has decimals)
	 */
	public Money getDisplayTotalPrice() {
		if (mCheckoutPriceType == CheckoutPriceType.TOTAL_WITH_MANDATORY_FEES) {
			return mTotalPriceWithMandatoryFees;
		}
		else {
			return mTotalAmountAfterTax;
		}
	}

	public String getRelativeDisplayPriceString(Rate other) {
		return getRelativeDisplayPriceString(other.getDisplayPrice());
	}

	public String getRelativeDisplayPriceString(Money other) {
		Money difference = new Money(getDisplayPrice());
		difference.subtract(other);
		String prefix = "+";
		int intvalue = difference.getAmount().intValue();
		if (intvalue < 0) {
			difference.negate();
			prefix = "-";
		}
		String display = prefix + difference.getFormattedMoney();
		return display;
	}

	public Rate clone() {
		Rate rate = new Rate();
		JSONObject json = toJson();
		rate.fromJson(json);
		return rate;
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
			GsonUtil.putForJsonable(obj, "nightlyRateTotal", mNightlyRateTotal);

			GsonUtil.putForJsonable(obj, "dailyAmountBeforeTax", mDailyAmountBeforeTax);
			GsonUtil.putForJsonable(obj, "totalAmountBeforeTax", mTotalAmountBeforeTax);
			GsonUtil.putForJsonable(obj, "totalAmountAfterTax", mTotalAmountAfterTax);

			obj.putOpt("rateType", mRateType);
			obj.putOpt("rateKey", mRateKey);
			obj.putOpt("roomTypeCode", mRoomTypeCode);
			GsonUtil.putForJsonable(obj, "taxesAndFeesPerRoom", mTaxesAndFeesPerRoom);
			GsonUtil.putForJsonable(obj, "extraGuestFee", mExtraGuestFee);
			obj.putOpt("bookingCode", mBookingCode);
			obj.putOpt("roomTypeName", mRoomTypeName);

			obj.putOpt("promoDescription", mPromoDescription);
			GsonUtil.putForJsonable(obj, "averageRate", mAverageRate);
			GsonUtil.putForJsonable(obj, "averageBaseRate", mAverageBaseRate);
			obj.put("discountPercent", mDiscountPercent);
			obj.putOpt("isMobileExclusive", mIsMobileExclusive);
			GsonUtil.putForJsonable(obj, "totalSurcharge", mTotalSurcharge);
			GsonUtil.putForJsonable(obj, "totalMandatoryFees", mTotalMandatoryFees);
			GsonUtil.putForJsonable(obj, "totalPriceWithMandatoryFees", mTotalPriceWithMandatoryFees);
			GsonUtil.putForJsonable(obj, "totalPriceAdjustments", mTotalPriceAdjustments);
			GsonUtil.putForJsonable(obj, "depositAmount", mDepositAmount);
			obj.putOpt("userPriceType", getUserPriceType().ordinal());
			JSONUtils.putEnum(obj, "checkoutPriceType", mCheckoutPriceType);
			JSONUtils.putEnum(obj, "taxStatusType", mTaxStatusType);
			GsonUtil.putForJsonable(obj, "priceToShowUsers", mPriceToShowUsers);
			GsonUtil.putForJsonable(obj, "depositToShowUsers", mDepositToShowUsers);
			GsonUtil.putForJsonable(obj, "strikethroughPriceToShowUsers", mStrikethroughPriceToShowUsers);
			obj.putOpt("numberOfNights", mNumberOfNights);
			obj.putOpt("numRoomsLeft", mNumRoomsLeft);
			obj.putOpt("hasFreeCancellation", mHasFreeCancellation);
			obj.putOpt("showResortFeeMessage", mShowResortFees);
			obj.putOpt("resortFeeInclusion", mResortFeeInclusion);
			obj.putOpt("depositRequired", mDepositRequired);
			if (mFreeCancellationWindowDate != null) {
				JodaUtils.putDateTimeInJson(obj, "freeCancellationWindowDateTime", mFreeCancellationWindowDate);
			}
			obj.putOpt("nonRefundable", mNonRefundable);
			JSONUtils.putStringList(obj, "valueAdds", mValueAdds);
			JSONUtils.putJSONableList(obj, "bedTypes", mBedTypes);
			JSONUtils.putJSONable(obj, "rateRules", mRateRules);
			JSONUtils.putJSONable(obj, "thumbnail", mThumbnail);
			obj.putOpt("airAttached", mAirAttached);
			JSONUtils.putJSONable(obj, "etpRate", mEtpRate);
			obj.putOpt("isPayLater", mIsPayLater);
			return obj;
		}
		catch (JSONException e) {
			Log.e("Could not convert RateBreakdown object to JSON.", e);
			return null;
		}
	}

	public boolean fromJson(JSONObject obj) {
		mRatePlanCode = obj.optString("ratePlanCode", null);
		mRatePlanName = obj.optString("ratePlanName", null);
		mRoomDescription = obj.optString("roomDescription", null);
		mRoomLongDescription = obj.optString("roomLongDescription", null);

		mRateBreakdown = JSONUtils.getJSONableList(obj, "rateBreakdown", RateBreakdown.class);
		mRateChange = obj.optBoolean("rateChange", false);
		mNightlyRateTotal = GsonUtil.getForJsonable(obj, "nightlyRateTotal", Money.class);
		mHasFreeCancellation = obj.optBoolean("hasFreeCancellation", false);
		if (obj.has("freeCancellationWindowDate")) {
			mFreeCancellationWindowDate = new DateTime(obj.optLong("freeCancellationWindowDate"));
		}
		else if (obj.has("freeCancellationWindowDateTime")) {
			mFreeCancellationWindowDate = JodaUtils.getDateTimeFromJsonBackCompat(obj,
					"freeCancellationWindowDateTime", null);
		}
		mNonRefundable = obj.optBoolean("nonRefundable", false);

		mDailyAmountBeforeTax = GsonUtil.getForJsonable(obj, "dailyAmountBeforeTax", Money.class);
		mTotalAmountBeforeTax = GsonUtil.getForJsonable(obj, "totalAmountBeforeTax", Money.class);
		mTotalAmountAfterTax = GsonUtil.getForJsonable(obj, "totalAmountAfterTax", Money.class);

		mRateType = obj.optInt("rateType");
		mRateKey = obj.optString("rateKey", null);
		mRoomTypeCode = obj.optString("roomTypeCode", null);

		mTaxesAndFeesPerRoom = GsonUtil.getForJsonable(obj, "taxesAndFeesPerRoom", Money.class);
		mExtraGuestFee = GsonUtil.getForJsonable(obj, "extraGuestFee", Money.class);

		mBookingCode = obj.optString("bookingCode", null);
		mRoomTypeName = obj.optString("roomTypeName", null);

		mPromoDescription = obj.optString("promoDescription", null);
		mAverageRate = GsonUtil.getForJsonable(obj, "averageRate", Money.class);
		mAverageBaseRate = GsonUtil.getForJsonable(obj, "averageBaseRate", Money.class);
		mDiscountPercent = obj.optDouble("discountPercent", UNSET_DISCOUNT_PERCENT);
		mIsMobileExclusive = obj.optBoolean("isMobileExclusive");
		mTotalSurcharge = GsonUtil.getForJsonable(obj, "totalSurcharge", Money.class);
		if (mTotalSurcharge == null) {
			// Try surcharge from EAN
			mTotalSurcharge = GsonUtil.getForJsonable(obj, "surcharge", Money.class);
		}
		mTotalMandatoryFees = GsonUtil.getForJsonable(obj, "totalMandatoryFees", Money.class);
		mTotalPriceWithMandatoryFees = GsonUtil.getForJsonable(obj, "totalPriceWithMandatoryFees", Money.class);
		mTotalPriceAdjustments = GsonUtil.getForJsonable(obj, "totalPriceAdjustments", Money.class);
		mDepositAmount = GsonUtil.getForJsonable(obj, "depositAmount", Money.class);
		mUserPriceType = UserPriceType.values()[obj.optInt("userPriceType", UserPriceType.UNKNOWN.ordinal())];
		mCheckoutPriceType = JSONUtils.getEnum(obj, "checkoutPriceType", CheckoutPriceType.class);
		mTaxStatusType = JSONUtils.getEnum(obj, "taxStatusType", TaxStatusType.class);
		mPriceToShowUsers = GsonUtil.getForJsonable(obj, "priceToShowUsers", Money.class);
		mDepositToShowUsers  = GsonUtil.getForJsonable(obj, "depositToShowUsers", Money.class);
		mStrikethroughPriceToShowUsers = GsonUtil.getForJsonable(obj, "strikethroughPriceToShowUsers",
				Money.class);
		mNumberOfNights = obj.optInt("numberOfNights", 0);
		mNumRoomsLeft = obj.optInt("numRoomsLeft", 0);
		mValueAdds = JSONUtils.getStringList(obj, "valueAdds");
		mShowResortFees = obj.optBoolean("showResortFeeMessage");
		mResortFeeInclusion = obj.optBoolean("resortFeeInclusion");
		mDepositRequired = obj.optBoolean("depositRequired");

		List<BedType> bedTypes = JSONUtils.getJSONableList(obj, "bedTypes", BedType.class);
		if (bedTypes != null) {
			mBedTypes = new HashSet<BedType>(bedTypes);
		}
		mRateRules = JSONUtils.getJSONable(obj, "rateRules", RateRules.class);
		mThumbnail = JSONUtils.getJSONable(obj, "thumbnail", Media.class);
		mAirAttached = obj.optBoolean("airAttached", false);
		mEtpRate = JSONUtils.getJSONable(obj, "etpRate", Rate.class);
		mIsPayLater = obj.optBoolean("isPayLater", false);
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
