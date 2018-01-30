package com.expedia.bookings.services.os;

import com.expedia.bookings.data.os.LastMinuteDealsResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OfferApi {
	@GET("/offers/v2/getOffers?")
	Observable<LastMinuteDealsResponse> lastMinuteDeals(
		@Query("siteId") String siteId,
		@Query("locale") String locale,
		@Query("groupBy") String groupBy,
		@Query("productType") String productType,
		@Query("destinationLimit") int destinationLimit,
		@Query("clientId") String clientId,
		@Query("page") String page,
		@Query("uid") String uid,
		@Query("scenario") String scenario,
		@Query("stayDateRanges") String stayDateRanges);
}
