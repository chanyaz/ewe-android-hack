package com.expedia.bookings.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;

public class ChildAgeSpinnerAdapter extends BaseAgeSpinnerAdapter {
	@Override
	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
		}

		TextView text = (TextView) convertView;
		text.setText(StrUtils.getChildTravelerAgeText(parent.getContext().getResources(), position));

		return convertView;
	}

	@Override
	public int getCount() {
		return GuestsPickerUtils.MAX_CHILD_AGE - GuestsPickerUtils.MIN_CHILD_AGE + 1;
	}

	@Override
	public Object getItem(int position) {
		return position + GuestsPickerUtils.MIN_CHILD_AGE;
	}

	@Override
	public long getItemId(int position) {
		return position + GuestsPickerUtils.MIN_CHILD_AGE;
	}
}

