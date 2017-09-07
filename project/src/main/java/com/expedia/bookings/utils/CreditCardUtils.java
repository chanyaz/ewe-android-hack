package com.expedia.bookings.utils;

import android.annotation.SuppressLint;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.PaymentType;

public class CreditCardUtils {

	/**
	 * Get human readable name of the card type. e.g. "Master Card"
	 *
	 * @param context
	 * @return
	 */
	public static String getHumanReadableName(Context context, PaymentType type) {
		return getHumanReadableCardTypeName(context, type);
	}

	/**
	 * Need to display a card type name? Use this method.
	 * <p/>
	 * Payment.CARD_MASTERCARD -> Master Card
	 *
	 * @param context
	 * @param cardType
	 * @return Human readable representation of cardType
	 */
	@SuppressLint("DefaultLocale")
	public static String getHumanReadableCardTypeName(Context context, PaymentType cardType) {
		cardType.assertIsCard();
		switch (cardType) {
		case CARD_AMERICAN_EXPRESS:
			return context.getString(R.string.cc_american_express);
		case CARD_CARTE_BLANCHE:
			return context.getString(R.string.cc_carte_blanche);
		case CARD_CHINA_UNION_PAY:
			return context.getString(R.string.cc_china_union_pay);
		case CARD_DINERS_CLUB:
			return context.getString(R.string.cc_diners_club);
		case CARD_DISCOVER:
			return context.getString(R.string.cc_discover);
		case CARD_JAPAN_CREDIT_BUREAU:
			return context.getString(R.string.cc_japan_credit_bureau);
		case CARD_MAESTRO:
			return context.getString(R.string.cc_maestro);
		case CARD_MASTERCARD:
			return context.getString(R.string.cc_master_card);
		case CARD_VISA:
			return context.getString(R.string.cc_visa);
		case CARD_CARTE_BLEUE:
			return context.getString(R.string.cc_carte_bleue);
		case CARD_CARTA_SI:
			return context.getString(R.string.cc_carta_si);
		case CARD_UNKNOWN:
			return context.getString(R.string.unknown_card);
		default:
			// If all else fails, just return the enum
			return cardType.toString();
		}
	}
}

