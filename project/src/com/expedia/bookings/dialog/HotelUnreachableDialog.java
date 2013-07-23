package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class HotelUnreachableDialog extends DialogFragment {
	private static String ARG_MESSAGE = "ARG_MESSAGE";

	public static HotelUnreachableDialog newInstance() {
		HotelUnreachableDialog frag = new HotelUnreachableDialog();
		Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, R.string.e3_error_hotel_offers_hotel_service_failure);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int messageId = args.getInt(ARG_MESSAGE, R.string.e3_error_hotel_offers_hotel_service_failure);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(getString(messageId));

		builder.setNeutralButton(com.mobiata.android.R.string.ok, null);

		return builder.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (getActivity() != null) {
			getActivity().finish();
		}
	}
}
