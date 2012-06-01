package com.expedia.bookings.server;

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicDomainHandler;

import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.Log;

public class ExpediaDomainHandler extends BasicDomainHandler {

	public ExpediaDomainHandler() {
		super();
	}

	@Override
	public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
		super.validate(cookie, origin);
		if (AndroidUtils.isRelease()) {
			// We only care about validating domains for releases so we can
			// keep using the mock proxy
			String domain = cookie.getDomain();
			if (! domain.endsWith(LocaleUtils.getPointOfSale())) {
				String message = "Domain attribute \"" +
					domain +
					"\" not the current point of sale";
				Log.d(message);
				throw new MalformedCookieException(message);
			}
		}
	}

	@Override
	public boolean match(Cookie cookie, CookieOrigin origin) {
		if (cookie == null) {
			throw new IllegalArgumentException("Cookie may not be null");
		}
		if (origin == null) {
			throw new IllegalArgumentException("Cookie origin may not be null");
		}
		String host = origin.getHost();
		String domain = cookie.getDomain();
		if (domain == null) {
			return false;
		}
		return host.endsWith(domain);
	}
}
