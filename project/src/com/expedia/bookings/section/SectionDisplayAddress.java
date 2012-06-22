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
		this(context, null);
	}

	public SectionDisplayAddress(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionDisplayAddress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
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
			if (mLocation.getStreetAddressString() != null) {
				mLineOne.setText(mLocation.getStreetAddressString());
			}
			if (mLocation.getCity() != null) {
				mCity.setText(mLocation.getCity());
			}
			if (mLocation.getStateCode() != null) {
				mState.setText(mLocation.getStateCode());
			}
			if (mLocation.getPostalCode() != null) {
				mZip.setText(mLocation.getPostalCode());
			}
		}
	}

}
