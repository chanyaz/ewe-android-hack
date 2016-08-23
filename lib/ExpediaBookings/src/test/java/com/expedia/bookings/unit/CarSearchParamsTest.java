package com.expedia.bookings.unit;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.cars.CarSearchParam;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CarSearchParamsTest {

	@Test
	public void testCarSearchParamsBuilder() {
		String location = "San Fransisco";
		DateTime dateTime = DateTime.now().withTimeAtStartOfDay();
		CarSearchParam searchParams = (CarSearchParam) new CarSearchParam.Builder()
			.startDateTime(dateTime)
			.endDateTime(dateTime)
			.origin(getDummySuggestion(location)).build();
		assertEquals(location, searchParams.getOriginLocation());
		assertEquals(location, searchParams.getOriginDescription());
		assertEquals(dateTime, searchParams.getStartDateTime());
	}

	@Test
	public void testIncompleteCarSearchParams() {
		try {
			new CarSearchParam.Builder()
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(14)).build();
			//If location or startDate or endDate is empty or null and builder should fail to build params
			fail("This has to throw exception");
		}
		catch (IllegalArgumentException e) {
			//If location is null or empty, builder should throw an IllegalArgumentException
		}
	}

	private SuggestionV4 getDummySuggestion(String locationName) {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "";
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = locationName;
		suggestion.regionNames.fullName = locationName;
		suggestion.regionNames.shortName = locationName;
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = new SuggestionV4.Airport();
		suggestion.hierarchyInfo.airport.airportCode = locationName;
		suggestion.type = "Airport";
		suggestion.isMinorAirport = false;
		return suggestion;
	}
}
