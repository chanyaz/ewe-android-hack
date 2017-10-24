package com.expedia.bookings.services;

import com.expedia.bookings.data.payment.CalculatePointsResponse;
import com.expedia.bookings.data.payment.CampaignDetails;
import com.expedia.bookings.data.payment.ContributeResponse;
import com.expedia.bookings.data.payment.ProgramName;

import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface LoyaltyApi {

	@GET("/m/api/trip/calculatePoints")
	Observable<CalculatePointsResponse> currencyToPoints(
		@Query("tripId") String tripId,
		@Query("programName") ProgramName programName,
		@Query("amount") String amount,
		@Query("rateId") String rateId
	);

	@GET("service/campaign/contribute")
	Observable<ContributeResponse> contribute(
		@Query("donorTUID") String donorTUID,
		@Query("donorName") String donorName,
		@Query("recieverTUID") String recieverTUID,
		@Query("amount") String amount,
		@Query("tripID") String tripID
	);

	@GET("service/campaign/registration")
	Observable<ContributeResponse> register(
		@Query("tuid") String tuid,
		@Query("tripID") String tripID,
		@Query("title") String title,
		@Query("message") String message,
		@Query("fundsRequested") String fundsRequested,
		@Query("fundsAvailable") String fundsAvailable,
		@Query("imageURL") String imageURL
	);

	@GET("service/campaign/details")
	Observable<CampaignDetails> campaignDetails(
		@Query("tripID") String tripID
	);
}
