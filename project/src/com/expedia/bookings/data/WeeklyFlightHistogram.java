package com.expedia.bookings.data;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import android.util.SparseArray;

import com.mobiata.android.time.util.JodaUtils;

/**
 * Holds a week's worth of FlightHistogram objects.
 */
public class WeeklyFlightHistogram extends SparseArray<FlightHistogram> implements Comparable<WeeklyFlightHistogram> {

	private LocalDate mWeekStart;
	private LocalDate mWeekEnd;

	/**
	 * Creates a new WeeklyFlightHistogram object and initiates it with the week in which the
	 * passed gram will fit.
	 *
	 * @param gram
	 */
	public WeeklyFlightHistogram(FlightHistogram gram) {
		super(7);
		LocalDate seed = gram.getKeyDate();
		mWeekStart = seed.minusDays(JodaUtils.getDayOfWeekNormalized(seed));
		mWeekEnd = mWeekStart.plusDays(6);
		add(gram);
	}

	public boolean add(FlightHistogram gram) {
		if (!isInWeek(gram)) {
			throw new RuntimeException("The passed flight histogram is not in the valid week range");
		}
		int index = Days.daysBetween(mWeekStart, gram.getKeyDate()).getDays();
		put(index, gram);
		return true;
	}

	public boolean isInWeek(FlightHistogram gram) {
		return gram.getKeyDate().compareTo(mWeekStart) >= 0 && gram.getKeyDate().compareTo(mWeekEnd) <= 0;
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
