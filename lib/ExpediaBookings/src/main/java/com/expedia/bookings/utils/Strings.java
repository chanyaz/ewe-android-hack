package com.expedia.bookings.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Strings {

	public static boolean isEmpty(CharSequence seq) {
		return seq == null || seq.length() <= 0;
	}

	public static boolean isNotEmpty(CharSequence seq) {
		return !isEmpty(seq);
	}

	/**
	 * Joins together a bunch of Strings, much like in Python,
	 * except that it ignores nulls and empty strings
	 *
	 * For example, joining [ "a", "", "b", null, "c" ] with ", " would
	 * result in "a, b, c"
	 *
	 * @param items a collection of strings
	 * @param sep the separator between each item
	 * @return joined string
	 */
	public static String joinWithoutEmpties(final CharSequence sep, final Collection<? extends CharSequence> items) {
		if (items == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		int a = 0;
		Iterator<? extends CharSequence> it = items.iterator();
		while (it.hasNext()) {
			CharSequence str = it.next();
			if (Strings.isNotEmpty(str)) {
				if (a > 0) {
					sb.append(sep);
				}
				sb.append(str);
				a++;
			}
		}

		return sb.toString();
	}

	public static boolean equals(CharSequence a, CharSequence b) {
		if (a == b) {
			return true;
		}

		int length;
		if (a != null && b != null && (length = a.length()) == b.length()) {
			if (a instanceof String && b instanceof String) {
				return a.equals(b);
			}
			else {
				for (int i = 0; i < length; i++) {
					if (a.charAt(i) != b.charAt(i)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	public static int compareTo(String a, String b) {
		if (Strings.equals(a, b)) {
			return 0;
		}

		if (a == null) {
			a = "";
		}
		if (b == null) {
			b = "";
		}
		return a.compareTo(b);
	}

	/**
	 * Does a string slice in the style of Python
	 *
	 * If you enter bullshit params, you will get an empty string.
	 */

	public static CharSequence slice(CharSequence str, int start) {
		return slice(str, start, null);
	}

	public static CharSequence slice(CharSequence str, int start, Integer end) {
		int len = str.length();

		if (start < 0) {
			start = len + start;
		}

		if (end == null) {
			end = len;
		}
		if (end < 0) {
			end = len + end;
		}

		// If the user put us in an awkward place, just return the empty string
		if (start < 0 || start > len) {
			return str.subSequence(0, 0);
		}
		if (end < start) {
			return str.subSequence(0, 0);
		}

		return str.subSequence(start, end);
	}

	/**
	 * Formats the given string as its hex representation of the underlying bytes
	 */
	public static String formatHexString(String str) {
		StringBuilder sb = new StringBuilder();
		if (Strings.isEmpty(str)) {
			return str;
		}

		for (byte b: str.getBytes()) {
			sb.append(String.format("%02x", b & 0xff));
		}

		return sb.toString();
	}

	public static String toPrettyString(Object any) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(any);
	}

	public static int cutAtWordBarrier(CharSequence body, int cutoffLimit) {
		int before = cutoffLimit;
		for (int i = cutoffLimit; i > 0; i--) {
			char c = body.charAt(i);
			if (c == ' ' || c == ',' || c == '.') {
				before = i;
				break;
			}
		}
		while (body.charAt(before) == ' ' || body.charAt(before) == ',' || body.charAt(before) == '.') {
			before--;
		}
		before++;
		int after = cutoffLimit;
		for (int i = cutoffLimit; i < body.length(); i++) {
			char c = body.charAt(i);
			if (c == ' ' || c == ',' || c == '.') {
				after = i;
				break;
			}
		}
		int leftDistance = Math.abs(cutoffLimit - before);
		int rightDistance = Math.abs(after - cutoffLimit);
		return (leftDistance < rightDistance) ? before : after;
	}

	public static String capitalizeFirstLetter(String word) {
		if (Strings.isEmpty(word)) {
			return word;
		}
		String upper = word.substring(0, 1).toUpperCase(Locale.US);
		String lower = word.substring(1).toLowerCase(Locale.US);
		return upper + lower;
	}
}
