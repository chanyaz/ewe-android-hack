package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.data.FlightLeg;

/**
 * Most of the functionality extends from FlightLegSummarySection. This class
 * just contains handling that is specific to tablet.
 */
public class FlightLegSummarySectionTablet extends FlightLegSummarySection {

	public FlightLegSummarySectionTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	void adjustLayout(final FlightLeg leg, boolean isIndividualFlight) {
	}
}
