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
	private double mMin, mQ1, mMedian, mQ3, mMax;

	// This is defined as 1.5 by the math gods, but we may want to increase if we are throwing out seemingly reasonable prices
	private static final double IQR_MULTIPLIER = 1.5d;

	public void setFlightHistograms(List<FlightHistogram> flightHistograms) {
		mFlightHistograms = flightHistograms;
		computeMetadata();
	}

	public List<FlightHistogram> getFlightHistograms() {
		return mFlightHistograms;
	}

	public double getMinPrice() {
		return mMin;
	}

	public double getMaxPrice() {
		return mMax;
	}

	//////////////////////////////////////////////////////
	// Quartile computation

	public void computeMetadata() {
		if (mFlightHistograms == null || (mFlightHistograms != null && mFlightHistograms.size() < 3)) {
			return;
		}

		long start = System.currentTimeMillis();

		// Sort the values by price
		Collections.sort(mFlightHistograms, sPriceComparator);

		// Compute min, q1, median, q3, max
		computeQuartiles();

		// Compute IQR
		double iqr = mQ3 - mQ1;
		double upperFenceValue = mQ3 + IQR_MULTIPLIER * iqr; // 1.5

		// Trash the outliers above the upper fence. We are OK with showing abnormally low prices...
		List<Double> removed = new LinkedList<Double>();
		Iterator iterator = mFlightHistograms.iterator();
		FlightHistogram gram;
		while (iterator.hasNext()) {
			gram = (FlightHistogram) iterator.next();
			if (gram.getMinPrice() > upperFenceValue) {
				removed.add(Double.valueOf(gram.getMinPrice()));
				iterator.remove();
			}
		}

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

	// much inspiration: http://en.wikipedia.org/wiki/Quartile
	private void computeQuartiles() {
		// Compute min, q1, median, q3, max
		mMinIndex = 0;
		mMaxIndex = mFlightHistograms.size() - 1;
		mMedianIndex = mMaxIndex / 2;
		mQ1Index = mMinIndex + (mMedianIndex - mMinIndex) / 2;
		mQ3Index = mMedianIndex + (mMaxIndex - mMedianIndex) / 2;

		mMin = mFlightHistograms.get(0).getMinPrice();
		mQ1 = mFlightHistograms.get(mQ1Index).getMinPrice();
		mMedian = mFlightHistograms.get(mMedianIndex).getMinPrice();
		mQ3 = mFlightHistograms.get(mQ3Index).getMinPrice();
		mMax = mFlightHistograms.get(mMaxIndex).getMinPrice();
	}

	private static final Comparator<FlightHistogram> sDateComparator = new Comparator<FlightHistogram>() {
		@Override
		public int compare(FlightHistogram lhs, FlightHistogram rhs) {
			if (lhs.getDate().isEqual(rhs.getDate())) {
				return 0;
			}
			else {
				boolean isBefore = lhs.getDate().isBefore(rhs.getDate());
				return isBefore ? -1 : 1;
			}
		}
	};

	private static final Comparator<FlightHistogram> sPriceComparator = new Comparator<FlightHistogram>() {
		@Override
		public int compare(FlightHistogram lhs, FlightHistogram rhs) {
			return (int) (lhs.getMinPrice() - rhs.getMinPrice());
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
