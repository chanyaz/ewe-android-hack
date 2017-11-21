package com.expedia.bookings.unit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters;
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock;
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params;
import com.expedia.bookings.data.hotels.HotelCreateTripParams;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.payment.PointsAndCurrency;
import com.expedia.bookings.data.payment.PointsType;
import com.expedia.bookings.data.payment.ProgramName;
import com.expedia.bookings.data.payment.TripDetails;
import com.expedia.bookings.data.payment.UserPreferencePointsDetails;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.utils.NumberUtils;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HotelServicesTest {
	@Rule
	public MockWebServer server = new MockWebServer();

	private HotelServices service;
	private HotelApplyCouponParameters couponParams;

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);
		Interceptor interceptor = new MockInterceptor();
		service = new HotelServices("http://localhost:" + server.getPort(),
			new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor, Schedulers.immediate(), Schedulers.immediate());
	}

	@Test
	public void testSearchWithZeroLongLatAndNullRegionId() throws IOException {
		// final array to make the test result flag/boolean accessible in the anonymous dispatch
		final boolean[] testResult = { true, true };
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
				boolean containsLongitudeParam = request.getPath().contains("longitude");
				boolean containsLatitudeParam = request.getPath().contains("latitude");
				boolean containsRegionId = request.getPath().contains("regionId");
				testResult[0] = containsLatitudeParam || containsLongitudeParam;
				testResult[1] = containsRegionId;
				return new MockResponse();
			}
		};
		server.setDispatcher(dispatcher);
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.coordinates = new SuggestionV4.LatLng();
		suggestion.coordinates.lat = 0;
		suggestion.coordinates.lng = 0;
		HotelSearchParams hotelSearchParams = (HotelSearchParams) new HotelSearchParams.Builder(0, 0)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(15)).adults(2).build();

		TestSubscriber testSubscriber = new TestSubscriber();
		service.search(hotelSearchParams, null).subscribe(testSubscriber);
		testSubscriber.awaitTerminalEvent();

		assertFalse("I don't expect to see longitude or latitude in a request where both are 0", testResult[0]);
		assertFalse("I don't expect to see regionId param in this request as it's not set", testResult[1]);
	}

	@Test
	public void testNeighborhoodFilterOverridesRegionId() {
		final boolean[] testResult = { false };
		final String expectedNeighborhoodId = "12345";
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
				boolean containsRegionId = request.getPath().contains("regionId=" + expectedNeighborhoodId + "&");
				testResult[0] = containsRegionId;
				return new MockResponse();
			}
		};
		server.setDispatcher(dispatcher);

		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "7732025862";
		suggestion.coordinates = new SuggestionV4.LatLng();
		HotelSearchParams hotelSearchParams = (HotelSearchParams) new HotelSearchParams.Builder(0, 0)
			.neighborhood(expectedNeighborhoodId)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(15)).adults(2).build();

		TestSubscriber testSubscriber = new TestSubscriber();
		service.search(hotelSearchParams, null).subscribe(testSubscriber);
		testSubscriber.awaitTerminalEvent();

		assertTrue("Failure: Region Id expected to match neighborhood id if set", testResult[0]);
	}

	@Test
	public void testSearchWithZeroRegionId() throws IOException {
		final boolean[] testResult = { true, true };
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
				boolean containsLongitudeParam = request.getPath().contains("longitude");
				boolean containsLatitudeParam = request.getPath().contains("latitude");
				boolean containsRegionId = request.getPath().contains("regionId");
				testResult[0] = containsLatitudeParam || containsLongitudeParam;
				testResult[1] = containsRegionId;
				return new MockResponse();
			}
		};
		server.setDispatcher(dispatcher);
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.coordinates = new SuggestionV4.LatLng();
		suggestion.coordinates.lat = 41.87;
		suggestion.coordinates.lng = 87.62;
		suggestion.gaiaId = "0";
		HotelSearchParams hotelSearchParams = (HotelSearchParams) new HotelSearchParams.Builder(0, 0)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(15)).adults(2).build();

		TestSubscriber testSubscriber = new TestSubscriber();
		service.search(hotelSearchParams, null).subscribe(testSubscriber);
		testSubscriber.awaitTerminalEvent();

		assertTrue("I expect to see longitude or latitude", testResult[0]);
		assertFalse("I don't expect to see regionId param in this request if gaia ID is 0", testResult[1]);
	}

	@Test
	public void testMockSearchBlowsUp() throws Throwable {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = givenNearbyHotelParams();

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<List<Hotel>> observer = new TestSubscriber<>();
		NearbyHotelParams params = givenNearbyHotelParams();

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	@Test
	public void testMockDetailsWithoutOffersWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelOffersResponse> observer = new TestSubscriber<>();

		HotelSearchParams params = givenHappyHotelSearchParams();
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
		givenServerUsingMockResponses();

		TestSubscriber<HotelOffersResponse> observer = new TestSubscriber<>();

		HotelSearchParams params = givenHappyHotelSearchParams();
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
	public void unknownRewardsTypesAreRemovedAndKnownRewardsTypesRemainInCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("hotel_pwp_multiple_points_types", false, 1, Collections.<Integer>emptyList());

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(1, response.getPointsDetails().size());
		Assert.assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void knownRewardsTypeRemainInCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("happypath_pwp_points_only", false, 1, Collections.<Integer>emptyList());

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(1, response.getPointsDetails().size());
		Assert.assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void unknownRewardsTypesAreRemovedFromCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("hotel_pwp_unknown_points_type", false, 1, Collections.<Integer>emptyList());

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.getOnNextEvents().get(0);
		Assert.assertEquals(0, response.getPointsDetails().size());
	}

	@Test
	public void testCheckoutWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCheckoutResponse> observer = new TestSubscriber<>();

		String tripId = "happypath_0";
		TripDetails tripDetails = new TripDetails(tripId, "12123.33", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
	}

	@Test
	public void testCheckoutWithPriceChangeAndUserPreferences() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCheckoutResponse> observer = new TestSubscriber<>();

		String tripId = "hotel_price_change_with_user_preferences";
		TripDetails tripDetails = new TripDetails(tripId, "675.81", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);
		assertNotNull(observer.getOnNextEvents().get(0).pointsDetails);
		assertNotNull(observer.getOnNextEvents().get(0).userPreferencePoints);
	}

	@Test
	public void checkoutWithPriceChangeDoesNotIncludeUnknownRewardsTypes() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCheckoutResponse> observer = new TestSubscriber<>();

		String tripId = "hotel_price_change_with_multiple_points_types";
		TripDetails tripDetails = new TripDetails(tripId, "675.81", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		HotelCheckoutResponse response = observer.getOnNextEvents().get(0);
		assertEquals(1, response.pointsDetails.size());
		assertEquals(ProgramName.ExpediaRewards, response.pointsDetails.get(0).getProgramName());
	}

	@Test
	public void testCouponRemove() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> subscriber = new TestSubscriber<>();
		service.removeCoupon("hotel_coupon_remove_success", true).subscribe(subscriber);
		subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);
		subscriber.assertCompleted();

		Assert.assertNotNull(subscriber.getOnNextEvents().get(0).newHotelProductResponse);
	}

	@Test
	public void couponRemoveResponseHasOnlyKnownPointsTypes() throws Throwable {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> subscriber = new TestSubscriber<>();
		service.removeCoupon("hotel_coupon_remove_success_with_multiple_points_types", true).subscribe(subscriber);
		subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);
		subscriber.assertCompleted();

		subscriber.assertNoErrors();
		subscriber.assertCompleted();
		subscriber.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = subscriber.getOnNextEvents().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void testCouponError() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();
		givenCouponParams("hotel_coupon_errors_expired");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertCompleted();
		ApiError apiError = observer.getOnNextEvents().get(0).getFirstError();
		Assert.assertEquals(ApiError.Code.APPLY_COUPON_ERROR, apiError.errorCode);
	}

	@Test
	public void testCouponSuccess() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();
		givenCouponParams("hotel_coupon_success");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = observer.getOnNextEvents().get(0);
		assertNull(response.getPointsDetails());
	}

	@Test
	public void couponApplyResponseHasOnlyKnownPointsTypes() throws IOException {
		givenServerUsingMockResponses();

		TestSubscriber<HotelCreateTripResponse> observer = new TestSubscriber<>();
		givenCouponParams("hotel_coupon_success_multiple_points_types");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertCompleted();
		observer.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = observer.getOnNextEvents().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	private List<Hotel> createDummyList(int hotelCount, boolean keepSponsoredItems) {
		List<Hotel> hotelList = new ArrayList<>();
		for (int index = 0; index < hotelCount; index++) {
			Hotel hotel = new Hotel();
			hotel.hotelId = "Normal";
			hotelList.add(hotel);
		}

		List<Integer> randomNumberList = NumberUtils.getRandomNumberList(hotelCount);
		if (keepSponsoredItems) {
			setHotelAsSponsored(hotelList.get(randomNumberList.get(0)));
			if (hotelCount >= 50) {
				setHotelAsSponsored(hotelList.get(randomNumberList.get(1)));
				setHotelAsSponsored(hotelList.get(randomNumberList.get(2)));
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

	private NearbyHotelParams givenNearbyHotelParams() {
		return new NearbyHotelParams("", "", "", "", "", "", "");
	}

	private HotelSearchParams givenHappyHotelSearchParams() {
		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.coordinates = new SuggestionV4.LatLng();
		return (HotelSearchParams) new HotelSearchParams.Builder(0, 0)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5))
			.endDate(LocalDate.now().plusDays(15))
			.adults(2)
			.children(new ArrayList<Integer>()).build();
	}

	private void givenServerUsingMockResponses() throws IOException {
		String root = new File("../mocked/templates").getCanonicalPath();
		FileSystemOpener opener = new FileSystemOpener(root);
		server.setDispatcher(new ExpediaDispatcher(opener));
	}

	private void givenCouponParams(String mockFileName) {
		List<UserPreferencePointsDetails> userPreferencePointsDetails = new ArrayList<>();
		userPreferencePointsDetails.add(new UserPreferencePointsDetails(ProgramName.ExpediaRewards,
			new PointsAndCurrency(0, PointsType.BURN, new Money(), null)));
		couponParams = new HotelApplyCouponParameters.Builder().tripId("58b6be8a-d533-4eb0-aaa6-0228e000056c")
			.couponCode(mockFileName)
			.userPreferencePointsDetails(userPreferencePointsDetails)
			.isFromNotSignedInToSignedIn(false).build();
	}
}
