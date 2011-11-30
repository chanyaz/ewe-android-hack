package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.server.ParserUtils;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

public class Rate implements JSONable {
	// Travelocity rate types
	public static final int TYPE_REGULAR = 1;
	public static final int TYPE_HOTRATE = 2;
	public static final int TYPE_SPECIAL = 3;

	// Expedia rate types (also uses TYPE_REGULAR)
	public static final int TYPE_IMMEDIATE = 2; // User is charged immediately

	// Common fields between HotelPal and StayHIP
	private String mRatePlanCode; // In Expedia, this is just rateCode
	private String mRatePlanName;
	private String mRoomDescription;
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

	// StayHIP unique fields
	private String mBookingCode;
	private String mRoomTypeName;

	// Expedia-specific fields
	private Money mAverageRate; // The average rate, post-sale
	private Money mAverageBaseRate; // The average rate, without sale discounts
	private int mNumberOfNights;
	private String mPromoDescription;
	private int mNumRoomsLeft;
	private List<String> mValueAdds = new ArrayList<String>();

	// For Expedia, RateRules are provided with with availability response
	private RateRules mRateRules;

	// These are computed rates, based on the user's current locale.  They should
	// not be saved, but instead computed on demand (since locale can change).
	private Money mInclusiveBaseRate = null;
	private Money mInclusiveRate = null;

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
		 * King bed types in order
		 * of priority
		 */
		ONE_KING_BED(new String[] {
				"KG", "4", "14" }),
		TWO_KING_BEDS(new String[] { 
				"2KG", "22" }),
		
		/*
		 * Queen bed types in order
		 * of priority
		 */
		ONE_QUEEN_BED(new String[]{
				"QN", "3", "15" }),
		TWO_QUEEN_BEDS(new String[] {
				"2QN", "7", "23" }),
						
		/*
		 * Double beds in order of
		 * priority  				
		 */
		ONE_DOUBLE_BED(new String[] {
				"DD", "2", "13" }),
		TWO_DOUBLE_BEDS(new String[]{
				"2DD", "6", "21" }),
				
		/*
		 * Twin beds in order
		 * of priority
		 */
		ONE_TWIN_BED(new String[] {
				"TW", "18" }),
		TWO_TWIN_BEDS(new String[] {
				"2TW", "5", "25" }),
		THREE_TWIN_BEDS(new String[] { 
				"30" }),
		FOUR_TWIN_BEDS(new String[] { 
				"34" }),
				
		/*
		 * Full beds in order 
		 * of priority		
		 */
		ONE_FULL_BED(new String[] {
				"46" }),
		TWO_FULL_BEDS(new String[] { 
				"47" }),

		/*
		 * Single beds in order
		 * of priority
		 */
		ONE_SINGLE_BED(new String[] {
				"42" }),
		TWO_SINGLE_BEDS(new String[] {
				"43" }),				
		THREE_SINGLE_BEDS(new String[] {
				"44" }),
		FOUR_SINGLE_BEDS(new String[] {
				"45" }),
				
		/*
		 * Remaining beds in order
		 * of priority
		 */
		ONE_BED(new String[] {
				"40" }),
		TWO_BEDS(new String[] { 
				"41" }),
		ONE_TRUNDLE_BED(new String[] {
				"48" }),
		ONE_MURPHY_BED(new String[] {
				"49" }),
		ONE_BUNK_BED(new String[] {
				"50" }),
		ONE_SLEEPER_SOFA(new String[] {
				"51" }),
		TWO_SLEEPER_SOFAS(new String[] {
				"52" }),
		THREE_SLEEPER_SOFAS(new String[] {
				"53" });
		
		private Set<String> mIds;
		
		private BedTypeId(String[] ids) {
			mIds = new HashSet<String>();
			for(String id: ids) {
				mIds.add(id);
			}
		}
		
		public static BedTypeId fromStringId(String id) {
			for (BedTypeId bedTypeId : values()) {
				if(bedTypeId.mIds.contains(id)) {
					return bedTypeId;
				}
			}
			return null;
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
		if(mBedTypes == null) {
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

	public Money getTotalSurcharge() {
		return mTotalSurcharge;
	}

	public void setTotalSurcharge(Money surcharge) {
		mTotalSurcharge = surcharge;
	}

	public int getNumberOfNights() {
		return mNumberOfNights;
	}

	public void setNumberOfNights(int numberOfNights) {
		mNumberOfNights = numberOfNights;
	}

	/**
	 * @return the savings between the base rate and the sale rate.  0 if no sale.
	 */
	public double getSavingsPercent() {
		if (mAverageRate != null && mAverageBaseRate != null) {
			double baseRate = mAverageBaseRate.getAmount();
			double saleRate = mAverageRate.getAmount();
			if (baseRate > saleRate) {
				return 1 - (saleRate / baseRate);
			}
		}

		return 0;
	}

	// #10905 - If the property's sale is <1%, we don't consider it on sale.
	public boolean isOnSale() {
		return getSavingsPercent() >= .01;
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

	//////////////////////////////////////////////////////////////////////////
	// Inclusive rates

	// For all European countries, we need to display the price as the
	// base/average rate PLUS the surcharges.  So for all displaying of rates,
	// use the methods below.

	private static String[] BAIT_N_SWITCH_COUNTRY_CODES = new String[] { "EU", "AD", "AL", "AT", "BA", "BE", "BG",
			"BY", "CH", "CZ", "DE", "DK", "EE", "ES", "FI", "FO", "FR", "FX", "GB", "GI", "GR", "HR", "HU", "IE", "IS",
			"IT", "LI", "LT", "LU", "LV", "MC", "MD", "MK", "MT", "NL", "NO", "PL", "PT", "RO", "SE", "SI", "SJ", "SK",
			"SM", "UA", "VA", "CS" };

	// Ensure BAIT_N_SWITCH_COUNTRY_CODES is sorted, for binary search
	static {
		Arrays.sort(BAIT_N_SWITCH_COUNTRY_CODES);
	}

	public static boolean showInclusivePrices() {
		return Arrays.binarySearch(BAIT_N_SWITCH_COUNTRY_CODES, Locale.getDefault().getCountry()) >= 0;
	}

	public Money getInclusiveBaseRate() {
		if (mInclusiveBaseRate == null) {
			double rate = mAverageBaseRate.getAmount() * mNumberOfNights;
			mInclusiveBaseRate = ParserUtils.createMoney(rate, mAverageBaseRate.getCurrency());
			mInclusiveBaseRate.add(mTotalSurcharge);
		}
		return mInclusiveBaseRate;
	}

	public Money getInclusiveRate() {
		if (mInclusiveRate == null) {
			double rate = mAverageRate.getAmount() * mNumberOfNights;
			mInclusiveRate = ParserUtils.createMoney(rate, mAverageRate.getCurrency());
			mInclusiveRate.add(mTotalSurcharge);
		}
		return mInclusiveRate;
	}

	public Money getDisplayBaseRate() {
		if (showInclusivePrices()) {
			return getInclusiveBaseRate();
		}
		return mAverageBaseRate;
	}

	public Money getDisplayRate() {
		if (showInclusivePrices()) {
			return getInclusiveRate();
		}
		return mAverageRate;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSON Stuff

	public JSONObject toJson() {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("ratePlanCode", mRatePlanCode);
			obj.putOpt("ratePlanName", mRatePlanName);
			obj.putOpt("roomDescription", mRoomDescription);

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
			JSONUtils.putJSONable(obj, "totalSurcharge", mTotalSurcharge);
			obj.putOpt("numberOfNights", mNumberOfNights);
			obj.putOpt("numRoomsLeft", mNumRoomsLeft);
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

		mRateBreakdown = (List<RateBreakdown>) JSONUtils.getJSONableList(obj, "rateBreakdown", RateBreakdown.class);
		mRateChange = obj.optBoolean("rateChange", false);

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
		mTotalSurcharge = (Money) JSONUtils.getJSONable(obj, "totalSurcharge", Money.class);
		mNumberOfNights = obj.optInt("numberOfNights", 0);
		mNumRoomsLeft = obj.optInt("numRoomsLeft", 0);
		mValueAdds = JSONUtils.getStringList(obj, "valueAdds");

		mRateRules = (RateRules) JSONUtils.getJSONable(obj, "rateRules", RateRules.class);

		return true;
	}

	@Override
	public boolean equals(Object o) {
		// This assumes that rate plan code is always available - may not actually always be the case once we
		// re-introduce GDS properties.
		if (o instanceof Rate) {
			Rate other = (Rate) o;
			return getRatePlanCode().equals(other.getRatePlanCode())
					&& getRoomTypeCode().equals(other.getRoomTypeCode());
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

	// **WARNING: USE FOR TESTING PURPOSES ONLY**
	public void fillWithTestData() throws JSONException {
		String data = "{\"roomTypeCode\":\"175351\",\"numberOfNights\":4,\"rateType\":0,\"totalAmountBeforeTax\":{\"amount\":317.8,\"currency\":\"USD\"},\"taxesAndFeesPerRoom\":{\"amount\":44.38,\"currency\":\"USD\"},\"rateKey\":\"545d5c8d-e37c-4089-bedc-e8c773b23f22\",\"surcharge\":{\"amount\":44.69,\"currency\":\"USD\"},\"rateBreakdown\":[{\"date\":{\"month\":12,\"year\":2011,\"dayOfMonth\":30},\"amount\":{\"amount\":71.4,\"currency\":\"USD\"}},{\"date\":{\"month\":12,\"year\":2011,\"dayOfMonth\":31},\"amount\":{\"amount\":79.8,\"currency\":\"USD\"}},{\"date\":{\"month\":1,\"year\":2012,\"dayOfMonth\":1},\"amount\":{\"amount\":83.3,\"currency\":\"USD\"}},{\"date\":{\"month\":1,\"year\":2012,\"dayOfMonth\":2},\"amount\":{\"amount\":83.3,\"currency\":\"USD\"}}],\"numRoomsLeft\":0,\"ratePlanName\":\"2 Double Beds\",\"ratePlanCode\":\"408276\",\"averageRate\":{\"amount\":79.45,\"currency\":\"USD\"},\"totalAmountAfterTax\":{\"amount\":362.49,\"currency\":\"USD\"},\"rateChange\":true,\"averageBaseRate\":{\"amount\":113.5,\"currency\":\"USD\"},\"dailyAmountBeforeTax\":{\"amount\":79.45,\"currency\":\"USD\"},\"rateRules\":{\"policies\":[{\"type\":3,\"description\":\"There are no room charges for children 17 years old and younger who occupy the same room as their parents or guardians, using existing bedding.   The following fees and deposits are charged by the property at time of service, check-in, or check-out.  Pet fee: USD 25 per pet, per day The above list may not be comprehensive. Fees and deposits may not include tax and are subject to change.\"},{\"type\":11,\"description\":\"Your credit card will be charged immediately for the full amount of the reservation upon booking.\"},{\"type\":2,\"description\":\"We understand that sometimes plans fall through. We do not charge a change or cancel fee. However, this property (Park Plaza) imposes the following penalty to its customers that we are required to pass on: Cancellations or changes made after 3:00 PM ((GMT-06:00) Central Time (US &amp; Canada)) on Dec 30, 2011 are subject to a 1 Night Room &amp; Tax penalty. The property makes no refunds for no shows or early checkouts.\"},{\"type\":8,\"description\":\"By proceeding with this reservation, you agree to all terms and conditions, which include the Cancellation Policy and all terms and conditions contained in the User Agreement.\n\nYou agree to pay the cost of your reservation. If you do not pay this debt and it is collected through the use of a collection agency, an attorney, or through other legal proceedings, you agree to pay all reasonable costs or fees, including attorney fees and court costs, incurred in connection with such collection effort.\"}]},\"valueAdds\":[\"Free Airport Shuttle\",\"Free High-Speed Internet\"],\"roomDescription\":\"Double Bed Guest Room-HighSpeed Internet\"}";
		JSONObject obj = new JSONObject(data);
		fromJson(obj);
	}
}
