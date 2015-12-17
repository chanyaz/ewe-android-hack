package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.squareup.phrase.Phrase;

public class HotelErrorDialog extends DialogFragment {
	private static final String ARG_SHOULD_FINISH_ACTIVITY = "ARG_SHOULD_FINISH_ACTIVITY";
	private static final String ARG_MESSAGE = "ARG_MESSAGE";

	private boolean mShouldFinishActivity;

	public static HotelErrorDialog newInstance() {
		HotelErrorDialog frag = new HotelErrorDialog();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	public void setMessage(String message) {
		Bundle args = getArguments();
		args.putString(ARG_MESSAGE, message);
		setArguments(args);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		String message = args
			.getString(ARG_MESSAGE, Phrase.from(getActivity(), R.string.error_hotel_is_now_sold_out_TEMPLATE)
				.put("brand", BuildConfig.brand).format().toString());
		mShouldFinishActivity = args.getBoolean(ARG_SHOULD_FINISH_ACTIVITY, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(message);

		builder.setNeutralButton(com.mobiata.android.R.string.ok, null);

		return builder.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (mShouldFinishActivity && getActivity() != null) {
			getActivity().finish();
		}
	}
}
