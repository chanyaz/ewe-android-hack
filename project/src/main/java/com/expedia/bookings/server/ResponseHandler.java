package com.expedia.bookings.server;

import java.io.IOException;

import okhttp3.Response;

public interface ResponseHandler<T> {
	T handleResponse(Response response) throws IOException;
}
