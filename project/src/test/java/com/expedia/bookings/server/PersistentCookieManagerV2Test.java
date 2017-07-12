package com.expedia.bookings.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowCookieManagerEB;

import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.Strings;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

@RunWith(RobolectricRunner.class)
@Config(shadows = { ShadowCookieManagerEB.class })
public class PersistentCookieManagerV2Test {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final List<Cookie> NO_COOKIES = new ArrayList<>();
	private static final List<Cookie> EXPEDIA_COOKIES = new ArrayList<>();
	private static final List<String> EXPEDIA_COOKIE_STRINGS = new ArrayList<>();
	private static final List<Cookie> EXPEDIA_COOKIES_WITHOUT_MC1 = new ArrayList<>();
	private static final List<String> EXPEDIA_COOKIE_STRINGS_ADDL = new ArrayList<>();
	private static final List<Cookie> REVIEWS_EXPEDIA_COOKIES = new ArrayList<>();
	private static final List<Cookie> EXPIRED_COOKIES = new ArrayList<>();
	private static final List<Cookie> LINFO_COOKIE = new ArrayList<>();
	private static final List<Cookie> SOME_OTHER_SITE_COOKIES = new ArrayList<>();
	private static final List<Cookie> VOYAGES_COOKIES = new ArrayList<>();
	private static final HttpUrl expedia = new HttpUrl.Builder().scheme("https").host("www.expedia.com").build();
	private static final HttpUrl voyages = new HttpUrl.Builder().scheme("https").host("agence.voyages-sncf.com").build();
	private static final HttpUrl reviews = new HttpUrl.Builder().scheme("https").host("reviewsvc.expedia.com").build();
	private static final HttpUrl omniture = new HttpUrl.Builder().scheme("https").host("omniture.com").build();
	private static final HttpUrl someOtherSite = new HttpUrl.Builder().scheme("https").host("someothersite.com").build();

	static {
		ArrayList<String> list = new ArrayList<>();
		list.add("TH=Zq1lI0f9YKT2/TwF8RKlZoltqBiQRIVt6+CeeeShGPbUa7trUEYzeQ94VLqfTlsrIApXJvOb/4o=|VNgEN5B50gAOfASNHIjtpg8Z/ucPcBwzPSZEgxBSozSIhHn3mcz0N21PU9QbNgKX02tk+PhOnZMX16DGvJ+enbPsqDVe33qF|sa5pvCHP8aYf8DKjxjUKO4rW98xwy5y1; Domain=.expedia.com; Path=/");
		list.add("SSID1=BwCKTh3EAAAAAADsbu5U2WcCEOxu7lQBAAAAAAAAAAAA7G7uVAAKihsEAAFEZAAA7G7uVAEACwQAAaFjAADsbu5UAQD1AwABBWMAAOxu7lQBABkEAAE2ZAAA7G7uVAEADgQAAbljAADsbu5UAQAABAABSGMAAOxu7lQBABcEAAEwZAAA7G7uVAEAEgQAARlkAADsbu5UAQD0AwAB-WIAAOxu7lQBAOkDAAFkYQAA7G7uVAEAAwQAAVFjAADsbu5UAQAYBAABMWQAAOxu7lQBAA8EAAHDYwAA7G7uVAEAEAQAAcdjAADsbu5UAQA; path=/; domain=.expedia.com");
		list.add("SSSC1=1.G6119950903803013081.1|1001.24932:1012.25337:1013.25349:1024.25416:1027.25425:1035.25505:1038.25529:1039.25539:1040.25543:1042.25625:1047.25648:1048.25649:1049.25654:1051.25668; path=/; domain=.expedia.com");
		list.add("SSRT1=7G7uVAIAAA; path=/; domain=.expedia.com");
		list.add("SSPV1=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA; path=/; domain=.expedia.com");
		list.add("SSLB=1; path=/; domain=.expedia.com");
		list.add("JSESSION=890e137e-c002-4718-b00f-76c4877bb296; Domain=.expedia.com; Path=/");
		list.add("tpid=v.1,1; Domain=.expedia.com; Path=/");
		list.add("iEAPID=0,; Domain=.expedia.com; Path=/");
		list.add("linfo=v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1; Domain=.expedia.com; Path=/");
		list.add("minfo=v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P; Domain=.expedia.com; Path=/");
		for (String string : list) {
			EXPEDIA_COOKIES_WITHOUT_MC1.add(Cookie.parse(expedia, string));
		}

		list.add("MC1=GUID=4a7e5c02232b479aa4807d32c6b7129c; Domain=.expedia.com; Path=/");
		for (String string : list) {
			EXPEDIA_COOKIES.add(Cookie.parse(expedia, string));
			EXPEDIA_COOKIE_STRINGS.add(string);
		}

		list = new ArrayList<>();
		list.add("test=v.1,1; Domain=.expedia.com; Path=/");
		for (String string : list) {
			EXPEDIA_COOKIE_STRINGS_ADDL.add(string);
		}

		list = new ArrayList<>();
		list.add("minfo=v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94; Domain=.expedia.com; Path=/");
		for (String string : list) {
			REVIEWS_EXPEDIA_COOKIES.add(Cookie.parse(reviews, string));
		}

		list = new ArrayList<>();
		list.add("MC1=GUID=4a7e5c02232b479aa4807d32c6b7129c; Domain=.expedia.com; Path=/; Expires=Fri, 26-Feb-2016 00:08:28 GMT");
		for (String string : list) {
			EXPIRED_COOKIES.add(Cookie.parse(expedia, string));
		}

		list = new ArrayList<>();
		list.add("linfo=v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1; Domain=.expedia.com; Path=/");
		for (String string : list) {
			LINFO_COOKIE.add(Cookie.parse(expedia, string));
		}

		list = new ArrayList<>();
		list.add("MC1=GUID=notARealMC1Cookie; Domain=.someothersite.com; Path=/");
		for (String string : list) {
			SOME_OTHER_SITE_COOKIES.add(Cookie.parse(someOtherSite, string));
		}

		list = new ArrayList<>();
		list.add("tpid=v.1,1; Domain=.agence.voyages-sncf.com; Path=/");
		list.add("iEAPID=0,; Domain=.agence.voyages-sncf.com; Path=/");
		list.add("linfo=v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1; Domain=.agence.voyages-sncf.com; Path=/");
		for (String string : list) {
			VOYAGES_COOKIES.add(Cookie.parse(voyages, string));
		}
	}

	private File oldCookieStorage;
	private PersistentCookieManagerV2 manager;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Before
	public void before() throws Throwable {
		File storage = folder.newFile();
		storage.delete();

		oldCookieStorage = folder.newFile();
		oldCookieStorage.delete();
		manager = new PersistentCookieManagerV2(storage);
		manager.clear();
	}

	@Test
	public void saveNoCookies() throws Throwable {
		manager.clear();
		manager.saveFromResponse(expedia, NO_COOKIES);
		expectCookies(expedia, 0);
	}

	@Test
	public void saveExpediaCookies() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookies(expedia, 12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");

		// Make sure we can overwrite the file
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookies(expedia, 12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
	}

	@Test
	public void putExpediaCookies() throws Throwable {

		HashMap<String, List<String>> headers = new HashMap<>();
		headers.put("Set-Cookie", EXPEDIA_COOKIE_STRINGS);
		manager.put(expedia.uri(), headers);
		expectCookies(expedia, 12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");

		headers.put("Set-Cookie", EXPEDIA_COOKIE_STRINGS_ADDL);
		// Make sure we can overwrite the file
		manager.put(expedia.uri(), headers);
		expectCookies(expedia, 13);
		expectCookie(expedia, "test", "v.1,1");
	}

	@Test
	public void testRemovingNamedCookiesWithInvalidDomainDoesNothing() throws Throwable {
		HashMap<String, List<String>> headers = new HashMap<>();
		headers.put("Set-Cookie", EXPEDIA_COOKIE_STRINGS);
		manager.put(expedia.uri(), headers);
		expectCookies(expedia, 12);

		String[] cookieNames = {""};
		manager.removeNamedCookies("e", cookieNames);

		expectCookies(expedia, 12);
	}

	@Test
	public void clearingCookies() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookies(expedia, 12);

		manager.clear();
		expectCookies(expedia, 0);
	}

	@Test
	public void deleteSpecificCookies() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);

		List<Cookie> omnitureCookies = new ArrayList<>();
		ArrayList<String> omnitureData = new ArrayList<>();
		omnitureData.add(
			"minfo=v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94; Domain=.omniture.com; Path=/");
		for (String string : omnitureData) {
			omnitureCookies.add(Cookie.parse(omniture, string));
		}

		manager.saveFromResponse(omniture, omnitureCookies);
		expectCookies(expedia, 12);
		expectCookies(omniture, 1);
		String[] userCookieNames = {
			"tpid",
			"iEAPID",
			"linfo",
		};

		manager.removeNamedCookies(expedia.toString(), userCookieNames);
		String tpidValue = manager.getCookieValue(expedia, "tpid");
		Assert.assertEquals("", tpidValue);
		String iEAPIDValue = manager.getCookieValue(expedia, "iEAPID");
		Assert.assertEquals("", iEAPIDValue);
		String linfoValue = manager.getCookieValue(expedia, "linfo");
		Assert.assertEquals("", linfoValue);
		expectCookies(omniture, 1);
	}

	@Test
	public void deleteVoyagesCookies() {
		manager.saveFromResponse(voyages, VOYAGES_COOKIES);
		String[] userCookieNames = {
			"tpid",
			"iEAPID",
			"linfo",
		};

		manager.removeNamedCookies(voyages.toString(), userCookieNames);
		String tpidValue = manager.getCookieValue(voyages, "tpid");
		Assert.assertEquals("", tpidValue);
		String iEAPIDValue = manager.getCookieValue(voyages, "iEAPID");
		Assert.assertEquals("", iEAPIDValue);
		String linfoValue = manager.getCookieValue(voyages, "linfo");
		Assert.assertEquals("", linfoValue);
	}

	@Test
	public void saveThenLoadExpediaCookies() throws Throwable {
		saveExpediaCookies();
		File storage = folder.newFile();
		manager = new PersistentCookieManagerV2(storage);
		expectCookies(expedia, 12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
	}

	@Test
	public void mixCookies() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
		manager.saveFromResponse(reviews, REVIEWS_EXPEDIA_COOKIES);
		expectCookie(reviews, "minfo",
			"v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94");
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
	}

	@Test
	public void retainsPreviousCookies() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
		manager.saveFromResponse(expedia, LINFO_COOKIE);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
	}


	@Test
	public void parseBadData() throws Throwable {
		final String emptyData = "[";
		File storage = folder.newFile();
		BufferedWriter writer = new BufferedWriter(new FileWriter(storage));
		writer.write(emptyData);
		writer.close();

		try {
			manager = new PersistentCookieManagerV2(storage);
			Assert.fail("expected json exception but it was not thrown");
		}
		catch (RuntimeException ignored) {
		}
	}

	@Test
	public void setNewMC1CookieEmptyCookieStoreCase() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES_WITHOUT_MC1);
		expectCookies(expedia, 11);
		String host = expedia.host();
		manager.setMC1Cookie("GUID=1111", host);
		expectCookies(expedia, 12);
		expectCookie(expedia, "MC1", "GUID=1111");
	}

	@Test
	public void setNewMC1CookieNonExpediaCookiesCase() throws Throwable {
		String host = expedia.host();
		manager.saveFromResponse(reviews, NO_COOKIES);
		manager.setMC1Cookie("GUID=1111", host);
		expectCookie(expedia, "MC1", "GUID=1111");
		expectCookies(expedia, 1);
	}

	@Test
	public void getCorrectMC1CookieForDomain() throws Throwable {
		manager.saveFromResponse(expedia, EXPEDIA_COOKIES);
		manager.saveFromResponse(someOtherSite, SOME_OTHER_SITE_COOKIES);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
		expectCookie(someOtherSite, "MC1", "GUID=notARealMC1Cookie");
	}

	private void expectCookies(HttpUrl url, int num) {
		List<Cookie> cookies = manager.loadForRequest(url);
		Assert.assertEquals("cookies: " + Strings.toPrettyString(cookies), num, cookies.size());
	}

	private void expectCookie(final HttpUrl uri, final String name, final String value) throws Throwable {
		List<Cookie> cookies = manager.loadForRequest(uri);
		for (Cookie cookie : cookies) {
			if (Strings.equals(name, cookie.name())) {
				Assert.assertEquals(value, cookie.value());
				return;
			}
		}
		throw new RuntimeException("cookie not found");
	}

}
