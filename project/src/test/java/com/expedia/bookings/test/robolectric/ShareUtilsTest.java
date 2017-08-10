package com.expedia.bookings.test.robolectric;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.utils.ShareUtils;

@RunWith(RobolectricRunner.class)
public class ShareUtilsTest {

	@Test
	public void testClipHotelName() {
		String hotelName;
		String longMessage;

		Assert.assertEquals(100, repeat('a', 100).length());
		Assert.assertEquals("aaaa", repeat('a', 4));
		Assert.assertEquals("", repeat('a', 0));

		// Short message - No clipping
		hotelName = "Some Hotel";
		longMessage = "George is staying at " + hotelName + " http://e.xpda.co/d2WXONUkwyTxoMvrjgwKzqzdcZ3";
		Assert.assertEquals(hotelName, ShareUtils.clipHotelName(longMessage.length(), hotelName));

		// Clip to shortest hotel name length
		hotelName = repeat('a', 100);
		longMessage = repeat('b', 140 + hotelName.length());
		Assert.assertEquals(20, ShareUtils.clipHotelName(longMessage.length(), hotelName).length());

		// Don't clip more than 20 characters
		hotelName = repeat('a', 100);
		longMessage = repeat('b', 150 + hotelName.length());
		Assert.assertEquals(20, ShareUtils.clipHotelName(longMessage.length(), hotelName).length());

		// Don't clip hotel names shorter than 20 characters
		hotelName = repeat('a', 15);
		longMessage = repeat('b', 150 + hotelName.length());
		Assert.assertEquals(15, ShareUtils.clipHotelName(longMessage.length(), hotelName).length());
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
