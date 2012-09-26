package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.mobiata.android.util.Ui;

public class HotelTravelerInfoOptionsFragment extends Fragment {
	private TextView mNameEditText;
	private TextView mPhoneNumberEditText;
	private TextView mEmailAddressEditText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_traveler_info_options, container, false);

		mNameEditText = Ui.findView(view, R.id.name_edit_text);
		mPhoneNumberEditText = Ui.findView(view, R.id.name_edit_text);
		mEmailAddressEditText = Ui.findView(view, R.id.email_address_edit_text);

		return view;
	}

	public Traveler buildTravler() {
		Traveler traveler = new Traveler();
		
		return null;
	}
}