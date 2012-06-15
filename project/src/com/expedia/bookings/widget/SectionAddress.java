package com.expedia.bookings.widget;

import java.util.ArrayList;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.EditText;

public class SectionAddress extends LinearLayout {
	public SectionAddress(Context context) {
		this(context, null);
	}

	public SectionAddress(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionAddress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
	}

	EditText mLineOne;
	EditText mLineTwo;
	EditText mCity;
	EditText mState;
	EditText mZip;

	Location mLocation;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mLineOne = Ui.findView(this, R.id.address_line_one);
		mLineTwo = Ui.findView(this, R.id.address_line_two);
		mCity = Ui.findView(this, R.id.address_city);
		mState = Ui.findView(this, R.id.address_state);
		mZip = Ui.findView(this, R.id.address_zip);

		TextWatcher addrWatcher = new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mLocation != null) {
					ArrayList<String> addr = new ArrayList<String>();
					addr.add(mLineOne.getText().toString());
					addr.add(mLineTwo.getText().toString());
					mLocation.setStreetAddress(addr);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		};

		mLineOne.addTextChangedListener(addrWatcher);
		mLineTwo.addTextChangedListener(addrWatcher);

		mCity.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mLocation != null) {
					mLocation.setCity(s.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		mState.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mLocation != null) {
					mLocation.setStateCode(s.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//TODO:State code validation...
			}
		});

		mZip.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mLocation != null) {
					mLocation.setPostalCode(s.toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//TODO:State/zip validation?
			}
		});

	}

	public void bind(Location loc) {
		//Update fields
		mLocation = loc;

		mLineOne.setText(mLocation.getStreetAddressString());
		mCity.setText(mLocation.getCity());
		mState.setText(mLocation.getStateCode());
		mZip.setText(mLocation.getPostalCode());
	}

}
