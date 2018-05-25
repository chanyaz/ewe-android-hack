package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse;
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse;
import com.expedia.bookings.data.packages.MultiItemCreateTripParams;
import com.expedia.bookings.data.packages.PackageApiError;
import com.expedia.bookings.data.packages.PackageOfferModel.PackagePrice;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.PackageProductSearchType;
import com.expedia.bookings.services.TestObserver;
import com.google.gson.Gson;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.HttpException;

public class PackageServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private PackageServices service;

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new PackageServices("http://localhost:" + server.getPort(),
			new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor, Schedulers.trampoline(), Schedulers.trampoline());
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params, PackageProductSearchType.MultiItemHotels).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockMIDHotelSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params, PackageProductSearchType.MultiItemHotels).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(50, response.getHotels().size());
		Assert.assertEquals(100, response.getFlightLegs().size());

		List<Hotel> hotels = response.getHotels();
		Set<Hotel> uniqueHotels = new HashSet<>(hotels);
		Assert.assertEquals(50, uniqueHotels.size());

		List<FlightLeg> flightLegs = response.getFlightLegs();
		Set<FlightLeg> uniqueOutboundFlightLegs = new HashSet<>();
		Set<FlightLeg> uniqueInboundFlightLegs = new HashSet<>();
		for (int index = 0; index < flightLegs.size(); index += 2) {
			uniqueOutboundFlightLegs.add(flightLegs.get(index));
		}
		for (int index = 1; index < flightLegs.size(); index += 2) {
			uniqueInboundFlightLegs.add(flightLegs.get(index));
		}
		Assert.assertEquals(1, uniqueOutboundFlightLegs.size());
		Assert.assertEquals(1, uniqueInboundFlightLegs.size());
	}

	@Test
	public void testMockMIDHotelSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();

		SuggestionV4 originSuggestion = getDummySuggestion();
		originSuggestion.hierarchyInfo.airport.airportCode = "error";

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(originSuggestion)
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		service.packageSearch(params, PackageProductSearchType.MultiItemHotels).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.errors().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError().getErrorCode());
		}
		else {
			Assert.fail("Error should be of type HttpException");
		}
	}

	@Test
	public void testMockMIDFlightOutboundSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("hotelID");
		params.getLatestSelectedOfferInfo().setRatePlanCode("flight_outbound_happy");
		params.getLatestSelectedOfferInfo().setRoomTypeCode("flight_outbound_happy");

		service.packageSearch(params, PackageProductSearchType.MultiItemOutboundFlights).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(67, response.getHotels().size());
		Assert.assertEquals(134, response.getFlightLegs().size());

		List<Hotel> hotels = response.getHotels();
		Set<Hotel> uniqueHotels = new HashSet<>(hotels);
		Assert.assertEquals(2, uniqueHotels.size());

		List<FlightLeg> flightLegs = response.getFlightLegs();
		Set<FlightLeg> uniqueOutboundFlightLegs = new HashSet<>();
		Set<FlightLeg> uniqueInboundFlightLegs = new HashSet<>();
		for (int index = 0; index < flightLegs.size(); index += 2) {
			uniqueOutboundFlightLegs.add(flightLegs.get(index));
		}
		for (int index = 1; index < flightLegs.size(); index += 2) {
			uniqueInboundFlightLegs.add(flightLegs.get(index));
		}
		Assert.assertEquals(67, uniqueOutboundFlightLegs.size());
		Assert.assertEquals(9, uniqueInboundFlightLegs.size());
	}

	@Test
	public void testMockMIDFlightOutboundSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("hotelID");
		params.getLatestSelectedOfferInfo().setRatePlanCode("error");
		params.getLatestSelectedOfferInfo().setRoomTypeCode("flight_outbound_happy");

		service.packageSearch(params, PackageProductSearchType.MultiItemOutboundFlights).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.errors().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError().getErrorCode());
		}
		else {
			Assert.fail("Error should be of type HttpException");
		}
	}

	@Test
	public void testMockMIDFlightInboundSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("hotelID");
		params.getLatestSelectedOfferInfo().setRatePlanCode("flight_outbound_happy");
		params.getLatestSelectedOfferInfo().setRoomTypeCode("flight_outbound_happy");
		params.setSelectedLegId("flight_inbound_happy");

		service.packageSearch(params, PackageProductSearchType.MultiItemInboundFlights).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(53, response.getHotels().size());
		Assert.assertEquals(106, response.getFlightLegs().size());

		List<Hotel> hotels = response.getHotels();
		Set<Hotel> uniqueHotels = new HashSet<>(hotels);
		Assert.assertEquals(1, uniqueHotels.size());

		List<FlightLeg> flightLegs = response.getFlightLegs();
		Set<FlightLeg> uniqueOutboundFlightLegs = new HashSet<>();
		Set<FlightLeg> uniqueInboundFlightLegs = new HashSet<>();
		for (int index = 0; index < flightLegs.size(); index += 2) {
			uniqueOutboundFlightLegs.add(flightLegs.get(index));
		}
		for (int index = 1; index < flightLegs.size(); index += 2) {
			uniqueInboundFlightLegs.add(flightLegs.get(index));
		}
		Assert.assertEquals(1, uniqueOutboundFlightLegs.size());
		Assert.assertEquals(53, uniqueInboundFlightLegs.size());
	}

	@Test
	public void testMockMIDFlightInboundSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("hotelID");
		params.getLatestSelectedOfferInfo().setRatePlanCode("flight_outbound_happy");
		params.getLatestSelectedOfferInfo().setRoomTypeCode("flight_outbound_happy");
		params.setSelectedLegId("error");

		service.packageSearch(params, PackageProductSearchType.MultiItemInboundFlights).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.errors().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError().getErrorCode());
		}
		else {
			Assert.fail("Error should be of type HttpException");
		}
	}

	@Test
	public void testMockMIDRoomSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<MultiItemApiSearchResponse> observer = new TestObserver<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("happy_room");

		service.multiItemRoomSearch(params).subscribe(observer);

		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertComplete();

		MultiItemApiSearchResponse response = observer.values().get(0);

		Assert.assertEquals("3241", response.getOffers().get(0).getPrice().getBasePrice().getAmount().toString());
	}

	@Test
	public void testMockMIDRoomSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("error");

		service.multiItemRoomSearch(params).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.errors().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(ApiError.Code.PACKAGE_SEARCH_ERROR, midError.getRoomResponseFirstErrorCode().getErrorCode());
		}
		else {
			Assert.fail("Error should be of type HttpException");
		}
	}

	@Test
	public void testCreateTripMultiTravelerWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<MultiItemApiCreateTripResponse> observer = new TestObserver<>();
		PackagePrice packagePrice = new PackagePrice();
		packagePrice.packageTotalPrice = new Money();
		MultiItemCreateTripParams params = new MultiItemCreateTripParams("mid_create_trip", "", "", "", "", packagePrice, "", "", 0, null, null);
		service.multiItemCreateTrip(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertComplete();
		MultiItemApiCreateTripResponse response = observer.values().get(0);
		Assert.assertEquals("859b3288-4dcf-46e5-a545-8e9daaa3be45", response.tripId);
	}

	@Test
	public void testGetSelectedFlightPIID() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.getLatestSelectedOfferInfo().setHotelId("hotelID");
		params.getLatestSelectedOfferInfo().setRatePlanCode("flight_outbound_happy");
		params.getLatestSelectedOfferInfo().setRoomTypeCode("flight_outbound_happy");
		params.setSelectedLegId("flight_inbound_happy");

		service.packageSearch(params, PackageProductSearchType.MultiItemInboundFlights).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals("mid_create_trip", response.getSelectedFlightPIID("flight_inbound_happy", "52eb781f016058f4a517e8e802331956"));
		Assert.assertNull(response.getSelectedFlightPIID(null, null));
		Assert.assertNull(response.getSelectedFlightPIID("484e6292832e2ace56acb0c2d202c6fd", "wrong_id"));
	}

	@Test
	public void testGetFlightPIIDFromSelectedHotel() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestObserver<BundleSearchResponse> observer = new TestObserver<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();

		service.packageSearch(params, PackageProductSearchType.MultiItemHotels).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(
			"v5-714439a54a9f7dd58e1b4298a5025d0e-0-0-1~0.S~AQoCCAESBwjUBBABGAEgASAHIA0gDCgD~AQpDCh8Iw7QBEgM2NjAYsnEggmgom7GLATC1uYsBOFZAAFgBCiAIw7QBEgQ2MDY3GIJoIOpkKO-9iwEwisCLAThLQABYAQpjCh8IzaoBEgM3NTgY6mQgijMoueSLATDl5YsBOFNAAFgBCh8IzaoBEgM3NDcYijMgrzUowuqLATCK7IsBOEJAAVgBCh8IzaoBEgM1ODkYrzUgsnEojO2LATC-8osBOFNAAVgBEgoIARABGAEqAkNaGAEiBAgBEAEoAigDKAQwAg",
			response.getFlightPIIDFromSelectedHotel(response.getHotels().get(0).hotelPid));
		Assert.assertNull(response.getFlightPIIDFromSelectedHotel("hotel-ZERO"));
		Assert.assertNull(response.getFlightPIIDFromSelectedHotel(null));
	}

	private SuggestionV4 getDummySuggestion()  {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "";
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = "";
		suggestion.regionNames.fullName = "";
		suggestion.regionNames.shortName = "";
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = new SuggestionV4.Airport();
		suggestion.hierarchyInfo.airport.airportCode = "happy";
		suggestion.hierarchyInfo.airport.multicity = "happy";
		return suggestion;
	}
}
