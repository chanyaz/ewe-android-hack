package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.hotels.SuggestionV4;
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
	}

	@Test
	public void testGenerateDeepLink() {
		String hotelId = "123456";
		SuggestionV4 suggestionV4 = new SuggestionV4();
		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		HotelSearchParams hotelSearchParams = new HotelSearchParams(suggestionV4, dtf.parseLocalDate("2015-09-12"),
			dtf.parseLocalDate("2015-09-16"), 3, new ArrayList<Integer>());
		String expected = "expda://hotelSearch?hotelId=123456&checkInDate=2015-09-12"
			+ "&checkOutDate=2015-09-16&numAdults=3";
		Assert.assertEquals(expected, ShareUtils.generateDeepLink(hotelId, hotelSearchParams));

		HotelSearchParams hotelSearchParams1 = new HotelSearchParams(suggestionV4, dtf.parseLocalDate("2015-09-12"),
			dtf.parseLocalDate("2015-09-16"), 3, new ArrayList<Integer>(Arrays.asList(2, 3, 4)));

		String expected1 = "expda://hotelSearch?hotelId=123456&checkInDate=2015-09-12"
			+ "&checkOutDate=2015-09-16&numAdults=3&childAges=2,3,4";

		Assert.assertEquals(expected1, ShareUtils.generateDeepLink(hotelId, hotelSearchParams1));
	}

	@Test
	public void testShareIntent() {
		String hotelId = "123456";
		SuggestionV4 suggestionV4 = new SuggestionV4();
		final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");
		HotelSearchParams hotelSearchParams = new HotelSearchParams(suggestionV4, dtf.parseLocalDate("2015-09-12"),
			dtf.parseLocalDate("2015-09-16"), 3, new ArrayList<Integer>());
		String expected = "expda://hotelSearch?hotelId=123456&checkInDate=2015-09-12"
			+ "&checkOutDate=2015-09-16&numAdults=3";
		Context context = RuntimeEnvironment.application.getApplicationContext();
		Intent shareIntent = ShareUtils.generateShareIntent(context, hotelId, hotelSearchParams);
		Assert.assertNotNull(shareIntent);
		Bundle extras = shareIntent.getExtras();
		Assert.assertEquals(expected, extras.getString(android.content.Intent.EXTRA_TEXT));
		Assert.assertEquals(context.getResources().getString(R.string.share_hotel_listing),
			extras.getString(Intent.EXTRA_SUBJECT));
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
