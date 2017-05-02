package com.expedia.bookings.utils;

import android.content.Context;
import android.content.Intent;

import com.readystatesoftware.chuck.Chuck;

public class ChuckShim {
	public static Intent getLaunchIntent(Context context) {
		return Chuck.getLaunchIntent(context);
	}
}
