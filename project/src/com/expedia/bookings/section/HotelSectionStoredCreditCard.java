package com.expedia.bookings.section;

import com.expedia.bookings.R;
import com.expedia.bookings.data.StoredCreditCard;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class HotelSectionStoredCreditCard extends SectionStoredCreditCard {
	public HotelSectionStoredCreditCard(Context context) {
		this(context, null, 0);
	}

	public HotelSectionStoredCreditCard(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelSectionStoredCreditCard(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mFields.remove(mDisplayCreditCardActiveIcon);
		mFields.add(mHotelDisplayCreditCardActiveIcon);
	}

	SectionField<ImageView, StoredCreditCard> mHotelDisplayCreditCardActiveIcon = new SectionField<ImageView, StoredCreditCard>(
			R.id.display_credit_card_active_icon) {
		@Override
		public void onHasFieldAndData(ImageView field, StoredCreditCard data) {
			field.setImageResource(R.drawable.ic_credit_card_white);
		}
	};
}