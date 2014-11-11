package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;

public class ConfirmationUtils {

	//////////////////////////////////////////////////////////////////////////////////////////
	// Miscellaneous

	public static String determineContactText(Context context) {

		return context.getString(Ui.obtainThemeResID(context, R.attr.skin_addToCalenderMessage),
			PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));

	}

}
