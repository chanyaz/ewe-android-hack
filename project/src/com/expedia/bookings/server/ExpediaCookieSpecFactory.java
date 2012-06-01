package com.expedia.bookings.server;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.params.HttpParams;

import android.content.Context;

public class ExpediaCookieSpecFactory implements CookieSpecFactory {
	private Context mContext;

	ExpediaCookieSpecFactory(Context context) {
		mContext = context;
	}

	public CookieSpec newInstance(final HttpParams params) {
		if (params != null) {
			return new ExpediaCookieSpec(mContext,
					(String []) params.getParameter(CookieSpecPNames.DATE_PATTERNS));
		} else {
			return new ExpediaCookieSpec(mContext);
		}
	}
}
