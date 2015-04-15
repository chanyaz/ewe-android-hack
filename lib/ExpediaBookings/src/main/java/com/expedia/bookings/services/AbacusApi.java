package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusLogResponse;
import com.expedia.bookings.data.abacus.AbacusResponse;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

public interface AbacusApi {

	@GET("/api/bucketing/v1/evaluateExperiments")
	public Observable<AbacusResponse> evaluateExperiments(
		@Query("guid") String guid,
		@Query("eapid") int eapid,
		@Query("tpid") int tpid,
		@Query("id") List<Integer> testID)
	;

	@POST("/api/bucketing/v1/logExperiments")
	public Observable<AbacusLogResponse> logExperiment(
		@Body AbacusLogQuery body)
	;
}
