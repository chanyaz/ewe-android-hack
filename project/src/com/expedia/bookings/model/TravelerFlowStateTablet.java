package com.expedia.bookings.model;

import android.content.Context;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.interfaces.ILOBable;
import com.expedia.bookings.section.SectionTravelerInfoTablet;

/**
 * This class uses our SectionClasses to perform validation, we take a minor performance penalty for doing a one time inflate.
 *
 * @author jdrotos
 */
public class TravelerFlowStateTablet implements ILOBable {
	private Context mContext;
	private SectionTravelerInfoTablet mSectionTraveler;
	private LineOfBusiness mLob;

	public TravelerFlowStateTablet(Context context, LineOfBusiness lob) {
		mContext = context;
		setLob(lob);
	}

	@Override
	public void setLob(LineOfBusiness lob) {
		mLob = lob;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if (lob == LineOfBusiness.FLIGHTS) {
			mSectionTraveler = (SectionTravelerInfoTablet) inflater
				.inflate(R.layout.section_flight_edit_traveler, null);
		}
		else if (lob == LineOfBusiness.HOTELS) {
			mSectionTraveler = (SectionTravelerInfoTablet) inflater.inflate(R.layout.section_hotel_edit_traveler, null);
		}
	}

	@Override
	public LineOfBusiness getLob() {
		return mLob;
	}

	public boolean isValid(Traveler traveler, boolean emailRequired, boolean passportRequired) {
		mSectionTraveler.setEmailFieldEnabled(emailRequired);
		mSectionTraveler.setPassportCountryFieldEnabled(passportRequired);
		mSectionTraveler.bind(traveler);
		return mSectionTraveler.hasValidInput();
	}
}
