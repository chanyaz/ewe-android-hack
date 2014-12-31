package com.expedia.bookings.test.unit.tests;


import junit.framework.TestCase;

import com.expedia.bookings.utils.ShareUtils;

public class ShareUtilsTests extends TestCase {

	public void testClipHotelName() {
		String hotelName;
		String longMessage;

		assertEquals(100, repeat('a', 100).length());
		assertEquals("aaaa", repeat('a', 4));
		assertEquals("", repeat('a', 0));

		// Short message - No clipping
		hotelName = "Some Hotel";
		longMessage = "George is staying at " + hotelName + " http://e.xpda.co/d2WXONUkwyTxoMvrjgwKzqzdcZ3";
		assertEquals(hotelName, ShareUtils.clipHotelName(longMessage.length(), hotelName));

		// Clip to shortest hotel name length
		hotelName = repeat('a', 100);
		longMessage = repeat('b', 140 + hotelName.length());
		assertEquals(20, ShareUtils.clipHotelName(longMessage.length(), hotelName).length());

		// Don't clip more than 20 characters
		hotelName = repeat('a', 100);
		longMessage = repeat('b', 150 + hotelName.length());
		assertEquals(20, ShareUtils.clipHotelName(longMessage.length(), hotelName).length());
	}

	private String repeat(char c, int num) {
		StringBuilder sb = new StringBuilder();
		while (num > 0) {
			sb.append(c);
			num--;
		}
		return sb.toString();
	}
}
