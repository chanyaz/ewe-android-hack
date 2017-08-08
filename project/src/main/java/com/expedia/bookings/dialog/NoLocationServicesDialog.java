package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.navigation.NavUtils;

public class NoLocationServicesDialog extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String TAG = "NO_LOCATION_FRAG";

	public static NoLocationServicesDialog newInstance() {
		return new NoLocationServicesDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.EnableLocationSettings);
		builder.setPositiveButton(R.string.ok, this);
		builder.setNegativeButton(R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			if (NavUtils.isIntentAvailable(getActivity(), locationSettingsIntent)) {
				startActivity(locationSettingsIntent);
			}
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dismiss();
			break;
		}
	}
}
