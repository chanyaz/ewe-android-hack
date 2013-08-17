package com.expedia.bookings.test.utils;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.utils.CurrencyUtils;

public class DataUtils {

	public static void setUpBillingInfo(BillingInfo b, String emailAddress,
			String phoneNumber, String phoneCountryCode, String firstName,
			String lastName, String cardNumber, String cardCVV,
			LocalDate ccExpirationDate, Location location) {
		b.setEmail(emailAddress);
		b.setFirstName(firstName);
		b.setLastName(lastName);
		b.setNameOnCard(b.getFirstName() + " " + b.getLastName());

		b.setLocation(location);

		b.setNumber(cardNumber);
		CreditCardType type = CurrencyUtils
				.detectCreditCardBrand(b.getNumber());
		b.setBrandCode(type.getCode());
		b.setBrandName(type.name());

		b.setExpirationDate(ccExpirationDate);

		b.setSecurityCode(cardCVV);
		b.setTelephone(phoneNumber);
		b.setTelephoneCountryCode(phoneCountryCode);
	}

	public static void setUpLocation(Location l, String city,
			String countryCode, String description, String addressLine,
			String postalCode, String stateCode, double latitude,
			double longitude) {
		l.setCity(city);
		l.setCountryCode(countryCode);
		l.setDescription(description);
		l.addStreetAddressLine(addressLine);
		l.setPostalCode(postalCode);
		l.setStateCode(stateCode);
		l.setLatitude(latitude);
		l.setLongitude(longitude);
	}
}
