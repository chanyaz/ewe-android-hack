package com.expedia.bookings.test.espresso;

import android.support.test.InstrumentationRegistry;

import com.expedia.bookings.test.R;

public class HotelsUserData {

	//Names
	public String firstName = "JexperCC";
	public String lastName = "MobiataTestaverde";

	//Contact
	public String phoneNumber = "7342122392";
	public String address = "1234 Test Blvd";
	public String city = "Ann Arbor";
	public String state = "MI";
	public String zipcode = "48104";

	//Credit Card Information
	public String creditCardNumber = "4111111111111111";
	public String cvv = "111";

	//Log in Information.
	public String email = InstrumentationRegistry.getInstrumentation().getContext().getString(R.string.user_name);
	public String password = InstrumentationRegistry.getInstrumentation().getContext().getString(R.string.user_password);
}
