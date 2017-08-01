package com.expedia.bookings.services;

import java.util.List;

import com.expedia.bookings.data.abacus.AbacusLogQuery;
import com.expedia.bookings.data.abacus.AbacusLogResponse;
import com.expedia.bookings.data.abacus.AbacusResponse;

import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import io.reactivex.Observable;

public interface AbacusApi {

	@GET("/api/bucketing/v1/evaluateExperiments")
	Observable<AbacusResponse> evaluateExperiments(
		@Query("guid") String guid,
		@Query("eapid") int eapid,
		@Query("tpid") int tpid,
		@Query("id") List<Integer> testID)
	;

	@POST("/api/bucketing/v1/logExperiments")
	Observable<AbacusLogResponse> logExperiment(
		@Body AbacusLogQuery body)
	;
}
