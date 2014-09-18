package com.expedia.bookings.server;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.expedia.bookings.data.SamsungWalletResponse;
import com.expedia.bookings.data.ServerError;
import com.mobiata.android.Log;

public class SamsungWalletResponseHandler extends JsonResponseHandler<SamsungWalletResponse> {

	private Context mContext;

	public SamsungWalletResponseHandler(Context context) {
		mContext = context;
	}

	@Override
	public SamsungWalletResponse handleJson(JSONObject response) {
		SamsungWalletResponse walletResponse = new SamsungWalletResponse();
		try {
			List<ServerError> errors = ParserUtils.parseErrors(mContext, ServerError.ApiMethod.SAMSUNG_WALLET, response);
			List<ServerError> warnings = ParserUtils.parseWarnings(mContext, ServerError.ApiMethod.SAMSUNG_WALLET, response);
			List<ServerError> allErrors = new ArrayList<ServerError>();
			if (errors != null) {
				allErrors.addAll(errors);
			}
			if (warnings != null) {
				allErrors.addAll(warnings);
			}
			walletResponse.addErrors(allErrors);

			if (walletResponse.hasErrors()) {
				return walletResponse;
			}

			walletResponse.setTicketId(response.optString("ticketId", null));

		}
		catch (JSONException e) {
			Log.e("Could not parse JSON SamsungWallet response.", e);
			return null;
		}

		return walletResponse;
	}
}
