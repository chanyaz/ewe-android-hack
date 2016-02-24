package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Spinner;

import com.expedia.bookings.R;

import butterknife.InjectView;

public class PackagePaymentWidget extends PaymentWidget {

	@InjectView(R.id.edit_creditcard_cvv)
	EditText creditCardCvv;

	@InjectView(R.id.edit_address_line_one)
	EditText addressLineOne;

	@InjectView(R.id.edit_address_line_two)
	EditText addressLineTwo;

	@InjectView(R.id.edit_address_city)
	EditText addressCity;

	@InjectView(R.id.edit_address_state)
	EditText addressState;

	@InjectView(R.id.edit_country_spinner)
	Spinner countrySpinner;


	public PackagePaymentWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		creditCardCvv.setOnFocusChangeListener(this);
		addressLineOne.setOnFocusChangeListener(this);
		addressLineTwo.setOnFocusChangeListener(this);
		addressCity.setOnFocusChangeListener(this);
		addressState.setOnFocusChangeListener(this);
	}

	@Override
	public boolean getMenuDoneButtonFocus() {
		if (creditCardPostalCode != null) {
			return creditCardPostalCode.hasFocus();
		}
		return false;
	}

	@Override
	public String getActionBarTitle() {
		if (paymentOptionsContainer.getVisibility() == VISIBLE) {
			return super.getActionBarTitle();
		}
		else {
			return getResources().getString(R.string.new_credit_debit_card);
		}
	}

	@Override
	public boolean isFilled() {
		return super.isFilled()
			|| !creditCardCvv.getText().toString().isEmpty()
			|| !addressLineOne.getText().toString().isEmpty()
			|| !addressCity.getText().toString().isEmpty()
			|| !addressState.getText().toString().isEmpty();
	}
}
