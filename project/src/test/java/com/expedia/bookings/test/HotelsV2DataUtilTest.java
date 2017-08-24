package com.expedia.bookings.test;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.flights.FlightSearchParams;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.utils.HotelsV2DataUtil;
import com.google.gson.Gson;

import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.test.robolectric.UserLoginTestUtil;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;

@RunWith(RobolectricRunner.class)
@Config(shadows = {ShadowGCM.class,ShadowUserManager.class,ShadowAccountManagerEB.class})
public class HotelsV2DataUtilTest {
	private final Context context = RuntimeEnvironment.application;

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
		HotelSearchParams v2params = (HotelSearchParams) new HotelSearchParams.Builder(0, 500, true)
			.destination(suggestionV4)
			.startDate(checkIn)
			.endDate(checkOut)
			.adults(numAdults)
			.children(childList).build();

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

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(context, v1Params);

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

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(context, v1Params);

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
	public void testGetHotelV2SearchParamsFromFlightV2RoundTrip() {
		FlightSearchParams testSearchParams = setupFlightSearchParams();
		String testArrivalDate = org.joda.time.DateTime.now().plusDays(1).toString();
		String testDepartureDate = org.joda.time.DateTime.now().plusDays(5).toString();

		FlightLeg outboundFlightLeg = setupFlightLeg(testArrivalDate);
		FlightLeg inboundFlightLeg = setupFlightLeg(testDepartureDate);
		inboundFlightLeg.segments.get(0).departureTimeRaw = testDepartureDate;

		List<FlightLeg> flightLegs = new ArrayList<>();
		flightLegs.add(outboundFlightLeg);
		flightLegs.add(inboundFlightLeg);

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2ParamsFromFlightV2Params(flightLegs, testSearchParams);

		Assert.assertEquals(LocalDate.now().plusDays(1), v2params.getCheckIn());
		Assert.assertEquals(LocalDate.now().plusDays(5), v2params.getCheckOut());

		SuggestionV4 testArrivalAirport = testSearchParams.getArrivalAirport();
		Assert.assertEquals(testArrivalAirport.regionNames, v2params.getSuggestion().regionNames);

		Assert.assertEquals(testArrivalAirport.gaiaId, v2params.getSuggestion().gaiaId);
		Assert.assertEquals(testArrivalAirport.coordinates, v2params.getSuggestion().coordinates);
		Assert.assertEquals(testArrivalAirport.type, v2params.getSuggestion().type);
		Assert.assertTrue(v2params.getShopWithPoints());

		Assert.assertEquals(testSearchParams.getGuests(), v2params.getGuests());
		Assert.assertEquals(testSearchParams.getAdults(), v2params.getAdults());
		Assert.assertEquals(testSearchParams.getChildren(), v2params.getChildren());
	}

	@Test
	public void testGetHotelV2SearchParamsFromFlightV2OneWay() {
		FlightSearchParams testSearchParams = setupFlightSearchParams();
		String testArrivalDate = org.joda.time.DateTime.now().plusDays(1).toString();

		FlightLeg outboundFlightLeg = setupFlightLeg(testArrivalDate);

		List<FlightLeg> flightLegs = new ArrayList<>();
		flightLegs.add(outboundFlightLeg);

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2ParamsFromFlightV2Params(flightLegs, testSearchParams);

		Assert.assertEquals(LocalDate.now().plusDays(1), v2params.getCheckIn());
		Assert.assertEquals(LocalDate.now().plusDays(2), v2params.getCheckOut());

		SuggestionV4 testArrivalAirport = testSearchParams.getArrivalAirport();
		Assert.assertEquals(testArrivalAirport.regionNames, v2params.getSuggestion().regionNames);
		Assert.assertEquals(testArrivalAirport.gaiaId, v2params.getSuggestion().gaiaId);
		Assert.assertEquals(testArrivalAirport.coordinates, v2params.getSuggestion().coordinates);
		Assert.assertEquals(testArrivalAirport.type, v2params.getSuggestion().type);

		Assert.assertEquals(testSearchParams.getGuests(), v2params.getGuests());
		Assert.assertEquals(testSearchParams.getAdults(), v2params.getAdults());
		Assert.assertEquals(testSearchParams.getChildren(), v2params.getChildren());
	}

	@Test
	public void testGuestString() {
		ArrayList<Integer> children = new ArrayList<>();
		children.add(10);
		children.add(7);
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.coordinates = new SuggestionV4.LatLng();
		HotelSearchParams params = (HotelSearchParams) new HotelSearchParams.Builder(0, 500, true)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5))
			.endDate(LocalDate.now().plusDays(15))
			.adults(2)
			.children(children).build();
		Assert.assertEquals("2,10,7", params.getGuestString());
	}

	@Test
	public void testSWPDisabledPOS() {
		signInUserWithPoints();
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_disabled_config.json");
		Assert.assertFalse(PointOfSale.getPointOfSale().isSWPEnabledForHotels());

		com.expedia.bookings.data.HotelSearchParams v1Params = getBasicV1Params();

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(context, v1Params);
		Assert.assertFalse("SWP expected to be disabled", v2params.getShopWithPoints());
	}

	@Test
	public void testSWPEnabledPOS() {
		signInUserWithPoints();
		PointOfSaleTestConfiguration.configurePointOfSale(context, "MockSharedData/pos_swp_enabled_config.json");
		Assert.assertTrue(PointOfSale.getPointOfSale().isSWPEnabledForHotels());

		com.expedia.bookings.data.HotelSearchParams v1Params = getBasicV1Params();

		HotelSearchParams v2params = HotelsV2DataUtil.Companion.getHotelV2SearchParams(context, v1Params);

		Assert.assertTrue("SWP expected to be enabled", v2params.getShopWithPoints());
	}

	private FlightSearchParams setupFlightSearchParams() {
		SuggestionV4 departureSuggestion = new SuggestionV4();
		departureSuggestion.gaiaId = "1234";
		SuggestionV4.RegionNames departureRegionNames = new SuggestionV4.RegionNames();
		departureRegionNames.displayName = "San Francisco";
		departureRegionNames.shortName = "SFO";
		departureSuggestion.regionNames = departureRegionNames;

		SuggestionV4.LatLng testDepartureCoordinates = new SuggestionV4.LatLng();
		testDepartureCoordinates.lat = 600.5;
		testDepartureCoordinates.lng = 300.3;
		departureSuggestion.coordinates = testDepartureCoordinates;


		SuggestionV4 arrivalSuggestion = new SuggestionV4();
		arrivalSuggestion.gaiaId = "5678";
		SuggestionV4.RegionNames arrivalRegionNames = new SuggestionV4.RegionNames();
		arrivalRegionNames.displayName = "Los Angeles";
		arrivalRegionNames.shortName = "LAX";
		arrivalSuggestion.regionNames = arrivalRegionNames;
		arrivalSuggestion.type = com.expedia.bookings.data.HotelSearchParams.SearchType.CITY.name();

		SuggestionV4.LatLng testArrivalCoordinates = new SuggestionV4.LatLng();
		testArrivalCoordinates.lat = 100.00;
		testArrivalCoordinates.lng = 500.00;
		arrivalSuggestion.coordinates = testArrivalCoordinates;

		List<Integer> childList = new ArrayList<>();
		childList.add(2);
		childList.add(4);
		LocalDate checkIn = new LocalDate().plusDays(2);
		LocalDate checkOut = new LocalDate().plusDays(3);

		return new FlightSearchParams(departureSuggestion, arrivalSuggestion, checkOut, checkIn, 2, childList, false, null, null, null, null, null, null);
	}

	private FlightLeg setupFlightLeg(String rawDate) {
		FlightLeg flightLeg = new FlightLeg();
		List<FlightLeg.FlightSegment> inboundSegmentList = new ArrayList<>();
		FlightLeg.FlightSegment inboundSegment = new FlightLeg.FlightSegment();
		inboundSegment.arrivalTimeRaw = rawDate;
		inboundSegmentList.add(inboundSegment);
		flightLeg.segments = inboundSegmentList;

		return flightLeg;
	}

	private void signInUserWithPoints() {
		UserLoyaltyMembershipInformation loyaltyInfo = new UserLoyaltyMembershipInformation();
		loyaltyInfo.setAllowedToShopWithPoints(true);
		User user = UserLoginTestUtil.mockUser();
		user.setLoyaltyMembershipInformation(loyaltyInfo);
		UserLoginTestUtil.setupUserAndMockLogin(user);
	}

	private com.expedia.bookings.data.HotelSearchParams getBasicV1Params() {
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
		return v1Params;
	}
}
