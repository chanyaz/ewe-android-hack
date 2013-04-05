package com.expedia.bookings.utils;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.WalletConstants;

/**
 * Utilities shared across our multiple Google Wallet instances.
 */
public class WalletUtils {

	public static final String TAG = "GoogleWallet";

	public static final String EXTRA_MASKED_WALLET = "EXTRA_MASKED_WALLET";
	public static final String EXTRA_FULL_WALLET = "EXTRA_FULL_WALLET";

	// TODO: This is currently always set the sandbox, but we will
	// eventually want to make this more dynamic.
	public static int getWalletEnvironment() {
		return WalletConstants.ENVIRONMENT_SANDBOX;
	}

	public static MaskedWalletRequest buildMaskedWalletRequest(Context context, Money total) {
		MaskedWalletRequest.Builder builder = MaskedWalletRequest.newBuilder();
		builder.setMerchantName(context.getString(R.string.merchant_name));
		builder.setPhoneNumberRequired(true);
		builder.setUseMinimalBillingAddress(true);
		builder.setCurrencyCode(total.getCurrency());
		builder.setEstimatedTotalPrice(total.getAmount().toPlainString());
		return builder.build();
	}
}
