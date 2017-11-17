package com.expedia.bookings.services.sos;

import com.expedia.bookings.data.sos.DealsResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface SmartOfferApi {
	@GET("/sos/offers/member-only-deals/v1?")
	Observable<DealsResponse> memberDeals(
			@Query("siteId") String siteId,
			@Query("locale") String locale,
			@Query("productType") String productType,
			@Query("groupBy") String groupBy,
			@Query("destinationLimit") int destinationLimit,
			@Query("clientId") String clientId);

	@GET("/sos/offers/last-minute-deals/v1?")
	Observable<DealsResponse> lastMinuteDeals(
			@Query("siteId") String siteId,
			@Query("locale") String locale,
			@Query("groupBy") String groupBy,
			@Query("productType") String productType,
			@Query("destinationLimit") int destinationLimit,
			@Query("clientId") String clientId,
			@Query("stayDateRanges") String stayDateRanges);
}
