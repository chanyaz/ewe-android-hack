package com.expedia.bookings.server;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.SettingUtils;

public enum EndPoint {
	PRODUCTION,
	DEV,
	INTEGRATION,
	STABLE,
	PROXY,
	MOCK_SERVER,
	PUBLIC_INTEGRATION,
	TRUNK,
	TRUNK_STUBBED,
	CUSTOM_SERVER,
	;

	private static Map<EndPoint, String> sServerUrls = new HashMap<>();

	public static void init(Context context, String assetPath) {
		try {
			InputStream is = context.getAssets().open(assetPath);
			JSONObject data = new JSONObject(IoUtils.convertStreamToString(is));

			sServerUrls.put(EndPoint.PRODUCTION, data.optString("production").replace('@', 's'));
			sServerUrls.put(EndPoint.DEV, data.optString("development").replace('@', 's'));
			sServerUrls.put(EndPoint.INTEGRATION, data.optString("integration").replace('@', 's'));
			sServerUrls.put(EndPoint.STABLE, data.optString("stable").replace('@', 's'));
			sServerUrls.put(EndPoint.PUBLIC_INTEGRATION, data.optString("publicIntegration").replace('@', 's'));
			sServerUrls.put(EndPoint.TRUNK, data.optString("trunk").replace('@', 's'));
			sServerUrls.put(EndPoint.TRUNK_STUBBED, data.optString("stubbed").replace('@', 's'));
		}
		catch (Exception e) {
			// If the endpoints fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the base E3 server url, based on dev settings
	 */
	public static String getE3EndpointUrl(Context context, final boolean isSecure) {
		EndPoint endPoint = getEndPoint(context);
		String domain = PointOfSale.getPointOfSale().getUrl();

		String urlTemplate = sServerUrls.get(endPoint);
		if (!TextUtils.isEmpty(urlTemplate)) {
			String protocol = isSecure ? "https" : "http";

			if (ProductFlavorFeatureConfiguration.getInstance().shouldUseDotlessDomain(endPoint)) {
				domain = TextUtils.join("", domain.split("\\."));
			}

			String serverURL = String.format(urlTemplate, protocol, domain);
			serverURL = ProductFlavorFeatureConfiguration.getInstance().touchupE3EndpointUrlIfRequired(serverURL);

			return serverURL;
		}

		else if (endPoint == EndPoint.PROXY || endPoint == EndPoint.MOCK_SERVER) {
			return "http://" + SettingUtils.get(context, context.getString(R.string.preference_proxy_server_address),
				"localhost:3000") + "/" + domain + "/";
		}
		else if (endPoint == EndPoint.CUSTOM_SERVER) {
			boolean forceHttp = SettingUtils
				.get(context, context.getString(R.string.preference_force_custom_server_http_only), false);
			String protocol = isSecure && !forceHttp ? "https" : "http";
			String server = SettingUtils
				.get(context, context.getString(R.string.preference_proxy_server_address), "localhost:3000");
			return protocol + "://" + server + "/";
		}
		else {
			throw new RuntimeException("Didn't know how to handle EndPoint: " + endPoint);
		}
	}

	/**
	 * Returns the base suggestion server url, based on dev settings
	 */
	private final static String ESS_PRODUCTION_ENDPOINT = "http://suggest.expedia.com";

	public static String getEssEndpointUrl(Context context, final boolean isSecure) {
		EndPoint endPoint = getEndPoint(context);

		if (endPoint == EndPoint.CUSTOM_SERVER) {
			boolean forceHttp = SettingUtils
				.get(context, context.getString(R.string.preference_force_custom_server_http_only), false);
			String protocol = isSecure && !forceHttp ? "https" : "http";
			String server = SettingUtils
				.get(context, context.getString(R.string.preference_proxy_server_address), "localhost:3000");
			return protocol + "://" + server + "/";
		}

		return ESS_PRODUCTION_ENDPOINT;
	}

	public static EndPoint getEndPoint(Context context) {
		boolean isRelease = AndroidUtils.isRelease(context);
		if (isRelease) {
			// Fastpath
			return EndPoint.PRODUCTION;
		}

		String which = SettingUtils.get(context, context.getString(R.string.preference_which_api_to_use_key), "");

		if (which.equals("Dev")) {
			return EndPoint.DEV;
		}
		else if (which.equals("Proxy")) {
			return EndPoint.PROXY;
		}
		else if (which.equals("Mock Server")) {
			return EndPoint.MOCK_SERVER;
		}
		else if (which.equals("Public Integration")) {
			return EndPoint.PUBLIC_INTEGRATION;
		}
		else if (which.equals("Integration")) {
			return EndPoint.INTEGRATION;
		}
		else if (which.equals("Stable")) {
			return EndPoint.STABLE;
		}
		else if (which.equals("Trunk")) {
			return EndPoint.TRUNK;
		}
		else if (which.equals("Trunk (Stubbed)")) {
			return EndPoint.TRUNK_STUBBED;
		}
		else if (which.equals("Custom Server")) {
			return EndPoint.CUSTOM_SERVER;
		}
		else {
			return EndPoint.PRODUCTION;
		}
	}

}
