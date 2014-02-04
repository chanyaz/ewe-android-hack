package com.expedia.bookings.section;

import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.utils.Ui;

/*
We introduce this class as a way to introduce tablet oriented name containers
 */
public class SectionTravelerInfoTablet extends com.expedia.bookings.section.SectionTravelerInfo {

	public SectionTravelerInfoTablet(android.content.Context context) {
		super(context);
	}

	public SectionTravelerInfoTablet(android.content.Context context, android.util.AttributeSet attrs) {
		super(context, attrs);
	}

	public SectionTravelerInfoTablet(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
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

}
