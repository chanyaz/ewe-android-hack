package com.expedia.bookings.utils;

import java.util.ArrayList;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;

public class TravelerUtils {

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


