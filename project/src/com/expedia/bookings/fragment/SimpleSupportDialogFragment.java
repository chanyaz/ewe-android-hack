package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * A copy of com.mobiata.android.app.SimpleDialogFragment
 * that uses the android.support.v4 libraries.
 */
public class SimpleSupportDialogFragment extends DialogFragment {

	private static final String TITLE = "TITLE";

	private static final String MESSAGE = "MESSAGE";

	public static SimpleSupportDialogFragment newInstance(String title, String message) {
		SimpleSupportDialogFragment fragment = new SimpleSupportDialogFragment();
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		args.putString(MESSAGE, message);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setTitle(getArguments().getString(TITLE));
		builder.setMessage(getArguments().getString(MESSAGE));
		builder.setNeutralButton(android.R.string.ok, null);
		return builder.create();
	}
}
