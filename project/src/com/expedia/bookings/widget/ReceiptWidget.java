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
		updateData(property, searchParams, rate, null, null);
	}

	public void updateData(Property property, SearchParams searchParams, Rate rate, BookingResponse bookingResponse,
			BillingInfo billingInfo) {
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
				addRow(mDetailsLayout, R.string.confirmation_number, bookingResponse.getHotelConfNumber());
			}
			addRow(mDetailsLayout, R.string.itinerary_number, bookingResponse.getItineraryId());
			addRow(mDetailsLayout, R.string.confirmation_email, billingInfo.getEmail());
		}

		mDetailsLayout.addView(mRoomTypeWidget.getView());

		View bedTypeRow = addRow(mDetailsLayout, R.string.bed_type, rate.getRatePlanName());
		mRoomTypeWidget.addClickableView(bedTypeRow);

		addRow(mDetailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(mContext, searchParams));

		addRow(mDetailsLayout, R.string.CheckIn, formatCheckInOutDate(searchParams.getCheckInDate()));
		addRow(mDetailsLayout, R.string.CheckOut, formatCheckInOutDate(searchParams.getCheckOutDate()));

		int numDays = searchParams.getStayDuration();
		addRow(mDetailsLayout, R.string.stay_duration,
				mContext.getResources().getQuantityString(R.plurals.length_of_stay, numDays, numDays));

		addSpace(mDetailsLayout, 8);

		// Rate breakdown list.  Only works with merchant hotels now.
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(mContext);
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String label = mContext.getString(R.string.room_rate_template, dateFormat.format(date));
				Money amount = breakdown.getAmount();
				if (amount.getAmount() == 0) {
					addRow(mDetailsLayout, label, mContext.getString(R.string.free));
				}
				else {
					addRow(mDetailsLayout, label, amount.getFormattedMoney());
				}
			}
		}

		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			addRow(mDetailsLayout, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
		}

		Money totalSurcharge = rate.getTotalSurcharge();
		if (totalSurcharge != null) {
			addRow(mDetailsLayout, R.string.TaxesAndFees, totalSurcharge.getFormattedMoney());
		}

		Money totalMandatoryFees = rate.getTotalMandatoryFees();
		if (totalMandatoryFees != null && totalMandatoryFees.getAmount() != 0 && shouldDisplayMandatoryFees()) {
			addRow(mDetailsLayout, R.string.MandatoryFees, totalMandatoryFees.getFormattedMoney());
		}

		// Configure the total cost and (if necessary) total cost paid to Expedia
		Money displayedTotal;
		if (shouldDisplayMandatoryFees()) {
			mBelowTotalCostLayout.setVisibility(View.VISIBLE);
			addRow(mBelowTotalCostLayout, R.string.PayToExpedia, rate.getTotalAmountAfterTax().getFormattedMoney());
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

	private View addRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, mContext.getString(labelStrId), value);
	}

	private View addRow(ViewGroup parent, CharSequence label, CharSequence value) {
		if (value == null || value.length() == 0) {
			return null;
		}

		View detailRow = mInflator.inflate(R.layout.snippet_booking_detail, parent, false);
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

	// Mandatory fees should only be displayed in IT and DE
	private boolean shouldDisplayMandatoryFees() {
		String pos = LocaleUtils.getPointOfSale(mContext);
		if (pos == null) {
			return false;
		}
		return pos.equals(mContext.getString(R.string.point_of_sale_it))
				|| pos.equals(mContext.getString(R.string.point_of_sale_de));
	}
}
