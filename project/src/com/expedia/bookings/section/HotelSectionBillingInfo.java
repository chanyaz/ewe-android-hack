package com.expedia.bookings.section;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.utils.BookingInfoUtils;

public class HotelSectionBillingInfo extends SectionBillingInfo {
	public HotelSectionBillingInfo(Context context) {
		this(context, null, 0);
	}

	public HotelSectionBillingInfo(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelSectionBillingInfo(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mFields.remove(mDisplayCreditCardBrandIconGrey);
		mFields.remove(mDisplayCreditCardBrandIconBlack);

		mFields.add(mHotelDisplayCreditCardBrandIconGray);
		mFields.add(mHotelDisplayCreditCardBrandIconBlack);
	}

	@Override
	protected void rebindNumDependantFields() {
		super.rebindNumDependantFields();

		mHotelDisplayCreditCardBrandIconGray.bindData(mBillingInfo);
		mHotelDisplayCreditCardBrandIconBlack.bindData(mBillingInfo);
	}

	SectionField<ImageView, BillingInfo> mHotelDisplayCreditCardBrandIconGray = new SectionField<ImageView, BillingInfo>(
			R.id.display_credit_card_brand_icon) {
		@Override
		public void onHasFieldAndData(ImageView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(data.getBrandName());
				if (cardType != null && !TextUtils.isEmpty(getData().getNumber())) {
					field.setImageResource(BookingInfoUtils.CREDIT_CARD_WHITE_ICONS.get(cardType));
				}
				else {
					field.setImageResource(R.drawable.ic_credit_card_white);
				}
			}
			else {
				field.setImageResource(R.drawable.ic_credit_card_white);
			}
		}
	};

	SectionField<ImageView, BillingInfo> mHotelDisplayCreditCardBrandIconBlack = new SectionField<ImageView, BillingInfo>(
			R.id.display_credit_card_brand_icon_black) {
		@Override
		public void onHasFieldAndData(ImageView field, BillingInfo data) {
			if (!TextUtils.isEmpty(data.getBrandName())) {
				CreditCardType cardType = CreditCardType.valueOf(data.getBrandName());
				if (cardType != null && !TextUtils.isEmpty(getData().getNumber())) {
					field.setImageResource(BookingInfoUtils.CREDIT_CARD_WHITE_ICONS.get(cardType));
				}
				else {
					field.setImageDrawable(null);
				}
			}
			else {
				field.setImageDrawable(null);
			}
		}
	};
}