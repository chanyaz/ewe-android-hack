package com.expedia.bookings.test.robolectric;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.server.HttpCookieStore;

@RunWith(RobolectricSubmoduleTestRunner.class)
public class HttpCookieStoreTest {
	@Test
	public void testRemoveAll() {
		final HttpCookieStore cs = new HttpCookieStore();

		// No cookies in store means no work to be done
		Assert.assertFalse(cs.removeAll());

		// Now there is a cookie it should indicate something was removed
		cs.add(newURI("http://expedia.com"), newCookie("expedia.com", "party", "hard"));
		Assert.assertTrue(cs.removeAll());
	}

	@Test
	public void testRemove() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI uri = newURI("http://expedia.com");
		final HttpCookie firstCookie = newCookie("expedia.com", "party", "hard");
		// I want these to be ref different
		final HttpCookie secondCookie = newCookie("expedia.com", "party", "hard");

		// No cookies in store means no work to be done
		Assert.assertFalse(cs.remove(uri, firstCookie));

		// Now there is a cookie it should indicate something was removed
		cs.add(uri, firstCookie);
		Assert.assertTrue(cs.remove(uri, firstCookie));

		// Test that it is doing deep equality
		cs.add(uri, firstCookie);
		Assert.assertTrue(cs.remove(uri, secondCookie));
	}

	@Test
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
		Assert.assertEquals(3, cookies.size());

		cs.removeAllCookiesByName(new String[] {"party"});
		cookies = cs.getCookies();
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(thirdCookie, cookies.get(0));
	}

	@Test
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
		Assert.assertEquals(3, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));
		Assert.assertTrue(cookies.contains(secondCookie));
		Assert.assertTrue(cookies.contains(thirdCookie));

		final URI otherUri = newURI("http://expedia.co.uk");
		final HttpCookie fourthCookie = newCookie("expedia.co.uk", "no", "fun");
		final HttpCookie fifthCookie = newCookie("expedia.co.uk", "so", "lame");

		cs.add(otherUri, fourthCookie);
		cs.add(otherUri, fifthCookie);

		cookies = cs.getCookies();
		Assert.assertEquals(5, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));
		Assert.assertTrue(cookies.contains(secondCookie));
		Assert.assertTrue(cookies.contains(thirdCookie));
		Assert.assertTrue(cookies.contains(fourthCookie));
		Assert.assertTrue(cookies.contains(fifthCookie));

		// Testing dups
		final HttpCookie dupKeyCookie = new HttpCookie("party", "hard");
		dupKeyCookie.setDomain("expedia.co.uk");
		dupKeyCookie.setMaxAge(-1);

		cs.add(otherUri, dupKeyCookie); // Different domains
		cs.add(uri, firstCookie); // Straight up dup

		cookies = cs.getCookies();
		Assert.assertEquals(6, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));
		Assert.assertTrue(cookies.contains(secondCookie));
		Assert.assertTrue(cookies.contains(thirdCookie));
		Assert.assertTrue(cookies.contains(fourthCookie));
		Assert.assertTrue(cookies.contains(fifthCookie));
		Assert.assertTrue(cookies.contains(dupKeyCookie));
	}

	@Test
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
		Assert.assertEquals(3, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));
		Assert.assertTrue(cookies.contains(secondCookie));
		Assert.assertTrue(cookies.contains(thirdCookie));
	}

	@Test
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
		Assert.assertEquals(0, cookies.size());

		firstCookie.setMaxAge(-1);
		cs.add(uri, firstCookie);
		cookies = cs.get(uri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));

		firstCookie.setMaxAge(5);
		cs.add(uri, firstCookie);
		cookies = cs.get(uri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertTrue(cookies.contains(firstCookie));
		HttpCookie cookie = cookies.get(0);
		// Make sure it was overwritten
		Assert.assertEquals(5, cookie.getMaxAge());
	}

	@Test
	public void testUriPathHandling() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final URI detailsUri = newURI("http://www.expedia.com/MobileHotel/Webapp/HotelOffers");
		final URI secureDetailsUri = newURI("https://www.expedia.com/MobileHotel/Webapp/HotelOffers");

		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");

		List<HttpCookie> cookies;
		cs.add(searchUri, cookie);

		cookies = cs.get(detailsUri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(cookie, cookies.get(0));

		cookies = cs.get(secureDetailsUri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(cookie, cookies.get(0));
	}

	@Test
	public void testSavingAfterAdd() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie sameCookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie differentCookie = newCookie("expedia.com", "party", "harder");

		List<HttpCookie> cookies;
		cs.add(searchUri, cookie);

		cookies = cs.get(searchUri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(cookie, cookies.get(0));
		Assert.assertEquals(1, cs.getTimesSavedToDisk());

		cs.add(searchUri, sameCookie);

		cookies = cs.get(searchUri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(sameCookie, cookies.get(0));
		Assert.assertEquals(1, cs.getTimesSavedToDisk());

		cs.add(searchUri, differentCookie);

		cookies = cs.get(searchUri);
		Assert.assertEquals(1, cookies.size());
		Assert.assertEquals(differentCookie, cookies.get(0));
		Assert.assertEquals(2, cs.getTimesSavedToDisk());
	}

	@Test
	public void testSavingAfterRemove() {
		final HttpCookieStore cs = new HttpCookieStore();
		final URI searchUri = newURI("http://www.expedia.com/MobileHotel/Webapp/SearchResults");
		final HttpCookie cookie = newCookie("expedia.com", "party", "hard");
		final HttpCookie differentCookie = newCookie("expedia.com", "much", "opinion");
		List<HttpCookie> cookies;

		// Jar is empty, no work being done means not dirty
		cs.remove(searchUri, cookie);
		Assert.assertEquals(0, cs.getTimesSavedToDisk());

		cs.removeAll();
		Assert.assertEquals(0, cs.getTimesSavedToDisk());

		cs.add(searchUri, cookie);
		Assert.assertEquals(1, cs.getTimesSavedToDisk());

		// Cookie not in jar, no work, no save
		cs.remove(searchUri, differentCookie);
		Assert.assertEquals(1, cs.getTimesSavedToDisk());

		// Cookie is in jar, jar is dirty
		cs.remove(searchUri, cookie);
		Assert.assertEquals(2, cs.getTimesSavedToDisk());

		cs.add(searchUri, cookie);
		Assert.assertEquals(3, cs.getTimesSavedToDisk());

		// Something in jar, clearing all makes jar dirty
		cs.removeAll();
		Assert.assertEquals(4, cs.getTimesSavedToDisk());
	}

	@Test
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
		Assert.assertEquals(1, cookies.size());

		// .expedia.com matches www.expedia.com so it is the same cookie
		cs.add(searchUri, sameCookie);
		cookies = cs.getCookies();
		Assert.assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		Assert.assertEquals("harder", testCookie.getValue());
		// Cookie should take on the domain of what it matched
		Assert.assertEquals(".expedia.com", testCookie.getDomain());

		// If there is no domain it should take on the domain of the uri
		cs.add(searchUri, anotherSameCookie);
		cookies = cs.getCookies();
		Assert.assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		Assert.assertEquals("hardest", testCookie.getValue());
		Assert.assertEquals(".expedia.com", testCookie.getDomain());

		cs.add(searchUri, yetAnotherSameCookie);
		cookies = cs.getCookies();
		Assert.assertEquals(1, cookies.size());
		testCookie = cookies.get(0);
		Assert.assertEquals("thehardest", testCookie.getValue());
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
