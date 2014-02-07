package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;

public class ConfirmationUtils {

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static String determineContactText(Context context) {
		if (ExpediaBookingApp.IS_TRAVELOCITY) {
			return context.getString(R.string.contact_phone_template_tvly, PointOfSale.getPointOfSale()
				.getSupportPhoneNumber());
		}

		return context.getString(R.string.contact_phone_template, PointOfSale.getPointOfSale()
			.getSupportPhoneNumber());
	}

}
