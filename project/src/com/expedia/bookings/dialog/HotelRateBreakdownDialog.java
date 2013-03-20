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
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.gridlayout.GridLayout;
import com.gridlayout.GridLayout.LayoutParams;
import com.mobiata.android.util.Ui;

import com.mobiata.android.Log;

public class HotelRateBreakdownDialog extends DialogFragment {

	private static final String ARG_RATE = "ARG_RATE";

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

		final Rate rate = Db.getSelectedRate();
		final SearchParams params = Db.getSearchParams();

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
		rightBuilder.setText(rate.getTotalAmountBeforeTax().getFormattedMoney());
		rightBuilder.build();

		// Room night breakdown
		leftBuilder.setLight();
		leftBuilder.setMarginLeft(17);
		rightBuilder.setLight();
                DateFormat breakdownFormat = android.text.format.DateFormat.getDateFormat(getActivity());
                if (rate.getRateBreakdownList() != null) {
                        for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
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

		// Taxes and fees
		if (rate.getTotalSurcharge() != null) {
			leftBuilder.setText(R.string.taxes_and_fees);
			leftBuilder.build();

			if (rate.getTotalSurcharge().isZero()) {
				rightBuilder.setText(R.string.included);
			}
			else {
				rightBuilder.setText(rate.getTotalSurcharge().getFormattedMoney());
			}
			rightBuilder.build();
		}

		// Extra guest fees
		if (rate.getExtraGuestFee() != null && !rate.getExtraGuestFee().isZero()) {
			leftBuilder.setText(R.string.extra_guest_charge);
			leftBuilder.build();

			rightBuilder.setText(rate.getExtraGuestFee().getFormattedMoney());
			rightBuilder.build();
		}

		addDivider();

                if (PointOfSale.getPointOfSale().displayMandatoryFees()) {
			leftBuilder.setText(R.string.total_due_today);
			leftBuilder.build();

			rightBuilder.setText(rate.getTotalAmountAfterTax().getFormattedMoney());
			rightBuilder.build();

			leftBuilder.setText(R.string.MandatoryFees);
			leftBuilder.build();

			rightBuilder.setText(rate.getTotalMandatoryFees().getFormattedMoney());
			rightBuilder.build();
		}

		// Total price
		leftBuilder.setHeavy();
		leftBuilder.setText(R.string.total_price_label);
		leftBuilder.build();

		Money displayedTotal;
                if (PointOfSale.getPointOfSale().displayMandatoryFees()) {
                        displayedTotal = rate.getTotalPriceWithMandatoryFees();
                }
                else {
                        displayedTotal = rate.getTotalAmountAfterTax();
                }
		rightBuilder.setText(displayedTotal.getFormattedMoney());
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
			mIsLeft = true;
			mMarginLeft = 0;
		}
	}
}
