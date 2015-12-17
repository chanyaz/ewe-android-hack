package com.expedia.bookings.fragment;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.widget.GuestPicker;
import com.mobiata.android.util.Ui;

/**
 * Results loading fragment for Tablet
 */
public class ResultsGuestPickerFragment extends Fragment implements GuestPicker.GuestPickerListener {

	private GuestPicker mGuestPicker;

	private TextView mInfantAlertTextView;

	private GuestPickerFragmentListener mListener;

	public interface GuestPickerFragmentListener {
		void onGuestsChanged(int numAdults, List<ChildTraveler> children);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, GuestPickerFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = Ui.inflate(inflater, R.layout.fragment_results_guests, null);
		mGuestPicker = Ui.findView(rootView, R.id.guest_picker);
		mGuestPicker.setListener(this);

		mInfantAlertTextView = Ui.findView(rootView, R.id.tablet_lap_infant_alert);

		return rootView;
	}

	@Override
	public void onGuestsChanged(int numAdults, List<ChildTraveler> children) {
		toggleInfantSeatingStates();
		mListener.onGuestsChanged(numAdults, children);
	}

	public String getHeaderString() {
		if (mGuestPicker == null) {
			return null;
		}
		return mGuestPicker.getHeaderString();
	}

	public void bind(int numAdults, List<ChildTraveler> children) {
		if (mGuestPicker != null) {
			mGuestPicker.bind(numAdults, children);
			toggleInfantSeatingStates();
		}
	}

	private void toggleInfantSeatingStates() {
		// Let's not show the alert if flights is not supported for the current POS
		if (mGuestPicker.moreInfantsThanAvailableLaps() && PointOfSale.getPointOfSale().isFlightSearchEnabledTablet()) {
			mInfantAlertTextView.setVisibility(View.VISIBLE);
		}
		else {
			mInfantAlertTextView.setVisibility(View.GONE);
		}
	}
}
