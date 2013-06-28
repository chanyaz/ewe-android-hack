package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;

public class OverwriteExistingTravelerDialogFragment extends DialogFragment {

	public static final String TAG = OverwriteExistingTravelerDialogFragment.class.getName();

	FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener mListener;

	public static OverwriteExistingTravelerDialogFragment newInstance() {
		return new OverwriteExistingTravelerDialogFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener)) {
			throw new RuntimeException(
					"OverwriteExistingTravelerDialogFragment activity must implement TravelerInfoYoYoListener!");
		}

		mListener = (FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setCancelable(false);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String workingTravelerName = Db.getWorkingTravelerManager().getWorkingTraveler().getFirstName() + " "
				+ Db.getWorkingTravelerManager().getWorkingTraveler().getLastName();

		AlertDialog dialog = new AlertDialog.Builder(getActivity())
				.setCancelable(false)
				.setTitle(R.string.cant_save_traveler)
				.setMessage(String.format(getString(R.string.you_already_have_traveler_TEMPLATE), workingTravelerName))
				.setPositiveButton(R.string.overwrite, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						//We want to overwrite, so we go through, find the traveler with the same name and steal his/her tuid
						if (User.isLoggedIn(getActivity()) && Db.getUser() != null
								&& Db.getUser().getAssociatedTravelers() != null) {
							for (Traveler trav : Db.getUser().getAssociatedTravelers()) {
								if (Db.getWorkingTravelerManager().getWorkingTraveler().compareNameTo(trav) == 0) {
									//We find the traveler with the same name, and steal his tuid
									Db.getWorkingTravelerManager().getWorkingTraveler().setTuid(trav.getTuid());
								}
							}
						}
						mListener.moveForward();
					}

				})
				.setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(false);
						mListener.moveForward();
					}
				}).create();
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.moveBackwards();
		}
	}
}
