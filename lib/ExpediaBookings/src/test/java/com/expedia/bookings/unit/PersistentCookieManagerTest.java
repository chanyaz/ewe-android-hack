package com.expedia.bookings.unit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.expedia.bookings.services.PersistentCookieManager;
import com.expedia.bookings.utils.Strings;

import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class PersistentCookieManagerTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final Map<String, List<String>> NO_COOKIES = new HashMap<>();
	private static final Map<String, List<String>> EXPEDIA_COOKIES = new HashMap<>();
	private static final Map<String, List<String>> REVIEWS_EXPEDIA_COOKIES = new HashMap<>();
	private static final Map<String, List<String>> EXPIRED_COOKIES = new HashMap<>();

	static {
		ArrayList<String> list = new ArrayList<>();
		list.add("TH=Zq1lI0f9YKT2/TwF8RKlZoltqBiQRIVt6+CeeeShGPbUa7trUEYzeQ94VLqfTlsrIApXJvOb/4o=|VNgEN5B50gAOfASNHIjtpg8Z/ucPcBwzPSZEgxBSozSIhHn3mcz0N21PU9QbNgKX02tk+PhOnZMX16DGvJ+enbPsqDVe33qF|sa5pvCHP8aYf8DKjxjUKO4rW98xwy5y1; Domain=.expedia.com; Path=/");
		list.add("SSID1=BwCKTh3EAAAAAADsbu5U2WcCEOxu7lQBAAAAAAAAAAAA7G7uVAAKihsEAAFEZAAA7G7uVAEACwQAAaFjAADsbu5UAQD1AwABBWMAAOxu7lQBABkEAAE2ZAAA7G7uVAEADgQAAbljAADsbu5UAQAABAABSGMAAOxu7lQBABcEAAEwZAAA7G7uVAEAEgQAARlkAADsbu5UAQD0AwAB-WIAAOxu7lQBAOkDAAFkYQAA7G7uVAEAAwQAAVFjAADsbu5UAQAYBAABMWQAAOxu7lQBAA8EAAHDYwAA7G7uVAEAEAQAAcdjAADsbu5UAQA; path=/; domain=.expedia.com");
		list.add("SSSC1=1.G6119950903803013081.1|1001.24932:1012.25337:1013.25349:1024.25416:1027.25425:1035.25505:1038.25529:1039.25539:1040.25543:1042.25625:1047.25648:1048.25649:1049.25654:1051.25668; path=/; domain=.expedia.com");
		list.add("SSRT1=7G7uVAIAAA; path=/; domain=.expedia.com");
		list.add("SSPV1=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA; path=/; domain=.expedia.com");
		list.add("SSLB=1; path=/; domain=.expedia.com");
		list.add("MC1=GUID=4a7e5c02232b479aa4807d32c6b7129c; Domain=.expedia.com; Path=/");
		list.add("JSESSION=890e137e-c002-4718-b00f-76c4877bb296; Domain=.expedia.com; Path=/");
		list.add("tpid=v.1,1; Domain=.expedia.com; Path=/");
		list.add("iEAPID=0,; Domain=.expedia.com; Path=/");
		list.add("linfo=v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1; Domain=.expedia.com; Path=/");
		list.add("minfo=v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P; Domain=.expedia.com; Path=/");
		EXPEDIA_COOKIES.put("Set-Cookie", list);

		list = new ArrayList<>();
		list.add("minfo=v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94; Domain=.expedia.com; Path=/");
		REVIEWS_EXPEDIA_COOKIES.put("Set-Cookie", list);

		list = new ArrayList<>();
		list.add("MC1=GUID=4a7e5c02232b479aa4807d32c6b7129c; Domain=.expedia.com; Path=/; Expires=Fri, 26-Feb-2016 00:08:28 GMT");
		EXPIRED_COOKIES.put("Set-Cookie", list);
	}

	private URI expedia;
	private URI reviews;
	private File storage;
	private PersistentCookieManager manager;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Before
	public void before() throws Throwable {

		expedia = new URI("https://www.expedia.com");
		reviews = new URI("https://reviewsvc.expedia.com");
		storage = folder.newFile();
		storage.delete();

		File oldCookieStorage = folder.newFile();
		oldCookieStorage.delete();
		manager = new PersistentCookieManager(storage);
	}

	@Test
	public void simple() throws Throwable {
		expectCookies(0);
	}

	@Test
	public void saveNoCookies() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, NO_COOKIES);
		expectCookies(0);
		expectExists(storage);
	}

	@Test
	public void saveExpediaCookies() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookies(12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
		expectExists(storage);

		// Make sure we can overwrite the file
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookies(12);
		expectExists(storage);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
	}

	@Test
	public void ignoreExpiredCookies() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, EXPIRED_COOKIES);
		expectCookies(0);
		expectExists(storage);
	}

	@Test
	public void get() throws Throwable {
		manager.put(expedia, EXPEDIA_COOKIES);
		Map<String, List<String>> cookies = manager.get(expedia, new HashMap<String, List<String>>());
		Assert.assertNotEquals("Expected cookies " + Strings.toPrettyString(cookies), 0, cookies.size());
	}

	@Test
	public void clearingCookies() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, EXPEDIA_COOKIES);
		expectExists(storage);
		expectCookies(12);

		manager.clear();
		expectCookies(0);
		expectNotExists(storage);
	}

	@Test
	public void removeCookiesByName() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, EXPEDIA_COOKIES);
		expectExists(storage);
		expectCookies(12);

		manager.removeNamedCookies(new String[] { "MC1" });
		expectCookies(11);
		expectExists(storage);

		manager.removeNamedCookies(new String[] { "JSESSION", "SSID1" });
		expectCookies(9);
		expectExists(storage);
	}

	@Test
	public void saveThenLoadExpediaCookies() throws Throwable {
		saveExpediaCookies();
		expectExists(storage);

		manager = new PersistentCookieManager(storage);
		expectCookies(12);
		expectCookie(expedia, "MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
		expectExists(storage);
	}

	@Test
	public void mixCookies() throws Throwable {
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
		manager.put(reviews, REVIEWS_EXPEDIA_COOKIES);
		expectCookie(reviews, "minfo",
			"v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94");
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo",
			"v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
	}

	@Test
	public void parseEmptyData() throws Throwable {
		final String emptyData = "";
		BufferedWriter writer = new BufferedWriter(new FileWriter(storage));
		writer.write(emptyData);
		writer.close();

		manager = new PersistentCookieManager(storage);
		expectCookies(0);
	}

	@Test(expected = RuntimeException.class)
	public void parseBadData() throws Throwable {
		final String emptyData = "[";
		BufferedWriter writer = new BufferedWriter(new FileWriter(storage));
		writer.write(emptyData);
		writer.close();

		manager = new PersistentCookieManager(storage);
		expectCookies(0);
	}

	public void expectCookies(int num) {
		List<HttpCookie> cookies = manager.getCookieStore().getCookies();
		Assert.assertEquals("cookies: " + Strings.toPrettyString(cookies), num, cookies.size());
	}

	public void expectCookie(final URI uri, final String name, final String value) throws Throwable {
		Map<String, List<String>> map = manager.get(uri, new HashMap<String, List<String>>());
		for (List<String> cookieHeaders : map.values()) {
			for (String header : cookieHeaders) {
				List<HttpCookie> cookies = HttpCookie.parse(header);
				for (HttpCookie cookie : cookies) {
					if (Strings.equals(name, cookie.getName())) {
						Assert.assertEquals(value, cookie.getValue());
						return;
					}
				}
			}
		}
		throw new RuntimeException("cookie not found");
	}

	public void expectNotExists(File file) {
		Assert.assertTrue("Expected file to not exist", !file.exists());
	}

	public void expectExists(File file) {
		Assert.assertTrue("Expected file to exist", file.exists());
	}

	@Test
	public void loadSampleDataFromDisk() throws Throwable {
		File file = new File("src/test/resources/cookies-4.dat");

		Source from = Okio.source(file);
		BufferedSink to = Okio.buffer(Okio.sink(storage));
		to.writeAll(from);
		to.close();
		manager = new PersistentCookieManager(storage);

		expectCookies(19);
		expectCookie(expedia, "minfo",
			"v.5,EX01734B8FD0$21$A6$DE$34$0B$C7$F1$0F$7C$A8$DF$EA$A6$F6$2C$9E$FE$D4$CC$15$B6yY$FAxO$D8U$B7RJ$39$CA$0D$E4$EB$EAh$89$0E$97$C0$0E4$B1$9A$AA$95$97$C3$AA$E0$F2$A4$36$C6$31$E8$BBX!2$DFD$B8$F3$AF$81$9C");
		manager.put(reviews, REVIEWS_EXPEDIA_COOKIES);
		expectCookie(reviews, "minfo",
			"v.5,EX016141700E$B3$98$D7$34$37$A0J$C2$E9$2Ae$A0$B98$AA$B9$99$93f$E9l$D1$2D$EB$E3$BD$D6L$DB$9A$B33$89w$81$92$FB0$9F$C7D$B1G$CF$83$B5$CE3$B2gu$A7v$B6$9B$89$87$E5$3F$89$DD$89$F5$E8$BBtd$94");
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookie(expedia, "minfo", "v.5,EX015B0EEC4B$0E$81O$3Bq$8C$98$DF$CC$93$DE$A1$ABS$EB$A36$CA$D6$5B$D4g$FF$81$C2a$DD$A6$19$D1$1B0$26$EC$B4$86$C3$27$1D$FB$3F$3A$C6$24$E3$A2$A4$A7X$F7P");
	}
}
