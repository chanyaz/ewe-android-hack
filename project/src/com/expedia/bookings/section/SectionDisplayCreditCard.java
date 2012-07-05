package com.expedia.bookings.section;

import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionDisplayCreditCard extends LinearLayout implements ISection<BillingInfo> {

	TextView mCCNum;
	TextView mCSV;
	TextView mExp;

	ImageView mCreditCardBrandIcon;

	BillingInfo mBillingInfo;

	public SectionDisplayCreditCard(Context context) {
		super(context);
		init(context);
	}

	public SectionDisplayCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionDisplayCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {

	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mCCNum = Ui.findView(this, R.id.creditcard_number);
		mCSV = Ui.findView(this, R.id.creditcard_cv);
		mExp = Ui.findView(this, R.id.creditcard_expiration);
		mCreditCardBrandIcon = Ui.findView(this, R.id.credit_card_brand_icon);

	}

	@Override
	public void bind(BillingInfo data) {
		mBillingInfo = (BillingInfo) data;

		if (mBillingInfo != null) {
			if (mCCNum != null && !TextUtils.isEmpty(mBillingInfo.getNumber())) {
				String ccNum = mBillingInfo.getNumber();
				String displayNums = ccNum.substring(ccNum.length() - 4);//last 4
				String ccBrand = mBillingInfo.getBrandName();
				String ccStr = String.format(getResources().getString(R.string.blanked_out_credit_card_TEMPLATE),
						ccBrand, displayNums);
				mCCNum.setText(ccStr);
			}
			if (mCSV != null && mBillingInfo.getSecurityCode() != null) {
				mCSV.setText(mBillingInfo.getSecurityCode());
			}
			if (mExp != null && mBillingInfo.getExpirationDate() != null) {
				mExp.setText(String.format("%02d/%02d", mBillingInfo.getExpirationDate().get(Calendar.MONTH),
						mBillingInfo
								.getExpirationDate().get(Calendar.YEAR)));
			}
			if (mCreditCardBrandIcon != null && !TextUtils.isEmpty(mBillingInfo.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(mBillingInfo.getBrandName());
				mCreditCardBrandIcon.setImageResource(BookingInfoUtils.CREDIT_CARD_ICONS.get(cardType));
			}
		}
	}

}
