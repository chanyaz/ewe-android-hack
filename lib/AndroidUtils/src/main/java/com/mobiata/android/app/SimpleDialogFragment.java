package com.mobiata.android.app;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.mobiata.android.R;

/**
 * Used for creating simple message dialogs.
 * 
 * See DialogUtils for an equivalent version for non-Fragment-based UIs.
 */
@TargetApi(11)
public class SimpleDialogFragment extends DialogFragment {

	private static final String TITLE = "TITLE";

	private static final String MESSAGE = "MESSAGE";

	public static SimpleDialogFragment newInstance(String title, String message) {
		SimpleDialogFragment fragment = new SimpleDialogFragment();
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
		builder.setNeutralButton(R.string.ok, null);
		return builder.create();
	}
}
