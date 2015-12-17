package com.expedia.bookings.server;

import java.io.IOException;

import com.squareup.okhttp.Response;

public interface ResponseHandler<T> {
	T handleResponse(Response response) throws IOException;
}
