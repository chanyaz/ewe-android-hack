package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.expedia.bookings.R;

// For now spits out a single message, but could be modified to spit out
// a given message.
public class RetryErrorDialogFragment extends DialogFragment implements OnClickListener {

	private RetryErrorDialogFragmentListener mListener;

	private static final String ARG_MESSAGE = "ARG_MESSAGE";

	public static RetryErrorDialogFragment newInstance(String errorMessage) {
		RetryErrorDialogFragment fragment = new RetryErrorDialogFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ARG_MESSAGE, errorMessage);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof RetryErrorDialogFragmentListener) {
			mListener = (RetryErrorDialogFragmentListener) activity;
		}
		else {
			throw new RuntimeException("RetryErrorDialogFragment Activity must implement listener!");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		builder.setMessage(getErrorMessage());
		builder.setPositiveButton(R.string.retry, this);
		builder.setNegativeButton(R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		mListener.onCancelError();
	}

	private String getErrorMessage() {
		Bundle args = getArguments();
		if (args != null) {
			String msg = args.getString(ARG_MESSAGE);
			if (!TextUtils.isEmpty(msg)) {
				return msg;
			}
		}

		return getString(R.string.error_server);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			mListener.onRetryError();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			mListener.onCancelError();
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface RetryErrorDialogFragmentListener {
		public void onRetryError();

		public void onCancelError();
	}
}
