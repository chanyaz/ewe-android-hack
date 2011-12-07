package com.expedia.bookings.utils;

import java.util.Currency;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;
import com.mobiata.android.Log;

public class CurrencyUtils {

	private static final String[] AMEX_CURRENCIES = new String[] {
			"CAD",
			"CHF",
			"DKK",
			"EUR",
			"GBP",
			"HKD",
			"NOK",
			"SEK",
			"SGD",
			"USD"
		};

	public static boolean currencySupportedByAmex(Context context, String currencyCode) {
		for (String currency : AMEX_CURRENCIES) {
			if (currency.equals(currencyCode)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Determines the type of credit card based on the card number.
	 * 
	 * Here is the information from Shelli Garcia:
	 * 
	 * CARD                LENGTH           PREFIX
	 * American Express    15               34, 37
	 * Carte Blanche       14               94, 95
	 * China Union Pay     16, 17, 18, 19   62
	 * Diners              14               30, 36, 38, 60
	 * Discover            16               60
	 * JCB                 15, 16           35
	 * Maestro             16, 18, 19       50, 63, 67
	 * MasterCard          16               51, 52, 53, 54, 55
	 * Visa                13, 16           4
	 * 
	 * @param context the context (used to retrieve strings/images)
	 * @param cardNumber the number of the card (as entered so far)
	 * @return the credit card brand if detected, null if not detected
	 */
	public static CreditCardType detectCreditCardBrand(Context context, String cardNumber) {
		int numDigits = cardNumber.length();

		// We don't start trying to detect cards until we have at least 13 digits
		if (numDigits < 13) {
			return null;
		}

		// The two-digit prefix
		int twoDigitPrefix = Integer.parseInt(cardNumber.substring(0, 2));

		// Note: This code could be coded more efficiently with switches.
		// I highly doubt we need that kind of speed here. ~dlew

		// American Express
		if (numDigits == 15 && (twoDigitPrefix == 34 || twoDigitPrefix == 37)) {
			return CreditCardType.AMERICAN_EXPRESS;
		}

		// Carte Blanche
		if (numDigits == 14 && (twoDigitPrefix == 94 || twoDigitPrefix == 95)) {
			return CreditCardType.CARTE_BLANCHE;
		}

		// China Union Pay
		if (numDigits >= 16 && numDigits <= 19 && twoDigitPrefix == 62) {
			return CreditCardType.CHINA_UNION_PAY;
		}

		// Diners
		if (numDigits == 14
				&& (twoDigitPrefix == 30 || twoDigitPrefix == 36 || twoDigitPrefix == 38 || twoDigitPrefix == 60)) {
			return CreditCardType.DINERS_CLUB;
		}

		// Discover
		if (numDigits == 16 && twoDigitPrefix == 60) {
			return CreditCardType.DISCOVER;
		}

		// JCB
		if ((numDigits == 15 || numDigits == 16) && twoDigitPrefix == 35) {
			return CreditCardType.JAPAN_CREDIT_BUREAU;
		}

		// Maestro
		if ((numDigits == 16 || numDigits == 18 || numDigits == 19)
				&& (twoDigitPrefix == 50 || twoDigitPrefix == 63 || twoDigitPrefix == 67)) {
			return CreditCardType.MAESTRO;
		}

		// MasterCard
		if (numDigits == 16 && twoDigitPrefix >= 51 && twoDigitPrefix <= 55) {
			return CreditCardType.MASTERCARD;
		}

		// Visa
		if ((numDigits == 13 || numDigits == 16) && twoDigitPrefix / 10 == 4) {
			return CreditCardType.VISA;
		}

		// Didn't find a valid card type, return null
		return null;
	}
}
