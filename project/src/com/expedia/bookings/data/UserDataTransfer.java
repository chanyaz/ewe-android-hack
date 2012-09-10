package com.expedia.bookings.data;

public class UserDataTransfer {

	/**
	 * This fills a FlightPassenger object from a User object, but it just copies values, it doesn't use stored passengers
	 * @param usr
	 * @param psngr
	 * @return
	 */
	public static Traveler fillPassengerFromUser(User usr, Traveler psngr) {
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

	/**
	 * Populate FlightPassenger from BillingInfo
	 * @param info
	 * @param psngr
	 * @return
	 */
	public static Traveler fillPassengerFromBillingInfo(BillingInfo info, Traveler psngr) {
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
	
	public static Traveler getBestGuessStoredPassenger(User usr){
		if(usr.getAssociatedTravelers() != null && usr.getAssociatedTravelers().size() > 0){
			return usr.getAssociatedTravelers().get(0);
		}else{
			return fillPassengerFromUser(usr,new Traveler());
		}
		
	}
}
