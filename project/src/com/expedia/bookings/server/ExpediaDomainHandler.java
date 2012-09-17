package com.expedia.bookings.server;

import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicDomainHandler;

import android.content.Context;

import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class ExpediaDomainHandler extends BasicDomainHandler {
	private Context mContext;

	public ExpediaDomainHandler(Context context) {
		super();
		mContext = context;
	}

	@Override
	public void validate(final Cookie cookie, final CookieOrigin origin) throws MalformedCookieException {
		super.validate(cookie, origin);
		if (AndroidUtils.isRelease(mContext)) {
			// We only care about validating domains for releases so we can
			// keep using the mock proxy
			String domain = cookie.getDomain();
			if (! domain.endsWith(LocaleUtils.getPointOfSale(mContext))) {
				String message = "Domain attribute \"" +
					domain +
					"\" not the current point of sale for cookie: " + cookie.toString();
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
