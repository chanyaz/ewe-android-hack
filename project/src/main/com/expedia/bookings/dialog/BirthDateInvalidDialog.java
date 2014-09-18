package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;

import static android.content.DialogInterface.OnClickListener;

public class BirthDateInvalidDialog extends DialogFragment implements OnClickListener {

	private static final String ARG_IS_ON_EDIT_TRAVELER = "ARG_IS_ON_EDIT_TRAVELER";

	public static BirthDateInvalidDialog newInstance(boolean isOnEditTraveler) {
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_ON_EDIT_TRAVELER, isOnEditTraveler);
		BirthDateInvalidDialog frag = new BirthDateInvalidDialog();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.invalid_birthdate_message);
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.edit_search, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		boolean isOnEditTraveler = getArguments().getBoolean(ARG_IS_ON_EDIT_TRAVELER);
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			if (isOnEditTraveler) {
				dismiss();
			}
			else {
				Events.post(new Events.BirthDateInvalidEditTraveler());
			}
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			Events.post(new Events.BirthDateInvalidEditSearch());
			break;
		}
	}
}
