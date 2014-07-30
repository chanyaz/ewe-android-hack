package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;

/**
 * Simple fragment that allows callbacks when the button is pressed.
 */
public class SimpleCallbackDialogFragment extends DialogFragment {

	public static final String TAG = SimpleCallbackDialogFragment.class.getName();

	public static final int CODE_INVALID_CC = 100;
	public static final int CODE_INVALID_PAYMENT = 102;
	public static final int CODE_INVALID_PHONENUMBER = 103;
	public static final int CODE_INVALID_POSTALCODE = 104;
	public static final int CODE_INVALID_MINOR = 105;
	public static final int CODE_EXPIRED_CC = 106;
	public static final int CODE_MINOR = 107;
	public static final int CODE_NAME_ONCARD_MISMATCH = 108;

	public static final int CODE_FLIGHT_ROUTES_FAILURE = 200;

	public static final int CODE_WALLET_PROMO_APPLY_ERROR = 300;

	public static final int CODE_TABLET_FLIGHTS_INFANT_CHOOSER = 400;


	private static final String ARG_TITLE = "ARG_TITLE";
	private static final String ARG_MESSAGE = "ARG_MESSAGE";
	private static final String ARG_BUTTON = "ARG_BUTTON";
	private static final String ARG_CALLBACK = "ARG_CALLBACK";

	public static SimpleCallbackDialogFragment newInstance(CharSequence title, CharSequence message,
			CharSequence button, int callbackId) {
		SimpleCallbackDialogFragment fragment = new SimpleCallbackDialogFragment();
		Bundle args = new Bundle();
		args.putCharSequence(ARG_TITLE, title);
		args.putCharSequence(ARG_MESSAGE, message);
		args.putCharSequence(ARG_BUTTON, button);
		args.putInt(ARG_CALLBACK, callbackId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Collect arguments
		Bundle args = getArguments();
		CharSequence title = args.getCharSequence(ARG_TITLE);
		CharSequence message = args.getCharSequence(ARG_MESSAGE);
		CharSequence button = args.getCharSequence(ARG_BUTTON);

		Builder builder = new Builder(getActivity());
		if (!TextUtils.isEmpty(title)) {
			builder.setTitle(title);
		}
		if (!TextUtils.isEmpty(message)) {
			builder.setMessage(message);
		}

		// Default to "ok" for button name
		if (TextUtils.isEmpty(button)) {
			button = getString(R.string.ok);
		}

		builder.setNeutralButton(button, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Events.post(new Events.SimpleCallBackDialogOnClick(getArguments().getInt(ARG_CALLBACK)));
			}
		});

		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		Events.post(new Events.SimpleCallBackDialogOnCancel(getArguments().getInt(ARG_CALLBACK)));
	}
}
