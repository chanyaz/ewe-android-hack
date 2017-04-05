package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.utils.HotelUtils;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
public class HotelUtilsTest {

	@Test
	public void testFirstUncommonHotelIndex() throws Throwable {
		ArrayList<Hotel> firstHotelsList = new ArrayList();
		ArrayList<Hotel> secondHotelsList = new ArrayList();

		for (int i = 0; i < 15; i++) {
			Hotel hotel = new Hotel();
			hotel.hotelId = i + "";
			firstHotelsList.add(hotel);
			secondHotelsList.add(hotel);
		}

		for (int i = 15; i < 25; i++) {
			Hotel hotel = new Hotel();
			hotel.hotelId = i + "";
			secondHotelsList.add(hotel);
		}
		assertEquals(Integer.MAX_VALUE, HotelUtils.getFirstUncommonHotelIndex(firstHotelsList, secondHotelsList));
		assertEquals(Integer.MAX_VALUE, HotelUtils.getFirstUncommonHotelIndex(secondHotelsList, firstHotelsList));

		Hotel hotel = new Hotel();
		hotel.hotelId = 18 + "";
		firstHotelsList.add(hotel);

		assertEquals(15, HotelUtils.getFirstUncommonHotelIndex(firstHotelsList, secondHotelsList));
		assertEquals(15, HotelUtils.getFirstUncommonHotelIndex(secondHotelsList, firstHotelsList));
	}
}
