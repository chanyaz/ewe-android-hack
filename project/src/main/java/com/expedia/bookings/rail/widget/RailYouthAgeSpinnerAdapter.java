package com.expedia.bookings.rail.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.ChildAgeSpinnerAdapter;

public class RailYouthAgeSpinnerAdapter extends ChildAgeSpinnerAdapter {
	@Override
	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
		}
		TextView text = (TextView) convertView;
		text.setText(StrUtils.getYouthTravelerAgeText(parent.getContext().getResources(), position));
		return convertView;
	}

	@Override
	public int getCount() {
		return GuestsPickerUtils.MAX_RAIL_YOUTH_AGE - GuestsPickerUtils.MIN_RAIL_YOUTH_AGE + 1;
	}

	@Override
	public Object getItem(int position) {
		return position + GuestsPickerUtils.MIN_RAIL_YOUTH_AGE;
	}

	@Override
	public long getItemId(int position) {
		return position + GuestsPickerUtils.MIN_RAIL_YOUTH_AGE;
	}
}

