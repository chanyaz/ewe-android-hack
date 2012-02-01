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


public class ChildAgeSpinnerAdapter extends BaseAdapter {
	private static final int MINIMUM_CHILD_AGE = 0;
	private static final int MAXIMUM_CHILD_AGE = 17;

	private LayoutInflater mInflater;
	private Resources mResources;

	public ChildAgeSpinnerAdapter(Context context) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResources = context.getResources();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_item);
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

		int age = position + MINIMUM_CHILD_AGE;
		String str = mResources.getQuantityString(R.plurals.child_age, age, age);
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
		return MAXIMUM_CHILD_AGE - MINIMUM_CHILD_AGE + 1;
	}

	@Override
	public Object getItem(int position) {
		return new Integer(position + MINIMUM_CHILD_AGE);
	}

	@Override
	public long getItemId(int position) {
		return position + MINIMUM_CHILD_AGE;
	}
}

