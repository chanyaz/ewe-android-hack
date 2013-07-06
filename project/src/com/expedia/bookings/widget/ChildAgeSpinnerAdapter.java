package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.GuestsPickerUtils;


public class ChildAgeSpinnerAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Resources mResources;

	public ChildAgeSpinnerAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
		mResources = context.getResources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, R.layout.simple_spinner_item);
	}

	private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
		View view;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		}
		else {
			view = convertView;
		}

		TextView text = (TextView) view;

		int age = position + GuestsPickerUtils.MIN_CHILD_AGE;
		String str = null;
		if (age == 0) {
			str = mResources.getString(R.string.child_age_less_than_one);
		}
		else {
			str = mResources.getQuantityString(R.plurals.child_age, age, age);
		}
		text.setText(Html.fromHtml(str));

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

