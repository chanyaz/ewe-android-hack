package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.squareup.phrase.Phrase;

public class UnhandledErrorDialogFragment extends DialogFragment implements OnClickListener {

	private static final String ARG_CASE_NUMBER = "ARG_CASE_NUMBER";

	public static UnhandledErrorDialogFragment newInstance(String caseNumber) {
		UnhandledErrorDialogFragment fragment = new UnhandledErrorDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_CASE_NUMBER, caseNumber);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());

		// If case number was not supplied, don't give user option to call support about it
		// This can happen when we get a null response from the server.
		String caseNumber = getArguments().getString(ARG_CASE_NUMBER);
		if (!TextUtils.isEmpty(caseNumber)) {
			CharSequence message = Phrase.from(getActivity(),
				R.string.error_flight_unhandled_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.put("itinerary", caseNumber)
				.format();

			builder.setMessage(message);
			builder.setNeutralButton(R.string.call_support, this);
		}
		else {
			builder.setMessage(R.string.error_flight_unhandled);
		}

		builder.setPositiveButton(R.string.retry, this);
		builder.setNegativeButton(R.string.cancel, this);

		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

		Events.post(new Events.UnhandledErrorDialogCancel());
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			Events.post(new Events.UnhandledErrorDialogRetry());
			break;
		case DialogInterface.BUTTON_NEUTRAL:
			Events.post(new Events.UnhandledErrorDialogCallCustomerSupport());
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			Events.post(new Events.UnhandledErrorDialogCancel());
			break;
		}
	}

}
