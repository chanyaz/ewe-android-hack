package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightPassenger;
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

	private TravelerFlowState(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTravelerInfoOne = (SectionTravelerInfo) inflater.inflate(R.layout.section_edit_traveler_pt1, null);
		mTravelerInfoTwo = (SectionTravelerInfo) inflater.inflate(R.layout.section_edit_traveler_pt2, null);
	}

	public static TravelerFlowState getInstance(Context context) {
		return new TravelerFlowState(context);
	}

	private void bind(FlightPassenger travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		mTravelerInfoTwo.bind(travelerInfo);
	}

	public boolean hasValidTravelerPartOne(FlightPassenger travelerInfo) {
		bind(travelerInfo);
		return mTravelerInfoOne.hasValidInput();
	}

	public boolean hasValidTravelerPartTwo(FlightPassenger travelerInfo) {
		bind(travelerInfo);
		return mTravelerInfoTwo.hasValidInput();
	}

	public boolean allTravelerInfoIsValid(FlightPassenger travelerInfo) {
		bind(travelerInfo);
		boolean travOne = mTravelerInfoOne.hasValidInput();
		boolean travTwo = mTravelerInfoTwo.hasValidInput();
		return travOne && travTwo;
	}

}
