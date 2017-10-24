package com.expedia.bookings.utils;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;

public class AlertDialogUtils {

	public static void showDialog(Context context, String message,
		final Object eventToPostOnPositiveButton, String positiveButtonLabel,
		final Object eventToPostOnNegativeButton, String negativeButtonLabel) {
		AlertDialog.Builder b = new AlertDialog.Builder(context);
		b.setCancelable(false)
			.setMessage(message);
		if (Strings.isNotEmpty(positiveButtonLabel)) {
			b.setPositiveButton(positiveButtonLabel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (eventToPostOnPositiveButton != null) {
						Events.post(eventToPostOnPositiveButton);
					}
				}
			});
		}

		if (Strings.isNotEmpty(negativeButtonLabel)) {
			b.setNegativeButton(negativeButtonLabel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (eventToPostOnNegativeButton != null) {
						Events.post(eventToPostOnNegativeButton);
					}
				}
			});
		}
		b.show();
	}

	public static void showBookmarkDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.show();
		progressDialog.setContentView(R.layout.processing_bookmark_layout);
		progressDialog.setCancelable(false);
	}
}
