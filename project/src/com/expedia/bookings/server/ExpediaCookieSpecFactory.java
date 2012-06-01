package com.expedia.bookings.server;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.params.HttpParams;

public class ExpediaCookieSpecFactory implements CookieSpecFactory {
	public CookieSpec newInstance(final HttpParams params) {
		if (params != null) {
			return new ExpediaCookieSpec(
					(String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS));
		} else {
			return new ExpediaCookieSpec();
		}
	}
}
