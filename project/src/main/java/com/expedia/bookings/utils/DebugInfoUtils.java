package com.expedia.bookings.utils;

import java.net.HttpCookie;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;

public class DebugInfoUtils {

	public static StringBuilder generateEmailBody(Context context) {
		StringBuilder body = new StringBuilder();

		body.append("\n\n\n");
		body.append("------");
		body.append("\n\n");
		body.append(context.getString(R.string.app_support_message_body));
		body.append("\n\n");

		body.append("PACKAGE: ");
		body.append(context.getPackageName());
		body.append("\n");
		body.append("VERSION: ");
		body.append(BuildConfig.VERSION_NAME);
		body.append("\n");
		body.append("CODE: ");
		body.append(AndroidUtils.getAppCode(context));
		body.append("\n");
		body.append("POS: ");
		body.append(PointOfSale.getPointOfSale().getPointOfSaleId().toString());
		body.append("\n");
		body.append("LOCALE: ");
		body.append(Locale.getDefault().toString());
		body.append("\n");
		body.append("ABACUS GUID: ");
		body.append(Db.getAbacusGuid());

		body.append("\n\n");

		body.append("MC1 COOKIE: ");
		body.append(getMC1CookieStr(context));

		body.append("\n\n");

		if (User.isLoggedIn(context) && Db.getUser() != null) {
			String email = Db.getUser().getPrimaryTraveler().getEmail();
			body.append(Phrase.from(context, R.string.email_user_name_template)
					.put("brand", BuildConfig.brand)
					.put("email", email)
					.format());

			body.append("\n\n");
		}

		LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		if (lm != null) {
			boolean gpsEnabled = false;
			boolean networkEnabled = false;

			try {
				gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
			}
			catch (Exception ex) {
				//ignore
			}

			try {
				networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			}
			catch (Exception ex) {
				//ignore
			}

			body.append("Location Services: GPS=" + gpsEnabled + ", Network=" + networkEnabled);

			body.append("\n\n");
		}

		if (GCMRegistrationKeeper.getInstance(context) != null && !TextUtils
			.isEmpty(GCMRegistrationKeeper.getInstance(context).getRegistrationId(context))) {
			body.append("GCM Push Token: " + GCMRegistrationKeeper.getInstance(context).getRegistrationId(context));

			body.append("\n\n");
		}

		body.append(DebugUtils.getBuildInfo());
		return body;
	}

	public static String getMC1CookieStr(Context context) {
		List<HttpCookie> cookies = ExpediaServices.getCookies(context);
		if (cookies != null) {
			for (HttpCookie cookie : cookies) {
				if (cookie.getName() != null && cookie.getName().equals("MC1")) {
					return cookie.getValue();
				}
			}
		}
		return "";
	}

}
