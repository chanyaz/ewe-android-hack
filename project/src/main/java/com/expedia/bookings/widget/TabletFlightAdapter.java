package com.expedia.bookings.widget;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.Ui;

/**
 * Most of the functionality extends from FlightAdapter. This class
 * just contains handling that is specific to tablet.
 */
public class TabletFlightAdapter extends FlightAdapter {

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(parent.getContext());
			convertView = inflater.inflate(R.layout.section_flight_leg_tablet_blue_card, parent, false);
		}
		Drawable background;
		if (mLegPosition == 0) {
			background = parent.getContext().getResources().getDrawable(
				Ui.obtainThemeResID(parent.getContext(), R.attr.skin_bgFlightSummaryRowTablet));
		}
		else {
			background = parent.getContext().getResources().getDrawable(
				Ui.obtainThemeResID(parent.getContext(), R.attr.skin_bgFlightSummaryRowReturnLegTabletDrawable));
		}
		FlightLegSummarySectionTablet flightView = Ui.findView(convertView, R.id.flight_card_container);
		flightView.setBackgroundDrawable(background);
		// Setting the background resets padding, so we have to reset it here.
		int leftRightPadding = (int) parent.getContext().getResources().getDimension(R.dimen.hotel_flight_card_padding_x);
		flightView.setPadding(leftRightPadding, 0, leftRightPadding, 0);

		return super.getView(position, convertView, parent);
	}

}
