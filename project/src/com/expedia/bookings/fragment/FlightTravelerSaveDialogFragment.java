package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.fragment.FlightTravelerInfoOptionsFragment.TravelerInfoYoYoListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FlightTravelerSaveDialogFragment extends DialogFragment {

	TravelerInfoYoYoListener mListener;

	int mCurrentPassengerIndex;
	FlightPassenger mPassenger;

	public static FlightTravelerSaveDialogFragment newInstance() {
		FlightTravelerSaveDialogFragment frag = new FlightTravelerSaveDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		mCurrentPassengerIndex = getActivity().getIntent().getIntExtra(Codes.PASSENGER_INDEX, 0);
		mPassenger = Db.getFlightPassengers().get(mCurrentPassengerIndex);

		return new AlertDialog.Builder(getActivity()).setCancelable(false)
				.setTitle(R.string.save_traveler)
				.setMessage(R.string.save_traveler_message)
				.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mPassenger.setSavePassengerToExpediaAccount(true);
						mListener.moveForward();

					}
				})
				.setNegativeButton(R.string.dont_save, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						mPassenger.setSavePassengerToExpediaAccount(false);
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
	public void onCancel(DialogInterface dialog){
		super.onCancel(dialog);
		if(mListener != null){
			mListener.moveBackwards();
		}
	}
}
