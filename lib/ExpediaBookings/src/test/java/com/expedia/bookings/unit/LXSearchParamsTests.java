package com.expedia.bookings.unit;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.SearchType;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LXSearchParamsTests {

	@Test
	public void testLXSearchParamsBuilder() {
		String location = "San Fransisco";
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().location(location)
			.imageCode("SFO")
			.modQualified(true)
			.searchType(SearchType.EXPLICIT_SEARCH)
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(14)).build();
		assertEquals(location, searchParams.getLocation());
		assertEquals(LocalDate.now(), searchParams.getActivityStartDate());
		assertEquals(LocalDate.now().plusDays(14), searchParams.getActivityEndDate());
		assertEquals("SFO", searchParams.getImageCode());
		assertEquals(SearchType.EXPLICIT_SEARCH, searchParams.getSearchType());
		assertEquals("", searchParams.getFilters());
		assertEquals("", searchParams.getActivityId());
		assertEquals(true, searchParams.getModQualified());
	}

	@Test
	public void testLocationSetUpInLXSearchParams() {
		LxSearchParams actualSearchParams;
		actualSearchParams = buildSearchParams(true, "New York");
		assertEquals("New York", actualSearchParams.getLocation());

		actualSearchParams = buildSearchParams(true, "San Fransisco");
		assertEquals("San Fransisco", actualSearchParams.getLocation());
	}

	@Test
	public void testIncompleteLXSearchParams() {
		try {
			new LxSearchParams.Builder().location("")
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(14)).build();
			//If location or startDate or endDate is empty or null and builder should fail to build params
			fail("This has to throw exception");
		}
		catch (IllegalArgumentException e) {
			//If location is null or empty, builder should throw an IllegalArgumentException
		}
	}

	private LxSearchParams buildSearchParams(boolean buildFromSuggestion, String location) {
		if (buildFromSuggestion) {
			return (LxSearchParams) new LxSearchParams.Builder().destination(getDummySuggestion(location))
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(14)).build();
		}
		else {
			return (LxSearchParams) new LxSearchParams.Builder().location(location)
				.startDate(LocalDate.now())
				.endDate(LocalDate.now().plusDays(14)).build();
		}
	}


	private SuggestionV4 getDummySuggestion(String locationName) {

		SuggestionV4 dummySuggestion = new SuggestionV4();
		SuggestionV4.RegionNames regionNames = new SuggestionV4.RegionNames();
		SuggestionV4.LatLng coordinates = new SuggestionV4.LatLng();

		regionNames.displayName = locationName;
		regionNames.shortName = locationName;
		regionNames.fullName = locationName;
		coordinates.lat = 0;
		coordinates.lng = 0;
		dummySuggestion.regionNames = regionNames;
		dummySuggestion.coordinates = coordinates;
		dummySuggestion.iconType = SuggestionV4.IconType.SEARCH_TYPE_ICON;
		return dummySuggestion;
	}
}
