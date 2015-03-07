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

public class PersistentCookieManagerTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final Map<String, List<String>> NO_COOKIES = new HashMap();
	private static final Map<String, List<String>> EXPEDIA_COOKIES = new HashMap() {
		{
			ArrayList<String> list = new ArrayList<>();
			list.add("tpid=v.1,1;Domain=.expedia.com;Path=/;Max-Age=86400");
			list.add("TH=Zq1lI0f9YKT2/TwF8RKlZoltqBiQRIVt6+CeeeShGPbUa7trUEYzeQ94VLqfTlsrIApXJvOb/4o=|VNgEN5B50gAOfASNHIjtpg8Z/ucPcBwzPSZEgxBSozSIhHn3mcz0N21PU9QbNgKX02tk+PhOnZMX16DGvJ+enbPsqDVe33qF|sa5pvCHP8aYf8DKjxjUKO4rW98xwy5y1; Domain=.expedia.com; Expires=Fri, 29-May-2015 15:08:32 GMT; Path=/");
			list.add("SSID1=BwCKTh3EAAAAAADsbu5U2WcCEOxu7lQBAAAAAAAAAAAA7G7uVAAKihsEAAFEZAAA7G7uVAEACwQAAaFjAADsbu5UAQD1AwABBWMAAOxu7lQBABkEAAE2ZAAA7G7uVAEADgQAAbljAADsbu5UAQAABAABSGMAAOxu7lQBABcEAAEwZAAA7G7uVAEAEgQAARlkAADsbu5UAQD0AwAB-WIAAOxu7lQBAOkDAAFkYQAA7G7uVAEAAwQAAVFjAADsbu5UAQAYBAABMWQAAOxu7lQBAA8EAAHDYwAA7G7uVAEAEAQAAcdjAADsbu5UAQA; path=/; domain=.expedia.com; expires=Fri, 26-Feb-2016 00:55:08 GMT");
			list.add("SSSC1=1.G6119950903803013081.1|1001.24932:1012.25337:1013.25349:1024.25416:1027.25425:1035.25505:1038.25529:1039.25539:1040.25543:1042.25625:1047.25648:1048.25649:1049.25654:1051.25668; path=/; domain=.expedia.com");
			list.add("SSRT1=7G7uVAIAAA; path=/; domain=.expedia.com; expires=Fri, 26-Feb-2016 00:55:08 GMT");
			list.add("SSLB=1; path=/; domain=.expedia.com");
			list.add("MC1=GUID=4a7e5c02232b479aa4807d32c6b7129c; Domain=.expedia.com; Expires=Fri, 28-Feb-2020 17:48:28 GMT; Path=/");
			list.add("JSESSION=890e137e-c002-4718-b00f-76c4877bb296; Domain=.expedia.com; Path=/");
			list.add("tpid=v.1,1; Domain=.expedia.com; Expires=Mon, 09-Mar-2015 14:41:48 GMT; Path=/");
			list.add("iEAPID=0,; Domain=.expedia.com; Path=/");
			list.add("linfo=v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1; Domain=.expedia.com; Expires=Fri, 28-Feb-2020 17:48:28 GMT; Path=/");
			list.add("SSPV1=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA; path=/; domain=.expedia.com; expires=Fri, 26-Feb-2016 00:55:08 GMT");
			put("Set-Cookie", list);
		}
	};

	private URI expedia;
	private File storage;
	private File oldCookieStorage;
	private PersistentCookieManager manager;

	@Before
	public void before() throws Throwable {
		expedia = new URI("http://www.expedia.com");
		storage = folder.newFile();
		storage.delete();

		oldCookieStorage = folder.newFile();
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
		expectCookies(11);
		expectCookie("MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
		expectExists(storage);

		// Make sure we can overwrite the file
		manager.put(expedia, EXPEDIA_COOKIES);
		expectCookies(11);
		expectExists(storage);
		expectCookie("MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
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
		expectCookies(11);

		manager.clear();
		expectCookies(0);
		expectNotExists(storage);
	}

	@Test
	public void removeCookiesByName() throws Throwable {
		expectNotExists(storage);
		manager.put(expedia, EXPEDIA_COOKIES);
		expectExists(storage);
		expectCookies(11);

		manager.removeNamedCookies(new String[] {"MC1"});
		expectCookies(10);
		expectExists(storage);

		manager.removeNamedCookies(new String[] {"JSESSION", "SSID1"});
		expectCookies(8);
		expectExists(storage);
	}

	@Test
	public void saveThenLoadExpediaCookies() throws Throwable {
		saveExpediaCookies();
		expectExists(storage);

		manager = new PersistentCookieManager(storage);
		expectCookies(11);
		expectCookie("MC1", "GUID=4a7e5c02232b479aa4807d32c6b7129c");
		expectExists(storage);
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

	@Test
	public void parseBadData() throws Throwable {
		final String emptyData = "[";
		BufferedWriter writer = new BufferedWriter(new FileWriter(storage));
		writer.write(emptyData);
		writer.close();

		manager = new PersistentCookieManager(storage);
		expectCookies(0);
	}

	@Test
	public void fillWithOldCookies() throws Throwable {
		List<HttpCookie> oldCookies;

		// No old file exists
		expectNotExists(storage);
		expectNotExists(oldCookieStorage);
		PersistentCookieManager.fillWithOldCookies(manager, oldCookieStorage);
		expectNotExists(storage);
		expectNotExists(oldCookieStorage);
		expectCookies(0);

		// Write old cookies to file
		final String oldCookieString = "[{\"domain\":\".expedia.com\",\"name\":\"SSLB\",\"path\":\"\\/\",\"value\":\"1\",\"version\":0,\"maxAge\":-1},{\"domain\":\".expedia.com\",\"name\":\"SSID1\",\"path\":\"\\/\",\"value\":\"BwAJdx3gAAAAAABdP_VUoMwOIl0_9VQBAAAAAAAAAAAAXT_1VAAKigsEAAGiYwAAXT_1VAEADgQAAbVjAABdP_VUAQAABAABR2MAAF0_9VQBABcEAAEwZAAAXT_1VAEAGgQAAT9kAABdP_VUAQAPBAABwWMAAF0_9VQBABAEAAHGYwAAXT_1VAEAGwQAAURkAABdP_VUAQD1AwABBWMAAF0_9VQBABIEAAEZZAAAXT_1VAEA9AMAAftiAABdP_VUAQANBAABrmMAAF0_9VQBAAMEAAFSYwAAXT_1VAEA6QMAAWRhAABdP_VUAQAYBAABMWQAAF0_9VQBAB0EAAFnZAAAXT_1VAEA\",\"version\":0,\"maxAge\":31535999},{\"domain\":\".expedia.com\",\"name\":\"SSSC1\",\"path\":\"\\/\",\"value\":\"1.G6121868937715960992.1|1001.24932:1012.25339:1013.25349:1024.25415:1027.25426:1035.25506:1037.25518:1038.25525:1039.25537:1040.25542:1042.25625:1047.25648:1048.25649:1050.25663:1051.25668:1053.25703\",\"version\":0,\"maxAge\":-1},{\"domain\":\".expedia.com\",\"name\":\"SSRT1\",\"path\":\"\\/\",\"value\":\"Zj_1VAIAAA\",\"version\":0,\"maxAge\":31535996},{\"domain\":\".expedia.com\",\"name\":\"SSPV1\",\"path\":\"\\/\",\"value\":\"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\"version\":0,\"maxAge\":31535999},{\"domain\":\".expedia.com\",\"name\":\"MC1\",\"path\":\"\\/\",\"value\":\"GUID=7d4075a155fc4342bcd2254e05974c9d\",\"version\":0,\"maxAge\":157999995},{\"domain\":\".expedia.com\",\"name\":\"JSESSION\",\"path\":\"\\/\",\"value\":\"ed6c1552-a215-4228-87cd-9fe8faf632b4\",\"version\":0,\"maxAge\":-1},{\"domain\":\".expedia.com\",\"name\":\"tpid\",\"path\":\"\\/\",\"value\":\"v.1,1\",\"version\":0,\"maxAge\":999995},{\"domain\":\".expedia.com\",\"name\":\"iEAPID\",\"path\":\"\\/\",\"value\":\"0,\",\"version\":0,\"maxAge\":-1},{\"domain\":\".expedia.com\",\"name\":\"linfo\",\"path\":\"\\/\",\"value\":\"v.4,|0|0|255|1|0||||||||1033|0|0||0|0|0|-1|-1\",\"version\":0,\"maxAge\":157999995},{\"domain\":\".expedia.com\",\"name\":\"TH\",\"path\":\"\\/\",\"value\":\"XGyhCyf4AyOlFm6ALVGc4STmAmvh7Xgki\\/lLoEpG8XjABKu\\/PsH2zRZmJmip\\/I3edxS5Bbi38hs+jb0TIPYDRyCCg61RpL55|hj4gwY6zfEk=\",\"version\":0,\"maxAge\":7999999},{\"domain\":\".hlserve.com\",\"name\":\"hmGUID\",\"path\":\"\\/\",\"value\":\"3a163cdd-ef48-4555-16cc-45e38817b894\",\"version\":0,\"maxAge\":31535999}]";
		BufferedWriter writer = new BufferedWriter(new FileWriter(oldCookieStorage));
		writer.write(oldCookieString);
		writer.close();

		expectExists(oldCookieStorage);
		PersistentCookieManager.fillWithOldCookies(manager, oldCookieStorage);
		expectNotExists(oldCookieStorage);
		expectNotExists(storage);
		expectCookies(12);
		expectCookie("MC1", "GUID=7d4075a155fc4342bcd2254e05974c9d");
	}

	@Test
	public void parseOldBadCookies() throws Throwable {
		// Write old cookies to file
		final String oldCookieString = "]";
		BufferedWriter writer = new BufferedWriter(new FileWriter(storage));
		writer.write(oldCookieString);
		writer.close();

		PersistentCookieManager.fillWithOldCookies(manager, storage);
		expectCookies(0);
		expectNotExists(storage);
	}

	public void expectCookies(int num) {
		List<HttpCookie> cookies = manager.getCookieStore().getCookies();
		Assert.assertEquals("cookies: " + Strings.toPrettyString(cookies), num, cookies.size());
	}

	public void expectCookie(final String name, final String value) {
		List<HttpCookie> cookies = manager.getCookieStore().getCookies();
		for (HttpCookie cookie : cookies) {
			if (Strings.equals(name, cookie.getName())) {
				Assert.assertEquals(value, cookie.getValue());
				return;
			}
		}

		Assert.fail("No cookie with name=" + name + " found. Cookies = " + Strings.toPrettyString(cookies));
	}

	public void expectNotExists(File file) {
		Assert.assertTrue("Expected file to not exist", !file.exists());
	}

	public void expectExists(File file) {
		Assert.assertTrue("Expected file to exist", file.exists());
	}
}
