package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.squareup.phrase.Phrase;

public class ConfirmationUtils {

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static String determineContactText(Context context) {

		String message = Phrase.from(context,
			R.string.contact_phone_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.put("phone", PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()))
			.format().toString();

		return message;

	}

}
