package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

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

	public void setMessage(int stringId) {
		Bundle args = getArguments();
		args.putInt(ARG_MESSAGE, stringId);
		setArguments(args);
	}

	public void shouldFinishActivity(boolean shouldFinish) {
		Bundle args = getArguments();
		args.putBoolean(ARG_SHOULD_FINISH_ACTIVITY, shouldFinish);
		setArguments(args);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int messageId = args
			.getInt(ARG_MESSAGE, Ui.obtainThemeResID(getActivity(), R.attr.skin_sorryRoomsSoldOutErrorMessage));
		mShouldFinishActivity = args.getBoolean(ARG_SHOULD_FINISH_ACTIVITY, true);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(getString(messageId));

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
