package com.expedia.bookings.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Traveler;
import com.google.android.gms.wallet.Address;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.ProxyCard;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.Log;

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

	public static Traveler convertToTraveler(MaskedWallet maskedWallet) {
		Traveler traveler = new Traveler();

		Address billingAddress = maskedWallet.getBillingAddress();

		Pair<String, String> name = WalletUtils.splitName(billingAddress.getName());
		traveler.setFirstName(name.first);
		traveler.setLastName(name.second);

		traveler.setEmail(maskedWallet.getEmail());

		traveler.setPhoneCountryCode("1"); // Currently, all wallet accounts are US only
		traveler.setPhoneNumber(billingAddress.getPhoneNumber());

		// Not all home address fields may be available if we are getting minimal billing info back
		Location address = new Location();
		address.addStreetAddressLine(billingAddress.getAddress1());
		address.addStreetAddressLine(billingAddress.getAddress2());
		address.addStreetAddressLine(billingAddress.getAddress3());
		address.setCity(billingAddress.getCity());
		address.setStateCode(billingAddress.getState());
		address.setPostalCode(billingAddress.getPostalCode());
		address.setCountryCode(billingAddress.getCountryCode());
		traveler.setHomeAddress(address);

		return traveler;
	}

	public static String getFormattedPaymentDescription(MaskedWallet maskedWallet) {
		StringBuilder sb = new StringBuilder();
		for (String line : maskedWallet.getPaymentDescriptions()) {
			sb.append(line + "\n");
		}
		return sb.toString().trim();
	}

	/**
	 * Arbitrarily splits the names.  We get back name fields as a single
	 * entity; we try our "best" to split it up, but leave it in a state where
	 * users can correct it if it's wrong.
	 */
	public static Pair<String, String> splitName(String name) {
		int firstSpace = name.indexOf(' ');
		return new Pair<String, String>(name.substring(0, firstSpace), name.substring(firstSpace + 2));
	}

	public static void logWallet(MaskedWallet wallet) {
		Log.d(TAG, "DUMPING MASKED WALLET");
		if (wallet.getBillingAddress() != null) {
			Log.d(TAG, "BillingAddress");
			logAddress(wallet.getBillingAddress());
		}
		if (wallet.getShippingAddress() != null) {
			Log.d(TAG, "ShippingAddress");
			logAddress(wallet.getShippingAddress());
		}
		Log.d(TAG, "Email=" + wallet.getEmail());
		Log.d(TAG, "GoogleTransactionId=" + wallet.getGoogleTransactionId() + ", merchantTransactionId="
				+ wallet.getMerchantTransactionId());

		String[] paymentDescriptions = wallet.getPaymentDescriptions();
		if (paymentDescriptions != null) {
			for (int a = 0; a < paymentDescriptions.length; a++) {
				Log.d(TAG, "payDesc[" + a + "]=" + paymentDescriptions[a]);
			}
		}
	}

	public static void logWallet(FullWallet wallet) {
		Log.d(TAG, "DUMPING FULL WALLET");
		if (wallet.getBillingAddress() != null) {
			Log.d(TAG, "BillingAddress");
			logAddress(wallet.getBillingAddress());
		}
		if (wallet.getShippingAddress() != null) {
			Log.d(TAG, "ShippingAddress");
			logAddress(wallet.getShippingAddress());
		}
		Log.d(TAG, "Email=" + wallet.getEmail());
		Log.d(TAG, "GoogleTransactionId=" + wallet.getGoogleTransactionId() + ", merchantTransactionId="
				+ wallet.getMerchantTransactionId());

		String[] paymentDescriptions = wallet.getPaymentDescriptions();
		if (paymentDescriptions != null) {
			for (int a = 0; a < paymentDescriptions.length; a++) {
				Log.d(TAG, "payDesc[" + a + "]=" + paymentDescriptions[a]);
			}
		}

		ProxyCard proxyCard = wallet.getProxyCard();
		Log.d(TAG, "proxyCard.pan=" + proxyCard.getPan());
		Log.d(TAG, "proxyCard.cvn=" + proxyCard.getCvn());
		Log.d(TAG, "proxyCard.expMonth=" + proxyCard.getExpirationMonth());
		Log.d(TAG, "proxyCard.expYear=" + proxyCard.getExpirationYear());
	}

	public static void logAddress(Address address) {
		if (address != null) {
			logVarIfNonEmpty("companyName", address.getCompanyName());
			logVarIfNonEmpty("name", address.getName());
			logVarIfNonEmpty("address1", address.getAddress1());
			logVarIfNonEmpty("address2", address.getAddress2());
			logVarIfNonEmpty("address3", address.getAddress3());
			logVarIfNonEmpty("city", address.getCity());
			logVarIfNonEmpty("state", address.getState());
			logVarIfNonEmpty("postalCode", address.getPostalCode());
			logVarIfNonEmpty("countryCode", address.getCountryCode());
			logVarIfNonEmpty("phoneNumber", address.getPhoneNumber());

			Log.d("isPostBox=" + address.isPostBox());
		}
	}

	private static void logVarIfNonEmpty(String label, String value) {
		if (!TextUtils.isEmpty(value)) {
			Log.d(TAG, label + "=" + value);
		}
	}

	public static void logError(int errorCode) {
		switch (errorCode) {
		case WalletConstants.ERROR_CODE_BUYER_CANCELLED:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_BUYER_CANCELLED");
			break;
		case WalletConstants.ERROR_CODE_SPENDING_LIMIT_EXCEEDED:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_SPENDING_LIMIT_EXCEEDED");
			break;
		case WalletConstants.ERROR_CODE_INVALID_PARAMETERS:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_INVALID_PARAMETERS");
			break;
		case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_AUTHENTICATION_FAILURE");
			break;
		case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_BUYER_ACCOUNT_ERROR");
			break;
		case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_MERCHANT_ACCOUNT_ERROR");
			break;
		case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_SERVICE_UNAVAILABLE");
			break;
		case WalletConstants.ERROR_CODE_UNSUPPORTED_API_VERSION:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_UNSUPPORTED_API_VERSION");
			break;
		case WalletConstants.ERROR_CODE_UNKNOWN:
			Log.e(WalletUtils.TAG, "Error: ERROR_CODE_UNKNOWN");
			break;
		default:
			Log.e(WalletUtils.TAG, "Unknown error code: " + errorCode);
			break;
		}
	}
}
