package com.expedia.bookings.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.json.JSONable;

// TODO UNIT TESTS
public class FlightSearchHistogramResponse extends Response implements JSONable {

	private List<FlightHistogram> mFlightHistograms;

	// Quartile vars
	private int mMinIndex, mQ1Index, mMedianIndex, mQ3Index, mMaxIndex;
	private Money mMin, mQ1, mMedian, mQ3, mMax;

	// This is defined as 1.5 by the math gods, but we may want to increase if we are throwing out seemingly reasonable prices
	private static final double IQR_MULTIPLIER = 1.5d;

	public void setFlightHistograms(List<FlightHistogram> flightHistograms) {
		mFlightHistograms = flightHistograms;
		computeMetadata();
	}

	public List<FlightHistogram> getFlightHistograms() {
		return mFlightHistograms;
	}

	public Money getMinPrice() {
		return mMin;
	}

	public Money getMaxPrice() {
		return mMax;
	}

	public int getCount() {
		return mFlightHistograms == null ? 0 : mFlightHistograms.size();
	}

	//////////////////////////////////////////////////////
	// Quartile computation

	public void computeMetadata() {
		if (mFlightHistograms == null || mFlightHistograms.size() == 0) {
			return;
		}

		long start = System.currentTimeMillis();

		// Sort the values by price
		Collections.sort(mFlightHistograms, sPriceComparator);

		// Compute min and max
		computeMinAndMax();

		if (mFlightHistograms.size() < 3) {
			return;
		}

		// Compute q1, median, q3
		computeQuartiles();

		// Compute IQR
		double iqr = mQ3.getAmount().doubleValue() - mQ1.getAmount().doubleValue();
		double upperFenceValue = mQ3.getAmount().doubleValue() + IQR_MULTIPLIER * iqr; // 1.5

		// Trash the outliers above the upper fence. We are OK with showing abnormally low prices...
		List<Money> removed = new LinkedList<>();
		Iterator iterator = mFlightHistograms.iterator();
		FlightHistogram gram;
		while (iterator.hasNext()) {
			gram = (FlightHistogram) iterator.next();
			if (gram.getMinPrice().getAmount().doubleValue() > upperFenceValue) {
				removed.add(gram.getMinPrice());
				iterator.remove();
			}
		}

		// Re-compute the min and max since some may have been removed
		computeMinAndMax();

		// Log some things
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		builder.append("Quartile Math");
		builder.append("\n");
		builder.append("Quartile indices: min=" + mMinIndex + " q1=" + mQ1Index + " median=" + mMedianIndex + " q3=" + mQ3Index + " max=" + mMaxIndex);
		builder.append("\n");
		builder.append("Quartile values: min=" + mMin + " q1=" + mQ1 + " median=" + mMedian + " q3=" + mQ3 + " max=" + mMax);
		builder.append("\n");
		builder.append("Quartile removed, upper fence=" + upperFenceValue + " : " + removed);
		Log.d(builder.toString());

		// Recompute the quartiles
		computeQuartiles();

		// Sort the data by date for the UI
		Collections.sort(mFlightHistograms, sDateComparator);

		Log.d("Quartile computation completed in " + (System.currentTimeMillis() - start) + " ms");
	}

	private void computeMinAndMax() {
		mMinIndex = 0;
		mMaxIndex = mFlightHistograms.size() - 1;
		mMin = mFlightHistograms.get(mMinIndex).getMinPrice();
		mMax = mFlightHistograms.get(mMaxIndex).getMinPrice();
	}

	// much inspiration: http://en.wikipedia.org/wiki/Quartile
	private void computeQuartiles() {
		// Compute min, q1, median, q3, max
		mMedianIndex = mMaxIndex / 2;
		mQ1Index = mMinIndex + (mMedianIndex - mMinIndex) / 2;
		mQ3Index = mMedianIndex + (mMaxIndex - mMedianIndex) / 2;

		mQ1 = mFlightHistograms.get(mQ1Index).getMinPrice();
		mMedian = mFlightHistograms.get(mMedianIndex).getMinPrice();
		mQ3 = mFlightHistograms.get(mQ3Index).getMinPrice();
	}

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
			throw new RuntimeException("Unable to FlightSearchHistogramResponse.toJson()");
		}
	}

	@Override
	public boolean fromJson(JSONObject obj) {
		super.fromJson(obj);
		mFlightHistograms = JSONUtils.getJSONableList(obj, "histograms", FlightHistogram.class);
		return true;
	}

}
