package com.expedia.bookings.preference;

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * This is a DialogPreference that just displays the GCM Id used for push notifications.
 * It does not actually alter any preferences.
 */
public class GCMIdDialogPreference extends DialogPreference {
	public GCMIdDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
}
