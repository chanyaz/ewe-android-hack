package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse;
import com.expedia.bookings.data.packages.PackageApiError;
import com.expedia.bookings.data.packages.PackageCreateTripParams;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.PackageServices;
import com.expedia.bookings.services.ProductSearchType;
import com.expedia.bookings.services.TestObserver;
import com.expedia.bookings.utils.Constants;
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
<<<<<<< HEAD
import retrofit2.HttpException;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
=======
>>>>>>> 5abc89409b... WIP

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

		service.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockPSSSearchWorks() throws Throwable {
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

		service.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(48, response.getHotels().size());
		Assert.assertEquals(2, response.getFlightLegs().size());
		System.out.println(response.getFlightLegs().get(0).flightSegments.get(0).airlineLogoURL);
		Assert.assertEquals(Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", "b6"), response.getFlightLegs().get(0).flightSegments.get(0).airlineLogoURL);
		Assert.assertEquals(null, response.getFlightLegs().get(0).flightSegments.get(1).airlineLogoURL);
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

		service.packageSearch(params, ProductSearchType.MultiItemHotels).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.getOnNextEvents().get(0);
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

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();

		SuggestionV4 originSuggestion = getDummySuggestion();
		originSuggestion.hierarchyInfo.airport.airportCode = "error";

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(originSuggestion)
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		service.packageSearch(params, ProductSearchType.MultiItemHotels).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.getOnErrorEvents().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError());
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

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("hotelID");
		params.setRatePlanCode("flight_outbound_happy");
		params.setRoomTypeCode("flight_outbound_happy");

		service.packageSearch(params, ProductSearchType.MultiItemOutboundFlights).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(50, response.getHotels().size());
		Assert.assertEquals(100, response.getFlightLegs().size());

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
		Assert.assertEquals(50, uniqueOutboundFlightLegs.size());
		Assert.assertEquals(24, uniqueInboundFlightLegs.size());
	}

	@Test
	public void testMockMIDFlightOutboundSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("hotelID");
		params.setRatePlanCode("error");
		params.setRoomTypeCode("flight_outbound_happy");

		service.packageSearch(params, ProductSearchType.MultiItemOutboundFlights).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.getOnErrorEvents().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError());
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

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();
		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("hotelID");
		params.setRatePlanCode("flight_outbound_happy");
		params.setRoomTypeCode("flight_outbound_happy");
		params.setSelectedLegId("flight_inbound_happy");

		service.packageSearch(params, ProductSearchType.MultiItemInboundFlights).subscribe(observer);
		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		BundleSearchResponse response = observer.values().get(0);
		Assert.assertEquals(50, response.getHotels().size());
		Assert.assertEquals(100, response.getFlightLegs().size());

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
		Assert.assertEquals(50, uniqueInboundFlightLegs.size());
	}

	@Test
	public void testMockMIDFlightInboundSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("hotelID");
		params.setRatePlanCode("flight_outbound_happy");
		params.setRoomTypeCode("flight_outbound_happy");
		params.setSelectedLegId("error");

		service.packageSearch(params, ProductSearchType.MultiItemInboundFlights).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.getOnErrorEvents().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(PackageApiError.Code.mid_could_not_find_results, midError.getFirstError());
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

		TestSubscriber<MultiItemApiSearchResponse> observer = new TestSubscriber<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("happy_room");

		service.multiItemRoomSearch(params).subscribe(observer);

		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertCompleted();

		MultiItemApiSearchResponse response = observer.getOnNextEvents().get(0);

		Assert.assertEquals("3052.42", response.getOffers().get(0).getPrice().getBasePrice().getAmount().toString());
	}

	@Test
	public void testMockMIDRoomSearchError() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<BundleSearchResponse> observer = new TestSubscriber<>();

		PackageSearchParams params = (PackageSearchParams) new PackageSearchParams.Builder(26, 329)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(1))
			.build();
		params.setHotelId("error");

		service.multiItemRoomSearch(params).subscribe(observer);

		observer.awaitTerminalEvent(3, TimeUnit.SECONDS);

		Throwable throwable = observer.getOnErrorEvents().get(0);
		if (throwable instanceof HttpException) {
			ResponseBody response = ((HttpException) throwable).response().errorBody();
			Assert.assertNotNull(response);
			MultiItemApiSearchResponse midError = new Gson()
				.fromJson(response.charStream(), MultiItemApiSearchResponse.class);
			Assert.assertNotNull(midError);
			Assert.assertEquals(ApiError.Code.PACKAGE_SEARCH_ERROR, midError.getRoomResponseFirstErrorCode());
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

		String prodID = "create_trip_multitraveler";
		String destID = "6139057";

		TestObserver<PackageCreateTripResponse> observer = new TestObserver<>();
		PackageCreateTripParams params = new PackageCreateTripParams(prodID, destID, 2, false, Arrays.asList(0, 8, 12));
		service.createTrip(params).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertComplete();
		PackageCreateTripResponse response = observer.values().get(0);
		Assert.assertEquals("$2,202.34", response.packageDetails.pricing.packageTotal.getFormattedMoneyFromAmountAndCurrencyCode());
		Assert.assertEquals("4", response.packageDetails.flight.details.offer.numberOfTickets);
	}

	@Test
	public void testInfantsInSeat() throws Throwable {
		PackageCreateTripParams params = new PackageCreateTripParams("", "", 2, true, Arrays.asList(0, 8, 12));
		Assert.assertTrue(params.isInfantsInLap());

		params = new PackageCreateTripParams("", "", 2, false, Arrays.asList(8, 8, 12));
		Assert.assertFalse(params.isInfantsInLap());
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
