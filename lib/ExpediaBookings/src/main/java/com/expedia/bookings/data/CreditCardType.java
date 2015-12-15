package com.expedia.bookings.data;

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
	EXPEDIA_REWARDS("ER"),
	UNKNOWN("?");

	private String mCode;

	private CreditCardType(String code) {
		mCode = code;
	}

	public String getCode() {
		return mCode;
	}

}
