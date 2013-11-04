package com.expedia.bookings.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.utils.ClipboardUtils;

/**
 * This is a DialogPreference that just displays the GCM Id used for push notifications.
 * It does not actually alter any preferences.
 */
public class GCMIdDialogPreference extends DialogPreference {

	Dialog mDialog;

	public GCMIdDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GCMIdDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public Dialog getDialog() {
		return mDialog;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		mDialog = null;
	}

	@Override
	protected void showDialog(Bundle state) {
		Context context = getContext();
		final String gcmId = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("GCM Info");

		if (TextUtils.isEmpty(gcmId)) {
			builder.setMessage("GCM ID NOT CURRENTLY AVAILABLE! Please wait a little a while (and ensure this isn't a VSC build).");

		}
		else {
			builder.setMessage(gcmId).setNeutralButton("Copy Text", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					ClipboardUtils.setText(getContext(), gcmId);
					Toast.makeText(getContext(), R.string.toast_copied_to_clipboard, Toast.LENGTH_SHORT).show();
				}

			});
		}

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		mDialog = builder.create();
		mDialog.show();
	}

}
