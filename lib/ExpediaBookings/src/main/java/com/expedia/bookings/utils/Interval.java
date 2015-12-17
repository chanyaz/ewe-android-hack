package com.expedia.bookings.utils;

/*
 * A very fancy tool for tracking min and max integer values
 */

public class Interval {

	private int min = Integer.MAX_VALUE;
	private int max = Integer.MIN_VALUE;

	public void add(int n) {
		min = Math.min(n, min);
		max = Math.max(n, max);
	}

	public void addIgnoreZero(int n) {
		if (n != 0) {
			add(n);
		}
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	// Check if the min and max are different.
	public boolean different() {
		return min != max;
	}

	// Check if both are non "infinity" (MAX_VALUE/MIN_VALUE)
	public boolean bounded() {
		return min != Integer.MAX_VALUE && max != Integer.MIN_VALUE;
	}

}
