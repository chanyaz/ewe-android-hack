package com.expedia.bookings.server;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.EnumMap;

import android.content.Context;
import android.support.annotation.NonNull;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.Strings;
import com.google.gson.Gson;
import com.mobiata.android.util.SettingUtils;

import okhttp3.HttpUrl;

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

	public String getShortlyEndpointUrl() {
		if (BuildConfig.RELEASE) {
			return "https://" + ProductFlavorFeatureConfiguration.getInstance().getHostnameForShortUrl();
		}

		// Mock Server if enabled
		EndPoint endPoint = getEndPoint();
		if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getE3EndpointUrl();
		}

		return "https://" + ProductFlavorFeatureConfiguration.getInstance().getHostnameForShortUrl();
	}

	public String getAbacusEndpointUrl() {
		// Always point to production if release
		if (BuildConfig.RELEASE) {
			return getE3EndpointUrl();
		}

		// Mock Server if enabled
		EndPoint endPoint = getEndPoint();
		if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getE3EndpointUrl();
		}

		// Default to Dev on debug
		return "http://abacus-experiment-api-server.exp-int.net/";
	}

	/**
	 * Returns the base E3 server url, based on dev settings
	 */
	public String getE3EndpointUrl() {
		return getE3EndpointUrl(getEndPoint());
	}

	public String getE3EndpointUrlWithPath(String path) {
		return getE3EndpointUrl(getEndPoint()) + path;
	}

	public @NonNull HttpUrl getE3EndpointAsHttpUrl() {
		HttpUrl url = HttpUrl.parse(getE3EndpointUrl());

		if (url == null) {
			throw new RuntimeException("url is unexpectedly null in getE3EndpointAsHttpUrl.");
		}

		return url;
	}

	public String getE3EndpointUrl(EndPoint endPoint) {
		String domain = PointOfSale.getPointOfSale().getUrl();

		String urlTemplate = serverUrls.get(endPoint);
		if (Strings.isNotEmpty(urlTemplate)) {
			String protocol = "https";

			if (ProductFlavorFeatureConfiguration.getInstance().shouldUseDotlessDomain(endPoint)) {
				domain = Strings.joinWithoutEmpties("", Arrays.asList(domain.split("\\.")));
			}

			String serverURL = String.format(urlTemplate, protocol, domain);
			serverURL = ProductFlavorFeatureConfiguration.getInstance().touchupE3EndpointUrlIfRequired(serverURL);

			return serverURL;
		}
		else if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getCustomServerAddress();
		}
		else {
			throw new RuntimeException("Didn't know how to handle EndPoint: " + endPoint);
		}
	}

	public String getUrgencyEndpointUrl() {
		String endpoint;
		switch (getEndPoint()) {
		case MOCK_MODE:
			endpoint = getCustomServerAddress();
			break;
		case INTEGRATION:
			endpoint = "https://www.expedia.com.urgency-prime.us-west-2.test.expedia.com/urgencyservice/v1/";
			break;
		case PRODUCTION:
			endpoint = "https://urgency.expedia.com/urgencyservice/v1/";
			break;
		default:
			endpoint = "https://urgency.expedia.com/urgencyservice/v1/";
		}
		return endpoint;
	}

	public String getRailEndpointUrl() {
		String endpoint;
		switch (getEndPoint()) {
		case MOCK_MODE:
			endpoint = getCustomServerAddress();
			break;
		case PRODUCTION:
			endpoint = "https://apim.expedia.com/";
			break;
		default:
			endpoint = "https://apim.int.expedia.com/";

		}
		return endpoint;
	}

	public String getRailWebViewEndpointUrl() {
		String railsMcicidTag = "mcicid=App.Rails.WebView";
		String railWebViewUrl = getE3EndpointUrl() + PointOfSale.getPointOfSale().getRailUrlInfix() + "?" + railsMcicidTag;
		return railWebViewUrl;
	}

	public String getSatelliteEndpointUrl() {
		String endpoint;
		switch (getEndPoint()) {
		case MOCK_MODE:
			endpoint = getCustomServerAddress();
			break;
		case PRODUCTION:
			endpoint = "https://apim.expedia.com/";
			break;
		default:
			endpoint = "https://apim.int.expedia.com/";
		}
		return endpoint;
	}

	/**
	 * Returns the base suggestion server url, based on dev settings
	 */
	private final static String ESS_PRODUCTION_ENDPOINT = "https://suggest.expedia.com/";

	public String getEssEndpointUrl() {
		EndPoint endPoint = getEndPoint();

		if (endPoint == EndPoint.CUSTOM_SERVER || endPoint == EndPoint.MOCK_MODE) {
			return getCustomServerAddress();
		}

		return ESS_PRODUCTION_ENDPOINT;
	}

	/**
	 * Returns the base suggestion server url for GAIA, based on dev settings
	 */
	private final static String GAIA_PROD_ENDPOINT = "https://apim.prod.expedia.com/m/geo/";

	public String getGaiaEndpointUrl() {
		EndPoint endPoint = getEndPoint();
		switch (endPoint) {
		case MOCK_MODE:
			return getCustomServerAddress();
		default:
			return GAIA_PROD_ENDPOINT;
		}
	}

	private static final String TEST_REVIEWS_BASE_URL = "https://reviewsvc.ewetest.expedia.com/";
	private static final String PROD_REVIEWS_BASE_URL = "https://reviewsvc.expedia.com/";

	public String getReviewsEndpointUrl() {
		EndPoint endPoint = getEndPoint();
		switch (endPoint) {
		case MOCK_MODE:
			return getCustomServerAddress();
		case INTEGRATION:
			return TEST_REVIEWS_BASE_URL;
		default:
			return PROD_REVIEWS_BASE_URL;
		}
	}

	private static final String PROD_KRAZY_GLUE_URL = "https://xsell.expedia.com";

	public String getKrazyglueEndpointUrl() {
		return PROD_KRAZY_GLUE_URL;
	}

	//Smart Offer Service
	private static final String SMART_OFFER_SERVICE_ENDPOINT = "https://www.expedia.com";

	public String getSmartOfferServiceEndpoint() {
		String endpoint;
		switch (getEndPoint()) {
			case MOCK_MODE:
				endpoint = getCustomServerAddress();
				break;
			default:
				endpoint = SMART_OFFER_SERVICE_ENDPOINT;
		}
		return endpoint;
	}

	public String getCustomServerAddress() {
		String server = SettingUtils.get(context, R.string.preference_proxy_server_address, "localhost:3000");
		return "https://" + server + "/";
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
		return (BuildConfig.DEBUG && getEndPoint() == EndPoint.PUBLIC_INTEGRATION);
	}

}
