package com.expedia.bookings.widget;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;

public abstract class BaseAgeSpinnerAdapter extends BaseAdapter {
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = createViewFromResource(position, convertView, parent, R.layout.age_spinner_item);
		TextView textView = (TextView) v;
		Drawable icon = ContextCompat.getDrawable(parent.getContext(), R.drawable.search_form_traveler_picker_person).mutate();
		icon.setColorFilter(ContextCompat.getColor(parent.getContext(), R.color.search_dialog_icon_color), PorterDuff.Mode.SRC_IN);
		textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
		return v;
	}

	protected abstract View createViewFromResource(int position, View convertView, ViewGroup parent, int resource);

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, R.layout.traveler_spinner_dropdown);
	}
}

