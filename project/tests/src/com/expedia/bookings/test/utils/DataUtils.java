package com.expedia.bookings.test.utils;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.utils.CurrencyUtils;

public class DataUtils {

	public static BillingInfo setUpBillingInfo(String emailAddress,
			String phoneNumber, String phoneCountryCode, String firstName,
			String lastName, String cardNumber, String cardCVV,
			LocalDate ccExpirationDate, Location location) {
		BillingInfo billingInfo = new BillingInfo();
		billingInfo.setEmail(emailAddress);
		billingInfo.setFirstName(firstName);
		billingInfo.setLastName(lastName);
		billingInfo.setNameOnCard(billingInfo.getFirstName() + " " + billingInfo.getLastName());

		billingInfo.setLocation(location);

		billingInfo.setNumber(cardNumber);
		CreditCardType type = CurrencyUtils
				.detectCreditCardBrand(billingInfo.getNumber());
		billingInfo.setBrandCode(type.getCode());
		billingInfo.setBrandName(type.name());

		billingInfo.setExpirationDate(ccExpirationDate);

		billingInfo.setSecurityCode(cardCVV);
		billingInfo.setTelephone(phoneNumber);
		billingInfo.setTelephoneCountryCode(phoneCountryCode);

		return billingInfo;
	}

	public static Location setUpLocation(String city,
			String countryCode, String description, String addressLine,
			String postalCode, String stateCode, double latitude,
			double longitude, String destinationId) {
		Location location = new Location();
		location.setCity(city);
		location.setCountryCode(countryCode);
		location.setDescription(description);
		location.addStreetAddressLine(addressLine);
		location.setPostalCode(postalCode);
		location.setStateCode(stateCode);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		location.setDestinationId(destinationId);
		return location;
	}

	public static FlightSearchParams setUpFlightSearchParams(Location depLoc,
			Location arrLoc,
			LocalDate depDate, LocalDate retDate, int numAdults) {
		FlightSearchParams params = new FlightSearchParams();
		params.setDepartureLocation(depLoc);
		params.setArrivalLocation(arrLoc);
		params.setDepartureDate(depDate);
		params.setReturnDate(retDate);
		params.setNumAdults(numAdults);
		return params;
	}

	public static Money setUpMoney(String amount, String currencyType) {
		Money money = new Money();
		money.setAmount(amount);
		money.setCurrency(currencyType);
		return money;
	}
}
