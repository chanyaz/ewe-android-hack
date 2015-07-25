package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.enums.PassengerCategory;

public class TravelerUtils {

	public static ArrayList<String> generateTravelerBoxLabels(Context context, List<Traveler> travelers) {
		ArrayList<String> travelerLabels = new ArrayList<String>();

		boolean isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightMissingTravelerInfoCallout);
		int testVariate = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppFlightMissingTravelerInfoCallout);

		if (travelers.size() == 1) {
			if (!isUserBucketedForTest) {
				travelerLabels.add(context.getString(R.string.traveler_details));
			}
			else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
				travelerLabels.add(context.getString(R.string.traveler_details_variate1));
			}
			else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
				travelerLabels.add(context.getString(R.string.traveler_details_variate2));
			}

		}
		else {
			int numAdultsAdded = 0, numAdults = 0;
			int numChildrenAdded = 0, numChildren = 0;
			int numInfantsInSeat = 0, numInfantsSeat = 0;
			int numInfantsInLap = 0, numInfantsLap = 0;
			int sectionLabelId = 0;
			int displayNumber = 0;

			// This the only way to get count of each passenger category, before we can accordingly assign labels numbered or not.
			for (int index = 0; index < travelers.size(); index++) {
				Traveler traveler = travelers.get(index);
				PassengerCategory travelerPassengerCategory = traveler.getPassengerCategory();
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
				PassengerCategory travelerPassengerCategory = traveler.getPassengerCategory();
				boolean useTemplate = false;
				switch (travelerPassengerCategory) {
				case ADULT:
				case SENIOR:
					if (numAdults > 1) {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_adult_number_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_adult_number_variate1_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_adult_number_variate2_TEMPLATE;
						}
						displayNumber = ++numAdultsAdded;
						useTemplate = true;
					}
					else {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_adult;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_adult_variate1;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_adult_variate2;
						}
					}
					break;
				case CHILD:
				case ADULT_CHILD:
					if (!isUserBucketedForTest) {
						sectionLabelId = R.string.add_child_with_age_TEMPLATE;
					}
					else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
						sectionLabelId = R.string.add_child_with_age_variate1_TEMPLATE;
					}
					else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
						sectionLabelId = R.string.add_child_with_age_variate2_TEMPLATE;
					}
					displayNumber = traveler.getSearchedAge();
					++numChildrenAdded;
					useTemplate = true;
					break;
				case INFANT_IN_LAP:
					if (numInfantsLap > 1) {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_infant_in_lap_number_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_lap_number_variate1_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_lap_number_variate2_TEMPLATE;
						}
						displayNumber = ++numInfantsInLap;
						useTemplate = true;
					}
					else {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_infant_in_lap;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_lap_variate1;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_lap_variate2;
						}
					}
					break;
				case INFANT_IN_SEAT:
					if (numInfantsSeat > 1) {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_infant_in_seat_number_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_seat_number_variate1_TEMPLATE;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_seat_number_variate2_TEMPLATE;
						}
						displayNumber = ++numInfantsInSeat;
						useTemplate = true;
					}
					else {
						if (!isUserBucketedForTest) {
							sectionLabelId = R.string.add_infant_in_seat;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SINGLE_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_seat_variate1;
						}
						else if (testVariate == AbacusUtils.FMissingTravelerCalloutVariate.SECOND_LINE_CALLOUT.ordinal()) {
							sectionLabelId = R.string.add_infant_in_seat_variate2;
						}
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


	public static boolean travelerFormRequiresEmail(int travelerNumber, LineOfBusiness lob, Context context) {
		if (travelerNumber == 0 && !User.isLoggedIn(context)) {
			if (Db.getBillingInfo() == null || !Db.getBillingInfo().isUsingGoogleWallet()) {
				return true;
			}
		}
		return false;
	}

	public static boolean travelerFormRequiresPassport(LineOfBusiness lob) {
		if (lob == LineOfBusiness.FLIGHTS) {
			return lob == LineOfBusiness.FLIGHTS && Db.getTripBucket().getFlight() != null
				&& Db.getTripBucket().getFlight().getFlightTrip() != null && (Db.getTripBucket().getFlight().getFlightTrip()
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
		if (traveler.compareNameTo(Db.getUser().getPrimaryTraveler()) == 0) {
			Db.getUser().getPrimaryTraveler().setIsSelectable(true);
			return;
		}
		// Check to find the desired traveler and reset his selectable state
		for (int i = 0; i < availableTravelers.size(); i++) {
			if (traveler.compareNameTo(availableTravelers.get(i)) == 0) {
				Db.getUser().getAssociatedTravelers().get(i).setIsSelectable(true);
			}
		}
	}
}


