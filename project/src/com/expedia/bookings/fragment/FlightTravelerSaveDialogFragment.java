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
import com.expedia.bookings.utils.Ui;

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

		String messageTemplate = getString(Ui.obtainThemeResID(getActivity(), R.attr.saveTravelerToAccountString));
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

		mListener = Ui.findFragmentListener(this, TravelerInfoYoYoListener.class);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.moveBackwards();
		}
	}
}
