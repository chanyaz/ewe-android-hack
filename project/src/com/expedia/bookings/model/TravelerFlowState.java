package com.expedia.bookings.model;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.utils.Ui;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class TravelerFlowState {
	Context mContext;

	SectionTravelerInfo mTravelerInfoOne;
	SectionTravelerInfo mTravelerInfoTwo;

	private TravelerFlowState(Context context) {
		mContext = context;
		mTravelerInfoOne = Ui.inflate(context, R.layout.section_flight_edit_traveler_pt1, null);
		mTravelerInfoTwo = Ui.inflate(context, R.layout.section_flight_edit_traveler_pt2, null);
	}

	public static TravelerFlowState getInstance(Context context) {
		if (context == null) {
			return null;
		}
		return new TravelerFlowState(context);
	}

	private void bind(Traveler travelerInfo) {
		int index = Db.getTravelers().indexOf(travelerInfo);
		mTravelerInfoOne.bind(travelerInfo, index);
		mTravelerInfoTwo.bind(travelerInfo);
	}

	public boolean hasValidTravelerPartOne(Traveler travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		return mTravelerInfoOne.hasValidInput();
	}

	public boolean hasValidTravelerPartTwo(Traveler travelerInfo) {
		mTravelerInfoTwo.bind(travelerInfo);
		return mTravelerInfoTwo.hasValidInput();
	}

	public boolean hasValidTravelerPartThree(Traveler travelerInfo) {
		return hasPassport(travelerInfo);
	}

	private boolean hasPassport(Traveler travelerInfo) {
		return travelerInfo.getPassportCountries() != null && travelerInfo.getPassportCountries().size() > 0;
	}

	public boolean allTravelerInfoIsValidForDomesticFlight(Traveler travelerInfo) {
		bind(travelerInfo);
		boolean travOne = mTravelerInfoOne.hasValidInput();
		boolean travTwo = mTravelerInfoTwo.hasValidInput();
		return travOne && travTwo;
	}

	public boolean allTravelerInfoIsValidForInternationalFlight(Traveler travelerInfo) {
		bind(travelerInfo);
		boolean travOne = mTravelerInfoOne.hasValidInput();
		boolean travTwo = mTravelerInfoTwo.hasValidInput();
		boolean hasPassport = hasPassport(travelerInfo);
		return travOne && travTwo && hasPassport;
	}

	public boolean allTravelerInfoValid(Traveler traveler, boolean isInternational) {
		if (isInternational) {
			return allTravelerInfoIsValidForInternationalFlight(traveler);
		}
		else {
			return allTravelerInfoIsValidForDomesticFlight(traveler);
		}
	}
}
