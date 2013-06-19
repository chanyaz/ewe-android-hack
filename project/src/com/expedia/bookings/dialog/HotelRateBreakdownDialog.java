package com.expedia.bookings.dialog;

import java.text.DateFormat;
import java.util.Date;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.pos.PointOfSale;
import com.gridlayout.GridLayout;
import com.gridlayout.GridLayout.LayoutParams;
import com.mobiata.android.util.Ui;

public class HotelRateBreakdownDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO - proper style
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.SocialMessageChooserDialogTheme);
	}

	private LayoutInflater mInflater;
	private ViewGroup mRoot;
	private GridLayout mGrid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mInflater = inflater;
		mRoot = (ViewGroup) mInflater.inflate(R.layout.dialog_hotel_rate_breakdown, container, false);

		mGrid = Ui.findView(mRoot, R.id.grid);

		TextView doneButton = Ui.findView(mRoot, R.id.done_button);
		doneButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		final Rate originalRate = Db.getHotelSearch().getSelectedRate();
		final Rate couponRate = Db.getHotelSearch().getCouponRate();
		final HotelSearchParams params = Db.getHotelSearch().getSearchParams();

		Builder leftBuilder = new Builder();
		leftBuilder.setLeft();

		Builder rightBuilder = new Builder();
		rightBuilder.setRight();

		// Number of Nights
		leftBuilder.setHeavy();
		leftBuilder.setQuantityText(R.plurals.number_of_nights, params.getStayDuration());
		leftBuilder.build();

		// Price before taxes and fees
		rightBuilder.setHeavy();
		rightBuilder.setText(originalRate.getNightlyRateTotal().getFormattedMoney());
		rightBuilder.build();

		// Room night breakdown
		leftBuilder.setLight();
		leftBuilder.setMarginLeft(17);
		rightBuilder.setLight();
		DateFormat breakdownFormat = android.text.format.DateFormat.getDateFormat(getActivity());
		if (originalRate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : originalRate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				leftBuilder.setText(breakdownFormat.format(date));
				leftBuilder.build();

				Money amount = breakdown.getAmount();
				if (amount.isZero()) {
					rightBuilder.setText(R.string.free);
				}
				else {
					rightBuilder.setText(amount.getFormattedMoney());
				}
				rightBuilder.build();
			}
		}
		// Reset margin now that we are done with the Date TextViews
		leftBuilder.setMarginLeft(0);

		leftBuilder.setMedium();
		rightBuilder.setMedium();

		// Discount from the potential coupon applied
		if (couponRate != null) {
			leftBuilder.setText(R.string.discount);
			leftBuilder.build();

			rightBuilder.setText(couponRate.getTotalPriceAdjustments().getFormattedMoney());
			rightBuilder.setTextColor(getResources().getColor(R.color.hotel_price_breakdown_discount_green));
			rightBuilder.build();
		}

		// Taxes and fees
		if (originalRate.getTotalSurcharge() != null) {
			leftBuilder.setText(R.string.taxes_and_fees);
			leftBuilder.build();

			if (originalRate.getTotalSurcharge().isZero()) {
				rightBuilder.setText(R.string.included);
			}
			else {
				rightBuilder.setText(originalRate.getTotalSurcharge().getFormattedMoney());
			}
			rightBuilder.build();
		}

		// Extra guest fees
		if (originalRate.getExtraGuestFee() != null && !originalRate.getExtraGuestFee().isZero()) {
			leftBuilder.setText(R.string.extra_guest_charge);
			leftBuilder.build();

			rightBuilder.setText(originalRate.getExtraGuestFee().getFormattedMoney());
			rightBuilder.build();
		}

		addDivider();

		if (PointOfSale.getPointOfSale().displayMandatoryFees()) {
			leftBuilder.setText(R.string.total_due_today);
			leftBuilder.build();

			rightBuilder.setText(originalRate.getTotalAmountAfterTax().getFormattedMoney());
			rightBuilder.build();

			leftBuilder.setText(R.string.MandatoryFees);
			leftBuilder.build();

			rightBuilder.setText(originalRate.getTotalMandatoryFees().getFormattedMoney());
			rightBuilder.build();
		}

		// Total price
		leftBuilder.setHeavy();
		leftBuilder.setText(R.string.total_price_label);
		leftBuilder.build();

		Money total = couponRate == null ? originalRate.getDisplayTotalPrice() : couponRate.getDisplayTotalPrice();
		rightBuilder.setText(total.getFormattedMoney());
		rightBuilder.build();

		// Reallocate the cells since we added children, forces a requestLayout
		mGrid.setColumnCount(2);

		mInflater = null;
		return mRoot;
	}

	private void addDivider() {
		View divider = mInflater.inflate(R.layout.snippet_breakdown_divider, mGrid, false);
		mGrid.addView(divider);
	}

	private class Builder {
		private int mLayout = R.layout.snippet_breakdown_light;
		private CharSequence mText;
		private int mColor = -1;
		private boolean mIsLeft = true;
		private int mMarginLeft = 0;

		public void setHeavy() {
			mLayout = R.layout.snippet_breakdown_heavy;
		}

		public void setMedium() {
			mLayout = R.layout.snippet_breakdown_medium;
		}

		public void setLight() {
			mLayout = R.layout.snippet_breakdown_light;
		}

		public void setText(int stringId) {
			mText = getString(stringId);
		}

		public void setText(CharSequence text) {
			mText = text;
		}

		public void setTextColor(int color) {
			mColor = color;
		}

		public void setQuantityText(int stringId, int quantity) {
			final Resources res = getResources();
			mText = res.getQuantityString(stringId, quantity, quantity);
		}

		public void setLeft() {
			mIsLeft = true;
		}

		public void setRight() {
			mIsLeft = false;
		}

		public void setMarginLeft(int pixels) {
			mMarginLeft = (int) (getResources().getDisplayMetrics().density * pixels);
		}

		public void build() {
			TextView tv = (TextView) mInflater.inflate(mLayout, mGrid, false);
			tv.setText(mText);

			if (mColor != -1) {
				tv.setTextColor(mColor);
				// After applying a color (which is an exception), just reset the Builder color cache so as not to reuse
				mColor = -1;
			}

			LayoutParams lp = (LayoutParams) tv.getLayoutParams();
			lp.setGravity(mIsLeft ? Gravity.LEFT : Gravity.RIGHT);

			if (mMarginLeft != 0) {
				lp.leftMargin = mMarginLeft;
			}

			tv.setLayoutParams(lp);

			mGrid.addView(tv);
		}

		public void reset() {
			mLayout = R.layout.snippet_breakdown_light;
			mText = null;
			mColor = -1;
			mIsLeft = true;
			mMarginLeft = 0;
		}
	}
}
