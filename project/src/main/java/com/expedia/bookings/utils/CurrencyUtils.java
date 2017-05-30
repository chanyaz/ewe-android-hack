package com.expedia.bookings.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.data.PaymentType;
import com.expedia.bookings.data.payment.ProgramName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mobiata.android.Log;

public class CurrencyUtils {
	private final static String mPath = "currency/currency.json";
	private static Map<String, String> mCurrencyMap;

	/**
	 * Determines the type of credit card based on the card number.
	 * <p/>
	 * Here is the information from Shelli Garcia:
	 * <p/>
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
	 * @param cardNumber the number of the card (as entered so far)
	 * @return the credit card brand if detected, null if not detected
	 */
	public static PaymentType detectCreditCardBrand(String cardNumber) {
		//If we dont have any input, we dont get any output
		if (Strings.isEmpty(cardNumber)) {
			return null;
		}

		// We don't start trying to detect cards until we have at least 13 digits
		int numDigits = cardNumber.length();
		if (numDigits < 13) {
			return null;
		}

		// The two-digit prefix
		int twoDigitPrefix = Integer.parseInt(cardNumber.substring(0, 2));

		// Note: This code could be coded more efficiently with switches.
		// I highly doubt we need that kind of speed here. ~dlew

		// American Express
		if (numDigits == 15 && (twoDigitPrefix == 34 || twoDigitPrefix == 37)) {
			return PaymentType.CARD_AMERICAN_EXPRESS;
		}

		// Carte Blanche
		if (numDigits == 14 && (twoDigitPrefix == 94 || twoDigitPrefix == 95)) {
			return PaymentType.CARD_CARTE_BLANCHE;
		}

		// China Union Pay
		if (numDigits >= 16 && numDigits <= 19 && twoDigitPrefix == 62) {
			return PaymentType.CARD_CHINA_UNION_PAY;
		}

		// Diners
		if (numDigits == 14
			&& (twoDigitPrefix == 30 || twoDigitPrefix == 36 || twoDigitPrefix == 38 || twoDigitPrefix == 60)) {
			return PaymentType.CARD_DINERS_CLUB;
		}

		// Discover
		if (numDigits == 16 && twoDigitPrefix == 60) {
			return PaymentType.CARD_DISCOVER;
		}

		// JCB
		if ((numDigits == 15 || numDigits == 16) && twoDigitPrefix == 35) {
			return PaymentType.CARD_JAPAN_CREDIT_BUREAU;
		}

		// Maestro
		if ((numDigits == 16 || numDigits == 18 || numDigits == 19)
			&& (twoDigitPrefix == 50 || twoDigitPrefix == 63 || twoDigitPrefix == 67)) {
			return PaymentType.CARD_MAESTRO;
		}

		// MasterCard
		if (numDigits == 16 && twoDigitPrefix >= 51 && twoDigitPrefix <= 55) {
			return PaymentType.CARD_MASTERCARD;
		}

		// Visa
		if ((numDigits == 13 || numDigits == 16) && twoDigitPrefix / 10 == 4) {
			return PaymentType.CARD_VISA;
		}

		// Didn't find a valid card type, return null
		return null;
	}

	public static PaymentType parsePaymentType(String type) {
		// Code lovingly stolen from iOS, where they note that these
		// values are not yet verified from the API folks.
		if (type.contains("AmericanExpress")) {
			return PaymentType.CARD_AMERICAN_EXPRESS;
		}
		else if (type.contains("CarteBlanche")) {
			return PaymentType.CARD_CARTE_BLANCHE;
		}
		else if (type.contains("ChinaUnionPay")) {
			return PaymentType.CARD_CHINA_UNION_PAY;
		}
		else if (type.contains("Diner")) {
			return PaymentType.CARD_DINERS_CLUB;
		}
		else if (type.contains("Discover")) {
			return PaymentType.CARD_DISCOVER;
		}
		else if (type.contains("JCB")) {
			return PaymentType.CARD_JAPAN_CREDIT_BUREAU;
		}
		else if (type.contains("Maestro")) {
			return PaymentType.CARD_MAESTRO;
		}
		else if (type.contains("MasterCard")) {
			return PaymentType.CARD_MASTERCARD;
		}
		else if (type.contains("Visa")) {
			return PaymentType.CARD_VISA;
		}
		else if (type.contains("CarteBleue")) {
			return PaymentType.CARD_CARTE_BLEUE;
		}
		else if (type.contains("CarteSi")) {
			return PaymentType.CARD_CARTA_SI;
		}
		else if (ProgramName.Companion.valueOf(type) != null) {
			return PaymentType.POINTS_REWARDS;
		}
		else {
			Log.w("Tried to parse an unknown credit card type, name=" + type);
			return PaymentType.UNKNOWN;
		}
	}

	public static void initMap(Context c) {
		Map<String, String> map = new HashMap<String, String>();
		try {
			InputStream is = c.getAssets().open(mPath);
			Reader reader = new InputStreamReader(is);
			Type mapType = new TypeToken<Map<String, String>>() {
			}.getType();
			map = new Gson().fromJson(reader, mapType);
		}
		catch (IOException ex) {
			Log.d("Currency Utils Error: " + ex);
		}
		mCurrencyMap = map;
	}

	//Takes a three letter country code
	public static String currencyForLocale(String code) {
		return mCurrencyMap.get(code);
	}

}

