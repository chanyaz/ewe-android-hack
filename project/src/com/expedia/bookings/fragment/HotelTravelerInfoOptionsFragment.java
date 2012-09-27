package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.widget.TelephoneSpinner;
import com.mobiata.android.util.Ui;

public class HotelTravelerInfoOptionsFragment extends Fragment {
	private TextView mFirstNameEditText;
	private TextView mMiddleNameEditText;
	private TextView mLastNameEditText;
	private TelephoneSpinner mPhoneCountryCodeTelephoneSpinner;
	private TextView mPhoneNumberEditText;
	private TextView mEmailAddressEditText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_traveler_info_options, container, false);

		mFirstNameEditText = Ui.findView(view, R.id.first_name_edit_text);
		mMiddleNameEditText = Ui.findView(view, R.id.middle_name_edit_text);
		mLastNameEditText = Ui.findView(view, R.id.last_name_edit_text);
		mPhoneCountryCodeTelephoneSpinner = Ui.findView(view, R.id.phone_number_country_code_spinner);
		mPhoneNumberEditText = Ui.findView(view, R.id.phone_number_edit_text);
		mEmailAddressEditText = Ui.findView(view, R.id.email_address_edit_text);

		updateViews();

		return view;
	}

	public void updateViews() {
		if (Db.getTravelers() != null && Db.getTravelers().size() > 0) {
			Traveler traveler = Db.getTravelers().get(0);

			mFirstNameEditText.setText(traveler.getFirstName());
			mMiddleNameEditText.setText(traveler.getMiddleName());
			mLastNameEditText.setText(traveler.getLastName());

			mPhoneNumberEditText.setText(traveler.getPhoneNumber());
			mEmailAddressEditText.setText(traveler.getEmail());
		}
	}

	public Traveler createTravler() {
		Traveler traveler = new Traveler();
		traveler.setFirstName(mFirstNameEditText.getText().toString());
		traveler.setMiddleName(mMiddleNameEditText.getText().toString());
		traveler.setLastName(mLastNameEditText.getText().toString());
		traveler.setPhoneCountryCode(mPhoneCountryCodeTelephoneSpinner.getSelectedTelephoneCountry());
		traveler.setPhoneNumber(mPhoneNumberEditText.getText().toString());
		traveler.setEmail(mEmailAddressEditText.getText().toString());

		return traveler;
	}
}