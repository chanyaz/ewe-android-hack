package com.expedia.bookings.section;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayTravelerInfo extends LinearLayout implements ISection<FlightPassenger> {

	TextView mName;
	TextView mPhone;

	FlightPassenger mPassenger;

	public SectionDisplayTravelerInfo(Context context) {
		this(context, null);
	}

	public SectionDisplayTravelerInfo(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionDisplayTravelerInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mName = Ui.findView(this, R.id.full_name);
		mPhone = Ui.findView(this, R.id.phone_number);

	}

	@Override
	public void bind(FlightPassenger passenger) {
		//Update fields
		mPassenger = passenger;

		if (mPassenger != null) {
			if (mName != null && mPassenger.getFirstName() != null) {
				mName.setText(mPassenger.getFirstName() + " " + mPassenger.getLastName());
			}
			if (mPhone != null && mPassenger.getPhoneNumber() != null) {
				mPhone.setText(mPassenger.getPhoneNumber());
			}
		}
	}

}
