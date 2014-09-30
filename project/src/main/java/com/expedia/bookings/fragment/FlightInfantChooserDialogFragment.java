package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;

public class FlightInfantChooserDialogFragment extends DialogFragment {

	private static final String INSTANCE_NEW_SELECTION = "INSTANCE_NEW_SELECTION";

	private static final int NUM_INFANT_SEATING_PREFS = 2;
	private int mNewSelection = -1;

	public static FlightInfantChooserDialogFragment newInstance() {
		FlightInfantChooserDialogFragment frag = new FlightInfantChooserDialogFragment();
		frag.setCancelable(false);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mNewSelection = savedInstanceState.getInt(INSTANCE_NEW_SELECTION, -1);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(INSTANCE_NEW_SELECTION, mNewSelection);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		CharSequence[] items = new CharSequence[NUM_INFANT_SEATING_PREFS];
		items[0] = getString(R.string.infants_in_laps);
		items[1] = getString(R.string.infants_in_seats);

		final int choiceInDb = Db.getFlightSearch().getSearchParams().getInfantSeatingInLap() ? 0 : 1;

		return new AlertDialog.Builder(getActivity())
			.setTitle(getString(R.string.infants_seating_preference_description))
			.setSingleChoiceItems(items, choiceInDb, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int newChoice) {
					mNewSelection = newChoice;
				}
			})
			.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mNewSelection != -1 && mNewSelection != choiceInDb) {
						// Pop a simple dialog
						String title = getString(R.string.infant_seating_preference);
						String message = mNewSelection == 0 ? getString(R.string.infants_in_laps_prompt) : getString(R.string.infants_in_seats_prompt);
						String button = getString(R.string.ok);
						SimpleCallbackDialogFragment df = SimpleCallbackDialogFragment.newInstance(title, message, button,
							SimpleCallbackDialogFragment.CODE_TABLET_FLIGHTS_INFANT_CHOOSER);
						df.show(getFragmentManager(), "infantDialog");
					}
				}
			})
			.create();
	}

}
