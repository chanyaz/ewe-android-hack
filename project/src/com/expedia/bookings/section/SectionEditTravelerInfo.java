package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.RequiredValidator;
import com.mobiata.android.validation.ValidationError;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.EditText;

public class SectionEditTravelerInfo extends LinearLayout implements ISection<FlightPassenger>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();

	EditText mFirstName;
	EditText mLastName;
	EditText mPhone;
	EditText mRedressNumber;
	DatePicker mBirthDate;

	FlightPassenger mPassenger;

	public SectionEditTravelerInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionEditTravelerInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionEditTravelerInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mFirstName = Ui.findView(this, R.id.first_name);
		mLastName = Ui.findView(this, R.id.last_name);
		mPhone = Ui.findView(this, R.id.phone_number);
		mBirthDate = Ui.findView(this, R.id.date_of_birth);
		mRedressNumber = Ui.findView(this, R.id.redress_number);

		if (mFirstName != null) {
			mFirstName.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					if (mPassenger != null) {
						mPassenger.setFirstName(mFirstName.getText().toString());
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
		if (mLastName != null) {
			mLastName.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					if (mPassenger != null) {
						mPassenger.setLastName(mLastName.getText().toString());
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
		if (mPhone != null) {
			mPhone.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					if (mPassenger != null) {
						mPassenger.setPhoneNumber(mPhone.getText().toString());
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

		if (mBirthDate != null) {
			Calendar now = Calendar.getInstance();
			mBirthDate.init(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH),
					new OnDateChangedListener() {
						@Override
						public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
							Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
							if (mPassenger != null) {
								mPassenger.setBirthDate(cal);
							}
							onChange();
						}
					});
		}

		if (mRedressNumber != null) {
			mRedressNumber.addTextChangedListener(new TextWatcher() {

				@Override
				public void afterTextChanged(Editable s) {

					if (mPassenger != null) {
						mPassenger.setRedressNumber(mRedressNumber.getText().toString());
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
	}

	@Override
	public void bind(FlightPassenger passenger) {
		//Update fields
		mPassenger = passenger;

		if (mPassenger != null) {
			if (mFirstName != null && mPassenger.getFirstName() != null) {
				mFirstName.setText(mPassenger.getFirstName());
			}
			if (mLastName != null && mPassenger.getLastName() != null) {
				mLastName.setText(mPassenger.getLastName());
			}
			if (mPhone != null && mPassenger.getPhoneNumber() != null) {
				mPhone.setText(mPassenger.getPhoneNumber());
			}
			if (mBirthDate != null && mPassenger.getBirthDate() != null) {
				mBirthDate.updateDate(mPassenger.getBirthDate().get(Calendar.YEAR),
						mPassenger.getBirthDate().get(Calendar.MONTH),
						mPassenger.getBirthDate().get(Calendar.DAY_OF_MONTH));
			}
			if (mRedressNumber != null && mPassenger.getRedressNumber() != null) {
				mRedressNumber.setText(mPassenger.getRedressNumber());
			}
		}
	}

	public boolean hasValidInput() {
		if (mFirstName == null || mLastName == null || mPhone == null) {
			return false;
		}
		else {
			RequiredValidator valEmpty = RequiredValidator.getInstance();
			TelephoneValidator valTel = new TelephoneValidator();

			if (valEmpty.validate(mPhone.getText()) == ValidationError.ERROR_DATA_MISSING
					|| valEmpty.validate(mFirstName.getText()) == ValidationError.ERROR_DATA_MISSING
					|| valEmpty.validate(mLastName.getText()) == ValidationError.ERROR_DATA_MISSING) {
				return false;
			}
			else {
				//We have values for everything, check if they are valid
				if (valTel.validate(mPhone.getText()) == ValidationError.ERROR_DATA_INVALID) {
					return false;
				}
				else {
					return true;
				}
			}
		}
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
