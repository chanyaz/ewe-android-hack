package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class ConfirmLogoutDialogFragment extends DialogFragment {

	public static final String TAG = "ConfirmLogoutDialogFragment";

	private static final String ARG_MESSAGE = "ARG_MESSAGE";
	private static final String ARG_NEGATIVE_BTN_TEXT = "ARG_NEGATIVE_BTN_TEXT";
	private static final String ARG_POSITIVE_BTN_TEXT = "ARG_POSITIVE_BTN_TEXT";

	public static interface DoLogoutListener {
		public void doLogout();
	}

	private DoLogoutListener mListener;

	public static ConfirmLogoutDialogFragment getInstance(DoLogoutListener listener) {
		ConfirmLogoutDialogFragment instance = new ConfirmLogoutDialogFragment();
		instance.setDoLogoutListener(listener);
		Bundle args = new Bundle();
		instance.setArguments(args);
		return instance;
	}

	public static ConfirmLogoutDialogFragment getInstance(DoLogoutListener listener, String message) {
		ConfirmLogoutDialogFragment instance = new ConfirmLogoutDialogFragment();
		instance.setDoLogoutListener(listener);
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, message);
		instance.setArguments(args);
		return instance;
	}

	public static ConfirmLogoutDialogFragment getInstance(DoLogoutListener listener, String message,
			String negativeBtnText, String positiveBtnText) {
		ConfirmLogoutDialogFragment instance = new ConfirmLogoutDialogFragment();
		instance.setDoLogoutListener(listener);
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, message);
		args.putString(ARG_NEGATIVE_BTN_TEXT, negativeBtnText);
		args.putString(ARG_POSITIVE_BTN_TEXT, positiveBtnText);
		instance.setArguments(args);
		return instance;
	}

	public void setDoLogoutListener(DoLogoutListener listener) {
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());

		//Defaults
		String messageText = getResources().getString(R.string.logout_confirmation);
		String postiveBtnText = getResources().getString(R.string.log_out);
		String negativeBtnText = getResources().getString(R.string.cancel);

		//Args
		Bundle args = getArguments();
		if (args != null) {
			if (args.containsKey(ARG_MESSAGE)) {
				messageText = args.getString(ARG_MESSAGE);
			}
			if (args.containsKey(ARG_POSITIVE_BTN_TEXT)) {
				postiveBtnText = args.getString(ARG_POSITIVE_BTN_TEXT);
			}
			if (args.containsKey(ARG_NEGATIVE_BTN_TEXT)) {
				negativeBtnText = args.getString(ARG_NEGATIVE_BTN_TEXT);
			}
		}

		builder.setMessage(messageText);
		builder.setPositiveButton(postiveBtnText, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mListener != null) {
					mListener.doLogout();
				}
				dismiss();
			}
		});
		builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		return builder.create();
	}
}
