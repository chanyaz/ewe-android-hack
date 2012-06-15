package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Location;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.EditText;

public class SectionCreditCard extends LinearLayout {
	public SectionCreditCard(Context context) {
		this(context, null);
	}

	public SectionCreditCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
	}

	EditText mCCNum;
	EditText mCSV;
	EditText mExp;

	BillingInfo mBi;

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mCCNum = Ui.findView(this, R.id.creditcard_number);
		mCSV = Ui.findView(this, R.id.creditcard_cv);
		mExp = Ui.findView(this, R.id.creditcard_expiration);
		
		mCCNum.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				
				if(mBi != null){
					mBi.setNumber(mCCNum.getText().toString());
				}
				
				if(mBi != null){
					//TODO: update the credit card brand photo if we have enough characters to make it work...
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
		});
		
		mCSV.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(mBi != null){
					mBi.setSecurityCode(mCSV.getText().toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});
		
		mExp.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable s) {
				if(mBi != null){
					Calendar cal = getExpirationCal(mExp.getText().toString());
					if(cal != null){
						mBi.setExpirationDate(cal);
					}
				}
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
	
	private Calendar getExpirationCal(String expr){
		//TODO: Impliment this stuff...
		return null;
	}

	public void bind(BillingInfo bi) {
		//Update fields
		mBi = bi;
		
		if(mBi.getNumber() != null)
			mCCNum.setText(mBi.getNumber());
		if(mBi.getSecurityCode() != null)
			mCSV.setText(mBi.getSecurityCode());
		if(mBi.getExpirationDate() != null)
			mExp.setText( mBi.getExpirationDate().get(Calendar.MONTH) + "/" + mBi.getExpirationDate().get(Calendar.YEAR));
	}

}
