package com.expedia.bookings.utils;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.enums.PassengerCategory;

import java.util.ArrayList;
import java.util.List;

public class TravelerUtils {

	public static ArrayList<String> generateTravelerBoxLabels(Context context, List<Traveler> travelers) {
		ArrayList<String> travelerLabels = new ArrayList<String>();

		if (travelers.size() == 1) {
			travelerLabels.add(context.getString(R.string.traveler_details_variate2));
		}
		else {
			int numAdultsAdded = 0, numAdults = 0;
			int numChildrenAdded = 0, numChildren = 0;
			int numInfantsInSeat = 0, numInfantsSeat = 0;
			int numInfantsInLap = 0, numInfantsLap = 0;
			int sectionLabelId = 0;
			int displayNumber = 0;

			FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
			// This the only way to get count of each passenger category, before we can accordingly assign labels numbered or not.
			for (int index = 0; index < travelers.size(); index++) {
				Traveler traveler = travelers.get(index);
				PassengerCategory travelerPassengerCategory = traveler.getPassengerCategory(searchParams);
				switch (travelerPassengerCategory) {
				case ADULT:
				case SENIOR:
					++numAdults;
					break;
				case CHILD:
				case ADULT_CHILD:
					++numChildren;
					break;
				case INFANT_IN_LAP:
					++numInfantsLap;
					break;
				case INFANT_IN_SEAT:
					++numInfantsSeat;
					break;
				default:
					throw new RuntimeException("Unidentified passenger category");
				}
			}



			for (int index = 0; index < travelers.size(); index++) {
				Traveler traveler = travelers.get(index);
				PassengerCategory travelerPassengerCategory = traveler.getPassengerCategory(searchParams);
				boolean useTemplate = false;
				switch (travelerPassengerCategory) {
				case ADULT:
				case SENIOR:
					if (numAdults > 1) {
						sectionLabelId = R.string.add_adult_number_variate2_TEMPLATE;
						displayNumber = ++numAdultsAdded;
						useTemplate = true;
					}
					else {
						sectionLabelId = R.string.add_adult_variate2;
					}
					break;
				case CHILD:
				case ADULT_CHILD:
					sectionLabelId = R.string.add_child_with_age_variate2_TEMPLATE;
					displayNumber = traveler.getSearchedAge();
					++numChildrenAdded;
					useTemplate = true;
					break;
				case INFANT_IN_LAP:
					if (numInfantsLap > 1) {
						sectionLabelId = R.string.add_infant_in_lap_number_variate2_TEMPLATE;
						displayNumber = ++numInfantsInLap;
						useTemplate = true;
					}
					else {
						sectionLabelId = R.string.add_infant_in_lap_variate2;
					}
					break;
				case INFANT_IN_SEAT:
					if (numInfantsSeat > 1) {
						sectionLabelId = R.string.add_infant_in_seat_number_variate2_TEMPLATE;
						displayNumber = ++numInfantsInSeat;
						useTemplate = true;
					}
					else {
						sectionLabelId = R.string.add_infant_in_seat_variate2;
					}
					break;
				default:
					throw new RuntimeException("Unidentified passenger category");
				}
				if (useTemplate) {
					travelerLabels.add(context.getString(sectionLabelId, displayNumber));
				}
				else {
					travelerLabels.add(context.getString(sectionLabelId));
				}

			}
		}
		return travelerLabels;
	}


	public static boolean travelerFormRequiresEmail(int travelerNumber, Context context) {
		if (travelerNumber == 0 && !User.isLoggedIn(context)) {
			return true;
		}
		return false;
	}

	public static boolean travelerFormRequiresPassport(LineOfBusiness lob) {
		if (lob == LineOfBusiness.FLIGHTS) {
			return Db.getTripBucket().getFlight() != null &&
				Db.getTripBucket().getFlight().getFlightTrip() != null &&
				(Db.getTripBucket().getFlight().getFlightTrip()
					.isInternational() || Db.getTripBucket().getFlight().getFlightTrip()
					.isPassportNeeded());
		}
		return false;
	}

	public static void setPhoneTextViewVisibility(View container, int travelerIndex) {
		int vis = travelerIndex > 0 ? View.GONE : View.VISIBLE;
		View phone = container.findViewById(R.id.display_phone_number_with_country_code);
		if (phone != null) {
			phone.setVisibility(vis);
		}
	}

	/**
	 * If the current traveler is replaced by another traveler from the list, let's reset {@link Traveler#isSelectable()} state.
	 * We need to do this so that the traveler available to be selected again.
	 *
	 * @param traveler
	 */
	public static void resetPreviousTravelerSelectState(Traveler traveler) {
		ArrayList<Traveler> availableTravelers = new ArrayList<Traveler>(Db.getUser().getAssociatedTravelers());
		availableTravelers.add(Db.getUser().getPrimaryTraveler());
		// Check if the traveler is the primary traveler
		if (traveler.nameEquals(Db.getUser().getPrimaryTraveler())) {
			Db.getUser().getPrimaryTraveler().setIsSelectable(true);
			return;
		}
		// Check to find the desired traveler and reset his selectable state
		for (int i = 0; i < availableTravelers.size(); i++) {
			if (traveler.nameEquals(availableTravelers.get(i))) {
				Db.getUser().getAssociatedTravelers().get(i).setIsSelectable(true);
			}
		}
	}

	public static Boolean isMainTraveler(int index) {
		return index == 0;
	}
}


