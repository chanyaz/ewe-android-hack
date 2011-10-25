package com.expedia.bookings.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.ConfirmationUtils;

public class BookingCancellationPolicyFragment extends Fragment {
	public static BookingCancellationPolicyFragment newInstance() {
		BookingCancellationPolicyFragment fragment = new BookingCancellationPolicyFragment();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_confirmation_cancellation_policy, container, false);

		Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();
		ConfirmationUtils.determineCancellationPolicy(rate, view);
		ConfirmationUtils.determineContactText(getActivity(), view);
		return view;
	}
}
