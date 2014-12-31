package com.expedia.bookings.server;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class SyncronizedHttpCookieStore extends HttpCookieStore {
	@Override
	public synchronized void add(URI uri, HttpCookie cookie) {
		super.add(uri, cookie);
	}

	@Override
	public synchronized List<HttpCookie> get(URI uri) {
		return super.get(uri);
	}

	@Override
	public synchronized List<HttpCookie> getCookies() {
		return super.getCookies();
	}

	@Override
	public synchronized List<URI> getURIs() {
		return super.getURIs();
	}

	@Override
	public synchronized boolean remove(URI uri, HttpCookie cookie) {
		return super.remove(uri, cookie);
	}

	@Override
	public synchronized boolean removeAll() {
		return super.removeAll();
	}
}
