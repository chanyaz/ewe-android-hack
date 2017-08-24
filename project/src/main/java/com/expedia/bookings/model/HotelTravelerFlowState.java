package com.expedia.bookings.model;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.utils.Ui;

/***
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 * @author jdrotos
 *
 */
public class HotelTravelerFlowState {
	//private static TravelerFlowState mInstance;

	final Context mContext;

	final SectionTravelerInfo mTravelerInfoOne;

	private HotelTravelerFlowState(Context context) {
		mContext = context;
		mTravelerInfoOne = Ui.inflate(context, R.layout.section_hotel_edit_traveler_pt1, null);
	}

	public static HotelTravelerFlowState getInstance(Context context) {
		if (context == null) {
			return null;
		}
		return new HotelTravelerFlowState(context);
	}

	public boolean hasValidTraveler(Traveler travelerInfo) {
		mTravelerInfoOne.bind(travelerInfo);
		return mTravelerInfoOne.performValidation();
	}
}
