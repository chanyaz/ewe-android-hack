package com.expedia.bookings.utils;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.TextView;

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

	public static void showBookmarkDialog(final Context context, String message) {
		final ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.show();
		progressDialog.setContentView(R.layout.processing_bookmark_layout);
		progressDialog.setCancelable(false);
		if (Strings.isNotEmpty(message)) {
			progressDialog.setMessage(message);
			TextView textView = progressDialog.findViewById(R.id.cancel_bookmark_button);
			textView.setVisibility(View.VISIBLE);
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					progressDialog.dismiss();
					DeeplinkSharedPrefParserUtils.Companion.removeSharedPref(context);
				}
			});
		}
	}
}
