package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.squareup.phrase.Phrase;

public class NoLocationPermissionDialog extends DialogFragment implements DialogInterface.OnClickListener {

	public static final String TAG = "NO_LOCATION_PERMISSION_FRAG";

	public static NoLocationPermissionDialog newInstance() {
		return new NoLocationPermissionDialog();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		String message = Phrase.from(getActivity(), R.string.EnableLocationPermissionSettings_TEMPLATE)
			.put("brand", BuildConfig.brand).format().toString();

		builder.setMessage(message);
		builder.setPositiveButton(R.string.go_to_settings, this);
		builder.setNegativeButton(R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			String uri = "package:" + getActivity().getPackageName();
			settingsIntent.setData(Uri.parse(uri));
			startActivity(settingsIntent);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			dismiss();
			break;
		}
	}
}
