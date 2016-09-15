package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;
import android.os.Build;

import com.expedia.account.AccountService;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.EndPoint;
import com.mobiata.android.util.SettingUtils;

public class ServicesUtil {

	private static final String APP_VERSION = "APP_VERSION";

	/**
	 * Constructs a user agent string to be used against Expedia requests. It is important to exclude the word "Android"
	 * otherwise mobile redirects occur when we don't want them. This is useful for all API requests contained here
	 * in ExpediaServices as well as certain requests through WebViewActivity in order to prevent the redirects.
	 *
	 * @param context
	 * @return
	 */
	public static String generateUserAgentString(Context context) {
		// Construct a proper user agent string
		String versionName = BuildConfig.VERSION_NAME;
		// Be careful not to use the word "Android" here
		// https://mingle/projects/e3_mobile_web/cards/676
		String userAgent = "ExpediaBookings/" + versionName + " (EHad; Mobiata)";
		return userAgent;
	}


	/**
	 * Constructs a x-eb client header string to be used against Expedia requests.
	 *
	 * @param context
	 * @return
	 */
	public static String generateXEbClientString(Context context) {
		// Construct x-eb-client header string
		StringBuilder sb = new StringBuilder();
		addBuildInfo(sb, "PLATFORM", "ANDROID");
		addBuildInfo(sb, "OS_VERSION", Build.VERSION.RELEASE);
		addBuildInfo(sb, "MANUFACTURER", Build.MANUFACTURER);
		addBuildInfo(sb, "MODEL", Build.MODEL);
		addBuildInfo(sb, "UPGRADE", String.valueOf(isUpgrade(context)));
		addBuildInfo(sb, "APP_VERSION", BuildConfig.VERSION_NAME);
		addBuildInfo(sb, "LOCALE", Locale.getDefault().toString());
		return sb.toString();
	}

	private static void addBuildInfo(StringBuilder sb, String key, String value) {
			sb.append(key);
			sb.append(':');
			sb.append(value);
			sb.append(';');
	}

	public static String generateClientId(Context context) {
		return generateClient(context) + ":" + BuildConfig.VERSION_NAME;
	}

	public static String generateClient(Context context) {
		String clientName = ProductFlavorFeatureConfiguration.getInstance().getClientShortName();
		String deviceType = ExpediaBookingApp.useTabletInterface(context) ? "tablet" : "phone";
		return clientName + ".app.android." + deviceType;
	}

	public static String generateSourceType() {
		return "mobileapp";
	}

	public static String generateLangId() {
		int langid = PointOfSale.getPointOfSale().getDualLanguageId();
		return langid == 0 ? "" : Integer.toString(langid);
	}

	public static String generateSiteId() {
		return Integer.toString(PointOfSale.getPointOfSale().getSiteId());
	}

	/**
	 * Has the app been upgraded to a new version?
	 *
	 * @param context
	 * @return
	 */
	private static boolean isUpgrade(Context context) {
		boolean isUpgrade = false;
		String trackVersion = SettingUtils.get(context, APP_VERSION, null);
		String currentVersion = BuildConfig.VERSION_NAME;
		//Check if it is a new installation.
		if (trackVersion == null) {
			SettingUtils.save(context, APP_VERSION, currentVersion);
		} //Check if it is an upgrade.
		else if (!trackVersion.equals(currentVersion)) {
			synchronized (ServicesUtil.class) {
				trackVersion = SettingUtils.get(context, APP_VERSION, null);
				if (!trackVersion.equals(currentVersion)) {
					SettingUtils.save(context, APP_VERSION, currentVersion);
					isUpgrade = true;
				}
			}
		}
		return isUpgrade;
	}

	/**
	 * Convenience method that generates an AccountLib.AccountService from EB configuration.
	 * @param context
	 * @return
	 */
	public static AccountService generateAccountService(Context context) {
		return new AccountService(
			Ui.getApplication(context).appComponent().okHttpClient(),
			Ui.getApplication(context).appComponent().endpointProvider().getE3EndpointUrl(),
			PointOfSale.getPointOfSale().getSiteId(),
			PointOfSale.getPointOfSale().getDualLanguageId(),
			ServicesUtil.generateClientId(context),
			generateUserAgentString(context));
	}

	public static String getRailApiKey(Context context, EndPoint endPoint) {
		switch (endPoint) {
		case PRODUCTION:
			return context.getResources().getString(R.string.rails_prod_api_key);
		case INTEGRATION:
			return context.getResources().getString(R.string.rails_int_api_key);
		default:
			return context.getResources().getString(R.string.rails_trunk_api_key);
		}
	}

}
