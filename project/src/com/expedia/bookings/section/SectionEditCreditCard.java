package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.EditText;

public class SectionEditCreditCard extends LinearLayout implements ISection<BillingInfo>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();

	EditText mCCNum;
	EditText mCSV;
	EditText mExp;

	BillingInfo mBi;

	public SectionEditCreditCard(Context context) {
		this(context, null);
	}

	public SectionEditCreditCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionEditCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mCCNum = Ui.findView(this, R.id.creditcard_number);
		mCSV = Ui.findView(this, R.id.creditcard_cv);
		mExp = Ui.findView(this, R.id.creditcard_expiration);

		mCCNum.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {

				if (mBi != null) {
					mBi.setNumber(mCCNum.getText().toString());
				}

				if (mBi != null) {
					//TODO: update the credit card brand photo if we have enough characters to make it work...
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

		mCSV.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (mBi != null) {
					mBi.setSecurityCode(mCSV.getText().toString());
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

		mExp.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (mBi != null) {
					Calendar cal = getExpirationCal(mExp.getText().toString());
					if (cal != null) {
						mBi.setExpirationDate(cal);
					}
				}
				onChange();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				//TODO:Date formatting...
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
	}

	private Calendar getExpirationCal(String expr) {
		//TODO: Impliment this stuff...
		return null;
	}

	@Override
	public void bind(BillingInfo data) {
		mBi = data;

		if (mBi != null) {
			if (mBi.getNumber() != null) {
				mCCNum.setText(mBi.getNumber());
			}
			if (mBi.getSecurityCode() != null) {
				mCSV.setText(mBi.getSecurityCode());
			}
			if (mBi.getExpirationDate() != null) {
				mExp.setText(mBi.getExpirationDate().get(Calendar.MONTH) + "/"
						+ mBi.getExpirationDate().get(Calendar.YEAR));
			}
		}

	}

	@Override
	public boolean hasValidInput() {
		// TODO perform validation
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
