package com.expedia.bookings.data;

import android.annotation.SuppressLint;
import android.content.Context;

import com.expedia.bookings.R;

/**
 * Enumeration of known credit card types.  This data only applies to Expedia
 * dealings, as it references codes that Expedia uses (not necessarily the same
 * as what others use, e.g. TravelCLICK).
 */
public enum CreditCardType {
	AMERICAN_EXPRESS("AX"),
	CARTE_BLANCHE("CB"),
	CHINA_UNION_PAY("CU"),
	DINERS_CLUB("DC"),
	DISCOVER("DS"),
	JAPAN_CREDIT_BUREAU("JC"),
	MAESTRO("TO"),
	MASTERCARD("CA"),
	VISA("VI"),
	GOOGLE_WALLET("GOOG"),
	CARTE_BLEUE("R"),
	CARTA_SI("T"),
	UNKNOWN("?");

	private String mCode;

	private CreditCardType(String code) {
		mCode = code;
	}

	public String getCode() {
		return mCode;
	}

	/**
	 * Get human readable name of the card type. e.g. "Master Card"
	 *
	 * @param context
	 * @return
	 */
	public String getHumanReadableName(Context context) {
		return CreditCardType.getHumanReadableCardTypeName(context, this);
	}

	/**
	 * Need to display a card type name? Use this method.
	 *
	 * CreditCardType.MASTERCARD -> Master Card
	 *
	 * @param context
	 * @param cardType
	 * @return Human readable representation of cardType
	 */
	@SuppressLint("DefaultLocale")
	public static String getHumanReadableCardTypeName(Context context, CreditCardType cardType) {
		switch (cardType) {
		case AMERICAN_EXPRESS:
			return context.getString(R.string.cc_american_express);
		case CARTE_BLANCHE:
			return context.getString(R.string.cc_carte_blanche);
		case CHINA_UNION_PAY:
			return context.getString(R.string.cc_china_union_pay);
		case DINERS_CLUB:
			return context.getString(R.string.cc_diners_club);
		case DISCOVER:
			return context.getString(R.string.cc_discover);
		case JAPAN_CREDIT_BUREAU:
			return context.getString(R.string.cc_japan_credit_bureau);
		case MAESTRO:
			return context.getString(R.string.cc_maestro);
		case MASTERCARD:
			return context.getString(R.string.cc_master_card);
		case VISA:
			return context.getString(R.string.cc_visa);
		case GOOGLE_WALLET:
			return context.getString(R.string.google_wallet);
		case CARTE_BLEUE:
			return context.getString(R.string.cc_carte_bleue);
		case CARTA_SI:
			return context.getString(R.string.cc_carta_si);
		default:
			// If all else fails, just return the enum
			return cardType.toString();
		}
	}
}
