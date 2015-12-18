package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.hotels.CalculatePointsParams;
import com.expedia.bookings.data.hotels.CalculatePointsResponse;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelApplyCouponParams;
import com.expedia.bookings.data.hotels.HotelCheckoutParams;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.hotels.ProgramName;
import com.expedia.bookings.data.hotels.SuggestionV4;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.services.HotelServices;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertTrue;

public class HotelServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private HotelServices service;
	private HotelApplyCouponParams couponParams;

	@Before
	public void before() {
		service = new HotelServices("http://localhost:" + server.getPort(),
			new OkHttpClient(), new MockInterceptor(),
			Schedulers.immediate(), Schedulers.immediate(),
			RestAdapter.LogLevel.FULL);
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(RetrofitError.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = new NearbyHotelParams("", "", "", "", "", "", "");

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	@Test
	public void testMockDetailsWithoutOffersWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<HotelOffersResponse> observer = new TestSubscriber<>();
		HotelSearchParams params = new HotelSearchParams(new SuggestionV4(), LocalDate.now().plusDays(5), LocalDate.now().plusDays(15), 2, new ArrayList<Integer>());

		service.info(params, "happy", observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		Assert.assertNull(observer.getOnNextEvents().get(0).hotelRoomResponse);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).checkInDate);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).checkOutDate);
	}

	@Test
	public void testMockDetailsWithOffersWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<HotelOffersResponse> observer = new TestSubscriber<>();
		HotelSearchParams params = new HotelSearchParams(new SuggestionV4(), LocalDate.now().plusDays(5), LocalDate.now().plusDays(15), 2, new ArrayList<Integer>());

		service.offers(params, "happypath", observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		Assert.assertNotNull(observer.getOnNextEvents().get(0).hotelRoomResponse);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).checkInDate);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).checkOutDate);
	}

	@Test
	public void testCheckoutWorks() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<HotelCheckoutResponse> observer = new TestSubscriber<>();
		HotelCheckoutParams params = new HotelCheckoutParams();
		params.tripId = "happypath_0";
		params.expectedTotalFare = "12123.33";
		params.tealeafTransactionId = "tealeafHotel:happypath_0";

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	@Test
	public void testCheckoutFailed() throws Throwable {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));

		TestSubscriber<HotelCheckoutResponse> observer = new TestSubscriber<>();
		HotelCheckoutParams params = new HotelCheckoutParams();
		params.tripId = "happypath_0";
		params.expectedTotalFare = "12,33";
		params.tealeafTransactionId = "tealeafHotel:happypath_0";

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNotCompleted();
	}

	@Test
	public void testCouponError() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();
		givenCouponParams("hotel_coupon_errors_expired");

		service.applyCoupon(couponParams).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertCompleted();
		ApiError apiError = observer.getOnNextEvents().get(0).getFirstError();
		Assert.assertEquals(ApiError.Code.APPLY_COUPON_ERROR, apiError.errorCode);
	}

	@Test
	public void testCalculatePoints() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("happy", observer);

		Assert.assertNotNull(observer.getOnNextEvents().get(0).getConversion());
		Assert.assertNotNull(observer.getOnNextEvents().get(0).getRemainingPayableByCard());
		Assert.assertNotNull(observer.getOnNextEvents().get(0).getProgramName());
	}

	@Test
	public void testCalculatePointsThrowsInvalidInput() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("invalid_amount_entered", observer);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).errors);
		Assert.assertEquals(ApiError.Code.INVALID_INPUT, observer.getOnNextEvents().get(0).errors.get(0).errorCode);
	}

	@Test
	public void testCalculatePointsThrowsTripServiceError() throws IOException {
		TestSubscriber<CalculatePointsResponse> observer = new TestSubscriber<>();
		setupCalculatePoints("trip_service_error", observer);
		Assert.assertNotNull(observer.getOnNextEvents().get(0).errors);
		Assert.assertEquals(ApiError.Code.TRIP_SERVICE_ERROR, observer.getOnNextEvents().get(0).errors.get(0).errorCode);
	}

	private void setupCalculatePoints(String tripId, TestSubscriber<CalculatePointsResponse> observer) throws IOException {
		givenServerUsingMockResponses();
		CalculatePointsParams calculatePointsParams = new CalculatePointsParams.Builder().
			tripId(tripId).programName(ProgramName.ExpediaRewards).amount("100").rateId("rateId").build();

		service.calculatePoints(calculatePointsParams, observer);

		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	private void givenServerUsingMockResponses() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
	}

	private void givenCouponParams(String mockFileName) {
		couponParams = new HotelApplyCouponParams("58b6be8a-d533-4eb0-aaa6-0228e000056c", mockFileName, false);
	}

	@Test
	public void testSponsoredOrderingWhenHotelCountIsMoreThan50AndHasSponsoredItems() throws Throwable {
		testSponsoredOrdering(createDummyList(100, true), true);
	}

	@Test
	public void testSponsoredOrderingWhenHotelCountIsLessThan50AndHasSponsoredItems() throws Throwable {
		testSponsoredOrdering(createDummyList(20, true), true);
	}

	@Test
	public void testSponsoredOrderingWhenNoSponsoredItems() throws Throwable {
		testSponsoredOrdering(createDummyList(100, false), false);
	}

	private void testSponsoredOrdering(List<Hotel> hotelList, boolean haveSponsoredItems) throws Throwable {
		List<Hotel> updatedHotelList = HotelServices.Companion.putSponsoredItemsInCorrectPlaces(hotelList);
		for (int index = 0; index < updatedHotelList.size(); index++) {
			if (haveSponsoredItems && (index == 0 || index == 50 || index == 51)) {
				assertTrue(isHotelSponsored(updatedHotelList.get(index)));
			}
			else {
				assertTrue(!isHotelSponsored(updatedHotelList.get(index)));
			}
		}
	}

	private List<Hotel> createDummyList(int hotelCount, boolean keepSponsoredItems) {
		List<Hotel> hotelList = new ArrayList<>();
		for (int index = 0; index < hotelCount; index++) {
			Hotel hotel = new Hotel();
			hotel.hotelId = "Normal";
			hotelList.add(hotel);
		}
		if (keepSponsoredItems) {
			Random random = new Random();
			setHotelAsSponsored(hotelList.get(random.nextInt(hotelCount)));
			if (hotelCount >= 50) {
				setHotelAsSponsored(hotelList.get(random.nextInt(hotelCount)));
				setHotelAsSponsored(hotelList.get(random.nextInt(hotelCount)));
			}
		}
		return hotelList;
	}

	private void setHotelAsSponsored(Hotel hotel) {
		hotel.isSponsoredListing = true;
		hotel.hotelId = "Sponsored";
	}

	private boolean isHotelSponsored(Hotel hotel) {
		return hotel.isSponsoredListing && hotel.hotelId.equals("Sponsored");
	}
}
