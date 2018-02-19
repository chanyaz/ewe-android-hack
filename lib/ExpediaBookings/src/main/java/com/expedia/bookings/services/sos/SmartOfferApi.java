package com.expedia.bookings.services.sos;

import com.expedia.bookings.data.sos.MemberDealsResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface SmartOfferApi {
	@GET("/sos/offers/member-only-deals/v1?")
	Observable<MemberDealsResponse> memberDeals(
			@Query("siteId") String siteId,
			@Query("locale") String locale,
			@Query("productType") String productType,
			@Query("groupBy") String groupBy,
			@Query("destinationLimit") int destinationLimit,
			@Query("clientId") String clientId);

}
