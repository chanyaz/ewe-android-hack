package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.interfaces.IDialogForwardBackwardListener;
import com.mobiata.android.util.Ui;
import com.squareup.phrase.Phrase;

public class FlightTravelerSaveDialogFragment extends DialogFragment {

	private IDialogForwardBackwardListener mListener;

	public static FlightTravelerSaveDialogFragment newInstance() {
		FlightTravelerSaveDialogFragment frag = new FlightTravelerSaveDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		String message =
			Phrase.from(getActivity(), R.string.save_traveler_message_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.put("name", Db.getWorkingTravelerManager().getWorkingTraveler().getFirstName()
					+ " " + Db.getWorkingTravelerManager().getWorkingTraveler().getLastName())
				.format().toString();

		return new AlertDialog.Builder(getActivity()).setCancelable(false)
			.setTitle(R.string.save_traveler)
			.setMessage(message)
			.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(true);
					mListener.onDialogMoveForward();
				}
			})
			.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Db.getWorkingTravelerManager().getWorkingTraveler().setSaveTravelerToExpediaAccount(false);
					mListener.onDialogMoveForward();

				}
			}).create();

	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, IDialogForwardBackwardListener.class);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		if (mListener != null) {
			mListener.onDialogMoveBackwards();
		}
	}
}
