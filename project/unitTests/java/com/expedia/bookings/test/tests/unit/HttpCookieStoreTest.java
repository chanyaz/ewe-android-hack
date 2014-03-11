package com.expedia.bookings.test.tests.unit;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import junit.framework.TestCase;

import com.expedia.bookings.server.HttpCookieStore;

public class HttpCookieStoreTest extends TestCase {
	public void testRemoveAll() {
		final HttpCookieStore cs = new HttpCookieStore();

		// No cookies in store means no work to be done
		assertFalse(cs.removeAll());

		// Now there is a cookie it should indicate something was removed
		cs.add(newURI("http://expedia.com"), newCookie("expedia.com", "party", "hard"));
		assertTrue(cs.removeAll());
	}

	public void testRemove() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI uri = newURI("http://expedia.com");
		final HttpCookie firstCookie = newCookie("expedia.com", "party", "hard");
		// I want these to be ref different
		final HttpCookie secondCookie = newCookie("expedia.com", "party", "hard");


		// No cookies in store means no work to be done
		assertFalse(cs.remove(uri, firstCookie));

		// Now there is a cookie it should indicate something was removed
		cs.add(uri, firstCookie);
		assertTrue(cs.remove(uri, firstCookie));

		// Test that it is doing deep equality
		cs.add(uri, firstCookie);
		assertTrue(cs.remove(uri, secondCookie));
	}

	public void testGetUris() {
		final HttpCookieStore cs = new HttpCookieStore();

		final URI firstUri = newURI("http://expedia.com");
		final HttpCookie firstCookie = newCookie("expedia.com", "party", "hard");

		final URI secondUri = newURI("http://expedia.co.uk");
		final HttpCookie secondCookie = newCookie("expedia.co.uk", "party", "harder");

		cs.add(firstUri, firstCookie);
		cs.add(secondUri, secondCookie);

		final List<URI> uris = cs.getURIs();
		assertEquals(2, uris.size());
		assertTrue(uris.contains(firstUri));
		assertTrue(uris.contains(secondUri));
	}

	public void testGetCookies() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI uri = newURI("http://expedia.com");
		final HttpCookie firstCookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie secondCookie = newCookie("expedia.com", "tons", "fun");
		final HttpCookie thirdCookie = newCookie("expedia.com", "lots", "beer");

		cs.add(uri, firstCookie);
		cs.add(uri, secondCookie);
		cs.add(uri, thirdCookie);

		List<HttpCookie> cookies = cs.getCookies();
		assertEquals(3, cookies.size());
		assertTrue(cookies.contains(firstCookie));
		assertTrue(cookies.contains(secondCookie));
		assertTrue(cookies.contains(thirdCookie));

		final URI otherUri = newURI("http://expedia.co.uk");
		final HttpCookie fourthCookie = newCookie("expedia.co.uk", "no", "fun");
		final HttpCookie fifthCookie = newCookie("expedia.co.uk", "so", "lame");

		cs.add(otherUri, fourthCookie);
		cs.add(otherUri, fifthCookie);

		cookies = cs.getCookies();
		assertEquals(5, cookies.size());
		assertTrue(cookies.contains(firstCookie));
		assertTrue(cookies.contains(secondCookie));
		assertTrue(cookies.contains(thirdCookie));
		assertTrue(cookies.contains(fourthCookie));
		assertTrue(cookies.contains(fifthCookie));

		// Testing dups
		final HttpCookie dupKeyCookie = new HttpCookie("party", "hard");
		dupKeyCookie.setDomain("expedia.co.uk");
		dupKeyCookie.setMaxAge(-1);

		cs.add(otherUri, dupKeyCookie); // Different domains
		cs.add(uri, firstCookie); // Straight up dup

		cookies = cs.getCookies();
		assertEquals(6, cookies.size());
		assertTrue(cookies.contains(firstCookie));
		assertTrue(cookies.contains(secondCookie));
		assertTrue(cookies.contains(thirdCookie));
		assertTrue(cookies.contains(fourthCookie));
		assertTrue(cookies.contains(fifthCookie));
		assertTrue(cookies.contains(dupKeyCookie));
	}

	public void testGet() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI uri = newURI("http://expedia.com");
		final HttpCookie firstCookie = new HttpCookie("party", "hard");
		final HttpCookie secondCookie = new HttpCookie("tons", "fun");
		final HttpCookie thirdCookie = new HttpCookie("lots", "beer");

		firstCookie.setMaxAge(-1);
		secondCookie.setMaxAge(-1);
		thirdCookie.setMaxAge(-1);

		cs.add(uri, firstCookie);
		cs.add(uri, secondCookie);
		cs.add(uri, thirdCookie);

		List<HttpCookie> cookies = cs.get(uri);
		assertEquals(3, cookies.size());
		assertTrue(cookies.contains(firstCookie));
		assertTrue(cookies.contains(secondCookie));
		assertTrue(cookies.contains(thirdCookie));
	}

	public void testAdd() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI uri = newURI("http://expedia.com");
		final HttpCookie firstCookie = new HttpCookie("party", "hard");
		final HttpCookie secondCookie = new HttpCookie("tons", "fun");
		final HttpCookie thirdCookie = new HttpCookie("lots", "beer");
		List<HttpCookie> cookies;

		// test expiry
		firstCookie.setMaxAge(0);

		cs.add(uri, firstCookie);
		cookies = cs.get(uri);
		assertEquals(0, cookies.size());

		firstCookie.setMaxAge(-1);
		cs.add(uri, firstCookie);
		cookies = cs.get(uri);
		assertEquals(1, cookies.size());
		assertTrue(cookies.contains(firstCookie));

		firstCookie.setMaxAge(5);
		cs.add(uri, firstCookie);
		cookies = cs.get(uri);
		assertEquals(1, cookies.size());
		assertTrue(cookies.contains(firstCookie));
		HttpCookie cookie = cookies.get(0);
		// Make sure it was overwritten
		assertEquals(5, cookie.getMaxAge());
	}

	private URI newURI(String str) {
		URI ret;
		try {
			ret = new URI(str);
		}
		catch (Exception e) {
			ret = null;
		}
		return ret;
	}

	private HttpCookie newCookie(String domain, String key, String value) {
		HttpCookie cookie = new HttpCookie(key, value);
		cookie.setDomain(domain);
		cookie.setMaxAge(-1);
		return cookie;
	}
}
