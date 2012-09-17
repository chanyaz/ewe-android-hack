package com.expedia.bookings.server;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.impl.cookie.NetscapeDraftSpec;

import android.content.Context;

class ExpediaCookieSpec extends NetscapeDraftSpec {
	ExpediaCookieSpec(Context context, final String[] datePatterns) {
		super(datePatterns);
		registerAttribHandler(ClientCookie.DOMAIN_ATTR, new ExpediaDomainHandler(context));
	}

	ExpediaCookieSpec(Context context) {
		this(context, null);
	}
}

