package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;

public class ConfirmLogoutDialogFragment extends DialogFragment {

	public static final String TAG = "ConfirmLogoutDialogFragment";

	public static interface DoLogoutListener {
		public void doLogout();
	}

	private DoLogoutListener mListener;

	public static ConfirmLogoutDialogFragment getInstance(DoLogoutListener listener) {
		ConfirmLogoutDialogFragment instance = new ConfirmLogoutDialogFragment();
		Bundle args = new Bundle();
		instance.setArguments(args);
		return instance;
	}

	public void setDoLogoutListener(DoLogoutListener listener) {
		mListener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setMessage(R.string.logout_confirmation);
		builder.setPositiveButton(R.string.log_out, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mListener != null) {
					mListener.doLogout();
				}
				dismiss();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		return builder.create();
	}
}
