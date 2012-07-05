package com.expedia.bookings.section;

import java.util.ArrayList;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.mobiata.android.util.Ui;
import com.mobiata.android.validation.PatternValidator.EmailValidator;
import com.mobiata.android.validation.PatternValidator.TelephoneValidator;
import com.mobiata.android.validation.RequiredValidator;
import com.mobiata.android.validation.ValidationError;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.EditText;

public class SectionEditContactInfo extends LinearLayout implements ISection<BillingInfo>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();

	EditText mFirstName;
	EditText mLastName;
	EditText mEmail;
	EditText mPhone;

	BillingInfo mBi;

	public SectionEditContactInfo(Context context) {
		super(context);
		init(context);
	}

	public SectionEditContactInfo(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionEditContactInfo(Context context, AttributeSet attrs, int defStyle) {
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
		mEmail = Ui.findView(this, R.id.email_address);
		mPhone = Ui.findView(this, R.id.phone_number);

		mFirstName.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				if (mBi != null) {
					mBi.setFirstName(mFirstName.getText().toString());
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

		mLastName.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				if (mBi != null) {
					mBi.setLastName(mLastName.getText().toString());
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

		mPhone.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				if (mBi != null) {
					mBi.setTelephone(mPhone.getText().toString());
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

		mEmail.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				if (mBi != null) {
					mBi.setEmail(mEmail.getText().toString());
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

	@Override
	public void bind(BillingInfo bi) {
		//Update fields
		mBi = bi;

		if (mBi != null) {
			if (mBi.getFirstName() != null) {
				mFirstName.setText(mBi.getFirstName());
			}
			if (mBi.getLastName() != null) {
				mLastName.setText(mBi.getLastName());
			}
			if (mBi.getEmail() != null) {
				mEmail.setText(mBi.getEmail());
			}
			if (mBi.getTelephone() != null) {
				mPhone.setText(mBi.getTelephone());
			}
		}
	}

	public boolean hasValidInput() {
		if (mFirstName == null || mLastName == null || mEmail == null || mPhone == null) {
			return false;
		}
		else {
			RequiredValidator valEmpty = RequiredValidator.getInstance();
			EmailValidator valEmail = new EmailValidator();
			TelephoneValidator valTel = new TelephoneValidator();

			if (valEmpty.validate(mEmail.getText()) == ValidationError.ERROR_DATA_MISSING
					|| valEmpty.validate(mPhone.getText()) == ValidationError.ERROR_DATA_MISSING
					|| valEmpty.validate(mFirstName.getText()) == ValidationError.ERROR_DATA_MISSING
					|| valEmpty.validate(mLastName.getText()) == ValidationError.ERROR_DATA_MISSING) {
				return false;
			}
			else {
				//We have values for everything, check if they are valid
				if (valEmail.validate(mEmail.getText()) == ValidationError.ERROR_DATA_INVALID) {
					return false;
				}
				else if (valTel.validate(mPhone.getText()) == ValidationError.ERROR_DATA_INVALID) {
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
