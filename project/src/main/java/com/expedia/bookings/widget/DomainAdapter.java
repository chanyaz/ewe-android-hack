package com.expedia.bookings.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.expedia.bookings.R;

public class DomainAdapter extends BaseAdapter {
	private CharSequence[] mNames;
	private CharSequence[] mDomains;

	private Integer[] flagsArray;
	private final LayoutInflater mInflater;
	private int mSelected = 0;

	public DomainAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public void setDomains(CharSequence[] names, CharSequence[] values, Integer[] flags) {
		mNames = names;
		mDomains = values;
		flagsArray = flags;
	}

	public void setSelected(int p) {
		mSelected = p;
	}

	private class DomainViewHolder {
		// View stuff
		android.widget.TextView mNameTextView;
		android.widget.TextView mDomainTextView;
		android.widget.ImageView mImageView;
		RadioButton mRadioButton;
	}

	public class DomainTuple {
		public CharSequence mName;
		public CharSequence mDomain;
		public int mDrawable;
	}

	// Adapter implementation

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getCount() {
		return mNames.length < mDomains.length ? mNames.length : mDomains.length;
	}

	@Override
	public Object getItem(int position) {
		if (mNames != null && mDomains != null && mNames.length > position && mDomains.length > position) {
			DomainTuple t = new DomainTuple();
			t.mName = mNames[position];
			t.mDomain = mDomains[position];
			t.mDrawable = flagsArray[position];
			return t;
		}
		else {
			return null;
		}
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		DomainViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.row_domain_preference, parent, false);
			holder = new DomainViewHolder();
			holder.mNameTextView = (android.widget.TextView) convertView.findViewById(R.id.country_name_text_view);
			holder.mDomainTextView = (android.widget.TextView) convertView.findViewById(R.id.domain_name_text_view);
			holder.mRadioButton = (RadioButton) convertView.findViewById(R.id.domain_radio_button);
			holder.mImageView = (ImageView) convertView.findViewById(R.id.imageView);
			convertView.setTag(holder);
		}
		else {
			holder = (DomainViewHolder) convertView.getTag();
		}

		if (position == mSelected) {
			holder.mRadioButton.setChecked(true);
		}
		else {
			holder.mRadioButton.setChecked(false);
		}

		DomainTuple d = (DomainTuple) getItem(position);

		holder.mNameTextView.setText(d.mName);
		holder.mDomainTextView.setText(d.mDomain);
		holder.mImageView.setImageResource(d.mDrawable);
		return convertView;
	}
}
