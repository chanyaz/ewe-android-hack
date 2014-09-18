package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;

/**
 * Most of the functionality extends from FlightLegSummarySection. This class
 * just contains handling that is specific to tablet.
 */
public class FlightLegSummarySectionTablet extends FlightLegSummarySection {

	public FlightLegSummarySectionTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected int getBagWithXDrawableResId() {
		return R.drawable.ic_tablet_baggage_check_fees;
	}

	@Override
	protected void adjustLayout(final FlightLeg leg, boolean isIndividualFlight) {
		// No special layout adjustments need to be made on the tablet
		// flight leg summary rows.
	}
}
