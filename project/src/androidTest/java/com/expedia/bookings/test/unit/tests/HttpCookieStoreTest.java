package com.expedia.bookings.test.unit.tests;

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

	public void testRemoveByName() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI comUri = newURI("http://expedia.com");
		final URI ukUri = newURI("http://expedia.co.uk");
		final HttpCookie firstCookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie secondCookie = newCookie("expedia.co.uk", "party", "harder");
		final HttpCookie thirdCookie = newCookie("expedia.com", "dont", "touchme");

		cs.add(comUri, firstCookie);
		cs.add(ukUri, secondCookie);
		cs.add(comUri, thirdCookie);

		List<HttpCookie> cookies = cs.getCookies();
		assertEquals(3, cookies.size());

		cs.removeAllCookiesByName(new String[] {"party"});
		cookies = cs.getCookies();
		assertEquals(1, cookies.size());
		assertEquals(thirdCookie, cookies.get(0));
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
		final URI uri = newURI("http://www.expedia.com");
		final HttpCookie firstCookie = newCookie(".expedia.com", "party", "hard");
		final HttpCookie secondCookie = newCookie(".expedia.com", "tons", "fun");
		final HttpCookie thirdCookie = newCookie(".expedia.com", "lots", "beer");
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

	public void testUriPathHandling() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final URI detailsUri = newURI("http://www.expedia.com/MobileHotel/Webapp/HotelOffers");
		final URI secureDetailsUri = newURI("https://www.expedia.com/MobileHotel/Webapp/HotelOffers");

		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");

		List<HttpCookie> cookies;
		cs.add(searchUri, cookie);

		cookies = cs.get(detailsUri);
		assertEquals(1, cookies.size());
		assertEquals(cookie, cookies.get(0));

		cookies = cs.get(secureDetailsUri);
		assertEquals(1, cookies.size());
		assertEquals(cookie, cookies.get(0));
	}

	public void testSavingAfterAdd() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie sameCookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie differentCookie = newCookie("expedia.com", "party", "harder");

		List<HttpCookie> cookies;
		cs.add(searchUri, cookie);

		cookies = cs.get(searchUri);
		assertEquals(1, cookies.size());
		assertEquals(cookie, cookies.get(0));
		assertEquals(1, cs.getTimesSavedToDisk());

		cs.add(searchUri, sameCookie);

		cookies = cs.get(searchUri);
		assertEquals(1, cookies.size());
		assertEquals(sameCookie, cookies.get(0));
		assertEquals(1, cs.getTimesSavedToDisk());

		cs.add(searchUri, differentCookie);

		cookies = cs.get(searchUri);
		assertEquals(1, cookies.size());
		assertEquals(differentCookie, cookies.get(0));
		assertEquals(2, cs.getTimesSavedToDisk());
	}

	public void testSavingAfterRemove() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie differentCookie = newCookie("expedia.com", "much", "opinion");
		List<HttpCookie> cookies;

		// Jar is empty, no work being done means not dirty
		cs.remove(searchUri, cookie);
		assertEquals(0, cs.getTimesSavedToDisk());

		cs.removeAll();
		assertEquals(0, cs.getTimesSavedToDisk());

		cs.add(searchUri, cookie);
		assertEquals(1, cs.getTimesSavedToDisk());

		// Cookie not in jar, no work, no save
		cs.remove(searchUri, differentCookie);
		assertEquals(1, cs.getTimesSavedToDisk());

		// Cookie is in jar, jar is dirty
		cs.remove(searchUri, cookie);
		assertEquals(2, cs.getTimesSavedToDisk());

		cs.add(searchUri, cookie);
		assertEquals(3, cs.getTimesSavedToDisk());

		// Something in jar, clearing all makes jar dirty
		cs.removeAll();
		assertEquals(4, cs.getTimesSavedToDisk());
	}

	public void testAddingDuplicatesWithDifferentDomains() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final HttpCookie cookie = newCookie(".expedia.com", "party", "hard");
		final HttpCookie sameCookie = newCookie("www.expedia.com", "party", "harder");
		final HttpCookie anotherSameCookie = newCookie(null, "party", "hardest");
		final HttpCookie yetAnotherSameCookie = newCookie("expedia.com", "party", "thehardest");
		List<HttpCookie> cookies;
		HttpCookie testCookie;

		cs.add(searchUri, cookie);
		cookies = cs.getCookies();
		assertEquals(1, cookies.size());

		// .expedia.com matches www.expedia.com so it is the same cookie
		cs.add(searchUri, sameCookie);
		cookies = cs.getCookies();
		assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		assertEquals("harder", testCookie.getValue());
		// Cookie should take on the domain of what it matched
		assertEquals(".expedia.com", testCookie.getDomain());

		// If there is no domain it should take on the domain of the uri
		cs.add(searchUri, anotherSameCookie);
		cookies = cs.getCookies();
		assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		assertEquals("hardest", testCookie.getValue());
		assertEquals(".expedia.com", testCookie.getDomain());

		cs.add(searchUri, yetAnotherSameCookie);
		cookies = cs.getCookies();
		assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		assertEquals("thehardest", testCookie.getValue());
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
		cookie.setPath("/");
		cookie.setDomain(domain);
		cookie.setMaxAge(-1);
		return cookie;
	}
}
