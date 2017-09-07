package com.expedia.bookings.data;

/**
 * Enumeration of known Payment types.  This data only applies to Expedia
 * dealings, as it references codes that Expedia uses (not necessarily the same
 * as what others use, e.g. TravelCLICK).
 */
public enum PaymentType {
	CARD_AMERICAN_EXPRESS("AX", "AmericanExpress"),
	CARD_CARTE_BLANCHE("CB", "CarteBlanche"),
	CARD_CHINA_UNION_PAY("CU", "ChinaUnionPay"),
	CARD_DINERS_CLUB("DC", "DinersClub"),
	CARD_DISCOVER("DS", "Discover"),
	CARD_JAPAN_CREDIT_BUREAU("JC", "JapanCreditBureau"),
	CARD_MAESTRO("TO", "Maestro"),
	CARD_MASTERCARD("CA", "Mastercard"),
	CARD_VISA("VI", "Visa"),
	CARD_CARTE_BLEUE("R", "CarteBleue"),
	CARD_CARTA_SI("T", "CartaSi"),

	WALLET_GOOGLE("GOOG", "GoogleWallet"),
	
	POINTS_REWARDS("ER", "Rewards"),
	CARD_UNKNOWN("?", "Unknown");

	private String mCode;
	private String mOmnitureTrackingCode;

	PaymentType(String code, String omnitureTrackingCode) {
		mCode = code;
		mOmnitureTrackingCode = omnitureTrackingCode;
	}

	public String getCode() {
		return mCode;
	}

	public String getOmnitureTrackingCode() {
		return mOmnitureTrackingCode;
	}

	public boolean isCard() {
		return name().startsWith("CARD_");
	}

	public boolean isPoints() {
		return name().startsWith("POINTS_");
	}

	public void assertIsCard() {
		if (!isCard()) {
			throw new UnsupportedOperationException("Can't use payment type " + name());
		}
	}

	public void assertIsPoints() {
		if (!isPoints()) {
			throw new UnsupportedOperationException("Can't use payment type " + name());
		}
	}

	public void assertIsCardOrPoints() {
		if (!isCard() && !isPoints()) {
			throw new UnsupportedOperationException("Can't use payment type " + name());
		}
	}
}
