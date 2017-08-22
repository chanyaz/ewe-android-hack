package com.expedia.bookings.enums;

public enum MerchandiseSpam {
	/*The guest user is always opted into promo emails */
	ALWAYS,
	/*The guest must consent via some UI element (a checkout box) to opt into promo emails. The checkbox is unchecked by default */
	CONSENT_TO_OPT_IN,
	/*The guest will be opted in unless they consent to OPT OUT via a checkbox. The checkbox is unchecked by default */
	CONSENT_TO_OPT_OUT,
	/*The guest must consent via some UI element (a checkout box) to opt into promo emails. The checkbox is checked by default*/
	CONSENT_TO_OPT_IN_SELECTED,
	/*The guest will be opted in unless they consent to OPT OUT via a checkbox. The checkbox is checked by default*/
	CONSENT_TO_OPT_OUT_SELECTED,
	/*For point of sale that's not relevant and for signed in users. */
	NOT_APPLICABLE
}
