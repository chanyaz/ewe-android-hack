package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.expedia.bookings.R;

public class BookingErrorDialogFragment extends DialogFragment {
	private static final String ERROR_MESSAGE = "ERROR_MESSAGE";

	public static BookingErrorDialogFragment newInstance(String message) {
		BookingErrorDialogFragment fragment = new BookingErrorDialogFragment();
		Bundle args = new Bundle();
		args.putString(ERROR_MESSAGE, message);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.error_booking_title);
		builder.setMessage(getArguments().getString(ERROR_MESSAGE));
		builder.setNeutralButton(android.R.string.ok, null);

		return builder.create();
	}
}
