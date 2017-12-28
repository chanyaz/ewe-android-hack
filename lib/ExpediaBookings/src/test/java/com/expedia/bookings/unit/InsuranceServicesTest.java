package com.expedia.bookings.unit;

import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.insurance.InsurancePriceType;
import com.expedia.bookings.data.insurance.InsuranceProduct;
import com.expedia.bookings.data.insurance.InsuranceTripParams;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.InsuranceServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

public class InsuranceServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private InsuranceServices insuranceServices;

	@Before
	public void setUp() throws IOException {
		insuranceServices = new InsuranceServices("http://localhost:" + server.getPort(), new OkHttpClient(),
			new MockInterceptor(), Schedulers.immediate(), Schedulers.immediate());

		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
	}

	@Test
	public void testInsuranceIsAvailableWhenRequestedAndCanBeAddedAndRemoved() throws Throwable {
		// verify insurance is available when requested
		FlightCreateTripResponse trip = getFlightCreateTripResponse("happy_round_trip_with_insurance_available");
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
		FlightCreateTripResponse flightCreateTripResponse = getFlightCreateTripResponse("happy_round_trip");
		List<InsuranceProduct> availableInsuranceProducts = flightCreateTripResponse.getAvailableInsuranceProducts();
		Assert.assertTrue(availableInsuranceProducts.isEmpty());
	}

	private FlightCreateTripResponse getFlightCreateTripResponse(String productKey) throws Throwable {
		HashMap<String, String> replacements = new HashMap<>();
		replacements.put("productKey", productKey);
		return Mocker.object(FlightCreateTripResponse.class, "api/flight/trip/create/" + productKey + ".json", replacements);
	}
}
