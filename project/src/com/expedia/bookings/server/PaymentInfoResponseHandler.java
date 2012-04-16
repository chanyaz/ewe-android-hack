package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.expedia.bookings.data.CreditCardBrand;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.net.JsonResponseHandler;

public class PaymentInfoResponseHandler extends JsonResponseHandler<List<CreditCardBrand>> {

	@Override
	public List<CreditCardBrand> handleJson(JSONObject response) {
		List<CreditCardBrand> creditCards = new ArrayList<CreditCardBrand>();
		try {
			JSONObject jsonResponse = response.getJSONObject("body").getJSONObject("HotelPaymentResponse");

			// Check for errors, return if found
			if (jsonResponse.has("EanWsError")) {
				return null;
			}

			JSONArray paymentTypes = jsonResponse.getJSONArray("PaymentType");
			int len = paymentTypes.length();
			for (int a = 0; a < len; a++) {
				JSONObject paymentType = paymentTypes.getJSONObject(a);
				CreditCardBrand brand = new CreditCardBrand();
				brand.setCode(paymentType.getString("code"));
				brand.setName(JSONUtils.getNormalizedString(paymentType, "name"));
				creditCards.add(brand);
			}
		}
		catch (JSONException e) {
			Log.e("Could not parse JSON payment info response.", e);
			return null;
		}

		return creditCards;
	}
}
