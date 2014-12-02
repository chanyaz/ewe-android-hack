package com.expedia.bookings.utils;

import java.util.Collection;
import java.util.Iterator;

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

	public static <T extends CharSequence> T slice(CharSequence str, int start) {
		return (T) slice(str, start, null);
	}

	public static <T extends CharSequence> T slice(CharSequence str, int start, Integer end) {
		int len = str.length();

		if (start < 0) {
			start = len + start;
		}

		if (end == null) {
			end = len;
		}
		else if (end < 0) {
			end = len + end;
		}

		// If the user put us in an awkward place, just return the empty string
		if (start > len || end < start) {
			return (T) str.subSequence(0, 0);
		}

		return (T) str.subSequence(start, end);
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
}
