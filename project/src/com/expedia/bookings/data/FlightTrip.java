package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import com.mobiata.flightlib.data.Flight;

public class FlightTrip {

	private String mProductKey;

	private List<FlightLeg> mLegs = new ArrayList<FlightLeg>();

	private Money mBaseFare;
	private Money mTotalFare;
	private Money mTaxes;
	private Money mFees;

	private int mSeatsRemaining;

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

	public int getSeatsRemaining() {
		return mSeatsRemaining;
	}

	public void setSeatsRemaining(int seatsRemaining) {
		mSeatsRemaining = seatsRemaining;
	}

	////////////////////////////////////////////////////////////////////////
	// More meta retrieval methods

	public boolean hasPricing() {
		return mBaseFare != null && mTotalFare != null && mTaxes != null && mFees != null;
	}

	////////////////////////////////////////////////////////////////////////
	// Comparators

	public static final Comparator<FlightTrip> PRICE_COMPARATOR = new Comparator<FlightTrip>() {
		@Override
		public int compare(FlightTrip lhs, FlightTrip rhs) {
			double lhsAmount = lhs.getTotalFare().getAmount();
			double rhsAmount = rhs.getTotalFare().getAmount();

			if (lhsAmount == rhsAmount) {
				return 0;
			}
			if (lhsAmount < rhsAmount) {
				return -1;
			}
			else {
				return 1;
			}
		}
	};

	// Utility: We end up comparing a leg of a trip a lot
	public abstract static class LegComparator implements Comparator<FlightTrip> {
		private int mLegPosition;

		public LegComparator(int legPosition) {
			mLegPosition = legPosition;
		}

		@Override
		public int compare(FlightTrip lhs, FlightTrip rhs) {
			return compare(lhs.getLeg(mLegPosition), rhs.getLeg(mLegPosition));
		}

		public abstract int compare(FlightLeg lhs, FlightLeg rhs);
	}

	public static class DepartureComparator extends LegComparator {

		public DepartureComparator(int legPosition) {
			super(legPosition);
		}

		@Override
		public int compare(FlightLeg lhs, FlightLeg rhs) {
			Flight leftFlight = lhs.getSegment(0);
			Flight rightFlight = rhs.getSegment(0);

			Calendar leftStart = leftFlight.mOrigin.getMostRelevantDateTime();
			Calendar rightStart = rightFlight.mOrigin.getMostRelevantDateTime();

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
	}

	public static class ArrivalComparator extends LegComparator {

		public ArrivalComparator(int legPosition) {
			super(legPosition);
		}

		@Override
		public int compare(FlightLeg lhs, FlightLeg rhs) {
			Flight leftFlight = lhs.getSegment(lhs.getSegmentCount() - 1);
			Flight rightFlight = rhs.getSegment(rhs.getSegmentCount() - 1);

			Calendar leftStart = leftFlight.mDestination.getMostRelevantDateTime();
			Calendar rightStart = rightFlight.mDestination.getMostRelevantDateTime();

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
	}

	public static class DurationComparator extends LegComparator {

		public DurationComparator(int legPosition) {
			super(legPosition);
		}

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
	}

}
