package com.expedia.bookings.test.unit.tests;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.expedia.bookings.utils.StrUtils;

public class StrUtilsTests extends TestCase {

	public void assertNotEquals(Object left, Object right) {
		assertTrue(!(left.equals(right)));
	}

	public void testCompareTo() {
		final String a = "a";
		final String b = "b";
		final String c = "c";

		assertEquals(StrUtils.compareTo(null, null), 0);
		assertEquals(StrUtils.compareTo(null, ""), 0);
		assertEquals(StrUtils.compareTo(a, a), a.compareTo(a));

		assertEquals(StrUtils.compareTo(a, null), 1);
		assertEquals(StrUtils.compareTo(null, a), -1);

		assertEquals(StrUtils.compareTo(a, b), a.compareTo(b));
		assertEquals(StrUtils.compareTo(b, a), b.compareTo(a));
		assertEquals(StrUtils.compareTo(c, a), c.compareTo(a));
	}

	public void testJoinWithoutEmpties() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("hello");
		list.add(null);
		list.add("");
		list.add("world");

		String joined = StrUtils.joinWithoutEmpties(" ", list);
		assertEquals(joined, "hello world");
	}

}
