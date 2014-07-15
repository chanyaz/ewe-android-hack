package com.expedia.bookings.data;

import java.util.ArrayList;

import org.joda.time.LocalDate;

import android.util.SparseArray;

/**
 * Holds a week's worth of FlightHistogram objects.
 */
public class WeeklyFlightHistogram extends SparseArray<FlightHistogram> implements Comparable<WeeklyFlightHistogram> {

	private String mKeyString;
	private LocalDate mWeekStart;
	private LocalDate mWeekEnd;

	private static String getKeyString(FlightHistogram gram) {
		return getKeyString(gram.getKeyDate());
	}

	private static String getKeyString(LocalDate mKeyDate) {
		int week = mKeyDate.getWeekOfWeekyear();
		return mKeyDate.getYear() + "-" + ((week < 10) ? "0" : "") + week;
	}

	private static int getGramIndex(FlightHistogram gram) {
		return getGramIndex(gram.getKeyDate());
	}

	private static int getGramIndex(LocalDate mKeyDate) {
		return mKeyDate.getDayOfWeek();
	}

	/**
	 * Creates a new WeeklyFlightHistogram object and initiates it with the week in which the
	 * passed gram will fit.
	 *
	 * @param gram
	 */
	public WeeklyFlightHistogram(FlightHistogram gram) {
		super(7);
		mKeyString = getKeyString(gram);
		mWeekStart = gram.getKeyDate().withDayOfWeek(1);
		mWeekEnd = gram.getKeyDate().withDayOfWeek(7);
		add(gram);
	}

	public boolean add(FlightHistogram gram) {
		if (!isInWeek(gram)) {
			throw new RuntimeException("The passed flight histogram is not in the valid week range");
		}
		int index = getGramIndex(gram);
		put(index, gram);
		return true;
	}

	public boolean isInWeek(FlightHistogram gram) {
		return getKeyString(gram).equals(mKeyString);
	}

	public String getKeyString() {
		return mKeyString;
	}

	public LocalDate getWeekStart() {
		return mWeekStart;
	}

	public LocalDate getWeekEnd() {
		return mWeekEnd;
	}

	@Override
	public int compareTo(WeeklyFlightHistogram another) {
		return mKeyString.compareTo(another.mKeyString);
	}
}
