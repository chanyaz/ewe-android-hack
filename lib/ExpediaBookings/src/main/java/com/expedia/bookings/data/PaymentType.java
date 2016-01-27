package com.expedia.bookings.data;

import com.expedia.bookings.utils.Strings;

/**
 * Enumeration of known Payment types.  This data only applies to Expedia
 * dealings, as it references codes that Expedia uses (not necessarily the same
 * as what others use, e.g. TravelCLICK).
 */
public enum PaymentType {
	CARD_AMERICAN_EXPRESS("AX", "AMERICAN_EXPRESS"),
	CARD_CARTE_BLANCHE("CB", "CARTE_BLANCHE"),
	CARD_CHINA_UNION_PAY("CU", "CHINA_UNION_PAY"),
	CARD_DINERS_CLUB("DC", "DINERS_CLUB"),
	CARD_DISCOVER("DS", "DISCOVER"),
	CARD_JAPAN_CREDIT_BUREAU("JC", "JAPAN_CREDIT_BUREAU"),
	CARD_MAESTRO("TO", "MAESTRO"),
	CARD_MASTERCARD("CA", "MASTERCARD"),
	CARD_VISA("VI", "VISA"),
	CARD_CARTE_BLEUE("R", "CARTE_BLEUE"),
	CARD_CARTA_SI("T", "CARTA_SI"),

	WALLET_GOOGLE("GOOG", "GOOGLE_WALLET"),
	
	POINTS_EXPEDIA_REWARDS("ER", "EXPEDIA_REWARDS"),
	UNKNOWN("?", "UNKNOWN");

	private String mCode;
	private String mOmnitureTrackingCode;

	private PaymentType(String code, String omnitureTrackingCode) {
		mCode = code;
		mOmnitureTrackingCode = omnitureTrackingCode;
	}

	public String getCode() {
		return mCode;
	}

	public String getOmnitureTrackingCode() {
		return (this != PaymentType.UNKNOWN)
				? Strings.capitalizeFirstLetter(mOmnitureTrackingCode)
				: "no card required";
	}
}
