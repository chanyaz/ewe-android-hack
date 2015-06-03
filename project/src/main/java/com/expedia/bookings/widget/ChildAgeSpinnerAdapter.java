package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.GuestsPickerUtils;


public class ChildAgeSpinnerAdapter extends BaseAdapter {
	protected LayoutInflater mInflater;
	protected Resources mResources;

	public ChildAgeSpinnerAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = createViewFromResource(position, convertView, parent, R.layout.child_spinner_item);
		TextView textView = (TextView) v;
		Drawable icon = mResources.getDrawable(R.drawable.traveler).mutate();
		icon.setColorFilter(mResources.getColor(R.color.cars_actionbar_text_color), PorterDuff.Mode.SRC_IN);
		textView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
		return v;
	}

	private String getAgeText(int position) {
		int age = position + GuestsPickerUtils.MIN_CHILD_AGE;
		String str = null;
		if (age == 0) {
			str = mResources.getString(R.string.child_age_less_than_one);
		}
		else {
			str = mResources.getQuantityString(R.plurals.child_age, age, age);
		}
		return Html.fromHtml(str).toString();
	}
	protected View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		}
		else {
			view = convertView;
		}

		TextView text = (TextView) view;
		text.setText(getAgeText(position));// converts to Html, then strips out so no loc changes

		return view;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public int getCount() {
		return GuestsPickerUtils.MAX_CHILD_AGE - GuestsPickerUtils.MIN_CHILD_AGE + 1;
	}

	@Override
	public Object getItem(int position) {
		return Integer.valueOf(position + GuestsPickerUtils.MIN_CHILD_AGE);
	}

	@Override
	public long getItemId(int position) {
		return position + GuestsPickerUtils.MIN_CHILD_AGE;
	}
}

