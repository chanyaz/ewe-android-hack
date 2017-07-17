package com.expedia.bookings.services;

import com.expedia.bookings.data.SatelliteSearchResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface SatelliteApi {

	@GET("/m/api/config/feature")

	Observable<SatelliteSearchResponse> satelliteSearch(
		@Query("clientid") String clientid,
		@Query("forceNoRedir") int forceNoRedir,
		@Query("siteid") int siteid);

}
