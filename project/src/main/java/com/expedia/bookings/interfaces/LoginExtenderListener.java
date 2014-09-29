package com.expedia.bookings.interfaces;

import com.expedia.bookings.utils.LoginExtender;

/**
 * This interface provides a call for
 */
public interface LoginExtenderListener {
	public void loginExtenderWorkComplete(LoginExtender extender);

	public void setExtenderStatus(String status);
}
