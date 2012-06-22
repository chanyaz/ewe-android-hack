package com.expedia.bookings.section;

import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayCreditCard extends LinearLayout implements ISection<BillingInfo> {

	TextView mCCNum;
	TextView mCSV;
	TextView mExp;

	BillingInfo mBi;

	public SectionDisplayCreditCard(Context context) {
		this(context, null);
	}

	public SectionDisplayCreditCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionDisplayCreditCard(Context context, AttributeSet attrs, int defStyle) {
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

	}

	@Override
	public void bind(BillingInfo data) {
		mBi = (BillingInfo) data;

		if (mBi != null) {
			if (mBi.getNumber() != null) {
				String ccNum = mBi.getNumber();
				String displayNums = ccNum.substring(ccNum.length() - 4);//last 4
				String blanked = "************";//TODO:This should maybe be the real length...

				mCCNum.setText(blanked + displayNums);
			}
			if (mBi.getSecurityCode() != null) {
				mCSV.setText(mBi.getSecurityCode());
			}
			if (mBi.getExpirationDate() != null) {
				mExp.setText(String.format("%02d/%02d", mBi.getExpirationDate().get(Calendar.MONTH), mBi
						.getExpirationDate().get(Calendar.YEAR)));
			}
		}
	}

}
