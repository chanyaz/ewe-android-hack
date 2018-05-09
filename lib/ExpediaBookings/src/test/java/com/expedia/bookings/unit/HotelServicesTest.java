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

import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelApplyCouponParameters;
import com.expedia.bookings.data.hotels.HotelCheckoutParamsMock;
import com.expedia.bookings.data.hotels.HotelCheckoutV2Params;
import com.expedia.bookings.data.hotels.HotelCreateTripParams;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchParams;
import com.expedia.bookings.data.hotels.HotelSearchResponse;
import com.expedia.bookings.data.hotels.NearbyHotelParams;
import com.expedia.bookings.data.hotels.Neighborhood;
import com.expedia.bookings.data.payment.PointsAndCurrency;
import com.expedia.bookings.data.payment.PointsType;
import com.expedia.bookings.data.payment.ProgramName;
import com.expedia.bookings.data.payment.TripDetails;
import com.expedia.bookings.data.payment.UserPreferencePointsDetails;
import com.expedia.bookings.interceptors.MockInterceptor;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.services.HotelServices;
import com.expedia.bookings.services.TestObserver;
import com.mobiata.mocke3.ExpediaDispatcher;
import com.mobiata.mocke3.FileSystemOpener;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

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
	private MockInterceptor satelliteInterceptor = new MockInterceptor();

	@Before
	public void before() {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor();
		logger.setLevel(HttpLoggingInterceptor.Level.BODY);

		Interceptor interceptor = new MockInterceptor();
		List<Interceptor> interceptors = new ArrayList<>();
		interceptors.add(satelliteInterceptor);

		String endpoint = "http://localhost:" + server.getPort();

		service = new HotelServices(endpoint, endpoint,
			new OkHttpClient.Builder().addInterceptor(logger).build(),
			interceptor, interceptors, false, Schedulers.trampoline(), Schedulers.trampoline());
	}

	@Test
	public void testSearchWithZeroLongLatAndNullRegionId() {
		// final array to make the test result flag/boolean accessible in the anonymous dispatch
		final boolean[] testResult = { true, true };
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
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

		TestObserver testObserver = new TestObserver();
		service.search(hotelSearchParams, null).subscribe(testObserver);
		testObserver.awaitTerminalEvent();

		assertFalse("I don't expect to see longitude or latitude in a request where both are 0", testResult[0]);
		assertFalse("I don't expect to see regionId param in this request as it's not set", testResult[1]);
	}

	@Test
	public void testNeighborhoodFilterOverridesRegionId() {
		final boolean[] testResult = { false };
		final String expectedNeighborhoodId = "12345";
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
				boolean containsRegionId = request.getPath().contains("regionId=" + expectedNeighborhoodId + "&");
				testResult[0] = containsRegionId;
				return new MockResponse();
			}
		};
		server.setDispatcher(dispatcher);

		SuggestionV4 suggestion = new SuggestionV4();
		suggestion.gaiaId = "7732025862";
		suggestion.coordinates = new SuggestionV4.LatLng();
		Neighborhood expectedNeighborhood = new Neighborhood();
		expectedNeighborhood.name = "name";
		expectedNeighborhood.id = expectedNeighborhoodId;
		HotelSearchParams hotelSearchParams = (HotelSearchParams) new HotelSearchParams.Builder(0, 0)
			.neighborhood(expectedNeighborhood)
			.destination(suggestion)
			.startDate(LocalDate.now().plusDays(5)).endDate(LocalDate.now().plusDays(15)).adults(2).build();

		TestObserver testObserver = new TestObserver();
		service.search(hotelSearchParams, null).subscribe(testObserver);
		testObserver.awaitTerminalEvent();

		assertTrue("Failure: Region Id expected to match neighborhood id if set", testResult[0]);
	}

	@Test
	public void testSearchWithZeroRegionId() {
		final boolean[] testResult = { true, true };
		Dispatcher dispatcher = new Dispatcher() {
			@Override
			public MockResponse dispatch(RecordedRequest request) {
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

		TestObserver testSubscriber = new TestObserver();
		service.search(hotelSearchParams, null).subscribe(testSubscriber);
		testSubscriber.awaitTerminalEvent();

		assertTrue("I expect to see longitude or latitude", testResult[0]);
		assertFalse("I don't expect to see regionId param in this request if gaia ID is 0", testResult[1]);
	}

	@Test
	public void testMockSearchBlowsUp() {
		server.enqueue(new MockResponse()
			.setBody("{garbage}"));

		TestObserver<List<Hotel>> observer = new TestObserver<>();
		NearbyHotelParams params = givenNearbyHotelParams();

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoValues();
		observer.assertError(IOException.class);
	}

	@Test
	public void testMockSearchWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<List<Hotel>> observer = new TestObserver<>();
		NearbyHotelParams params = givenNearbyHotelParams();

		service.nearbyHotels(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFastSearchHappyResponse() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelSearchResponse> observer = new TestObserver<>();
		PublishSubject testSubject = PublishSubject.create();

		HotelSearchParams params = givenHappyHotelSearchParams();

		service.setBucketedForHotelSatelliteSearch(true);
		service.search(params, testSubject).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelSearchResponse response = observer.values().get(0);
		assertEquals(215, response.hotelList.size());
		assertEquals(11, response.allNeighborhoodsInSearchRegion.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFastSearchHitsAllInterceptors() throws Throwable {
		givenServerUsingMockResponses();
		TestObserver<HotelSearchResponse> observer = new TestObserver<>();
		PublishSubject testSubject = PublishSubject.create();

		HotelSearchParams params = givenHappyHotelSearchParams();

		service.setBucketedForHotelSatelliteSearch(true);
		service.search(params, testSubject).subscribe(observer);
		observer.awaitTerminalEvent();
		observer.assertComplete();

		assertTrue(satelliteInterceptor.wasCalled());
	}

	@Test
	public void testMockDetailsWithoutOffersWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelOffersResponse> observer = new TestObserver<>();

		HotelSearchParams params = givenHappyHotelSearchParams();
		service.info(params, "happy", observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		Assert.assertNull(observer.values().get(0).hotelRoomResponse);
		Assert.assertNotNull(observer.values().get(0).checkInDate);
		Assert.assertNotNull(observer.values().get(0).checkOutDate);
	}

	@Test
	public void testMockDetailsWithOffersWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelOffersResponse> observer = new TestObserver<>();

		HotelSearchParams params = givenHappyHotelSearchParams();
		service.offers(params, "happypath", observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		Assert.assertNotNull(observer.values().get(0).hotelRoomResponse);
		Assert.assertNotNull(observer.values().get(0).checkInDate);
		Assert.assertNotNull(observer.values().get(0).checkOutDate);
	}

	@Test
	public void unknownRewardsTypesAreRemovedAndKnownRewardsTypesRemainInCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("hotel_pwp_multiple_points_types", false, 1,
			Collections.<Integer>emptyList());

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.values().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void knownRewardsTypeRemainInCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("happypath_pwp_points_only", false, 1,
			Collections.<Integer>emptyList());

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.values().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void unknownRewardsTypesAreRemovedFromCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("hotel_pwp_unknown_points_type", false, 1,
			Collections.<Integer>emptyList());

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.values().get(0);
		assertEquals(0, response.getPointsDetails().size());
	}

	@Test
	public void testCheckoutWorks() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelCheckoutResponse> observer = new TestObserver<>();

		String tripId = "happypath_0";
		TripDetails tripDetails = new TripDetails(tripId, "12123.33", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
	}

	@Test
	public void testCheckoutWithPriceChangeAndUserPreferences() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelCheckoutResponse> observer = new TestObserver<>();

		String tripId = "hotel_price_change_with_user_preferences";
		TripDetails tripDetails = new TripDetails(tripId, "675.81", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);
		assertNotNull(observer.values().get(0).pointsDetails);
		assertNotNull(observer.values().get(0).userPreferencePoints);
	}

	@Test
	public void checkoutWithPriceChangeDoesNotIncludeUnknownRewardsTypes() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelCheckoutResponse> observer = new TestObserver<>();

		String tripId = "hotel_price_change_with_multiple_points_types";
		TripDetails tripDetails = new TripDetails(tripId, "675.81", "USD", true);

		HotelCheckoutV2Params params = new HotelCheckoutV2Params.Builder().tripDetails(tripDetails)
			.checkoutInfo(HotelCheckoutParamsMock.checkoutInfo()).paymentInfo(HotelCheckoutParamsMock.paymentInfo())
			.traveler(HotelCheckoutParamsMock.traveler()).misc(HotelCheckoutParamsMock.miscellaneousParams(tripId))
			.build();

		service.checkout(params, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelCheckoutResponse response = observer.values().get(0);
		assertEquals(1, response.pointsDetails.size());
		assertEquals(ProgramName.ExpediaRewards, response.pointsDetails.get(0).getProgramName());
	}

	@Test
	public void testCouponRemove() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelCreateTripResponse> subscriber = new TestObserver<>();
		service.removeCoupon("happypath_coupon_remove_success", true).subscribe(subscriber);
		subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);
		subscriber.assertComplete();

		Assert.assertNotNull(subscriber.values().get(0).newHotelProductResponse);
	}

	@Test
	public void couponRemoveResponseHasOnlyKnownPointsTypes() throws Throwable {
		givenServerUsingMockResponses();

		TestObserver<HotelCreateTripResponse> subscriber = new TestObserver<>();
		service.removeCoupon("hotel_coupon_remove_success_with_multiple_points_types", true).subscribe(subscriber);
		subscriber.awaitTerminalEvent(2, TimeUnit.SECONDS);
		subscriber.assertComplete();

		subscriber.assertNoErrors();
		subscriber.assertComplete();
		subscriber.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = subscriber.values().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void testCouponError() throws IOException {
		givenServerUsingMockResponses();

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();
		givenCouponParams("hotel_coupon_errors_expired");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
		observer.assertComplete();
		ApiError apiError = observer.values().get(0).getFirstError();
		assertEquals(ApiError.Code.APPLY_COUPON_ERROR, apiError.getErrorCode());
	}

	@Test
	public void testCouponSuccess() throws IOException {
		givenServerUsingMockResponses();

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();
		givenCouponParams("hotel_coupon_success");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = observer.values().get(0);
		assertNull(response.getPointsDetails());
	}

	@Test
	public void couponApplyResponseHasOnlyKnownPointsTypes() throws IOException {
		givenServerUsingMockResponses();

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();
		givenCouponParams("hotel_coupon_success_multiple_points_types");

		service.applyCoupon(couponParams, true).subscribe(observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		// Guest user has no points details
		HotelCreateTripResponse response = observer.values().get(0);
		assertEquals(1, response.getPointsDetails().size());
		assertEquals(ProgramName.ExpediaRewards, response.getPointsDetails().get(0).getProgramName());
	}

	@Test
	public void testStoredCouponsWithCreateTripResponse() throws Throwable {
		givenServerUsingMockResponses();

		HotelCreateTripParams params = new HotelCreateTripParams("happypath_createtrip_saved_coupons", false, 1,
			Collections.<Integer>emptyList());

		TestObserver<HotelCreateTripResponse> observer = new TestObserver<>();

		service.createTrip(params, true, observer);
		observer.awaitTerminalEvent(10, TimeUnit.SECONDS);

		observer.assertNoErrors();
		observer.assertComplete();
		observer.assertValueCount(1);

		HotelCreateTripResponse response = observer.values().get(0);
		assertEquals(3, response.userCoupons.size());
		HotelCreateTripResponse.SavedCoupon firstCoupon = response.userCoupons.get(0);

		assertEquals(HotelCreateTripResponse.RedemptionStatus.REDEEMED, firstCoupon.redemptionStatus);
		assertEquals("1", firstCoupon.instanceId);
		assertEquals("firstCoupon", firstCoupon.name);

		HotelCreateTripResponse.SavedCoupon secondCoupon = response.userCoupons.get(1);
		assertEquals(HotelCreateTripResponse.RedemptionStatus.VALID, secondCoupon.redemptionStatus);
		assertEquals("2", secondCoupon.instanceId);
		assertEquals("ESCAPE20PERCENT - US", secondCoupon.name);

		HotelCreateTripResponse.SavedCoupon thirdCoupon = response.userCoupons.get(2);
		assertEquals(HotelCreateTripResponse.RedemptionStatus.EXPIRED, thirdCoupon.redemptionStatus);
		assertEquals("3", thirdCoupon.instanceId);
		assertEquals("Employee Escape Package Coupon - $1000-$1999", thirdCoupon.name);
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
