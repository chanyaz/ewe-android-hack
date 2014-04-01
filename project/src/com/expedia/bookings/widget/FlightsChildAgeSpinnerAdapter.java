package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.GuestsPickerUtils;

public class FlightsChildAgeSpinnerAdapter extends ChildAgeSpinnerAdapter {

	public FlightsChildAgeSpinnerAdapter(Context context) {
		super(context);
	}

	@Override
	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		}
		else {
			view = convertView;
		}

		android.widget.TextView text = (android.widget.TextView) view;

		position = position + GuestsPickerUtils.MIN_CHILD_AGE;
		String str = null;
		if (position == 0) {
			str = mResources.getString(R.string.child_age_less_than_one_on_lap);
		}
		else if (position == 1) {
			str = mResources.getString(R.string.child_age_less_than_one_own_seat);
		}
		else if (position == 2) {
			str = mResources.getString(R.string.child_age_one_on_lap);
		}
		else if (position == 3) {
			str = mResources.getString(R.string.child_age_one_own_seat);
		}
		else {
			position -= 2;
			str = mResources.getQuantityString(R.plurals.child_age, position, position);
		}
		text.setText(Html.fromHtml(str));

		return view;
	}

	@Override
	public int getCount() {
		return GuestsPickerUtils.MAX_CHILD_AGE - GuestsPickerUtils.MIN_CHILD_AGE + 3;
	}
}
