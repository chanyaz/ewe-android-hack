package com.expedia.bookings.section;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayContactInfo extends LinearLayout implements ISection<BillingInfo> {

	TextView mName;
	TextView mEmail;
	TextView mPhone;

	BillingInfo mBillingInfo;

	public SectionDisplayContactInfo(Context context) {
		this(context, null);
	}

	public SectionDisplayContactInfo(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SectionDisplayContactInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// real work here
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mName = Ui.findView(this, R.id.full_name);
		mEmail = Ui.findView(this, R.id.email_address);
		mPhone = Ui.findView(this, R.id.phone_number);

	}

	@Override
	public void bind(BillingInfo bi) {
		//Update fields
		mBillingInfo = bi;

		if (mBillingInfo != null) {
			if (mName != null && mBillingInfo.getFirstName() != null) {
				mName.setText(mBillingInfo.getFirstName() + " " + mBillingInfo.getLastName());
			}
			if (mEmail != null && mBillingInfo.getEmail() != null) {
				mEmail.setText(mBillingInfo.getEmail());
			}
			if (mPhone != null && mBillingInfo.getTelephone() != null) {
				mPhone.setText(mBillingInfo.getTelephone());
			}
		}
	}

}
