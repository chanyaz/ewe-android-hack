package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.location.LocationManager;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.notification.GCMRegistrationKeeper;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.PersistentCookieManagerV2;
import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.services.PersistentCookiesCookieJar;
import com.mobiata.android.DebugUtils;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class DebugInfoUtils {

	public static StringBuilder generateEmailBody(Context context) {
		UserStateManager userStateManager = Ui.getApplication(context).appComponent().userStateManager();
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
		body.append(Db.sharedInstance.getAbacusGuid());

		body.append("\n\n");

		body.append("MC1 COOKIE: ");
		body.append(getMC1CookieStr(context));

		body.append("\n\n");

		User user = userStateManager.getUserSource().getUser();

		if (userStateManager.isUserAuthenticated() && user != null) {
			String email = user.getPrimaryTraveler().getEmail();
			UserLoyaltyMembershipInformation loyaltyMembershipInformation = user.getLoyaltyMembershipInformation();

			body.append(Phrase.from(context, R.string.email_user_name_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.put("email", email)
					.format());
			body.append("\n");

			if (loyaltyMembershipInformation != null) {
				body.append(Phrase.from(context, R.string.user_points_level_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.put("level", loyaltyMembershipInformation.getLoyaltyMembershipTier().name())
					.format());
				body.append("\n");

				body.append(Phrase.from(context, R.string.user_points_available_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.put("points_amount", String.valueOf(loyaltyMembershipInformation.getLoyaltyPointsAvailable()))
					.format());
				body.append("\n");

				body.append(Phrase.from(context, R.string.user_points_pending_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.put("points_amount", String.valueOf(loyaltyMembershipInformation.getLoyaltyPointsPending()))
					.format());
				body.append("\n");
			}

			if (Db.sharedInstance.getSignInType() != null) {
				body.append(Phrase.from(context, R.string.account_sign_in_method_TEMPLATE)
					.put("sign_in_method", Db.sharedInstance.getSignInType().name())
					.format());
			}

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
		PersistentCookiesCookieJar mCookieManager = new ExpediaServices(context).mCookieManager;
		if (mCookieManager instanceof PersistentCookieManagerV2) {
			HttpUrl url = Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointAsHttpUrl();

			return ((PersistentCookieManagerV2) mCookieManager).getCookieValue(url, "MC1");
		}
		else {
			HashMap<String, HashMap<String, Cookie>> cookiesStore = ((PersistentCookieManager)mCookieManager).getCookieStore();
			if (cookiesStore != null) {
				for (HashMap<String, Cookie> cookies : cookiesStore.values()) {
					if (cookies.containsKey("MC1")) {
						return cookies.get("MC1").value();
					}
				}
			}
			return "";
		}
	}

}
