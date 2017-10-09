package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.squareup.phrase.Phrase;

public class ConfirmationUtils {

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static String determineContactText(Context context) {
		UserStateManager userStateManager = Ui.getApplication(context).appComponent().userStateManager();
		User user = userStateManager.getUserSource().getUser();

		String message = Phrase.from(context,
			R.string.contact_phone_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.put("phone", PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(user))
			.format().toString();

		return message;

	}

}
