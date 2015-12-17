package com.expedia.bookings.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Weeks;
import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;
import com.mobiata.android.time.util.JodaUtils;

// TODO UNIT TESTS
public class FlightSearchHistogramResponse extends Response implements JSONable {

	// This is defined as 1.5 by the math gods, but we may want to
	// increase if we are throwing out seemingly reasonable prices
	private static final double IQR_MULTIPLIER = 1.5d;

	private List<FlightHistogram> mFlightHistograms;

	private Money mMin, mMax;

	public void setFlightHistograms(List<FlightHistogram> flightHistograms) {
		mFlightHistograms = flightHistograms;
		mMin = null;
		mMax = null;

		// Sort the data by date for the UI
		Collections.sort(mFlightHistograms, sDateComparator);
	}

	public List<FlightHistogram> getFlightHistograms() {
		return mFlightHistograms;
	}

	public Money getMinPrice() {
		if (mMin == null && getCount() != 0) {
			mMin = Collections.min(mFlightHistograms, sPriceComparator).getMinPrice();
		}
		return mMin;
	}

	public Money getMaxPrice() {
		if (mMax == null && getCount() != 0) {
			mMax = Collections.max(mFlightHistograms, sPriceComparator).getMinPrice();
		}
		return mMax;
	}

	public int getCount() {
		return mFlightHistograms == null ? 0 : mFlightHistograms.size();
	}

	//////////////////////////////////////////////////////
	// Filtering Rules

	// Trash the outliers above the upper fence. We are OK with showing abnormally low prices...
	public void removeUpperOutliers() {
		if (getCount() < 3) {
			return;
		}

		// Temporarily sort by price. We'll re-sort it later in this method.
		Collections.sort(mFlightHistograms, sPriceComparator);

		// Compute quartiles
		// much inspiration: http://en.wikipedia.org/wiki/Quartile
		int maxIndex = mFlightHistograms.size() - 1;
		int q1Index = maxIndex / 4;
		int q3Index = maxIndex - q1Index;
		double dq1 = mFlightHistograms.get(q1Index).getMinPrice().getAmount().doubleValue();
		double dq3 = mFlightHistograms.get(q3Index).getMinPrice().getAmount().doubleValue();

		// We've decided to remove everything above (iqr * 1.5) higher than the 3rd quartile
		double iqr = dq3 - dq1;
		double upperFenceValue = dq3 + IQR_MULTIPLIER * iqr;

		Iterator iterator = mFlightHistograms.iterator();
		FlightHistogram gram;
		while (iterator.hasNext()) {
			gram = (FlightHistogram) iterator.next();
			if (gram.getMinPrice().getAmount().doubleValue() > upperFenceValue) {
				iterator.remove();
			}
		}

		// The max value may have changed if we removed some
		mMax = null;

		// Sort the data by date for the UI
		Collections.sort(mFlightHistograms, sDateComparator);
	}

	// Follow rules here:
	// https://expedia.mingle.thoughtworks.com/projects/eb_ad_app/cards/3281
	public void apply8WeekFilter() {
		// 2. If there are less than 10 data points within the first 8 weeks, don't show GDE at all

		// Less than 10 total. So obvi that means less than 10 in the first 8 weeks.
		if (getCount() < 10) {
			mFlightHistograms.clear();
			mMin = null;
			mMax = null;
			return;
		}

		// If the 10th data point is more than 8 weeks in the future, that means there
		// are less than 10 data points in the first 8 weeks.
		if (weeksFromToday(9) > 8) {
			mFlightHistograms.clear();
			mMin = null;
			mMax = null;
			return;
		}

		// 3. As you are drawing out the weeks, after you have drawn 8 weeks, if you hit two
		// consecutive weeks with zero data points, cut off the GDE calendar above these two weeks.

		for (int i = 10; i < getCount(); i++) {
			while (i < getCount() && weeksFromToday(i) > 8 && calendarWeeksBetween(i - 1, i) > 2) {
				mFlightHistograms.remove(i);
				mMin = null;
				mMax = null;
			}
		}
	}

	private int weeksFromToday(int b) {
		return Weeks.weeksBetween(getWeekStartDate(LocalDate.now()),
			getWeekStartDate(mFlightHistograms.get(b))).getWeeks();
	}

	private int calendarWeeksBetween(int a, int b) {
		return Weeks.weeksBetween(getWeekStartDate(mFlightHistograms.get(a)),
			getWeekStartDate(mFlightHistograms.get(b))).getWeeks();
	}

	private LocalDate getWeekStartDate(FlightHistogram gram) {
		return getWeekStartDate(gram.getKeyDate());
	}

	private LocalDate getWeekStartDate(LocalDate date) {
		return date.minusDays(JodaUtils.getDayOfWeekNormalized(date));
	}

	//////////////////////////////////////////////////////
	// Comparators used for sorting

	private static final Comparator<FlightHistogram> sDateComparator = new Comparator<FlightHistogram>() {
		@Override
		public int compare(FlightHistogram lhs, FlightHistogram rhs) {
			return lhs.compareTo(rhs);
		}
	};

	private static final Comparator<FlightHistogram> sPriceComparator = new Comparator<FlightHistogram>() {
		@Override
		public int compare(FlightHistogram lhs, FlightHistogram rhs) {
			return lhs.getMinPrice().compareTo(rhs.getMinPrice());
		}
	};

	//////////////////////////////////////////////////////
	// JSONable

	@Override
	public JSONObject toJson() {
		JSONObject obj = super.toJson();
		if (obj == null) {
			return null;
		}

		try {
			JSONUtils.putJSONableList(obj, "histograms", mFlightHistograms);
			return obj;
		}
		catch (JSONException e) {
			throw new RuntimeException("Unable to FlightSearchHistogramResponse.toJson()", e);
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightHistograms = JSONUtils.getJSONableList(obj, "histograms", FlightHistogram.class);
		return true;
	}

}
