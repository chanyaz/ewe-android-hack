package com.expedia.bookings.test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.utils.HotelsV2DataUtil;
import com.google.gson.Gson;

public class HotelsV2DataUtilTest {

	@Test
	public void v2SearchParamsFromJson() {
		SuggestionV4 suggestionV4 = new SuggestionV4();
		suggestionV4.gaiaId = "1234";
		SuggestionV4.RegionNames regionNames = new SuggestionV4.RegionNames();
		regionNames.displayName = "San Francisco";
		regionNames.shortName = "SFO";
		suggestionV4.regionNames = regionNames;
		List<Integer> childList = new ArrayList<Integer>();
		childList.add(2);
		childList.add(4);
		LocalDate checkIn = new LocalDate("2015-10-20");
		LocalDate checkOut = new LocalDate("2015-10-25");
		int numAdults = 2;
		HotelSearchParams v2params = (HotelSearchParams) new HotelSearchParams.Builder(0).departure(suggestionV4).startDate(checkIn).endDate(checkOut).adults(numAdults).children(childList).build();

		Gson gson = HotelsV2DataUtil.Companion.generateGson();
		String paramsJsonString = gson.toJson(v2params);
		HotelSearchParams newv2params = HotelsV2DataUtil.Companion.getHotelV2SearchParamsFromJSON(paramsJsonString);

		Assert.assertEquals(checkIn, newv2params.getCheckIn());
		Assert.assertEquals(checkOut, newv2params.getCheckOut());
		Assert.assertEquals(suggestionV4.gaiaId, newv2params.getSuggestion().gaiaId);
		Assert.assertEquals(regionNames.displayName, newv2params.getSuggestion().regionNames.displayName);
		Assert.assertEquals(regionNames.shortName, newv2params.getSuggestion().regionNames.shortName);
		Assert.assertEquals(v2params.getAdults(), newv2params.getAdults());
		Assert.assertEquals(v2params.getChildren(), newv2params.getChildren());
	}

	@Test
	public void fromV1SearchParamsToV2SearchParams() {
		com.expedia.bookings.data.HotelSearchParams v1Params = new com.expedia.bookings.data.HotelSearchParams();
		v1Params.setRegionId("1234");
		v1Params.setQuery("San Francisco");
		LocalDate checkIn = new LocalDate("2017-09-27");
		LocalDate checkOut = new LocalDate("2017-09-29");
		v1Params.setCheckInDate(checkIn);
		v1Params.setCheckOutDate(checkOut);

		List<ChildTraveler> childList = new ArrayList<ChildTraveler>();
		childList.add(new ChildTraveler(2, true));
		childList.add(new ChildTraveler(4, true));
		v1Params.setChildren(childList);
		v1Params.setNumAdults(2);

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(v1Params);

		Assert.assertEquals(v1Params.getCheckInDate(), v2params.getCheckIn());
		Assert.assertEquals(v1Params.getCheckOutDate(), v2params.getCheckOut());
		Assert.assertEquals(v1Params.getRegionId(), v2params.getSuggestion().gaiaId);
		Assert.assertEquals(v1Params.getQuery(), v2params.getSuggestion().regionNames.shortName);
		Assert.assertEquals(v1Params.getQuery(), v2params.getSuggestion().regionNames.displayName);
		Assert.assertEquals(v1Params.getNumAdults(), v2params.getAdults());
		List<Integer> child = new ArrayList<Integer>(childList.size());
		for (int index = 0; index < v1Params.getChildren().size(); index++) {
			child.add(v1Params.getChildren().get(index).getAge());
		}

		Assert.assertEquals(child, v2params.getChildren());

	}

	@Test
	public void fromV1SearchParamsToV2SearchParamsWithPastDate() {
		com.expedia.bookings.data.HotelSearchParams v1Params = new com.expedia.bookings.data.HotelSearchParams();
		v1Params.setRegionId("1234");
		v1Params.setQuery("San Francisco");
		LocalDate checkIn = new LocalDate("2014-09-27");
		LocalDate checkOut = new LocalDate("2014-09-29");
		v1Params.setCheckInDate(checkIn);
		v1Params.setCheckOutDate(checkOut);

		List<ChildTraveler> childList = new ArrayList<ChildTraveler>();
		childList.add(new ChildTraveler(2, true));
		childList.add(new ChildTraveler(4, true));
		v1Params.setChildren(childList);
		v1Params.setNumAdults(2);

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(v1Params);

		Assert.assertEquals(LocalDate.now(), v2params.getCheckIn());
		Assert.assertEquals(LocalDate.now().plusDays(1), v2params.getCheckOut());
		Assert.assertEquals(v1Params.getRegionId(), v2params.getSuggestion().gaiaId);
		Assert.assertEquals(v1Params.getQuery(), v2params.getSuggestion().regionNames.shortName);
		Assert.assertEquals(v1Params.getQuery(), v2params.getSuggestion().regionNames.displayName);
		Assert.assertEquals(v1Params.getNumAdults(), v2params.getAdults());
		List<Integer> child = new ArrayList<Integer>(childList.size());
		for (int index = 0; index < v1Params.getChildren().size(); index++) {
			child.add(v1Params.getChildren().get(index).getAge());
		}

		Assert.assertEquals(child, v2params.getChildren());

	}

	@Test
	public void testGuestString() {
		ArrayList children = new ArrayList<Integer>();
		children.add(10);
		children.add(7);
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.coordinates = new SuggestionV4.LatLng();
		HotelSearchParams params = (HotelSearchParams) new HotelSearchParams.Builder(0).departure(suggestion).startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(15)).adults(2).children(children).build();
		Assert.assertEquals("2,10,7", params.getGuestString());
	}
}
