package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;
import com.squareup.phrase.Phrase;

public class LoginConfirmLogoutDialogFragment extends DialogFragment {

	public static final String TAG = "LoginConfirmLogoutDialogFragment";

	private static final String ARG_MESSAGE = "ARG_MESSAGE";

	public interface DoLogoutListener {
		void doLogout();
	}

	private DoLogoutListener mListener;

	public static LoginConfirmLogoutDialogFragment getInstance(String message) {
		LoginConfirmLogoutDialogFragment instance = new LoginConfirmLogoutDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_MESSAGE, message);
		instance.setArguments(args);
		instance.setCancelable(false);
		return instance;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, DoLogoutListener.class, false);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());

		// Defaults
		CharSequence messageText = Phrase.from(getActivity(), R.string.sign_out_confirmation_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.format();

		// Args
		Bundle args = getArguments();
		if (args != null) {
			if (args.containsKey(ARG_MESSAGE)) {
				messageText = args.getString(ARG_MESSAGE);
			}
		}

		builder.setMessage(messageText);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (mListener != null) {
					mListener.doLogout();
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
}
