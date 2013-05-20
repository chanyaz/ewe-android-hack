package com.expedia.bookings.data;

/**
 * Response for push notification registration.
 */
public class PushNotificationRegistrationResponse extends Response {

	private boolean mSuccess = false;

	public PushNotificationRegistrationResponse() {

	}

	public void setSuccess(boolean success) {
		mSuccess = success;
	}

	public boolean getSuccess() {
		return mSuccess;
	}

}
