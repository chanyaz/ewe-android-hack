package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.flights.FlightCreateTripParams;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightSearchParams;
import com.expedia.bookings.data.flights.FlightSearchResponse;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.data.insurance.InsurancePriceType;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.expedia.bookings.data.insurance.InsuranceTripParams;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.FlightServices;
import com.expedia.bookings.services.InsuranceServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class InsuranceServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private FlightServices flightServices;
	private InsuranceServices insuranceServices;

	@Before
	public void setUp() throws IOException {
		flightServices = new FlightServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(), Schedulers.immediate(), Schedulers.immediate());
		insuranceServices = new InsuranceServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(), Schedulers.immediate(), Schedulers.immediate());

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
	}

	@Test
	public void testInsuranceIsAvailableWhenRequestedAndCanBeAddedAndRemoved() throws Throwable {
		// verify insurance is available when requested
		TestSubscriber<FlightCreateTripResponse> createTripObserver = createTrip("happy_round_trip_with_insurance_available");
		createTripObserver.awaitTerminalEvent();
		createTripObserver.assertNoErrors();
		FlightCreateTripResponse trip = createTripObserver.getOnNextEvents().get(0);
		List<InsuranceProduct> insuranceProducts = trip.getAvailableInsuranceProducts();
		Assert.assertFalse(insuranceProducts.isEmpty());

		// verify insurance is valid
		InsuranceProduct product = insuranceProducts.get(0);
		Assert.assertEquals(product.description.size(), 1);
		Assert.assertEquals(product.description.get(0).text, "Why you might need travel protection:");
		Assert.assertEquals(product.description.get(0).children.size(), 2);
		Assert.assertEquals(product.description.get(0).children.get(0).text, "foo");
		Assert.assertEquals(product.description.get(0).children.get(1).text, "bar");
		Assert.assertEquals(product.displayPrice.formattedPrice, "$19.00");
		Assert.assertEquals(product.displayPriceType, InsurancePriceType.PRICE_PER_PERSON);
		Assert.assertEquals(product.name, "Flight Protection");
		Assert.assertEquals(product.productId, "abcdefgh-ijkl-mnop-qrst-uvwxyz012345");
		Assert.assertEquals(product.terms.text, "Terms and conditions");
		Assert.assertEquals(product.terms.url, "http://example.com/flight_protection.html");
		Assert.assertEquals(product.title, "Protect your trip");
		Assert.assertEquals(product.typeId, "100001");

		// add insurance to the trip and verify it comes back selected
		TestSubscriber<FlightCreateTripResponse> addInsuranceObserver = new TestSubscriber<>();
		insuranceServices.addInsuranceToTrip(new InsuranceTripParams(trip.getNewTrip().getTripId(), product.productId))
			.subscribe(addInsuranceObserver);
		addInsuranceObserver.awaitTerminalEvent();
		addInsuranceObserver.assertNoErrors();
		FlightCreateTripResponse updatedTrip = addInsuranceObserver.getOnNextEvents().get(0);
		Assert.assertNotNull(updatedTrip.getSelectedInsuranceProduct());

		// remove insurance from the trip and verify it comes back blank
		TestSubscriber<FlightCreateTripResponse> removeInsuranceObserver = new TestSubscriber<>();
		insuranceServices.removeInsuranceFromTrip(new InsuranceTripParams(trip.getNewTrip().getTripId())).subscribe(
			removeInsuranceObserver);
		removeInsuranceObserver.awaitTerminalEvent();
		removeInsuranceObserver.assertNoErrors();
		updatedTrip = removeInsuranceObserver.getOnNextEvents().get(0);
		Assert.assertNull(updatedTrip.getSelectedInsuranceProduct());
	}

	@Test
	public void testInsuranceIsNotAvailableWhenNotRequested() throws Throwable {
		TestSubscriber<FlightCreateTripResponse> tripObserver = createTrip("happy_round_trip");
		tripObserver.awaitTerminalEvent();
		tripObserver.assertNoErrors();
		FlightCreateTripResponse flightCreateTripResponse = tripObserver.getOnNextEvents().get(0);
		List<InsuranceProduct> availableInsuranceProducts = flightCreateTripResponse.getAvailableInsuranceProducts();
		Assert.assertTrue(availableInsuranceProducts.isEmpty());
	}

	private TestSubscriber<FlightCreateTripResponse> createTrip(String productKey) throws Throwable {
		TestSubscriber<FlightCreateTripResponse> tripObserver = new TestSubscriber<>();
		flightServices.createTrip(new FlightCreateTripParams.
			Builder().productKey(productKey).build(), tripObserver);
		tripObserver.awaitTerminalEvent();
		tripObserver.assertNoErrors();
		return tripObserver;
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
		suggestion.hierarchyInfo.airport.airportCode = "";
		return suggestion;
	}

	private FlightTripDetails.FlightOffer getFlightOffer() {
		TestSubscriber<FlightSearchResponse> flightSearchObserver = new TestSubscriber<>();
		FlightSearchParams flightSearchParams = (FlightSearchParams) new FlightSearchParams.Builder(26, 500)
			.origin(getDummySuggestion())
			.destination(getDummySuggestion())
			.startDate(LocalDate.now())
			.endDate(LocalDate.now().plusDays(5))
			.adults(1)
			.build();

		flightServices.flightSearch(flightSearchParams, flightSearchObserver, null);
		flightSearchObserver.awaitTerminalEvent();
		flightSearchObserver.assertNoErrors();
		FlightSearchResponse flightSearchResponse = flightSearchObserver.getOnNextEvents().get(0);
		Assert.assertFalse(flightSearchResponse.getOffers().isEmpty());
		return flightSearchResponse.getOffers().get(0);
	}
}
