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

public class UnhandledErrorDialogFragment extends DialogFragment implements OnClickListener {

	private static final String ARG_CASE_NUMBER = "ARG_CASE_NUMBER";

	private UnhandledErrorDialogFragmentListener mListener;

	public static UnhandledErrorDialogFragment newInstance(String caseNumber) {
		UnhandledErrorDialogFragment fragment = new UnhandledErrorDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_CASE_NUMBER, caseNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof UnhandledErrorDialogFragmentListener) {
			mListener = (UnhandledErrorDialogFragmentListener) activity;
		}
		else {
			throw new RuntimeException("UnhandledErrorDialogFragment Activity must implement listener!");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());

		// If case number was not supplied, don't give user option to call support about it
		// This can happen when we get a null response from the server.
		String caseNumber = getArguments().getString(ARG_CASE_NUMBER);
		if (!TextUtils.isEmpty(caseNumber)) {
			builder.setMessage(getString(R.string.error_flight_unhandled_TEMPLATE, caseNumber));
			builder.setNeutralButton(R.string.call_support, this);
		}
		else {
			builder.setMessage(R.string.error_flight_unhandled);
		}

		builder.setPositiveButton(R.string.retry, this);
		builder.setNegativeButton(R.string.cancel, this);

		return builder.create();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			mListener.onRetryUnhandledException();
			break;
		case DialogInterface.BUTTON_NEUTRAL:
			mListener.onCallCustomerSupport();
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			mListener.onCancelUnhandledException();
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface UnhandledErrorDialogFragmentListener {
		public void onRetryUnhandledException();

		public void onCallCustomerSupport();

		public void onCancelUnhandledException();
	}
}
