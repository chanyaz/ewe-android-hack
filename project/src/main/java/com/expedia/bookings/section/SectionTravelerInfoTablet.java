package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.interfaces.ILOBable;
import com.expedia.bookings.utils.Ui;

/*
We introduce this class as a way to introduce tablet oriented name containers
 */
public class SectionTravelerInfoTablet extends SectionTravelerInfo implements ILOBable {

	private LineOfBusiness mLob = null;

	public SectionTravelerInfoTablet(Context context) {
		super(context);
	}

	public SectionTravelerInfoTablet(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SectionTravelerInfoTablet(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void preFinishInflate() {
		//Load the pos specific name edit fields if we have the container for them.
		ViewGroup nameContainer = Ui.findView(this, R.id.edit_names_container);
		if (nameContainer != null) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			if (pos.showLastNameFirst()) {
				View.inflate(mContext, R.layout.include_edit_traveler_names_reversed_tablet, nameContainer);
			}
			else {
				View.inflate(mContext, R.layout.include_edit_traveler_names_tablet, nameContainer);
			}
		}
	}

	@Override
	protected void postFinishInflate() {
		super.postFinishInflate();
	}

	@Override
	public void setLob(LineOfBusiness lob) {
		if (mLob != null) {
			throw new RuntimeException("Once set, LOB cannot be changed on SectionTravelerInfoTablet");
		}
		mLob = lob;
	}

	@Override
	public LineOfBusiness getLob() {
		return mLob;
	}

	public void setEmailFieldEnabled(boolean enabled) {
		mFields.setFieldEnabled(mEditEmailAddress, enabled);
	}

	public void setPassportCountryFieldEnabled(boolean enabled) {
		mFields.setFieldEnabled(mEditPassportCountrySpinner, enabled);
	}
}
