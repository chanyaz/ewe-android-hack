package com.expedia.bookings.widget;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class CarDriverWidget extends CardView {

	public CarDriverWidget(Context context, AttributeSet attr) {
		super(context, attr);
	}

	@InjectView(R.id.driver_info_container)
	android.widget.LinearLayout driverInfoContainer;

	@InjectView(R.id.driver_info_text)
	TextView driverInfoText;

	@InjectView(R.id.edit_first_name)
	EditText firstName;

	@InjectView(R.id.edit_last_name)
	EditText lastName;

	@InjectView(R.id.edit_email_address)
	EditText emailAddress;

	@InjectView(R.id.phone_country_code_spinner)
	TelephoneSpinner phoneSpinner;

	@InjectView(R.id.edit_phone_number)
	EditText phoneNumber;


	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		ButterKnife.inject(this);

		// TODO - encapsulate data fields better, so that this isn't here.
		TelephoneSpinnerAdapter adapter = (TelephoneSpinnerAdapter) phoneSpinner.getAdapter();
		String targetCountry = getContext().getString(PointOfSale.getPointOfSale()
			.getCountryNameResId());
		for (int i = 0; i < adapter.getCount(); i++) {
			if (targetCountry.equalsIgnoreCase(adapter.getCountryName(i))) {
				phoneSpinner.setSelection(i);
				break;
			}
		}
	}

	public void setExpanded(boolean isExpanded) {
		driverInfoText.setVisibility(isExpanded ? GONE : VISIBLE);
		driverInfoContainer.setVisibility(isExpanded ? VISIBLE : GONE);
	}

}
