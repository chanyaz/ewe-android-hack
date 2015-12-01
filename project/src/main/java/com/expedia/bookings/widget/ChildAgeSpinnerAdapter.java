package com.expedia.bookings.widget;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;


public class ChildAgeSpinnerAdapter extends BaseAdapter {
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = createViewFromResource(position, convertView, parent, R.layout.child_spinner_item);
		TextView textView = (TextView) v;
		Drawable icon = parent.getContext().getResources().getDrawable(R.drawable.traveler).mutate();
		icon.setColorFilter(parent.getContext().getResources().getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
		return v;
	}

	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
		}

		TextView text = (TextView) convertView;
		text.setText(StrUtils.getChildTravelerAgeText(parent.getContext().getResources(), position));

		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, R.layout.traveler_spinner_dropdown);
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

