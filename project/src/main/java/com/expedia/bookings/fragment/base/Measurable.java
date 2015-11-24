package com.expedia.bookings.fragment.base;

/**
 * Marks a Fragment (or View or whatever) that can know when
 * it can be measured.
 */
public interface Measurable {
	boolean isMeasurable();
}
