package com.expedia.bookings.data;

public class ScenarioSetResponse extends Response {

	private boolean mSuccess;

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	public boolean isSuccess() {
		return mSuccess;
	}
}
