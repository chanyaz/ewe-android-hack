package com.expedia.bookings.interfaces;

import com.expedia.bookings.utils.LoginExtender;

public interface LoginExtenderListener {
	void loginExtenderWorkComplete(LoginExtender extender);

	void setExtenderStatus(String status);
}
