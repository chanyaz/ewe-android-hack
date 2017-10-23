package com.expedia.bookings.services.sos;

import com.expedia.bookings.data.sos.MemberDealResponse;
import com.expedia.bookings.data.sos.TrendingDestinationResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface SmartOfferApi {
	@GET("/sos/offers/member-only-deals/v1?")
	Observable<MemberDealResponse> memberDeals(
			@Query("siteId") String siteId,
			@Query("locale") String locale,
			@Query("productType") String productType,
			@Query("groupBy") String groupBy,
			@Query("destinationLimit") int destinationLimit,
			@Query("clientId") String clientId);

	@GET("static/mobile/trendingdestination?")
	Observable<TrendingDestinationResponse> trendingDestinations(
		@Query("siteId") String siteId,
		@Query("locale") String locale,
		@Query("productType") String productType,
		@Query("groupBy") String groupBy,
		@Query("destinationLimit") int destinationLimit,
		@Query("clientId") String clientId);
}
