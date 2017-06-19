package com.expedia.bookings.services;

import com.expedia.bookings.data.rail.responses.TranslateResponse;

import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

public interface TranslateApi {

	@POST("api/v1.5/tr.json/translate")
	Observable<TranslateResponse> translate(@Query("key") String key,
		@Query("text") String text,
		@Query("lang") String lang
		);

}
