package com.expedia.bookings.widget;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * This is a wrapper class to avoid ProgressDialog deprecation warning
 */
public class DeprecatedProgressDialog extends ProgressDialog {
	public DeprecatedProgressDialog(Context context) {
		super(context);
	}
}
