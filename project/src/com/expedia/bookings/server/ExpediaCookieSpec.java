package com.expedia.bookings.server;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.NetscapeDraftSpec;
import org.apache.http.HeaderElement;
import org.apache.http.cookie.ClientCookie;

import android.content.Context;

import com.mobiata.android.Log;

class ExpediaCookieSpec extends NetscapeDraftSpec {
	ExpediaCookieSpec(Context context, final String[] datePatterns) {
		super(datePatterns);
		registerAttribHandler(ClientCookie.DOMAIN_ATTR, new ExpediaDomainHandler(context));
	}

	ExpediaCookieSpec(Context context) {
		this(context, null);
	}
}

