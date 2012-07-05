package com.expedia.bookings.section;

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

public class SectionEditAddress extends LinearLayout implements ISection<Location>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();

	EditText mLineOne;
	EditText mLineTwo;
	EditText mCity;
	EditText mState;
	EditText mZip;

	Location mLocation;

	public SectionEditAddress(Context context) {
		super(context);
		init(context);
	}

	public SectionEditAddress(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionEditAddress(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

	}

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
				onChange();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		};

		if (mLineOne != null) {
			mLineOne.addTextChangedListener(addrWatcher);
		}
		if (mLineTwo != null) {
			mLineTwo.addTextChangedListener(addrWatcher);
		}

		if (mCity != null) {
			mCity.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (mLocation != null) {
						mLocation.setCity(s.toString());
					}
					onChange();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
				}
			});

		}

		if (mState != null) {
			mState.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (mLocation != null) {
						mLocation.setStateCode(s.toString());
					}
					onChange();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					//TODO:State code validation...
				}
			});
		}

		if (mZip != null) {
			mZip.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					if (mLocation != null) {
						mLocation.setPostalCode(s.toString());
					}
					onChange();
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

	}

	public void bind(Location loc) {
		//Update fields
		mLocation = loc;

		if (mLocation != null && mLocation.getStreetAddress() != null) {
			if (mLocation.getStreetAddress().size() == 2) {
				//Wierdness happens if i put the .get(0) directly into setText...
				String l1 = mLocation.getStreetAddress().get(0);
				String l2 = mLocation.getStreetAddress().get(1);
				if (mLineOne != null) {
					mLineOne.setText(l1);
				}
				if (mLineTwo != null) {
					mLineTwo.setText(l2);
				}
			}
			else if (mLocation.getStreetAddress().size() == 1) {
				if (mLineOne != null) {
					mLineOne.setText(mLocation.getStreetAddress().get(0));
				}
				if (mLineTwo != null) {
					mLineTwo.setText("");
				}
			}
			else {
				if (mLineOne != null) {
					mLineOne.setText(mLocation.getStreetAddressString());
				}

				if (mLineTwo != null) {
					mLineTwo.setText("");
				}
			}

			if (mCity != null) {
				mCity.setText(mLocation.getCity());
			}
			if (mState != null) {
				mState.setText(mLocation.getStateCode());
			}
			if (mZip != null) {
				mZip.setText(mLocation.getPostalCode());
			}
		}
	}

	@Override
	public boolean hasValidInput() {
		// TODO Auto-generated method stub
		return true;
	}

	public void onChange() {
		for (SectionChangeListener listener : mChangeListeners) {
			listener.onChange();
		}
	}

	@Override
	public void addChangeListener(SectionChangeListener listener) {
		mChangeListeners.add(listener);
	}

	@Override
	public void removeChangeListener(SectionChangeListener listener) {
		mChangeListeners.remove(listener);
	}

	@Override
	public void clearChangeListeners() {
		mChangeListeners.clear();

	}

}
