package com.expedia.bookings.fragment.base;

import android.support.v4.app.Fragment;

public interface MeasurableFragmentListener {

	/**
	 * Tells you once you can get the width/height/location of
	 * the Views of this Fragment.
	 */
	public void canMeasure(Fragment fragment);

}
