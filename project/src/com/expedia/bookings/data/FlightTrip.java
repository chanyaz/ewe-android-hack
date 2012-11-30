package com.expedia.bookings.data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
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

	private int mSeatsRemaining;

	// These are modifiers for each segment in leg
	private List<List<FlightSegmentAttributes>> mFlightSegmentAttrs = new ArrayList<List<FlightSegmentAttributes>>();

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

	public void addFlightSegmentAttributes(List<FlightSegmentAttributes> attributes) {
		mFlightSegmentAttrs.add(attributes);
	}

	public List<FlightSegmentAttributes> getFlightSegmentAttributes(int legPosition) {
		return mFlightSegmentAttrs.get(legPosition);
	}

	public List<FlightSegmentAttributes> getFlightSegmentAttributes(FlightLeg leg) {
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

	@Override
	public JSONObject toJson() {
		return toJson(true);
	}

	public JSONObject toJson(boolean includeFullLegData) {
		try {
			JSONObject obj = new JSONObject();
			obj.putOpt("productKey", mProductKey);

			if (includeFullLegData) {
				JSONUtils.putJSONableList(obj, "legs", mLegs);
			}
			else {
				JSONArray legIds = new JSONArray();
				for (FlightLeg leg : mLegs) {
					legIds.put(leg.getLegId());
				}
				obj.putOpt("legIds", legIds);
			}

			JSONUtils.putJSONable(obj, "baseFare", mBaseFare);
			JSONUtils.putJSONable(obj, "totalFare", mTotalFare);
			JSONUtils.putJSONable(obj, "taxes", mTaxes);
			JSONUtils.putJSONable(obj, "fees", mFees);
			JSONUtils.putJSONable(obj, "priceChangeAmount", mPriceChangeAmount);
			obj.putOpt("rewardsPoints", mRewardsPoints);
			obj.putOpt("seatsRemaining", mSeatsRemaining);

			JSONArray arr = new JSONArray();
			for (List<FlightSegmentAttributes> attributes : mFlightSegmentAttrs) {
				JSONUtils.putJSONableList(arr, attributes);
			}
			obj.putOpt("flightSegmentAttributes", arr);

			obj.putOpt("itineraryNumber", mItineraryNumber);

			if (mRules != null) {
				JSONUtils.putJSONableList(obj, "rules", new ArrayList<Rule>(mRules.values()));
			}

			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		return fromJson(obj, null);
	}

	public boolean fromJson(JSONObject obj, Map<String, FlightLeg> legMap) {
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
		mRewardsPoints = obj.optString("rewardsPoints");
		mSeatsRemaining = obj.optInt("seatsRemaining");

		JSONArray arr = obj.optJSONArray("flightSegmentAttributes");
		for (int a = 0; a < arr.length(); a++) {
			mFlightSegmentAttrs.add(JSONUtils.getJSONableList(arr, a, FlightSegmentAttributes.class));
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
