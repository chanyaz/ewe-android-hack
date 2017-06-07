package com.expedia.bookings.unit;

import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

import com.expedia.bookings.utils.Strings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringsTests {
	@Test
	public void testStringsConstructor() {
		new Strings();
	}

	@Test
	public void testStringsIsEmpty() {
		assertTrue(Strings.isEmpty(null));
		assertTrue(Strings.isEmpty(""));

		assertFalse(Strings.isEmpty("a"));
	}

	@Test
	public void testStringsIsNotEmpty() {
		assertTrue(Strings.isNotEmpty("a"));

		assertFalse(Strings.isNotEmpty(null));
		assertFalse(Strings.isNotEmpty(""));
	}

	@Test
	public void testStringsJoinWithoutEmpties() {
		ArrayList<String> list;
		final String expected = "hello world";
		String joined;

		// null returns null
		joined = Strings.joinWithoutEmpties(" ", null);
		assertEquals(null, joined);

		// test simple join
		list = new ArrayList<String>();
		list.add("hello");
		list.add("world");

		joined = Strings.joinWithoutEmpties(" ", list);
		assertEquals(expected, joined);

		// test join with null
		list = new ArrayList<String>();
		list.add("hello");
		list.add(null);
		list.add("world");

		joined = Strings.joinWithoutEmpties(" ", list);
		assertEquals(expected, joined);

		// test join with empty
		list = new ArrayList<String>();
		list.add("hello");
		list.add("");
		list.add("world");

		joined = Strings.joinWithoutEmpties(" ", list);
		assertEquals(expected, joined);

		// test join with empty and null
		list = new ArrayList<String>();
		list.add("");
		list.add(null);
		list.add("hello");
		list.add("");
		list.add(null);
		list.add("world");
		list.add("");
		list.add(null);

		joined = Strings.joinWithoutEmpties(" ", list);
		assertEquals(expected, joined);
	}

	@Test
	public void testStringsEquals() {
		final String a = "a";
		final String b = "b";

		// Trivial positive base cases
		assertTrue(Strings.equals(null, null));
		assertTrue(Strings.equals("", ""));
		assertTrue(Strings.equals("a", "a"));
		assertTrue(Strings.equals(a, a));

		// Trivial negative base cases
		assertFalse(Strings.equals(null, ""));
		assertFalse(Strings.equals("", null));
		assertFalse(Strings.equals("a", null));
		assertFalse(Strings.equals("a", ""));
		assertFalse(Strings.equals(null, "a"));
		assertFalse(Strings.equals("", "a"));
		assertFalse(Strings.equals("a", "b"));
		assertFalse(Strings.equals(a, b));
		assertFalse(Strings.equals(b, a));

		// Non Strings
		final StringBuilder empty = new StringBuilder();
		final StringBuilder first = new StringBuilder();
		final StringBuilder second = new StringBuilder();
		final StringBuilder third = new StringBuilder();
		final StringBuilder forth = new StringBuilder();
		first.append("aa");
		second.append("bb");
		third.append("aaa");
		forth.append("aa");

		assertTrue(Strings.equals(first, first));
		assertTrue(Strings.equals(first, "aa"));
		assertTrue(Strings.equals("aa", first));

		assertTrue(Strings.equals(empty, ""));
		assertTrue(Strings.equals("", empty));

		assertFalse(Strings.equals(first, second));
		assertFalse(Strings.equals(first, third));
		assertFalse(Strings.equals(empty, first));
	}

	@Test
	public void testStringsCompareTo() {
		final String a = "a";
		final String b = "b";
		final String c = "c";

		assertEquals(Strings.compareTo(null, null), 0);
		assertEquals(Strings.compareTo(null, ""), 0);
		assertEquals(Strings.compareTo(a, a), a.compareTo(a));

		assertEquals(Strings.compareTo(a, null), 1);
		assertEquals(Strings.compareTo(null, a), -1);

		assertEquals(Strings.compareTo(a, b), a.compareTo(b));
		assertEquals(Strings.compareTo(b, a), b.compareTo(a));
		assertEquals(Strings.compareTo(c, a), c.compareTo(a));
	}

	@Test
	public void testStringsSlice() {
		assertEquals("", Strings.slice("", 0, 0));
		assertEquals("", Strings.slice("abcd", 0, 0));

		assertEquals("a", Strings.slice("abcd", 0, 1));
		assertEquals("ab", Strings.slice("abcd", 0, 2));
		assertEquals("abc", Strings.slice("abcd", 0, 3));
		assertEquals("abcd", Strings.slice("abcd", 0, 4));

		assertEquals("d", Strings.slice("abcd", -1));
		assertEquals("cd", Strings.slice("abcd", -2));
		assertEquals("c", Strings.slice("abcd", -2, -1));

		// Stupid input gives us the empty string
		assertEquals("", Strings.slice("abcd", -100, 0));
		assertEquals("", Strings.slice("abcd", 0, -100));
		assertEquals("", Strings.slice("abcd", 5, 6));
	}

	@Test
	public void testStringsFormatHexString() {
		assertEquals("", Strings.formatHexString(""));
		assertEquals("61", Strings.formatHexString("a"));
		assertEquals("6162636465", Strings.formatHexString("abcde"));
	}

	@Test
	public void cutAtWordBarrier() {
		final String firstCutAtWord = "Ain't";
		final String secondCutAtWord = "Ain't no";
		final String thirdCutAtWord = "Ain't no sunshine";
		final String body = "Ain't no sunshine when she's gone";

		// Don't try and cut past the end of a string
		assertEquals(firstCutAtWord.length(), Strings.cutAtWordBarrier(firstCutAtWord, 10));

		// Cutting to the work barrier
		assertEquals(firstCutAtWord.length(), Strings.cutAtWordBarrier(body, 5));
		assertEquals(secondCutAtWord.length(), Strings.cutAtWordBarrier(body, 7));
		assertEquals(thirdCutAtWord.length(), Strings.cutAtWordBarrier(body, 14));
	}

	@Test
	public void testStringsCapitalizeFirstLetter() {
		assertEquals("", Strings.capitalizeFirstLetter(""));
		assertEquals(null, Strings.capitalizeFirstLetter(null));
		assertEquals("Abcd", Strings.capitalizeFirstLetter("aBCD"));
		assertEquals("Bcde", Strings.capitalizeFirstLetter("BCDE"));
		assertEquals("Bcde", Strings.capitalizeFirstLetter("bcde"));
	}

	@Test
	public void testStringsSplitAndCapitalizeFirstLetters() {
		assertEquals("", Strings.splitAndCapitalizeFirstLetters(""));
		assertEquals(null, Strings.splitAndCapitalizeFirstLetters(null));
		assertEquals("AbcdEfgh", Strings.splitAndCapitalizeFirstLetters("aBCD_EFGH"));
		assertEquals("BcdeDnas", Strings.splitAndCapitalizeFirstLetters("BCDE_dnAS"));
		assertEquals("DirectAgency", Strings.splitAndCapitalizeFirstLetters("DIRECT_AGENCY"));
		assertEquals("Merchant", Strings.splitAndCapitalizeFirstLetters("MERCHANT"));
	}

	@Test
	public void testCapitalizeString() {
		assertEquals("", Strings.capitalize("", Locale.US));
		assertEquals(null, Strings.capitalize(null, Locale.US));
		assertEquals("Abcd_Efgh", Strings.capitalize("aBCD_EFGH", '_', Locale.US));
		assertEquals("Bcde*Dnas", Strings.capitalize("bCDE*dnAS", '*', Locale.US));
		assertEquals("Direct Agency", Strings.capitalize("DIRECT AGENCY", Locale.US));
		assertEquals("Merchant", Strings.capitalize("MERCHANT", Locale.US));
	}

	@Test
	public void testEscapeQuotes() {
		String textWithQuotes = "Test &quot;One&quot;";
		String expectedTextWithQuotes = "Test \"One\"";
		String textWithoutQuotes = "Test One";

		assertEquals(expectedTextWithQuotes, Strings.escapeQuotes(textWithQuotes));
		assertEquals(textWithoutQuotes, Strings.escapeQuotes(textWithoutQuotes));
	}

	@Test
	public void testcharacterCutOffWithMinBulletsShown() {
		String content = null;
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 1));

		content = "";
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 1));

		content = "<p>abcdefghijklmnopqrstuvwxyz</p>";
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 1));
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 2));

		content = "<p>abcdefghijklmnopqrstuvwxyz</p><ul><li>123</li><li>12345</li><li>1234567890</li>";
		assertEquals(49, Strings.characterCutOffWithMinBulletsShown(content, 1));
		assertEquals(63, Strings.characterCutOffWithMinBulletsShown(content, 2));

		content = "<p>abcdefghijklmnopqrstuvwxyz</p><ul><li>123</li>";
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 1));
		assertEquals(37, Strings.characterCutOffWithMinBulletsShown(content, 0));

		content = "abcdefghijklmnopqrstuvwxyz";
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 1));

		content = "abcdefghijklmnopqrstuvwxyz<ul><li>123</li><li>12345</li><li>1234567890</li>";
		assertEquals(30, Strings.characterCutOffWithMinBulletsShown(content, 0));
		assertEquals(42, Strings.characterCutOffWithMinBulletsShown(content, 1));
		assertEquals(56, Strings.characterCutOffWithMinBulletsShown(content, 2));
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 3));
		assertEquals(0, Strings.characterCutOffWithMinBulletsShown(content, 4));
	}
}
