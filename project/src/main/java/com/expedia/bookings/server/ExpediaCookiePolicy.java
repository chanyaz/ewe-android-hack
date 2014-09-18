package com.expedia.bookings.server;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

import android.content.Context;

import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class ExpediaCookiePolicy implements CookiePolicy {
	private boolean mIsRelease = false;

	public void updateSettings(Context context) {
		mIsRelease = AndroidUtils.isRelease(context);
	}

	@Override
	public boolean shouldAccept(URI uri, HttpCookie cookie) {
		// We only care about validating domains for releases so we can
		// keep using the mock proxy
		if (mIsRelease) {
			// 1697. VSC. Get rid of the 1st period in domain to check with the POS_Url
			// Since for VSC url="agence.voyages-sncf.com", domain=".voyages-sncf.com"
			// and for EBad url="expedia.com", domain=".expedia.com"
			String domain = cookie.getDomain();
			if (domain.toCharArray()[0] == '.') {
				domain = domain.substring(1);
			}

			if (!PointOfSale.getPointOfSale().getUrl().endsWith(domain)) {
				Log.e("Domain \"" + domain + "\" not the current point of sale for cookie: " + cookie.toString());
				// Reject the cookie
				return false;
			}

			return true;
		}
		else {
			return true;
		}
	}
}
