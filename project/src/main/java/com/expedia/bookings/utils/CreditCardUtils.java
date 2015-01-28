package com.expedia.bookings.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;

public class CreditCardUtils {

	/**
	 * Get human readable name of the card type. e.g. "Master Card"
	 *
	 * @param context
	 * @return
	 */
	public static String getHumanReadableName(Context context, CreditCardType type) {
		return getHumanReadableCardTypeName(context, type);
	}

	/**
	 * Need to display a card type name? Use this method.
	 * <p/>
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
