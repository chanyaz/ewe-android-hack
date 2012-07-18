package com.expedia.bookings.data;

public class UserDataTransfer {

	public static FlightPassenger fillPassengerFromUser(User usr, FlightPassenger psngr) {
		if (usr == null || psngr == null) {
			return psngr;
		}

		if (usr.getFirstName() != null) {
			psngr.setFirstName(usr.getFirstName());
		}
		if (usr.getMiddleName() != null) {
			psngr.setMiddleName(usr.getMiddleName());
		}
		if (usr.getLastName() != null) {
			psngr.setLastName(usr.getLastName());
		}
		if (usr.getEmail() != null) {
			psngr.setEmail(usr.getEmail());
		}

		return psngr;
	}

	public static FlightPassenger fillPassengerFromBillingInfo(BillingInfo info, FlightPassenger psngr) {
		if (info == null || psngr == null) {
			return psngr;
		}

		if (info.getFirstName() != null) {
			psngr.setFirstName(info.getFirstName());
		}

		if (info.getLastName() != null) {
			psngr.setLastName(info.getLastName());
		}

		if (info.getTelephone() != null) {
			psngr.setPhoneNumber(info.getTelephone());
		}

		if (info.getTelephoneCountryCode() != null) {
			psngr.setPhoneCountryCode(info.getTelephoneCountryCode());
		}

		if (info.getEmail() != null) {
			psngr.setEmail(info.getEmail());
		}

		return psngr;
	}
}
