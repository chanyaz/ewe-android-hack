package com.expedia.bookings.data;

public class GsonResponse<T> extends Response {

	private T mObject;

	public GsonResponse(T object) {
		mObject = object;
	}

	public T get() {
		return mObject;
	}
}
