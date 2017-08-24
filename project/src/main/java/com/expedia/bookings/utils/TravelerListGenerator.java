package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.PassengerCategoryPrice;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.enums.PassengerCategory;

/**
 * Created by kecarpenter on 4/28/14.
 */
public class TravelerListGenerator {

	public final Comparator<Traveler> byPassengerCategory = new Comparator<Traveler>() {
		@Override
		public int compare(Traveler lhs, Traveler rhs) {
			FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
			PassengerCategory lhsCategory = lhs.getPassengerCategory(searchParams);
			PassengerCategory rhsCategory = rhs.getPassengerCategory(searchParams);
			if (lhsCategory != rhsCategory) {
				return lhs.getPassengerCategory(searchParams).compareTo(rhs.getPassengerCategory(searchParams));
			}
			else {
				return lhs.getSearchedAge() - rhs.getSearchedAge();
			}
		}
	};

	private final ArrayList<Traveler> mTravelerList;

	private int mNumDesiredAdults = 0;
	private int mNumDesiredAdultChildren = 0;
	private int mNumDesiredChildren = 0;
	private int mNumDesiredLapInfants = 0;
	private int mNumDesiredSeatInfants = 0;

	private int mNumAddedAdults = 0;
	private int mNumAddedAdultChildren = 0;
	private int mNumAddedChildren = 0;
	private int mNumAddedLapInfants = 0;
	private int mNumAddedSeatInfants = 0;


	public TravelerListGenerator(final List<PassengerCategoryPrice> passengerList, final List<Traveler> travelers) {
		mTravelerList = new ArrayList<Traveler>(passengerList.size());
		for (int i = 0; i < passengerList.size(); i++) {
			switch (passengerList.get(i).getPassengerCategory()) {
			case SENIOR:
			case ADULT:
				mNumDesiredAdults++;
				break;
			case ADULT_CHILD:
				mNumDesiredAdultChildren++;
				break;
			case CHILD:
				mNumDesiredChildren++;
				break;
			case INFANT_IN_LAP:
				mNumDesiredLapInfants++;
				break;
			case INFANT_IN_SEAT:
				mNumDesiredSeatInfants++;
				break;
			default:
				break;
			}
		}
		FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
		for (Traveler traveler : travelers) {
			// If the Traveler in Db does not have a category assigned, we throw it out.
			if (traveler.getPassengerCategory(searchParams) == null) {
				continue;
			}
			mTravelerList.add(traveler);
			switch (traveler.getPassengerCategory(searchParams)) {
			case SENIOR:
			case ADULT:
				mNumAddedAdults++;
				break;
			case ADULT_CHILD:
				mNumAddedAdultChildren++;
				break;
			case CHILD:
				mNumAddedChildren++;
				break;
			case INFANT_IN_LAP:
				mNumAddedLapInfants++;
				break;
			case INFANT_IN_SEAT:
				mNumAddedSeatInfants++;
				break;
			default:
				break;
			}
		}
	}

	private void addDesiredNumberOfTravelers() {
		while (mNumAddedAdults < mNumDesiredAdults) {
			addPassengerOfCategory(PassengerCategory.ADULT);
			mNumAddedAdults++;
		}
		while (mNumAddedAdultChildren < mNumDesiredAdultChildren) {
			addPassengerOfCategory(PassengerCategory.ADULT_CHILD);
			mNumAddedAdultChildren++;
		}
		while (mNumAddedChildren < mNumDesiredChildren) {
			addPassengerOfCategory(PassengerCategory.CHILD);
			mNumAddedChildren++;
		}
		while (mNumAddedLapInfants < mNumDesiredLapInfants) {
			addPassengerOfCategory(PassengerCategory.INFANT_IN_LAP);
			mNumAddedLapInfants++;
		}
		while (mNumAddedSeatInfants < mNumDesiredSeatInfants) {
			addPassengerOfCategory(PassengerCategory.INFANT_IN_SEAT);
			mNumAddedSeatInfants++;
		}
	}

	private void addPassengerOfCategory(PassengerCategory passengerCategory) {
		Traveler newPassenger = new Traveler();
		newPassenger.setPassengerCategory(passengerCategory);
		mTravelerList.add(newPassenger);
	}

	private void removePassengerOfCategory(PassengerCategory passengerCategory, int numberToRemove) {
		// Prioritize the traveler list, so that the ones at the end
		// are more likely to be travelers we want to remove
		// (i.e. they are less likely to have stored info
		Collections.sort(mTravelerList, byPassengerCategory);
		int numberRemoved = 0;
		FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
		for (int i = mTravelerList.size() - 1; i > 0; i--) {
			Traveler traveler = mTravelerList.get(i);
			if (traveler.getPassengerCategory(searchParams) == passengerCategory) {
				mTravelerList.remove(traveler);
				if (++numberRemoved == numberToRemove) {
					break;
				}
			}
		}
	}

	private void removeUndesiredTravelers() {
		if (mNumAddedAdults > mNumDesiredAdults) {
			removePassengerOfCategory(PassengerCategory.ADULT, mNumAddedAdults - mNumDesiredAdults);
		}
		if (mNumAddedAdultChildren > mNumDesiredAdultChildren) {
			removePassengerOfCategory(PassengerCategory.ADULT_CHILD, mNumAddedAdultChildren - mNumDesiredAdultChildren);
		}
		if (mNumAddedChildren > mNumDesiredChildren) {
			removePassengerOfCategory(PassengerCategory.CHILD, mNumAddedChildren - mNumDesiredChildren);
		}
		if (mNumAddedLapInfants > mNumDesiredLapInfants) {
			removePassengerOfCategory(PassengerCategory.INFANT_IN_LAP, mNumAddedLapInfants - mNumDesiredLapInfants);
		}
		if (mNumAddedSeatInfants > mNumDesiredSeatInfants) {
			removePassengerOfCategory(PassengerCategory.INFANT_IN_SEAT, mNumAddedSeatInfants - mNumDesiredSeatInfants);
		}
	}

	public void assignAgesToChildTravelers() {
		List<ChildTraveler> children = Db.getTripBucket().getFlight().getFlightSearchParams().getChildren();

		Collections.sort(children, Collections.reverseOrder());
		Collections.sort(mTravelerList, byPassengerCategory);
		int firstChildIndex = 0;
		FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
		for (int i = 0; i < mTravelerList.size(); i++) {
			PassengerCategory pc = mTravelerList.get(i).getPassengerCategory(searchParams);
			if (pc != PassengerCategory.ADULT && pc != PassengerCategory.SENIOR) {
				firstChildIndex = i;
				break;
			}
			else {
				mTravelerList.get(i).setSearchedAge(-1); // Age does not matter for adults.
			}
		}
		if (children.size() != 0) {
			int childStart = 0;
			for (int j = firstChildIndex; j < mTravelerList.size(); j++) {
				int age = children.get(childStart++).getAge();
				Traveler t = mTravelerList.get(j);
				if (t.getSearchedAge() != 0) {
					mTravelerList.get(j).setSearchedAge(age);
				}
			}
		}
	}

	public ArrayList<Traveler> generateTravelerList() {
		addDesiredNumberOfTravelers();
		removeUndesiredTravelers();
		Collections.sort(mTravelerList, byPassengerCategory);
		assignAgesToChildTravelers();
		return mTravelerList;
	}
}
