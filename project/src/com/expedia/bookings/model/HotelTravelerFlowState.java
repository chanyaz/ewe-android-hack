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
public class HotelTravelerFlowState {
	//private static TravelerFlowState mInstance;

	Context mContext;

	SectionTravelerInfo mTravelerInfoOne;

	private HotelTravelerFlowState(Context context) {
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mTravelerInfoOne = (SectionTravelerInfo) inflater.inflate(R.layout.section_hotel_edit_traveler_pt1, null);
	}

	public static HotelTravelerFlowState getInstance(Context context) {
		if (context == null) {
			return null;
		}
		return new HotelTravelerFlowState(context);
	}

	public boolean hasValidTraveler(Traveler travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		return mTravelerInfoOne.hasValidInput();
	}
}
