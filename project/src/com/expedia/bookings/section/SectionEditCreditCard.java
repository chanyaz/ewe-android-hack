package com.expedia.bookings.section;

import java.util.ArrayList;
import java.util.Calendar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.mobiata.android.util.Ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class SectionEditCreditCard extends LinearLayout implements ISection<BillingInfo>, ISectionEditable {

	ArrayList<SectionChangeListener> mChangeListeners = new ArrayList<SectionChangeListener>();

	Context mContext;

	EditText mCCNum;
	EditText mCSV;
	EditText mExp;

	TextView mSecurityCodeInfo;

	Spinner mCreditCardBrandSpinner;
	ImageView mCreditCardBrandIcon;

	BillingInfo mBi;

	public SectionEditCreditCard(Context context) {
		super(context);
		init(context);
	}

	public SectionEditCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SectionEditCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		mContext = context;
	}

	@Override
	public void onFinishInflate() {
		super.onFinishInflate();

		//Find fields
		mCCNum = Ui.findView(this, R.id.creditcard_number);
		mCSV = Ui.findView(this, R.id.creditcard_security_code);
		mExp = Ui.findView(this, R.id.creditcard_expiration);
		mCreditCardBrandSpinner = Ui.findView(this, R.id.credit_card_type_spinner);
		mCreditCardBrandIcon = Ui.findView(this, R.id.credit_card_brand_icon);
		mSecurityCodeInfo = Ui.findView(this, R.id.security_code_info);

		if (mCreditCardBrandSpinner != null) {
			mCreditCardBrandSpinner.setAdapter(new ArrayAdapter<CreditCardType>(mContext,
					android.R.layout.simple_list_item_1, CreditCardType.values()));

			//if(mCreditCardBrandSpinner.getAdapter().)

			mCreditCardBrandSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
					// TODO Auto-generated method stub
					if (mBi != null) {
						CreditCardType type = (CreditCardType) parent.getItemAtPosition(pos);
						mBi.setBrandCode(type.getCode());
						mBi.setBrandName(type.name());

						//Updates the card icon
						bind(mBi);

						onChange();
					}
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) {
					// TODO Auto-generated method stub

				}
			});
		}

		if (mCCNum != null) {
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
		}

		if (mCSV != null) {
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
		}

		if (mExp != null) {
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
	}

	private Calendar getExpirationCal(String expr) {
		//TODO: Impliment this stuff...
		return null;
	}

	@Override
	public void bind(BillingInfo data) {
		mBi = data;

		if (mBi != null) {
			if (mCCNum != null && mBi.getNumber() != null) {
				mCCNum.setText(mBi.getNumber());
			}
			if (mCSV != null && mBi.getSecurityCode() != null) {
				mCSV.setText(mBi.getSecurityCode());
			}
			if (mExp != null && mBi.getExpirationDate() != null) {
				mExp.setText(mBi.getExpirationDate().get(Calendar.MONTH) + "/"
						+ mBi.getExpirationDate().get(Calendar.YEAR));
			}

			//Card brand specific
			if (!TextUtils.isEmpty(mBi.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(mBi.getBrandName());
				if (mCreditCardBrandSpinner != null) {
					@SuppressWarnings("unchecked")
					ArrayAdapter<CreditCardType> cardBrandAdapter = (ArrayAdapter<CreditCardType>) mCreditCardBrandSpinner
							.getAdapter();
					if (cardBrandAdapter != null) {
						int pos = cardBrandAdapter.getPosition(cardType);
						mCreditCardBrandSpinner.setSelection(pos);
					}

				}
				if (mCreditCardBrandIcon != null) {
					mCreditCardBrandIcon.setImageResource(BookingInfoUtils.CREDIT_CARD_ICONS.get(cardType));
				}
				if (mSecurityCodeInfo != null) {
					mSecurityCodeInfo.setText(BookingInfoUtils.CREDIT_CARD_SECURITY_LOCATION.get(cardType));
				}
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
