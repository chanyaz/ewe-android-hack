package com.expedia.bookings.utils;

import java.util.Locale;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.expedia.account.AccountService;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.dagger.AppComponent;
import com.expedia.bookings.data.DeviceType;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.server.EndpointProvider;
import com.mobiata.android.LocationServices;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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
	 */
	public static AccountService generateAccountService(Context context) {

		AppComponent injectedComponents = Ui.getApplication(context).appComponent();
		boolean isUserBucketedForAPIMAuth = AbacusFeatureConfigManager
			.isBucketedForTest(context, AbacusUtils.EBAndroidAppAccountsAPIKongEndPoint);
		EndpointProvider endpointProvider = injectedComponents.endpointProvider();

		return new AccountService(
			injectedComponents.okHttpClient(),
			isUserBucketedForAPIMAuth ? endpointProvider.getKongEndpointUrl() : endpointProvider.getE3EndpointUrl(),
			PointOfSale.getPointOfSale().getSiteId(),
			PointOfSale.getPointOfSale().getDualLanguageId(),
			ServicesUtil.generateClientId(context),
			generateUserAgentString(),
			Schedulers.io(),
			AndroidSchedulers.mainThread()
		);
	}

	public static String generateXDevLocationString(Context context) {
		int permissionCheck = ContextCompat.checkSelfPermission(context,
			Manifest.permission.ACCESS_FINE_LOCATION);

		if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
			// User location
			android.location.Location bestLastLocation = LocationServices.getLastBestLocation(context, 0);
			if (bestLastLocation != null) {
				return distortCoordinates(bestLastLocation.getLatitude()) + "," + distortCoordinates(
					bestLastLocation.getLongitude());
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

	public static String getGaiaApiKey(Context context) {
		return context.getResources().getString(R.string.gaia_prod_api_key);
	}

	@NonNull
	public static DeviceType getDeviceType(Context context) {
		if (AndroidUtils.isTablet(context)) {
			return DeviceType.TABLET;
		}
		return DeviceType.PHONE;
	}

	public static String getTravelGraphClientId(Context context) {
		return context.getResources().getString(R.string.tg_client_id);
	}

	public static String getTravelGraphToken(Context context, EndPoint endPoint) {
		if (EndPoint.INTEGRATION == endPoint) {
			return context.getResources().getString(R.string.tg_int_client_token);
		}
		return context.getResources().getString(R.string.tg_prod_client_token);
	}

	public static String getHotelShortlistClientId(Context context) {
		return context.getString(R.string.hotel_shortlist_client_id);
	}

	public static String getHotelShortlistClientToken(Context context, EndPoint endPoint) {
		if (endPoint == EndPoint.INTEGRATION) {
			return context.getResources().getString(R.string.hotel_shortlist_test_client_token);
		}
		return context.getResources().getString(R.string.hotel_shortlist_prod_client_token);
	}

	public static String getHotelReviewsClientId(Context context) {
		return context.getString(R.string.hotel_reviews_client_id);
	}

	public static String getHotelReviewsApiKey(Context context) {
		return context.getString(R.string.hotel_reviews_api_key);
	}
}
