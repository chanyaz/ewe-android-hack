package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;

public class ConfirmItinRemoveDialogFragment extends DialogFragment {

	public static final String TAG = "ConfirmItinRemoveDialogFragment";

	private static String mItinKey;

	public static ConfirmItinRemoveDialogFragment getInstance(String itinKey) {
		ConfirmItinRemoveDialogFragment instance = new ConfirmItinRemoveDialogFragment();
		Bundle args = new Bundle();
		mItinKey = itinKey;
		instance.setArguments(args);
		return instance;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());

		builder.setTitle(getResources().getString(R.string.remove_shared_itin_title));
		builder.setMessage(getResources().getString(R.string.remove_shared_itin_message));
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				ItineraryManager.getInstance().removeItin(mItinKey);
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}
}
