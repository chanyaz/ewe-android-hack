package com.expedia.bookings.data;

/**
 * Enumeration of known Payment types.  This data only applies to Expedia
 * dealings, as it references codes that Expedia uses (not necessarily the same
 * as what others use, e.g. TravelCLICK).
 */
public enum PaymentType {
	CARD_AMERICAN_EXPRESS("AX"),
	CARD_CARTE_BLANCHE("CB"),
	CARD_CHINA_UNION_PAY("CU"),
	CARD_DINERS_CLUB("DC"),
	CARD_DISCOVER("DS"),
	CARD_JAPAN_CREDIT_BUREAU("JC"),
	CARD_MAESTRO("TO"),
	CARD_MASTERCARD("CA"),
	CARD_VISA("VI"),
	CARD_CARTE_BLEUE("R"),
	CARD_CARTA_SI("T"),

	WALLET_GOOGLE("GOOG"),
	
	POINTS_EXPEDIA_REWARDS("ER"),
	UNKNOWN("?");

	private String mCode;

	private PaymentType(String code) {
		mCode = code;
	}

	public String getCode() {
		return mCode;
	}

}
