package com.expedia.bookings.section;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayAddress extends LinearLayout implements ISection<Location> {
	TextView mLineOne;
	TextView mCity;
	TextView mState;
	TextView mZip;

	Location mLocation;

	public SectionDisplayAddress(Context context) {
		super(context);
		init(context);
	}

	public SectionDisplayAddress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionDisplayAddress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {

	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mLineOne = Ui.findView(this, R.id.address_line_one);
		mCity = Ui.findView(this, R.id.address_city);
		mState = Ui.findView(this, R.id.address_state);
		mZip = Ui.findView(this, R.id.address_zip);

	}

	public void bind(Location loc) {
		//Update fields
		mLocation = loc;
		if (mLocation != null) {
			if (mLineOne != null && mLocation.getStreetAddressString() != null) {
				mLineOne.setText(mLocation.getStreetAddressString());
			}
			if (mCity != null && mLocation.getCity() != null) {
				mCity.setText(mLocation.getCity());
			}
			if (mState != null && mLocation.getStateCode() != null) {
				mState.setText(mLocation.getStateCode());
			}
			if (mZip != null && mLocation.getPostalCode() != null) {
				mZip.setText(mLocation.getPostalCode());
			}
		}
	}

}
