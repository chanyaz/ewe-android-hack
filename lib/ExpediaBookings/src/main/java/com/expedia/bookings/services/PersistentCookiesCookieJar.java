package com.expedia.bookings.services;

import okhttp3.CookieJar;

public interface PersistentCookiesCookieJar extends CookieJar {

	void removeNamedCookies(String endpointURL, String[] names);

	void setMC1Cookie(String guid, String posUrl);

	void clear();

}
