package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;

// Support class for receipt layouts
// Works well with include_receipt.xml, but can also be used if you provide
// all the right Views (as in the case of activity_confirmation.xml)
public class ReceiptWidget {

	private Context mContext;
	private LayoutInflater mInflator;

	// Cached views
	private ImageView mThumbnailImageView;
	private TextView mNameTextView;
	private TextView mAddress1TextView;
	private TextView mAddress2TextView;
	private ViewGroup mDetailsLayout;
	private TextView mTotalCostTextView;
	private ViewGroup mBelowTotalCostLayout;

	// The room type widget
	// TODO: Should this be integrated with ReceiptWidget?
	private RoomTypeWidget mRoomTypeWidget;

	public ReceiptWidget(Context context, View rootView, boolean isRoomTypeExpandable) {
		mContext = context;
		mInflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mThumbnailImageView = (ImageView) rootView.findViewById(R.id.thumbnail_image_view);
		mNameTextView = (TextView) rootView.findViewById(R.id.name_text_view);
		mAddress1TextView = (TextView) rootView.findViewById(R.id.address1_text_view);
		mAddress2TextView = (TextView) rootView.findViewById(R.id.address2_text_view);
		mDetailsLayout = (ViewGroup) rootView.findViewById(R.id.details_layout);
		mTotalCostTextView = (TextView) rootView.findViewById(R.id.total_cost_text_view);
		mBelowTotalCostLayout = (ViewGroup) rootView.findViewById(R.id.below_total_details_layout);

		mRoomTypeWidget = new RoomTypeWidget(mContext, isRoomTypeExpandable);
	}

	public void saveInstanceState(Bundle outState) {
		mRoomTypeWidget.saveInstanceState(outState);
	}

	public void restoreInstanceState(Bundle savedState) {
		mRoomTypeWidget.restoreInstanceState(savedState);
	}

	public void updateData(Property property, SearchParams searchParams, Rate rate) {
		updateData(property, searchParams, rate, null, null, null);
	}

	public void updateData(Property property, SearchParams searchParams, Rate rate, Rate discountRate) {
		updateData(property, searchParams, rate, null, null, discountRate);
	}

	public void updateData(Property property, SearchParams searchParams, Rate rate, BookingResponse bookingResponse,
			BillingInfo billingInfo, Rate discountRate) {
		reset();

		mRoomTypeWidget.updateRate(rate);

		// Configuring the header at the top
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), mThumbnailImageView);
			mThumbnailImageView.setVisibility(View.VISIBLE);
		}
		else {
			mThumbnailImageView.setVisibility(View.GONE);
		}

		mNameTextView.setText(property.getName());

		Location location = property.getLocation();
		mAddress1TextView.setText(Html.fromHtml(StrUtils.formatAddressStreet(location)));
		mAddress2TextView.setText(Html.fromHtml(StrUtils.formatAddressCity(location)));

		// Configure the details
		if (billingInfo != null && bookingResponse != null) {
			if (!TextUtils.isEmpty(bookingResponse.getHotelConfNumber())) {
				addRateRow(mDetailsLayout, R.string.confirmation_number, bookingResponse.getHotelConfNumber());
			}
			addRateRow(mDetailsLayout, R.string.itinerary_number, bookingResponse.getItineraryId());
			addRateRow(mDetailsLayout, R.string.confirmation_email, billingInfo.getEmail());
		}

		mDetailsLayout.addView(mRoomTypeWidget.getView());

		if (property.isMerchant()) {
			View bedTypeRow = addTextRow(mDetailsLayout, R.string.bed_type, rate.getRatePlanName());
			mRoomTypeWidget.addClickableView(bedTypeRow);
		}

		addTextRow(mDetailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(mContext, searchParams));

		addTextRow(mDetailsLayout, R.string.CheckIn, formatCheckInOutDate(searchParams.getCheckInDate()));
		addTextRow(mDetailsLayout, R.string.CheckOut, formatCheckInOutDate(searchParams.getCheckOutDate()));

		int numDays = searchParams.getStayDuration();
		addTextRow(mDetailsLayout, R.string.stay_duration,
				mContext.getResources().getQuantityString(R.plurals.length_of_stay, numDays, numDays));

		addSpace(mDetailsLayout, 8);

		// Rate breakdown list.  Only works with merchant hotels now.
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String label = mContext.getString(R.string.room_rate_template, dateFormat.format(date));
				Money amount = breakdown.getAmount();
				if (amount.isZero()) {
					addRateRow(mDetailsLayout, label, mContext.getString(R.string.free));
				}
				else {
					addRateRow(mDetailsLayout, label, amount.getFormattedMoney());
				}
			}
		}

		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			addRateRow(mDetailsLayout, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
		}

		Money totalSurcharge = rate.getTotalSurcharge();
		if (totalSurcharge != null) {
			addRateRow(mDetailsLayout, R.string.TaxesAndFees, totalSurcharge.getFormattedMoney());
		}

		Money totalMandatoryFees = rate.getTotalMandatoryFees();
		if (totalMandatoryFees != null && !totalMandatoryFees.isZero()
				&& LocaleUtils.shouldDisplayMandatoryFees(mContext)) {
			addRateRow(mDetailsLayout, R.string.MandatoryFees, totalMandatoryFees.getFormattedMoney());
		}

		// Configure the total cost and (if necessary) total cost paid to Expedia
		if (discountRate != null) {
			Money amountDiscounted;
			if (discountRate.getTotalPriceAdjustments() != null) {
				amountDiscounted = discountRate.getTotalPriceAdjustments();
			}
			else {
				Money after;

				if (LocaleUtils.shouldDisplayMandatoryFees(mContext)) {
					amountDiscounted = new Money(rate.getTotalPriceWithMandatoryFees());
					after = discountRate.getTotalPriceWithMandatoryFees();
				}
				else {
					amountDiscounted = new Money(rate.getTotalAmountAfterTax());
					after = discountRate.getTotalAmountAfterTax();
				}
				amountDiscounted.subtract(after);
				amountDiscounted.negate();
			}

			rate = discountRate;
			addTextRow(mDetailsLayout, R.string.discount, amountDiscounted.getFormattedMoney());
		}

		Money displayedTotal;
		if (LocaleUtils.shouldDisplayMandatoryFees(mContext)) {
			mBelowTotalCostLayout.setVisibility(View.VISIBLE);
			addTextRow(mBelowTotalCostLayout, R.string.PayToExpedia, rate.getTotalAmountAfterTax().getFormattedMoney());
			displayedTotal = rate.getTotalPriceWithMandatoryFees();
		}
		else {
			mBelowTotalCostLayout.setVisibility(View.GONE);
			displayedTotal = rate.getTotalAmountAfterTax();
		}

		mTotalCostTextView.setText(displayedTotal.getFormattedMoney());

	}

	private void reset() {
		// Clear current data
		// TODO: If this ever becomes a performance issue, we could start caching
		// views and re-using them.
		mDetailsLayout.removeAllViews();
		mBelowTotalCostLayout.removeAllViews();
	}

	/**
	 * This adds a row, using snippet_booking_detail_text, where the LEFT column has a
	 * width of wrap_content and the RIGHT column is wrapped if too long.
	 */
	private View addTextRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, mContext.getString(labelStrId), value, R.layout.snippet_booking_detail_text);
	}

	/**
	 * This adds a row, using snippet_booking_detail_rate, where the RIGHT column has a
	 * width of wrap_content and the LEFT column is wrapped if too long.
	 */
	private View addRateRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, mContext.getString(labelStrId), value, R.layout.snippet_booking_detail_rate);
	}

	/**
	 * This adds a row, using snippet_booking_detail_rate, where the RIGHT column has a
	 * width of wrap_content and the LEFT column is wrapped if too long.
	 */
	private View addRateRow(ViewGroup parent, CharSequence label, CharSequence value) {
		return addRow(parent, label, value, R.layout.snippet_booking_detail_rate);
	}

	private View addRow(ViewGroup parent, CharSequence label, CharSequence value, int layoutResId) {
		if (value == null || value.length() == 0) {
			return null;
		}

		View detailRow = mInflator.inflate(layoutResId, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
		return detailRow;
	}

	private String formatCheckInOutDate(Calendar cal) {
		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(mContext);
		medDf.setTimeZone(CalendarUtils.getFormatTimeZone());
		return DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_MEDIUM) + ", "
				+ medDf.format(cal.getTime());
	}

	public void addSpace(ViewGroup parent, int spaceInDp) {
		int height = (int) mContext.getResources().getDisplayMetrics().density * spaceInDp;
		View v = new View(mContext);
		v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, height));
		parent.addView(v);
	}

}
