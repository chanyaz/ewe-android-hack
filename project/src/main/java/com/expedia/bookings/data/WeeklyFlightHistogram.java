package com.expedia.bookings.data;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import android.util.SparseArray;

import com.mobiata.android.Log;
import com.mobiata.android.time.util.JodaUtils;

/**
 * Holds a week's worth of FlightHistogram objects.
 */
public class WeeklyFlightHistogram extends SparseArray<FlightHistogram> implements Comparable<WeeklyFlightHistogram> {

	private LocalDate mWeekStart;
	private LocalDate mWeekEnd;

	/**
	 * Creates a new WeeklyFlightHistogram object and initiates it with the week in which the
	 * passed LocalDate will fit.
	 *
	 * @param seed
	 */
	public WeeklyFlightHistogram(LocalDate seed) {
		super(7);
		mWeekStart = seed.minusDays(JodaUtils.getDayOfWeekNormalized(seed));
		mWeekEnd = mWeekStart.plusDays(6);
	}

	public boolean add(FlightHistogram gram) {
		if (!isInWeek(gram)) {
			Log.w("The passed flight histogram (" + gram.getKeyDate() + ") is not in the valid week range ("
				+ getWeekStart() + "-" + getWeekEnd() + ")");
			return false;
		}
		int index = Days.daysBetween(mWeekStart, gram.getKeyDate()).getDays();
		put(index, gram);
		return true;
	}

	public boolean isInWeek(FlightHistogram gram) {
		return isInWeek(gram.getKeyDate());
	}

	public boolean isInWeek(LocalDate date) {
		return date.compareTo(mWeekStart) >= 0 && date.compareTo(mWeekEnd) <= 0;
	}

	public LocalDate getWeekStart() {
		return mWeekStart;
	}

	public LocalDate getWeekEnd() {
		return mWeekEnd;
	}

	@Override
	public int compareTo(WeeklyFlightHistogram another) {
		// We'll assume that if their start dates are equal, then their end dates are too
		return mWeekStart.compareTo(another.mWeekStart);
	}
}
