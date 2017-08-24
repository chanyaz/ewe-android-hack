package com.expedia.bookings.data;

public class GsonResponse<T> extends Response {

	private final T mObject;

	public GsonResponse(T object) {
		mObject = object;
	}

	public T get() {
		return mObject;
	}
}
