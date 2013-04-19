package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.flightlib.data.Flight;

public class FlightTrip implements JSONable {

	private static double PRICE_CHANGE_NOTIFY_CUTOFF = .01;

	private String mProductKey;

	private String mRewardsPoints;

	private List<FlightLeg> mLegs = new ArrayList<FlightLeg>();

	private Money mBaseFare;
	private Money mTotalFare;
	private Money mTaxes;
	private Money mFees;

	// Possible price change from last time we queried this trip
	private Money mPriceChangeAmount;

	// Server indicates ahead of time *if* we might be charged
	// online booking feess
	private boolean mMayChargeObFees;

	/**
	 * This one is a mouthful. For certain POS/regions, the Expedia API returns essentially duplicate offers that differ
	 * only on price. This price difference can be attributed to the fare including baggage fees or not including baggage
	 * fees. In the client we want to denote this difference to make it obvious to our users why there are essentially
	 * duplicate FlightTrips in the list that have different prices.
	 *
	 * Note: It kind of hurts to have this boolean act as a "double negative", but I don't want to confuse the issue any
	 * further. It is important to know the implementation details here on the API as it also returns a field, that is
	 * so aptly named, "showBaggageFeesIncluded" which is the mutually exclusive counterpart to the field this boolean
	 * represents, "showNoBaggageFeesIncluded". Apparently Expedia backend is super whack and is set up to support the
	 * case where there are duplicate trips and we want to (1) explicitly call out the baggage fee is NOT included, the
	 * norm being that baggage fee is included, and (2) explicitly call out that the baggage fee IS included, norm being
	 * the baggage fee is NOT included. I guess Expedia backend is all-knowing and barks out orders on which type of
	 * message we have to display. Hell, it might obscure some silly POS logic or something for all I know.
	 *
	 * Currently, our app only wants to call out the case where the baggage fee is not included, so we only parse out
	 * this boolean.
	 *
	 */
	private boolean mShowBaggageFeesNotIncluded;

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

	private String mBaggageFeesUrl;

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

	public Money getBaseFare() {
		return mBaseFare;
	}

	public void setBaseFare(Money baseFare) {
		mBaseFare = baseFare;
	}

	public Money getTotalFare() {
		return mTotalFare;
	}

	public void setTotalFare(Money totalFare) {
		mTotalFare = totalFare;
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

	public void setBaggageFeesUrl(String url) {
		mBaggageFeesUrl = url;
	}

	public String getBaggageFeesUrl() {
		return mBaggageFeesUrl;
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

	public Money getTotalFareWithObFees() {
		Money money = new Money(mTotalFare);

		// mTotalFare already includes the OBFees. Defect #395
		//money.add(mOnlineBookingFeesAmount);

		return money;
	}

	public void setShowBaggageFeesNotIncluded(boolean show) {
		mShowBaggageFeesNotIncluded = show;
	}

	public boolean showBaggageFeesNotIncluded() {
		return mShowBaggageFeesNotIncluded;
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
		double newAmount = mTotalFare.getAmount().doubleValue();
		double oldAmount = newAmount - changeAmount;

		if (newAmount > oldAmount) {
			return true;
		}

		return 1.0 - (newAmount / oldAmount) >= PRICE_CHANGE_NOTIFY_CUTOFF;
	}

	/**
	 * Does the trip contain any segments which cross into a different country?
	 * @return true if we cross an international border sometime in this trip
	 */
	public boolean isInternational() {
		boolean retVal = false;
		if (mLegs != null && mLegs.size() > 0) {
			String countryCode = mLegs.get(0).getFirstWaypoint().getAirport().mCountryCode;
			for (FlightLeg leg : mLegs) {
				for (Flight flight : leg.getSegments()) {
					if (flight.mDestination != null && flight.mDestination.getAirport() != null
							&& flight.mDestination.getAirport().mCountryCode != null
							&& !flight.mDestination.getAirport().mCountryCode.equalsIgnoreCase(countryCode)) {
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

	/**
	 * Does this FlightTrip pass through the country supplied via the countryCode param
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
					if (flight.mDestination != null && flight.mDestination.getAirport() != null
							&& flight.mDestination.getAirport().mCountryCode != null
							&& flight.mDestination.getAirport().mCountryCode.equalsIgnoreCase(countryCode)) {
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
			BigDecimal currentPrice = mTotalFare.copy().getAmount();
			BigDecimal priceChange = mPriceChangeAmount.copy().getAmount();
			BigDecimal oldPrice = currentPrice.subtract(priceChange);
			BigDecimal percentageChange = priceChange.divide(oldPrice, RoundingMode.HALF_UP);

			String percentChange = Integer.toString(percentageChange.intValue() * 100);
			percentChange += "%";

			return percentChange;
		}
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
		private int mLegPosition;

		// A list of fields to compare; automatically generated based on the focus field
		private CompareField[] mToCompare;

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
			Money lhsMoney = lhs.getTotalFare();
			Money rhsMoney = rhs.getTotalFare();

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
			Calendar leftStart = lhs.getFirstWaypoint().getMostRelevantDateTime();
			Calendar rightStart = rhs.getFirstWaypoint().getMostRelevantDateTime();

			if (leftStart.before(rightStart)) {
				return -1;
			}
			else if (leftStart.after(rightStart)) {
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
			Calendar leftStart = lhs.getLastWaypoint().getMostRelevantDateTime();
			Calendar rightStart = rhs.getLastWaypoint().getMostRelevantDateTime();

			if (leftStart.before(rightStart)) {
				return -1;
			}
			else if (leftStart.after(rightStart)) {
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
			long leftDuration = lhs.getDuration();
			long rightDuration = rhs.getDuration();

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
		// Things we do not update (since they shoud not change):
		// - Product key
		// - Legs
		// - Leg segment attributes

		if (other.hasPricing()) {
			mBaseFare = other.mBaseFare;
			mTotalFare = other.mTotalFare;
			mTaxes = other.mTaxes;
			mFees = other.mFees;
		}

		if (other.hasPriceChanged()) {
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

		if (other.mRules != null) {
			mRules = other.mRules;
		}
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
	private static final String KEY_TAXES = "g";
	private static final String KEY_FEES = "h";
	private static final String KEY_PRICE_CHANGE_AMOUNT = "i";
	private static final String KEY_ONLINE_BOOKING_FEES_AMOUNT = "j";
	private static final String KEY_REWARDS_POINTS = "k";
	private static final String KEY_SEATS_REMAINING = "l";
	private static final String KEY_BAGGAGE_FEES_URL = "m";
	private static final String KEY_MAY_CHARGE_OB_FEES = "n";
	private static final String KEY_SHOW_BAGGAGE_FEES_NOT_INCLUDED = "o";
	private static final String KEY_FARE_NAME = "p";
	private static final String KEY_FLIGHT_SEGMENT_ATTRS = "q";
	private static final String KEY_ITINERARY_NUMBER = "r";
	private static final String KEY_RULES = "s";
	private static final String KEY_CURRENCY = "t";

	@Override
	public JSONObject toJson() {
		return toJson(true);
	}

	public JSONObject toJson(boolean includeFullLegData) {
		try {
			JSONObject obj = new JSONObject();

			obj.put(KEY_VERSION, JSON_VERSION);

			obj.putOpt(KEY_PRODUCT_KEY, mProductKey);

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

			obj.put(KEY_CURRENCY, mBaseFare.getCurrency());
			addMoney(obj, KEY_BASE_FARE, mBaseFare);
			addMoney(obj, KEY_TOTAL_FARE, mTotalFare);
			addMoney(obj, KEY_TAXES, mTaxes);
			addMoney(obj, KEY_FEES, mFees);
			addMoney(obj, KEY_PRICE_CHANGE_AMOUNT, mPriceChangeAmount);
			addMoney(obj, KEY_ONLINE_BOOKING_FEES_AMOUNT, mOnlineBookingFeesAmount);

			obj.putOpt(KEY_REWARDS_POINTS, mRewardsPoints);
			obj.putOpt(KEY_SEATS_REMAINING, mSeatsRemaining);
			obj.putOpt(KEY_BAGGAGE_FEES_URL, mBaggageFeesUrl);
			if (mMayChargeObFees) {
				obj.putOpt(KEY_MAY_CHARGE_OB_FEES, mMayChargeObFees);
			}
			if (mShowBaggageFeesNotIncluded) {
				obj.putOpt(KEY_SHOW_BAGGAGE_FEES_NOT_INCLUDED, mShowBaggageFeesNotIncluded);
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

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
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

		String currency = obj.optString(KEY_CURRENCY);
		if (!TextUtils.isEmpty(currency)) {
			mBaseFare = parseMoney(obj, KEY_BASE_FARE, currency);
			mTotalFare = parseMoney(obj, KEY_TOTAL_FARE, currency);
			mTaxes = parseMoney(obj, KEY_TAXES, currency);
			mFees = parseMoney(obj, KEY_FEES, currency);
			mPriceChangeAmount = parseMoney(obj, KEY_PRICE_CHANGE_AMOUNT, currency);
			mOnlineBookingFeesAmount = parseMoney(obj, KEY_ONLINE_BOOKING_FEES_AMOUNT, currency);
		}

		mRewardsPoints = obj.optString(KEY_REWARDS_POINTS);
		mSeatsRemaining = obj.optInt(KEY_SEATS_REMAINING);
		mBaggageFeesUrl = obj.optString(KEY_BAGGAGE_FEES_URL);
		mMayChargeObFees = obj.optBoolean(KEY_MAY_CHARGE_OB_FEES, false);
		mShowBaggageFeesNotIncluded = obj.optBoolean(KEY_SHOW_BAGGAGE_FEES_NOT_INCLUDED, false);
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

		return true;
	}

	/**
	 * Backwards compatible (aka old) version of the FlightTrip parser.
	 * 
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

		mBaseFare = JSONUtils.getJSONable(obj, "baseFare", Money.class);
		mTotalFare = JSONUtils.getJSONable(obj, "totalFare", Money.class);
		mTaxes = JSONUtils.getJSONable(obj, "taxes", Money.class);
		mFees = JSONUtils.getJSONable(obj, "fees", Money.class);
		mPriceChangeAmount = JSONUtils.getJSONable(obj, "priceChangeAmount", Money.class);
		mOnlineBookingFeesAmount = JSONUtils.getJSONable(obj, "onlineBookingFeesAmount", Money.class);
		mRewardsPoints = obj.optString("rewardsPoints");
		mSeatsRemaining = obj.optInt("seatsRemaining");
		mBaggageFeesUrl = obj.optString("baggageFeesUrl");
		mMayChargeObFees = obj.optBoolean("mayChargeObFees");
		mShowBaggageFeesNotIncluded = obj.optBoolean("showBaggageFeesNotIncluded");
		mFareName = obj.optString("fareName");

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
