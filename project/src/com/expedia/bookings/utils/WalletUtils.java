package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.ServerError.ErrorCode;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.EndPoint;
import com.google.android.gms.wallet.Address;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.LineItem;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.NotifyTransactionStatusRequest;
import com.google.android.gms.wallet.ProxyCard;
import com.google.android.gms.wallet.WalletConstants;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * Utilities shared across our multiple Google Wallet instances.
 */
public class WalletUtils {

	public static final String TAG = "GoogleWallet";

	/**
	 * The upper limit on transactions from Google Wallet.  If the charge exceeds this,
	 * then we should not show any Google Wallet options at all.
	 */
	public static final int MAX_TRANSACTION_CHARGE = 1800;

	public static final String EXTRA_MASKED_WALLET = "EXTRA_MASKED_WALLET";
	public static final String EXTRA_FULL_WALLET = "EXTRA_FULL_WALLET";

	public static final String SETTING_SHOW_WALLET_COUPON = "com.expedia.bookings.wallet.coupon.2013.enabled";

	private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("0.00");

	// Force the separator to be '.', since that's the format that Google Wallet requires
	static {
		DecimalFormatSymbols symbols = MONEY_FORMAT.getDecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		MONEY_FORMAT.setDecimalFormatSymbols(symbols);
	}

	/**
	 * Returns the Google Wallet environment.
	 *
	 * The GW production environment only works on release builds, so we're
	 * making it such that it's always prod when we're on a release build
	 * (and sandbox otherwise).
	 */
	public static int getWalletEnvironment(Context context) {
		if (AndroidUtils.isRelease(context)) {
			Log.v("Using Google Wallet environment: PRODUCTION");
			return WalletConstants.ENVIRONMENT_PRODUCTION;
		}

		Log.v("Using Google Wallet environment: SANDBOX");
		return WalletConstants.ENVIRONMENT_SANDBOX;
	}

	/**
	 * In some cases we can't offer google wallet (e.g., it goes over the transaction limit).
	 */
	public static boolean offerGoogleWallet(Money total) {
		return total.getAmount().compareTo(new BigDecimal(MAX_TRANSACTION_CHARGE)) < 0;
	}

	public static boolean offerGoogleWalletCoupon(Context context) {
		return SettingUtils.get(context, SETTING_SHOW_WALLET_COUPON, false)
				&& Db.getHotelSearch().getSelectedProperty().isMerchant();
	}

	public static String getWalletCouponCode(Context context) {
		if (ExpediaServices.getEndPoint(context) == EndPoint.PRODUCTION) {
			// This is the official coupon code for Wallet on Prod
			return "MOBILEWALLET";
		}
		else {
			// This code is known to give 10% off on integration; may not work on other environments
			return "hotelsapp2";
		}
	}

	public static void addStandardFieldsToMaskedWalletRequest(Context context, MaskedWalletRequest.Builder builder,
			Money total) {
		builder.setMerchantName(context.getString(R.string.merchant_name));
		builder.setCurrencyCode(total.getCurrency());
		builder.setEstimatedTotalPrice(formatAmount(total));
	}

	public static void bindWalletToBillingInfo(MaskedWallet wallet, BillingInfo billingInfo) {
		Log.d(TAG, "Binding MASKED wallet data to billing info...");

		billingInfo.setStoredCard(WalletUtils.convertToStoredCreditCard(wallet));

		// With a masked wallet, we actually explicitly *clear* some data from the BillingInfo
		// The reason why we do this is so that the app does not simultaneously think that we
		// have some half-filled BillingInfo in addition to a stored credit card
		billingInfo.setLocation(null);
		billingInfo.setNumber(null);
	}

	public static void bindWalletToBillingInfo(FullWallet wallet, BillingInfo billingInfo) {
		Log.d(TAG, "Binding FULL wallet data to billing info...");
		billingInfo.setLocation(convertAddressToLocation(wallet.getBillingAddress()));

		ProxyCard proxyCard = wallet.getProxyCard();
		billingInfo.setNumber(proxyCard.getPan());
		billingInfo.setSecurityCode(proxyCard.getCvn());
		billingInfo.setExpirationDate(new LocalDate(proxyCard.getExpirationYear(), proxyCard.getExpirationMonth(), 1));
		billingInfo.setNameOnCard(wallet.getBillingAddress().getName());
	}

	// Unbind just the data that is added via the full wallet; this is important
	// to do after the user books (as we do not want the
	public static void unbindFullWalletDataFromBillingInfo(BillingInfo billingInfo) {
		Log.d(TAG, "Unbinding full wallet data from billing info...");

		billingInfo.setNumber(null);
		billingInfo.setSecurityCode(null);
		billingInfo.setLocation(null);
		billingInfo.setNameOnCard(null);
		billingInfo.setExpirationDate(null);
	}

	// If something goes wrong, we actually *don't* want GoogleWallet in the billing info anymore
	// so clear it out.
	public static void unbindAllWalletDataFromBillingInfo(BillingInfo billingInfo) {
		Log.d(TAG, "Unbinding ALL wallet data from billing info...");

		unbindFullWalletDataFromBillingInfo(billingInfo);

		StoredCreditCard scc = billingInfo.getStoredCard();
		if (scc != null && scc.isGoogleWallet()) {
			billingInfo.setStoredCard(null);
		}
	}

	public static Traveler addWalletAsTraveler(Context context, MaskedWallet maskedWallet) {
		Traveler gwTraveler = WalletUtils.convertToTraveler(maskedWallet);

		// Only add the traveler if we don't have someone with the same name already
		//
		// Alternatively, replace the current traveler if it is a wallet traveler
		List<Traveler> currTravelers = BookingInfoUtils.getAlternativeTravelers(context);
		currTravelers.addAll(Db.getTravelers());
		for (Traveler traveler : currTravelers) {
			if (!traveler.fromGoogleWallet() && traveler.compareNameTo(gwTraveler) == 0) {
				return null;
			}
		}

		Db.setGoogleWalletTraveler(gwTraveler);
		return gwTraveler;
	}

	public static Traveler convertToTraveler(MaskedWallet maskedWallet) {
		Traveler traveler = new Traveler();

		traveler.setFromGoogleWallet(true);

		Address billingAddress = maskedWallet.getBillingAddress();

		String fullName = billingAddress.getName();
		String[] name = WalletUtils.splitName(fullName);
		traveler.setFirstName(name[0]);
		traveler.setMiddleName(name[1]);
		traveler.setLastName(name[2]);

		traveler.setEmail(maskedWallet.getEmail());

		traveler.setPhoneCountryCode("1"); // Currently, all wallet accounts are US only
		traveler.setPhoneNumber(billingAddress.getPhoneNumber());

		traveler.setHomeAddress(convertAddressToLocation(billingAddress));

		// The app reacts badly if this is null, set it to blank
		traveler.setRedressNumber("");

		return traveler;
	}

	public static StoredCreditCard convertToStoredCreditCard(MaskedWallet maskedWallet) {
		StoredCreditCard scc = new StoredCreditCard();
		scc.setDescription(getFormattedPaymentDescription(maskedWallet));
		scc.setId(maskedWallet.getGoogleTransactionId()); // For now, set ID == google transaction id
		scc.setIsGoogleWallet(true);
		return scc;
	}

	// Not all home address fields may be available if we are getting minimal billing info back
	public static Location convertAddressToLocation(Address address) {
		Location location = new Location();
		location.addStreetAddressLine(address.getAddress1());
		location.addStreetAddressLine(address.getAddress2());
		location.addStreetAddressLine(address.getAddress3());
		location.setCity(address.getCity());
		location.setStateCode(address.getState());
		location.setPostalCode(address.getPostalCode());
		location.setCountryCode(address.getCountryCode());
		return location;
	}

	public static String formatAmount(Money money) {
		return MONEY_FORMAT.format(money.getAmount());
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
	 *
	 * @return a String array of length 3 (first/middle/last)
	 */
	public static String[] splitName(String name) {
		String[] split = name.trim().split("\\W+");
		String[] ret = new String[] { "", "", "" };

		ret[0] = split[0];
		if (split.length == 2) {
			ret[2] = split[1];
		}
		else if (split.length > 2) {
			// Assume that everything after the first two spaces is last name
			ret[1] = split[1];
			ret[2] = TextUtils.join(" ", Arrays.copyOfRange(split, 2, split.length));
		}

		return ret;
	}

	/**
	 * Returns the NotifyTransactionStatusRequest.Status for the response, or
	 * 0 if there is no reason to notify
	 *
	 * In general, we don't notify if we don't know what's gone wrong; we should
	 * only notify Google Wallet of transaction issues if our payment processor
	 * hated what they gave us.
	 */
	public static int getStatus(Response response) {
		if (response == null) {
			return 0;
		}
		else if (response.hasErrors()) {
			// We only examine the first error for the most core reason it failed
			ServerError error = response.getErrors().get(0);
			ErrorCode errorCode = error.getErrorCode();

			if (errorCode == ErrorCode.INVALID_INPUT) {
				String field = error.getExtra("field");

				if (!TextUtils.isEmpty(field)) {
					if ("creditCardNumber".equals(field) || "expirationDate".equals(field)) {
						return NotifyTransactionStatusRequest.Status.Error.BAD_CARD;
					}
					else if ("cvv".equals(field)) {
						return NotifyTransactionStatusRequest.Status.Error.BAD_CVC;
					}
				}
			}
			else if (errorCode == ErrorCode.BOOKING_FAILED
					&& error.getDiagnosticFullText().contains("INVALID_CCNUMBER")) {
				return NotifyTransactionStatusRequest.Status.Error.BAD_CARD;
			}
			else if (errorCode == ErrorCode.TRIP_ALREADY_BOOKED) {
				// If the trip was already booked, consider it a massive success!
				return NotifyTransactionStatusRequest.Status.SUCCESS;
			}

			// If we got here, we don't know why it failed, so don't notify Google
			return 0;
		}
		else {
			return NotifyTransactionStatusRequest.Status.SUCCESS;
		}
	}

	public static boolean tryToCreateCvvChallenge(Context context) {
		return !AndroidUtils.isRelease(context)
				&& SettingUtils.get(context, context.getString(R.string.preference_google_wallet_cvv_challenge), false);
	}

	public static FullWalletRequest buildCvvChallengeRequest(String googleWalletTransactionId) {
		// To create a CVV challenge, we need to do two things:
		// 1. Greatly increase the price (but not above the limit)
		// 2. Add the cvv_challenge line item

		FullWalletRequest.Builder walletRequestBuilder = FullWalletRequest.newBuilder();
		walletRequestBuilder.setGoogleTransactionId(googleWalletTransactionId);

		String currency = "USD";
		String price = Integer.toString(MAX_TRANSACTION_CHARGE);

		Cart.Builder cartBuilder = Cart.newBuilder();
		cartBuilder.setCurrencyCode(currency);
		cartBuilder.setTotalPrice(price);

		LineItem.Builder cvvChallengeBuilder = LineItem.newBuilder();
		cvvChallengeBuilder.setCurrencyCode(currency);
		cvvChallengeBuilder.setDescription("cvv_challenge");
		cvvChallengeBuilder.setRole(LineItem.Role.REGULAR);
		cvvChallengeBuilder.setTotalPrice(price);
		cartBuilder.addLineItem(cvvChallengeBuilder.build());

		walletRequestBuilder.setCart(cartBuilder.build());
		return walletRequestBuilder.build();
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

	public static LineItem createLineItem(Money money, String description, int role) {
		return (LineItem.newBuilder())
				.setCurrencyCode(money.getCurrency())
				.setDescription(description)
				.setRole(LineItem.Role.REGULAR)
				.setTotalPrice(WalletUtils.formatAmount(money))
				.build();
	}

	public static Cart buildHotelCart(Context context) {
		HotelSearch search = Db.getHotelSearch();

		Property property = search.getSelectedProperty();
		Rate originalRate = search.getSelectedRate();
		Rate couponRate = search.getCouponRate();
		Money total = couponRate == null ? originalRate.getDisplayTotalPrice() : couponRate.getDisplayTotalPrice();

		Cart.Builder cartBuilder = Cart.newBuilder();

		// Total
		cartBuilder.setCurrencyCode(total.getCurrency());
		cartBuilder.setTotalPrice(WalletUtils.formatAmount(total));

		// Base rate
		cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getNightlyRateTotal(), property.getName(),
				LineItem.Role.REGULAR));

		// Discount
		if (couponRate != null) {
			Money discount = new Money(couponRate.getTotalPriceAdjustments());
			discount.negate();
			cartBuilder.addLineItem(WalletUtils.createLineItem(discount, property.getName(), LineItem.Role.REGULAR));
		}

		// Taxes & Fees
		if (originalRate.getTotalSurcharge() != null && !originalRate.getTotalSurcharge().isZero()) {
			cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getTotalSurcharge(),
					context.getString(R.string.taxes_and_fees), LineItem.Role.TAX));
		}

		// Extra guest fees
		if (originalRate.getExtraGuestFee() != null && !originalRate.getExtraGuestFee().isZero()) {
			cartBuilder.addLineItem(WalletUtils.createLineItem(originalRate.getExtraGuestFee(),
					context.getString(R.string.extra_guest_charge), LineItem.Role.TAX));
		}

		return cartBuilder.build();
	}
}
