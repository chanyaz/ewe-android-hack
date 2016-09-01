package com.expedia.bookings.test.robolectric.shadows;

import java.util.Locale;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;
import android.text.format.DateFormat;

@Implements(DateFormat.class)
public class ShadowDateFormat {
	@Implementation
	public static java.text.DateFormat getTimeFormat(Context context) {
		return new java.text.SimpleDateFormat("h:mm a", Locale.US);
	}
}
