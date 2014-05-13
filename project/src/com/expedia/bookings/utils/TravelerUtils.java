package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.enums.PassengerCategory;

public class TravelerUtils {

	public static ArrayList<String> generateTravelerBoxLabels(Context context, List<Traveler> travelers) {
		ArrayList<String> travelerLabels = new ArrayList<String>();
		if (travelers.size() == 1) {
			travelerLabels.add(context.getString(R.string.traveler_details));
		}
		else {
			int numAdultsAdded = 0;
			int numChildrenAdded = 0;
			int numInfantsInSeat = 0;
			int numInfantsInLap = 0;
			int sectionLabelId;
			int displayNumber;

			for (int index = 0; index < travelers.size(); index++) {
				Traveler traveler = travelers.get(index);
				PassengerCategory travelerPassengerCategory = traveler.getPassengerCategory();
				switch (travelerPassengerCategory) {
				case ADULT:
				case SENIOR:
					sectionLabelId = R.string.add_adult_number_TEMPLATE;
					displayNumber = ++numAdultsAdded;
					break;
				case CHILD:
				case ADULT_CHILD:
					sectionLabelId = R.string.add_child_with_age_TEMPLATE;
					displayNumber = traveler.getSearchedAge();
					++numChildrenAdded;
					break;
				case INFANT_IN_LAP:
					sectionLabelId = R.string.add_infant_in_lap_number_TEMPLATE;
					displayNumber = ++numInfantsInLap;
					break;
				case INFANT_IN_SEAT:
					sectionLabelId = R.string.add_infant_in_seat_number_TEMPLATE;
					displayNumber = ++numInfantsInSeat;
					break;
				default:
					throw new RuntimeException("Unidentified passenger category");
				}
				travelerLabels.add(context.getString(sectionLabelId, displayNumber));
			}
		}
		return travelerLabels;
	}


	public static boolean travelerFormRequiresEmail(int travelerNumber, LineOfBusiness lob, Context context) {
		if (travelerNumber == 0) {
			if (!User.isLoggedIn(context)) {
				if (Db.getBillingInfo() == null || !Db.getBillingInfo().isUsingGoogleWallet()) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean travelerFormRequiresPassport(LineOfBusiness lob) {
		if (lob == LineOfBusiness.FLIGHTS) {
			return lob == LineOfBusiness.FLIGHTS && Db.getFlightSearch() != null
				&& Db.getFlightSearch().getSelectedFlightTrip() != null && Db.getFlightSearch().getSelectedFlightTrip()
				.isInternational();
		}
		return false;
	}
}


