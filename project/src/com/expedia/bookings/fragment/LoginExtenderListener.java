package com.expedia.bookings.fragment;

/**
 * This interface provides a call for
 */
public interface LoginExtenderListener {
	public void loginExtenderWorkComplete(LoginExtender extender);

	public void setExtenderStatus(String status);
}
