package com.expedia.bookings.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumMap;

import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.Strings;
import com.google.gson.Gson;
import com.mobiata.android.util.SettingUtils;

public class EndpointProvider {

	private EnumMap<EndPoint, String> serverUrls = new EnumMap<EndPoint, String>(EndPoint.class);
	private Context context;

	public static class EndpointMap {
		String production;
		String integration;
		String development;
		String trunk;
		String publicIntegration;
	}

	public EndpointProvider(Context context, InputStream input) {
		this.context = context.getApplicationContext();

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			EndpointMap endpoints = new Gson().fromJson(reader, EndpointMap.class);
			reader.close();

			serverUrls.put(EndPoint.PRODUCTION, endpoints.production.replace('@', 's'));
			serverUrls.put(EndPoint.DEV, endpoints.development.replace('@', 's'));
			serverUrls.put(EndPoint.INTEGRATION, endpoints.integration.replace('@', 's'));
			serverUrls.put(EndPoint.PUBLIC_INTEGRATION, endpoints.publicIntegration.replace('@', 's'));
			serverUrls.put(EndPoint.TRUNK, endpoints.trunk.replace('@', 's'));
		}
		catch (Exception e) {
			// If the endpoints fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	public String getAbacusEndpoint(final boolean isSecure) {
		// Always point to production if release
		if (BuildConfig.RELEASE) {
			return getE3EndpointUrl(isSecure);
		}

		// Mock Server if enabled
		EndPoint endPoint = getEndPoint();
		if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getE3EndpointUrl(isSecure);
		}

		// Default to Dev on debug
		return "http://phelabstb101.karmalab.net:9117/";
	}

	/**
	 * Returns the base E3 server url, based on dev settings
	 */
	public String getE3EndpointUrl(final boolean isSecure) {
		return getE3EndpointUrl(isSecure, getEndPoint());
	}

	public String getE3EndpointUrl(final boolean isSecure, EndPoint endPoint) {
		String domain = PointOfSale.getPointOfSale().getUrl();

		String urlTemplate = serverUrls.get(endPoint);
		if (Strings.isNotEmpty(urlTemplate)) {
			String protocol = isSecure ? "https" : "http";

			if (ProductFlavorFeatureConfiguration.getInstance().shouldUseDotlessDomain(endPoint)) {
				domain = Strings.joinWithoutEmpties("", Arrays.asList(domain.split("\\.")));
			}

			String serverURL = String.format(urlTemplate, protocol, domain);
			serverURL = ProductFlavorFeatureConfiguration.getInstance().touchupE3EndpointUrlIfRequired(serverURL);

			return serverURL;
		}
		else if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getCustomServerAddress(isSecure);
		}
		else {
			throw new RuntimeException("Didn't know how to handle EndPoint: " + endPoint);
		}
	}

	/**
	 * Returns the base suggestion server url, based on dev settings
	 */
	private final static String ESS_PRODUCTION_ENDPOINT = "http://suggest.expedia.com/";

	public String getEssEndpointUrl(final boolean isSecure) {
		EndPoint endPoint = getEndPoint();

		if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getCustomServerAddress(isSecure);
		}

		return ESS_PRODUCTION_ENDPOINT;
	}

	private String getCustomServerAddress(final boolean isSecure) {
		boolean forceHttp = SettingUtils.get(context, R.string.preference_force_custom_server_http_only, false);
		String protocol = isSecure && !forceHttp ? "https" : "http";
		String server = SettingUtils.get(context, R.string.preference_proxy_server_address, "localhost:3000");
		return protocol + "://" + server + "/";
	}

	public EndPoint getEndPoint() {
		if (BuildConfig.RELEASE) {
			// Fastpath
			return EndPoint.PRODUCTION;
		}

		String which = SettingUtils.get(context, context.getString(R.string.preference_which_api_to_use_key), "");

		if (which.equals("Dev")) {
			return EndPoint.DEV;
		}
		else if (which.equals("Public Integration")) {
			return EndPoint.PUBLIC_INTEGRATION;
		}
		else if (which.equals("Integration")) {
			return EndPoint.INTEGRATION;
		}
		else if (which.equals("Trunk")) {
			return EndPoint.TRUNK;
		}
		else if (which.equals("Custom Server")) {
			return EndPoint.CUSTOM_SERVER;
		}
		else if (which.equals("Mock Mode")) {
			return EndPoint.MOCK_MODE;
		}
		else {
			return EndPoint.PRODUCTION;
		}
	}

	public boolean requestRequiresSiteId() {
		return BuildConfig.DEBUG && getEndPoint() == EndPoint.PUBLIC_INTEGRATION;
	}

}
