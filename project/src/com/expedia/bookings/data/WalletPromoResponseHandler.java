package com.expedia.bookings.data;

import org.json.JSONObject;

import com.mobiata.android.net.JsonResponseHandler;

public class WalletPromoResponseHandler extends JsonResponseHandler<WalletPromoResponse> {

	@Override
	public WalletPromoResponse handleJson(JSONObject response) {
		WalletPromoResponse promoResponse = new WalletPromoResponse();

		if (response != null) {
			promoResponse.setEnabled(response.optBoolean("walletPromotionEnabled", false));
		}

		return promoResponse;
	}

}
