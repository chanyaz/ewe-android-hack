package com.expedia.bookings.data;

import java.io.IOException;

import org.json.JSONObject;

import com.expedia.bookings.server.JsonResponseHandler;
import com.squareup.okhttp.Response;

public class WalletPromoResponseHandler extends JsonResponseHandler<WalletPromoResponse> {

	@Override
	public WalletPromoResponse handleResponse(Response response) throws IOException {
		WalletPromoResponse promoResponse = super.handleResponse(response);

		if (promoResponse == null) {
			promoResponse = new WalletPromoResponse();
		}

		return promoResponse;
	}

	@Override
	public WalletPromoResponse handleJson(JSONObject response) {
		WalletPromoResponse promoResponse = new WalletPromoResponse();

		if (response != null) {
			promoResponse.setEnabled(response.optBoolean("walletPromotionEnabled", false));
		}

		return promoResponse;
	}

}
