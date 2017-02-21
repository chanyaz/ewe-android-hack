package com.expedia.bookings.services.sos;

import com.expedia.bookings.data.sos.MemberOnlyDealResponse;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface SmartOfferApi {
	@GET("/sos/offers/member-only-deals/v1?")
	Observable<MemberOnlyDealResponse> memberOnlyDeals(
			@Query("siteId") String siteId,
			@Query("locale") String locale,
			@Query("productType") String productType,
			@Query("groupBy") String groupBy,
			@Query("destinationLimit") int destinationLimit,
			@Query("clientId") String clientId);

}
