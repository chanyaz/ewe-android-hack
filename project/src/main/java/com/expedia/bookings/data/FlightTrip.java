package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.payment.LoyaltyEarnInfo;
import com.expedia.bookings.data.payment.PointsEarnInfo;
import com.expedia.bookings.data.payment.PriceEarnInfo;
import com.expedia.bookings.data.trips.TripBucketItem;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.utils.GsonUtil;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Flight;

public class FlightTrip implements JSONable {

	private static final double PRICE_CHANGE_NOTIFY_CUTOFF = .01;
	private static final String LOYALTY_TOTAL_POINTS = "loyaltyTotalPoints";
	private static final String LOYALTY_TOTAL_PRICE = "loyaltyTotalPrice";
	private static final String LOYALTY_CURRENCY = "loyaltyCurrency";
	private static final String TOTAL_REWARDS_POINTS = "totalRewardsPoints";
	private static final String REWARDS_CURRENCY = "rewardsCurrency";
	private static final String TOTAL_REWARDS_AMOUNT_TO_EARN = "totalRewardsAmountToEarn";

	private String mProductKey;

	private String mRewardsPoints;

	private List<FlightLeg> mLegs = new ArrayList<>();

	private List<PassengerCategoryPrice> mPassengers = new ArrayList<>();

	private Money mTotalPrice;
	private Money mBaseFare;
	private Money mTotalFare;
	private Money mAverageTotalFare;
	private Money mTaxes;
	private Money mFees;

	private LoyaltyEarnInfo earnInfo;
	private RewardsInfo rewards;

	private boolean isPassportNeeded;
	private boolean isSplitTicket;

	// For price changes
	private Money mOldTotalPrice;

	// Possible price change from last time we queried this trip
	private Money mPriceChangeAmount;

	// Server indicates ahead of time *if* we might be charged
	// online booking feess
	private boolean mMayChargeObFees;

	// A boolean that denotes whether or not to show the total fare with or without the card fee. This is dependent
	// on which screens the user has seen and what billing info they have selected. Transient; do not serialize.
	private boolean mShowFareWithCardFee;

	// Optional name for certain fares, such as low cost or saver fare, etc..
	private String mFareName;

	// Possible online booking fees (only set when server processes a
	// real credit card, on non-US/CA POS for specific routes)
	private Money mOnlineBookingFeesAmount;

	private int mSeatsRemaining;

	// These are modifiers for each segment in leg
	private FlightSegmentAttributes[][] mFlightSegmentAttrs;

	// The associated itinerary (not created until requested)
	private String mItineraryNumber;

	// Rules associated with offer (will not exist until we have an itinerary)
	private Map<String, Rule> mRules;

	public String getProductKey() {
		return mProductKey;
	}

	public void setProductKey(String productKey) {
		mProductKey = productKey;
	}

	public void addLeg(FlightLeg leg) {
		mLegs.add(leg);
	}

	public int getLegCount() {
		return mLegs.size();
	}

	public FlightLeg getLeg(int position) {
		return mLegs.get(position);
	}

	public List<FlightLeg> getLegs() {
		return mLegs;
	}

	public void addPassenger(PassengerCategoryPrice passenger) {
		mPassengers.add(passenger);
	}

	public int getPassengerCount() {
		return mPassengers.size();
	}

	public PassengerCategoryPrice getPassenger(int position) {
		return mPassengers.get(position);
	}

	public List<PassengerCategoryPrice> getPassengers() {
		return mPassengers;
	}

	public Money getBaseFare() {
		return mBaseFare;
	}

	public void setBaseFare(Money baseFare) {
		mBaseFare = baseFare;
	}

	public Money getOldTotalPrice() {
		return mOldTotalPrice;
	}

	public void setTotalFare(Money totalFare) {
		mTotalFare = totalFare;
	}

	public Money getTotalFare() {
		return mTotalFare;
	}

	public void setAverageTotalFare(Money averageTotalFare) {
		mAverageTotalFare = averageTotalFare;
	}

	public Money getAverageTotalFare() {
		return mAverageTotalFare;
	}

	public Money getTaxes() {
		return mTaxes;
	}

	public void setTaxes(Money taxes) {
		mTaxes = taxes;
	}

	public Money getFees() {
		return mFees;
	}

	public void setFees(Money fees) {
		mFees = fees;
	}

	public Money getPriceChangeAmount() {
		return mPriceChangeAmount;
	}

	public void setPriceChangeAmount(Money priceChangeAmount) {
		mPriceChangeAmount = priceChangeAmount;
	}

	public Money getTotalFareWithCardFee(BillingInfo billingInfo, TripBucketItem item) {
		Money base = new Money(mTotalPrice);
		Money cardFee = item.getPaymentFee(billingInfo);

		if (cardFee != null) {
			base.add(cardFee);
		}

		return base;
	}

	/**
	 * Whether or not we show total fare with the card fee has some mildly complicated logic. We explicitly set that
	 * the fare should be shown with card fee based upon (a) the user has a valid card selected in the billingInfo and
	 * (b) if the user has visited the "checkout" screen (which we store in mShowFareWithCardFee).
	 *
	 * @param context     - context required to inflate the FlowState and validate the credit card
	 * @param billingInfo
	 * @return
	 */
	public boolean showFareWithCardFee(Context context, BillingInfo billingInfo) {
		return mShowFareWithCardFee && FlightPaymentFlowState.getInstance(context).hasAValidCardSelected(billingInfo);
	}

	public void setShowFareWithCardFee(boolean showFareWithCardFee) {
		mShowFareWithCardFee = showFareWithCardFee;
	}

	public int getSeatsRemaining() {
		return mSeatsRemaining;
	}

	public void setSeatsRemaining(int seatsRemaining) {
		mSeatsRemaining = seatsRemaining;
	}

	public void initFlightSegmentAttributes(int len) {
		mFlightSegmentAttrs = new FlightSegmentAttributes[len][];
	}

	public void addFlightSegmentAttributes(int index, FlightSegmentAttributes[] attributes) {
		mFlightSegmentAttrs[index] = attributes;
	}

	public void setFlightSegmentAttributes(FlightSegmentAttributes[][] attrs) {
		mFlightSegmentAttrs = attrs;
	}

	public FlightSegmentAttributes[] getFlightSegmentAttributes(int legPosition) {
		return mFlightSegmentAttrs[legPosition];
	}

	public FlightSegmentAttributes[] getFlightSegmentAttributes(FlightLeg leg) {
		for (int a = 0; a < mLegs.size(); a++) {
			if (leg.equals(mLegs.get(a))) {
				return getFlightSegmentAttributes(a);
			}
		}
		return null;
	}

	public String getItineraryNumber() {
		return mItineraryNumber;
	}

	public void setItineraryNumber(String itineraryNumber) {
		mItineraryNumber = itineraryNumber;
	}

	public void addRule(Rule rule) {
		if (mRules == null) {
			mRules = new HashMap<String, Rule>();
		}
		mRules.put(rule.getName(), rule);
	}

	public Rule getRule(String name) {
		if (mRules == null) {
			return null;
		}
		return mRules.get(name);
	}

	public Set<String> getRules() {
		return mRules.keySet();
	}

	public void setRewardsPoints(String rewardsPoints) {
		mRewardsPoints = rewardsPoints;
	}

	public String getRewardsPoints() {
		return mRewardsPoints;
	}

	public RewardsInfo getRewards() {
		return rewards;
	}

	public void setRewards(RewardsInfo rewards) {
		this.rewards = rewards;
	}

	public void setMayChargeObFees(boolean mayChargeObFees) {
		mMayChargeObFees = mayChargeObFees;
	}

	public boolean getMayChargeObFees() {
		return mMayChargeObFees;
	}

	public void setOnlineBookingFeesAmount(Money amount) {
		mOnlineBookingFeesAmount = amount;
	}

	public Money getOnlineBookingFeesAmount() {
		return mOnlineBookingFeesAmount;
	}

	public void setFareName(String fareName) {
		mFareName = fareName;
	}

	public String getFareName() {
		return mFareName;
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	public boolean hasPricing() {
		return mBaseFare != null && mTotalFare != null && mTaxes != null && mFees != null;
	}

	public boolean hasPriceChanged() {
		return mPriceChangeAmount != null;
	}

	public boolean notifyPriceChanged() {
		if (!hasPriceChanged()) {
			return false;
		}

		double changeAmount = mPriceChangeAmount.getAmount().doubleValue();
		double newAmount = mTotalPrice.getAmount().doubleValue();
		double oldAmount = newAmount - changeAmount;

		if (newAmount > oldAmount) {
			return true;
		}

		return 1.0 - (newAmount / oldAmount) >= PRICE_CHANGE_NOTIFY_CUTOFF;
	}

	/**
	 * Does the trip contain any segments which cross into a different country?
	 *
	 * @return true if we cross an international border sometime in this trip
	 */
	public boolean isInternational() {
		boolean retVal = false;
		if (mLegs != null && mLegs.size() > 0) {
			String countryCode = mLegs.get(0).getFirstWaypoint().getAirport().mCountryCode;
			for (FlightLeg leg : mLegs) {
				for (Flight flight : leg.getSegments()) {
					if (flight.getDestinationWaypoint() != null && flight.getDestinationWaypoint().getAirport() != null
						&& flight.getDestinationWaypoint().getAirport().mCountryCode != null
						&& !flight.getDestinationWaypoint().getAirport().mCountryCode.equalsIgnoreCase(countryCode)) {
						//Country codes don't match so we consider the flight to be international
						retVal = true;
						break;
					}
				}
				if (retVal) {
					break;
				}
			}
		}
		return retVal;
	}

	public void setPassportNeeded(boolean isNeeded) {
		isPassportNeeded = isNeeded;
	}

	public boolean isPassportNeeded() {
		return isPassportNeeded;
	}

	public void setSplitTicket(boolean splitTicket) {
		isSplitTicket = splitTicket;
	}

	public boolean isSplitTicket() {
		return isSplitTicket;
	}

	/**
	 * Does this FlightTrip pass through the country supplied via the countryCode param
	 *
	 * @param countryCode - The country code as it will be found in a an airport object. Typically two letters like: "US"
	 * @return
	 */
	public boolean passesThroughCountry(String countryCode) {
		boolean retVal = false;
		if (mLegs != null && mLegs.size() > 0) {
			for (FlightLeg leg : mLegs) {
				String startCountry = leg.getFirstWaypoint().getAirport().mCountryCode;

				if (startCountry != null && startCountry.equalsIgnoreCase(countryCode)) {
					retVal = true;
					break;
				}

				for (Flight flight : leg.getSegments()) {
					if (flight.getDestinationWaypoint() != null && flight.getDestinationWaypoint().getAirport() != null
						&& flight.getDestinationWaypoint().getAirport().mCountryCode != null
						&& flight.getDestinationWaypoint().getAirport().mCountryCode.equalsIgnoreCase(countryCode)) {
						retVal = true;
						break;
					}
				}
				if (retVal) {
					break;
				}
			}
		}
		return retVal;
	}

	public String computePercentagePriceChangeForOmnitureTracking() {
		if (mPriceChangeAmount == null) {
			return null;
		}
		else {
			BigDecimal currentPrice = mTotalPrice.copy().getAmount();
			BigDecimal priceChange = mPriceChangeAmount.copy().getAmount();
			BigDecimal oldPrice = currentPrice.subtract(priceChange);
			BigDecimal percentageChange = priceChange.divide(oldPrice, RoundingMode.HALF_UP);

			String percentChange = Integer.toString(percentageChange.intValue() * 100);
			percentChange += "%";

			return percentChange;
		}
	}

	public LoyaltyEarnInfo getEarnInfo() {
		return earnInfo;
	}

	public void setEarnInfo(LoyaltyEarnInfo earnInfo) {
		this.earnInfo = earnInfo;
	}

	public void setTotalPrice(Money totalPrice) {
		mTotalPrice = totalPrice;
	}

	public Money getTotalPrice() {
		return mTotalPrice;
	}

	////////////////////////////////////////////////////////////////////////
	// Comparators
	//
	// A short explanation on how it works: there is a way to compare each
	// field we care about of a FlightTrip/FlightLeg.  We then use
	// FlightTripComparator to automatically order these, based on descending
	// importance.

	public enum CompareField {
		PRICE,
		DEPARTURE,
		ARRIVAL,
		DURATION,
		LEG_ID
	}

	public static class FlightTripComparator implements Comparator<FlightTrip> {
		private final int mLegPosition;

		// A list of fields to compare; automatically generated based on the focus field
		private final CompareField[] mToCompare;

		public FlightTripComparator(int legPosition, CompareField focus) {
			mLegPosition = legPosition;

			switch (focus) {
			case PRICE:
				mToCompare = new CompareField[] { CompareField.PRICE, CompareField.DURATION, CompareField.LEG_ID };
				break;
			case DEPARTURE:
				mToCompare = new CompareField[] { CompareField.DEPARTURE, CompareField.PRICE, CompareField.LEG_ID };
				break;
			case ARRIVAL:
				mToCompare = new CompareField[] { CompareField.ARRIVAL, CompareField.PRICE, CompareField.LEG_ID };
				break;
			case DURATION:
				mToCompare = new CompareField[] { CompareField.DURATION, CompareField.PRICE, CompareField.LEG_ID };
				break;
			case LEG_ID:
			default:
				mToCompare = new CompareField[] { CompareField.LEG_ID };
				break;
			}
		}

		@Override
		public int compare(FlightTrip lhs, FlightTrip rhs) {
			FlightLeg lhsLeg = lhs.getLeg(mLegPosition);
			FlightLeg rhsLeg = rhs.getLeg(mLegPosition);

			int result;
			for (int field = 0; field < mToCompare.length; field++) {
				switch (mToCompare[field]) {
				case PRICE:
					result = PRICE_COMPARATOR.compare(lhs, rhs);
					break;
				case DEPARTURE:
					result = DEPARTURE_COMPARATOR.compare(lhsLeg, rhsLeg);
					break;
				case ARRIVAL:
					result = ARRIVAL_COMPARATOR.compare(lhsLeg, rhsLeg);
					break;
				case DURATION:
					result = DURATION_COMPARATOR.compare(lhsLeg, rhsLeg);
					break;
				case LEG_ID:
				default:
					result = lhsLeg.getLegId().compareTo(rhsLeg.getLegId());
					break;
				}

				if (result != 0) {
					return result;
				}
			}

			return 0;
		}
	}

	public static final Comparator<FlightTrip> PRICE_COMPARATOR = new Comparator<FlightTrip>() {
		@Override
		public int compare(FlightTrip lhs, FlightTrip rhs) {
			Money lhsMoney = lhs.getTotalPrice();
			Money rhsMoney = rhs.getTotalPrice();

			if (lhsMoney == null && rhsMoney == null) {
				return 0;
			}
			else if (lhsMoney == null) {
				return 1;
			}
			else if (rhsMoney == null) {
				return -1;
			}

			return lhsMoney.getAmount().compareTo(rhsMoney.getAmount());
		}
	};

	public static final Comparator<FlightLeg> DEPARTURE_COMPARATOR = new Comparator<FlightLeg>() {
		@Override
		public int compare(FlightLeg lhs, FlightLeg rhs) {
			DateTime leftStart = lhs.getFirstWaypoint().getMostRelevantDateTime();
			DateTime rightStart = rhs.getFirstWaypoint().getMostRelevantDateTime();

			if (leftStart.isBefore(rightStart)) {
				return -1;
			}
			else if (leftStart.isAfter(rightStart)) {
				return 1;
			}
			else {
				return 0;
			}
		}
	};

	public static final Comparator<FlightLeg> ARRIVAL_COMPARATOR = new Comparator<FlightLeg>() {
		@Override
		public int compare(FlightLeg lhs, FlightLeg rhs) {
			DateTime leftStart = lhs.getLastWaypoint().getMostRelevantDateTime();
			DateTime rightStart = rhs.getLastWaypoint().getMostRelevantDateTime();

			if (leftStart.isBefore(rightStart)) {
				return -1;
			}
			else if (leftStart.isAfter(rightStart)) {
				return 1;
			}
			else {
				return 0;
			}
		}
	};

	public static final Comparator<FlightLeg> DURATION_COMPARATOR = new Comparator<FlightLeg>() {
		@Override
		public int compare(FlightLeg lhs, FlightLeg rhs) {
			long leftDuration = lhs.getDurationFromWaypoints();
			long rightDuration = rhs.getDurationFromWaypoints();

			if (leftDuration == rightDuration) {
				return 0;
			}
			else if (leftDuration > rightDuration) {
				return 1;
			}
			else {
				return -1;
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Playing with others

	public void updateFrom(FlightTrip other) {
		// Things we do not update (since they should not change):
		// - Product key
		// - Legs
		// - Leg segment attributes

		if (other.hasPricing()) {
			// Save the old price for price changes
			mOldTotalPrice = mTotalPrice;

			mBaseFare = other.mBaseFare;
			mTotalPrice = other.mTotalPrice;
			mTaxes = other.mTaxes;
			mFees = other.mFees;
			mPassengers = other.mPassengers;
		}

		if (other.hasPriceChanged()) {
			// Defect 3440 : Set old fare based on the changed amount from api in case of price change
			mOldTotalPrice.setAmount(other.getTotalPrice().getAmount().subtract(other.getPriceChangeAmount().getAmount()));
			mPassengers = other.mPassengers;
			mPriceChangeAmount = other.mPriceChangeAmount;
		}

		if (other.mOnlineBookingFeesAmount != null) {
			mOnlineBookingFeesAmount = other.mOnlineBookingFeesAmount;
		}

		if (other.mSeatsRemaining != 0) {
			mSeatsRemaining = other.mSeatsRemaining;
		}

		if (!TextUtils.isEmpty(other.mItineraryNumber)) {
			mItineraryNumber = other.mItineraryNumber;
		}

		if (!TextUtils.isEmpty(other.getRewardsPoints())) {
			mRewardsPoints = other.getRewardsPoints();
		}

		if (other.getRewards() != null) {
			rewards = other.getRewards();
		}

		if (other.mRules != null) {
			mRules = other.mRules;
		}
	}

	public FlightTrip clone() {
		JSONObject json = this.toJson();
		FlightTrip flightTrip = new FlightTrip();
		flightTrip.fromJson(json);
		return flightTrip;
	}

	//////////////////////////////////////////////////////////////////////////
	// JSONable
	// There is a core problem that FlightTrips can be *very* numerous when
	// saved, so we take steps to reduce the size as much as possible

	private static final int JSON_VERSION = 2;

	private static final String KEY_VERSION = "a";
	private static final String KEY_PRODUCT_KEY = "b";
	private static final String KEY_LEGS = "c";
	private static final String KEY_LEG_IDS = "d";
	private static final String KEY_BASE_FARE = "e";
	private static final String KEY_TOTAL_FARE = "f";
	private static final String KEY_TOTAL_PRICE = "totalPrice";
	private static final String KEY_OLD_TOTAL_PRICE = "oldTotalPrice";
	private static final String KEY_TAXES = "g";
	private static final String KEY_FEES = "h";
	private static final String KEY_PRICE_CHANGE_AMOUNT = "i";
	private static final String KEY_ONLINE_BOOKING_FEES_AMOUNT = "j";
	private static final String KEY_REWARDS_POINTS = "k";
	private static final String KEY_SEATS_REMAINING = "l";
	private static final String KEY_MAY_CHARGE_OB_FEES = "n";
	private static final String KEY_FARE_NAME = "p";
	private static final String KEY_FLIGHT_SEGMENT_ATTRS = "q";
	private static final String KEY_ITINERARY_NUMBER = "r";
	private static final String KEY_RULES = "s";
	private static final String KEY_CURRENCY = "t";
	private static final String KEY_PASSENGERS = "v";
	private static final String KEY_AVG_TOTAL_FARE = "w";

	@Override
	public JSONObject toJson() {
		return toJson(true);
	}

	public JSONObject toJson(boolean includeFullLegData) {
		try {
			JSONObject obj = new JSONObject();

			obj.put(KEY_VERSION, JSON_VERSION);

			obj.putOpt(KEY_PRODUCT_KEY, mProductKey);

			if (earnInfo != null) {
				addEarnInfo(obj, earnInfo);
			}
			if (rewards != null) {
				addRewardsInfo(obj, rewards);
			}
			if (includeFullLegData) {
				JSONUtils.putJSONableList(obj, KEY_LEGS, mLegs);
			}
			else {
				JSONArray legIds = new JSONArray();
				for (FlightLeg leg : mLegs) {
					legIds.put(leg.getLegId());
				}
				obj.putOpt(KEY_LEG_IDS, legIds);
			}

			if (mBaseFare != null) {
				obj.put(KEY_CURRENCY, mBaseFare.getCurrency());
			}
			addMoney(obj, KEY_BASE_FARE, mBaseFare);
			addMoney(obj, KEY_TOTAL_FARE, mTotalFare);
			addMoney(obj, KEY_AVG_TOTAL_FARE, mAverageTotalFare);
			addMoney(obj, KEY_OLD_TOTAL_PRICE, mOldTotalPrice);
			addMoney(obj, KEY_TAXES, mTaxes);
			addMoney(obj, KEY_FEES, mFees);
			addMoney(obj, KEY_PRICE_CHANGE_AMOUNT, mPriceChangeAmount);
			addMoney(obj, KEY_ONLINE_BOOKING_FEES_AMOUNT, mOnlineBookingFeesAmount);
			addMoney(obj, KEY_TOTAL_PRICE, mTotalPrice);

			obj.putOpt(KEY_REWARDS_POINTS, mRewardsPoints);
			obj.putOpt(KEY_SEATS_REMAINING, mSeatsRemaining);
			if (mMayChargeObFees) {
				obj.putOpt(KEY_MAY_CHARGE_OB_FEES, mMayChargeObFees);
			}
			if (!TextUtils.isEmpty(mFareName)) {
				obj.putOpt(KEY_FARE_NAME, mFareName);
			}

			if (mFlightSegmentAttrs != null) {
				JSONArray arr = new JSONArray();
				for (FlightSegmentAttributes[] attributes : mFlightSegmentAttrs) {
					JSONUtils.putJSONableList(arr, Arrays.asList(attributes));
				}
				obj.putOpt(KEY_FLIGHT_SEGMENT_ATTRS, arr);
			}

			obj.putOpt(KEY_ITINERARY_NUMBER, mItineraryNumber);

			if (mRules != null) {
				JSONUtils.putJSONableList(obj, KEY_RULES, new ArrayList<Rule>(mRules.values()));
			}

			if (mPassengers != null) {
				JSONUtils.putJSONableList(obj, KEY_PASSENGERS, mPassengers);
			}
			obj.putOpt("isPassportNeeded", isPassportNeeded);
			obj.putOpt("isSplitTicket", isSplitTicket);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	private RewardsInfo getRewardsInfo(JSONObject obj) throws JSONException {
		RewardsInfo rewardsInfo = new RewardsInfo();
		float totalRewardsPoints = (float) obj.optDouble(TOTAL_REWARDS_POINTS, 0);
		rewardsInfo.setTotalPointsToEarn(totalRewardsPoints);
		String rewardsCurrency = obj.optString(REWARDS_CURRENCY);
		rewardsInfo.setTotalAmountToEarn(parseMoney(obj, TOTAL_REWARDS_AMOUNT_TO_EARN, rewardsCurrency));
		return rewardsInfo;
	}

	private void addRewardsInfo(JSONObject obj, RewardsInfo rewards) throws JSONException {
		float totalRewardsPoints = rewards.getTotalPointsToEarn();
		Money totalAmountToEarn = rewards.getTotalAmountToEarn();
		obj.put(TOTAL_REWARDS_POINTS, totalRewardsPoints);
		if (totalAmountToEarn != null) {
			obj.put(REWARDS_CURRENCY, totalAmountToEarn.getCurrency());
			addMoney(obj, TOTAL_REWARDS_AMOUNT_TO_EARN, totalAmountToEarn);
		}
	}

	private void addEarnInfo(JSONObject obj, LoyaltyEarnInfo loyaltyEarnInfo) throws JSONException {
		PointsEarnInfo pointsEarnInfo = loyaltyEarnInfo.getPoints();
		if (pointsEarnInfo != null) {
			int totalPoints = pointsEarnInfo.getTotal();
			obj.put(LOYALTY_TOTAL_POINTS, totalPoints);
		}
		PriceEarnInfo priceEarnInfo = loyaltyEarnInfo.getPrice();
		if (priceEarnInfo != null) {
			Money totalPrice = priceEarnInfo.getTotal();
			addMoney(obj, LOYALTY_TOTAL_PRICE, totalPrice);
			obj.put(LOYALTY_CURRENCY, totalPrice.getCurrency());
		}
	}

	private LoyaltyEarnInfo getEarnInfo(JSONObject obj) throws JSONException {
		PointsEarnInfo pointsEarnInfo = null;
		PriceEarnInfo priceEarnInfo = null;
		int loyaltyTotalPoints = obj.optInt(LOYALTY_TOTAL_POINTS);
		if (loyaltyTotalPoints != 0) {
			pointsEarnInfo = new PointsEarnInfo(0, 0, loyaltyTotalPoints);
		}
		String loyaltyCurrency = obj.optString(LOYALTY_CURRENCY);
		Money loyaltyTotalPrice = parseMoney(obj, LOYALTY_TOTAL_PRICE, loyaltyCurrency);
		if (loyaltyTotalPrice != null) {
			priceEarnInfo = new PriceEarnInfo(null, null, loyaltyTotalPrice);
		}
		return new LoyaltyEarnInfo(pointsEarnInfo, priceEarnInfo);
	}

	private void addMoney(JSONObject obj, String key, Money money) throws JSONException {
		if (money != null) {
			obj.put(key, money.getAmount().toPlainString());
		}
	}

	private Money parseMoney(JSONObject obj, String key, String currency) {
		if (obj.has(key)) {
			Money money = new Money();
			money.setAmount(obj.optString(key));
			money.setCurrency(currency);
			return money;
		}
		return null;
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		return fromJson(obj, null);
	}

	public boolean fromJson(JSONObject obj, Map<String, FlightLeg> legMap) {
		int version = obj.optInt(KEY_VERSION, 1);
		if (version == 1) {
			return fromJsonV1(obj, legMap);
		}

		mProductKey = obj.optString(KEY_PRODUCT_KEY, null);

		if (obj.has(KEY_LEGS)) {
			mLegs = JSONUtils.getJSONableList(obj, KEY_LEGS, FlightLeg.class);
		}
		else if (obj.has(KEY_LEG_IDS) && legMap != null) {
			JSONArray legIds = obj.optJSONArray(KEY_LEG_IDS);
			for (int a = 0; a < legIds.length(); a++) {
				addLeg(legMap.get(legIds.opt(a)));
			}
		}

		try {
			this.earnInfo = getEarnInfo(obj);
		}
		catch (JSONException e) {
			Log.e("Could not parse LoyaltyEarnInfo.", e);
		}

		try {
			this.rewards = getRewardsInfo(obj);
		}
		catch (JSONException e) {
			Log.e("Could not parse RewardsInfo.", e);
		}

		String currency = obj.optString(KEY_CURRENCY);
		if (!TextUtils.isEmpty(currency)) {
			mBaseFare = parseMoney(obj, KEY_BASE_FARE, currency);
			mTotalFare = parseMoney(obj, KEY_TOTAL_FARE, currency);
			mAverageTotalFare = parseMoney(obj, KEY_AVG_TOTAL_FARE, currency);
			mOldTotalPrice = parseMoney(obj, KEY_OLD_TOTAL_PRICE, currency);
			mTaxes = parseMoney(obj, KEY_TAXES, currency);
			mFees = parseMoney(obj, KEY_FEES, currency);
			mPriceChangeAmount = parseMoney(obj, KEY_PRICE_CHANGE_AMOUNT, currency);
			mOnlineBookingFeesAmount = parseMoney(obj, KEY_ONLINE_BOOKING_FEES_AMOUNT, currency);
			mTotalPrice = parseMoney(obj, KEY_TOTAL_PRICE, currency);
		}

		mRewardsPoints = obj.optString(KEY_REWARDS_POINTS);
		mSeatsRemaining = obj.optInt(KEY_SEATS_REMAINING);
		mMayChargeObFees = obj.optBoolean(KEY_MAY_CHARGE_OB_FEES, false);
		mFareName = obj.optString(KEY_FARE_NAME, null);

		JSONArray arr = obj.optJSONArray(KEY_FLIGHT_SEGMENT_ATTRS);
		if (arr != null) {
			initFlightSegmentAttributes(arr.length());
			for (int a = 0; a < arr.length(); a++) {
				List<FlightSegmentAttributes> attrs = JSONUtils.getJSONableList(arr, a,
					FlightSegmentAttributes.class);
				mFlightSegmentAttrs[a] = attrs.toArray(new FlightSegmentAttributes[0]);
			}
		}

		mItineraryNumber = obj.optString(KEY_ITINERARY_NUMBER);

		List<Rule> rules = JSONUtils.getJSONableList(obj, KEY_RULES, Rule.class);
		if (rules != null) {
			for (Rule rule : rules) {
				addRule(rule);
			}
		}

		if (obj.has(KEY_PASSENGERS)) {
			mPassengers = new ArrayList<PassengerCategoryPrice>(
				JSONUtils.getJSONableList(obj, KEY_PASSENGERS, PassengerCategoryPrice.class));
		}
		isPassportNeeded = obj.optBoolean("isPassportNeeded", false);
		isSplitTicket = obj.optBoolean("isSplitTicket", false);
		return true;
	}

	/**
	 * Backwards compatible (aka old) version of the FlightTrip parser.
	 * <p/>
	 * Can slowly be phased out.
	 */
	public boolean fromJsonV1(JSONObject obj, Map<String, FlightLeg> legMap) {
		mProductKey = obj.optString("productKey", null);

		if (obj.has("legs")) {
			mLegs = JSONUtils.getJSONableList(obj, "legs", FlightLeg.class);
		}
		else if (obj.has("legIds") && legMap != null) {
			JSONArray legIds = obj.optJSONArray("legIds");
			for (int a = 0; a < legIds.length(); a++) {
				addLeg(legMap.get(legIds.opt(a)));
			}
		}

		mBaseFare = GsonUtil.getForJsonable(obj, "baseFare", Money.class);
		mTotalFare = GsonUtil.getForJsonable(obj, "totalFare", Money.class);
		mTaxes = GsonUtil.getForJsonable(obj, "taxes", Money.class);
		mFees = GsonUtil.getForJsonable(obj, "fees", Money.class);
		mPriceChangeAmount = GsonUtil.getForJsonable(obj, "priceChangeAmount", Money.class);
		mOnlineBookingFeesAmount = GsonUtil.getForJsonable(obj, "onlineBookingFeesAmount", Money.class);
		mRewardsPoints = obj.optString("rewardsPoints");
		mSeatsRemaining = obj.optInt("seatsRemaining");
		mMayChargeObFees = obj.optBoolean("mayChargeObFees");
		mFareName = obj.optString("fareName");
		rewards = GsonUtil.getForJsonable(obj, "rewards", RewardsInfo.class);

		JSONArray arr = obj.optJSONArray("flightSegmentAttributes");
		if (arr != null) {
			initFlightSegmentAttributes(arr.length());
			for (int a = 0; a < arr.length(); a++) {
				List<FlightSegmentAttributes> attrs = JSONUtils.getJSONableList(arr, a, FlightSegmentAttributes.class);
				mFlightSegmentAttrs[a] = attrs.toArray(new FlightSegmentAttributes[0]);
			}
		}

		mItineraryNumber = obj.optString("itineraryNumber");

		List<Rule> rules = JSONUtils.getJSONableList(obj, "rules", Rule.class);
		if (rules != null) {
			for (Rule rule : rules) {
				addRule(rule);
			}
		}

		return true;
	}
}
