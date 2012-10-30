package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener;

public class FlightTravelerSaveDialogFragment extends DialogFragment {

	TravelerInfoYoYoListener mListener;

	public static FlightTravelerSaveDialogFragment newInstance() {
		FlightTravelerSaveDialogFragment frag = new FlightTravelerSaveDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String messageTemplate = getString(R.string.save_traveler_message_TEMPLATE);
		String message = String.format(messageTemplate, Db.getWorkingTravelerManager().getWorkingTraveler()
				.getFirstName()
				+ " " + Db.getWorkingTravelerManager().getWorkingTraveler().getLastName());

		return new AlertDialog.Builder(getActivity()).setCancelable(false)
				.setTitle(R.string.save_traveler)
				.setMessage(message)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(true);
						mListener.moveForward();
					}
				})
				.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(false);
						mListener.moveForward();

					}
				}).create();

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof TravelerInfoYoYoListener)) {
			throw new RuntimeException(
					"FlightTravelerSaveDialogFragment activity must implement TravelerInfoYoYoListener!");
		}

		mListener = (TravelerInfoYoYoListener) activity;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.moveBackwards();
		}
	}
}
