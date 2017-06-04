package com.expedia.bookings.utils;

import java.util.Locale;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import com.expedia.account.AccountService;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.LocationServices;
import com.mobiata.android.util.SettingUtils;

public class ServicesUtil {

	private static final String APP_VERSION = "APP_VERSION";

	/**
	 * Constructs a user agent string to be used against Expedia requests. It is important to exclude the word "Android"
	 * otherwise mobile redirects occur when we don't want them. This is useful for all API requests contained here
	 * in ExpediaServices as well as certain requests through WebViewActivity in order to prevent the redirects.
	 *
	 * @return
	 */
	public static String generateUserAgentString() {
		// Construct a proper user agent string
		String versionName = BuildConfig.VERSION_NAME;
		// Be careful not to use the word "Android" here
		// https://mingle/projects/e3_mobile_web/cards/676
		String userAgent = BuildConfig.USER_AGENT + "/" + versionName + " (EHad; Mobiata)";
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
		String deviceType = "phone";
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
			generateUserAgentString());
	}

	public static String generateXDevLocationString(Context context) {
		int permissionCheck = ContextCompat.checkSelfPermission(context,
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			// User location
			android.location.Location bestLastLocation = LocationServices.getLastBestLocation(context, 0);
			if (bestLastLocation != null) {
				return distortCoordinates(bestLastLocation.getLatitude()) + "," + distortCoordinates(bestLastLocation.getLongitude());
			}
		}
		return null;
	}

	//rounds the coordinates to a certain number of decimal places(0.1)-
	//  37.2994921 -> 37.3
	// -122.4995990 -> -122.5
	public static double distortCoordinates(double coordinates) {
		return Math.floor(coordinates * 10 + 0.5) / 10;
	}

	public static String getRailApiKey(Context context) {
			return context.getResources().getString(R.string.rails_prod_api_key);
	}

	public static String getHmacSecretKey(Context context) {
		return context.getResources().getString(R.string.hmac_secret_key);
	}

	public static String getHmacUserName(Context context) {
		return context.getResources().getString(R.string.hmac_user_name);
	}

	public static String getGaiaApiKey(Context context) {
		return context.getResources().getString(R.string.gaia_prod_api_key);
	}
}
