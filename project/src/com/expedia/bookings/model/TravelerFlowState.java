package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.SectionTravelerInfo;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class TravelerFlowState {
	//private static TravelerFlowState mInstance;

	Context mContext;

	SectionTravelerInfo mTravelerInfoOne;
	SectionTravelerInfo mTravelerInfoTwo;
	SectionTravelerInfo mTravelerInfoThree;

	private TravelerFlowState(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTravelerInfoOne = (SectionTravelerInfo) inflater.inflate(R.layout.section_edit_traveler_pt1, null);
		mTravelerInfoTwo = (SectionTravelerInfo) inflater.inflate(R.layout.section_edit_traveler_pt2, null);
		mTravelerInfoThree = (SectionTravelerInfo) inflater.inflate(R.layout.section_edit_traveler_pt3, null);
		mTravelerInfoThree.setAutoChoosePassportCountryEnabled(false);//Turn off auto select passport country for validation purposes
	}

	public static TravelerFlowState getInstance(Context context) {
		return new TravelerFlowState(context);
	}

	private void bind(Traveler travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		mTravelerInfoTwo.bind(travelerInfo);
		mTravelerInfoThree.bind(travelerInfo);
	}

	public boolean hasValidTravelerPartOne(Traveler travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		return mTravelerInfoOne.hasValidInput();
	}

	public boolean hasValidTravelerPartTwo(Traveler travelerInfo) {
		mTravelerInfoTwo.bind(travelerInfo);
		return mTravelerInfoTwo.hasValidInput();
	}
	
	public boolean hasValidTravelerPartThree(Traveler travelerInfo){
		mTravelerInfoThree.bind(travelerInfo);
		return mTravelerInfoThree.hasValidInput();
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
		boolean travThree = mTravelerInfoThree.hasValidInput();
		return travOne && travTwo && travThree;
	}

}
