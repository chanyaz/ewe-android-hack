package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class HotelSoldOutDialog extends DialogFragment {
	private static String ARG_MESSAGE = "ARG_MESSAGE";

	public static HotelSoldOutDialog newInstance() {
		HotelSoldOutDialog frag = new HotelSoldOutDialog();
		Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, R.string.error_hotel_is_now_sold_out);
		frag.setArguments(args);
		return frag;
	}

	public void setMessage(int stringId) {
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}

		args.putInt(ARG_MESSAGE, stringId);
		setArguments(args);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int messageId = args.getInt(ARG_MESSAGE, R.string.error_hotel_is_now_sold_out);

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
