package com.expedia.bookings.section;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;

public class HotelOverviewSectionStoredCreditCard extends SectionStoredCreditCard {
	public HotelOverviewSectionStoredCreditCard(Context context) {
		this(context, null);
	}

	public HotelOverviewSectionStoredCreditCard(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public HotelOverviewSectionStoredCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();

	}

	private void init() {
		mFields.remove(mDisplayCreditCardActiveIcon);
		mFields.add(mHotelDisplayCreditCardActiveIcon);
	}

	SectionField<ImageView, StoredCreditCard> mHotelDisplayCreditCardActiveIcon = new SectionField<ImageView, StoredCreditCard>(
			R.id.display_credit_card_active_icon) {
		@Override
		public void onHasFieldAndData(ImageView field, StoredCreditCard data) {
			field.setImageResource(R.drawable.ic_credit_card_gray);
		}
	};
}
