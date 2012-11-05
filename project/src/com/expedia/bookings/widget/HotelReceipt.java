package com.expedia.bookings.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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

public class HotelReceipt extends FrameLayout {
	public interface OnSizeChangedListener {
		public void onReceiptSizeChanged(int w, int h, int oldw, int oldh);
	}

	private OnSizeChangedListener mOnSizeChangedListener;

	private LayoutInflater mInflater;

	// Cached views
	private ImageView mThumbnailImageView;
	private TextView mNameTextView;
	private TextView mAddress1TextView;
	private TextView mAddress2TextView;
	private ViewGroup mDetailsLayout;
	private ViewGroup mExtrasLayout;
	private View mExtraSectionDivider;
	private HotelReceiptMini mHotelReceiptMini;

	// The room type widget
	// TODO: Should this be integrated with ReceiptWidget?
	private RoomTypeWidget mRoomTypeWidget;

	// Constructors

	public HotelReceipt(Context context) {
		this(context, null, 0);
	}

	public HotelReceipt(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HotelReceipt(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mInflater.inflate(R.layout.widget_hotel_receipt, this);

		mThumbnailImageView = (ImageView) findViewById(R.id.thumbnail_image_view);
		mNameTextView = (TextView) findViewById(R.id.name_text_view);
		mAddress1TextView = (TextView) findViewById(R.id.address1_text_view);
		mAddress2TextView = (TextView) findViewById(R.id.address2_text_view);
		mDetailsLayout = (ViewGroup) findViewById(R.id.details_layout);
		mExtrasLayout = (ViewGroup) findViewById(R.id.extras_layout);
		mExtraSectionDivider = (View) findViewById(R.id.extras_div);
		mHotelReceiptMini = (HotelReceiptMini) findViewById(R.id.receipt_mini);

		boolean isRoomTypeExpandable = false;
		if (getParent() instanceof DialogFragment) {
			isRoomTypeExpandable = !((DialogFragment) getParent()).getShowsDialog();
		}

		mRoomTypeWidget = new RoomTypeWidget(getContext(), isRoomTypeExpandable);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mOnSizeChangedListener != null) {
			mOnSizeChangedListener.onReceiptSizeChanged(w, h, oldw, oldh);
		}
	}

	// public methods

	public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
		mOnSizeChangedListener = onSizeChangedListener;
	}

	public void setMiniReceiptOnSizeChangedListener(HotelReceiptMini.OnSizeChangedListener onSizeChangedListener) {
		mHotelReceiptMini.setOnSizeChangedListener(onSizeChangedListener);
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

		addTextRow(mDetailsLayout, R.string.GuestsLabel, StrUtils.formatGuests(getContext(), searchParams));

		addTextRow(mDetailsLayout, R.string.CheckIn, formatCheckInOutDate(searchParams.getCheckInDate()));
		addTextRow(mDetailsLayout, R.string.CheckOut, formatCheckInOutDate(searchParams.getCheckOutDate()));

		int numDays = searchParams.getStayDuration();
		addTextRow(mDetailsLayout, R.string.stay_duration,
				getContext().getResources().getQuantityString(R.plurals.length_of_stay, numDays, numDays));

		addSpace(mDetailsLayout, 8);

		// Rate breakdown list.  Only works with merchant hotels now.
		DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String label = getContext().getString(R.string.room_rate_template, dateFormat.format(date));
				Money amount = breakdown.getAmount();
				if (amount.isZero()) {
					addRateRow(mDetailsLayout, label, getContext().getString(R.string.free));
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
				&& LocaleUtils.shouldDisplayMandatoryFees(getContext())) {
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

				if (LocaleUtils.shouldDisplayMandatoryFees(getContext())) {
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

		if (LocaleUtils.getBestPriceGuaranteeUrl(getContext()) != null) {
			addExtra(mExtrasLayout, R.string.best_price_guarantee);
		}

		if (rate.shouldShowFreeCancellation()) {
			Date window = rate.getFreeCancellationWindowDate();
			if (window != null) {
				DateFormat df = new SimpleDateFormat("ha, MMM dd");
				String formattedDate = df.format(window);
				String formattedString = getContext()
						.getString(R.string.free_cancellation_date_TEMPLATE, formattedDate);
				addExtra(mExtrasLayout, Html.fromHtml(formattedString));
			}
			else {
				addExtra(mExtrasLayout, R.string.free_cancellation);
			}
		}

		mHotelReceiptMini.updateData(property, searchParams, rate);
	}

	public void showTotalCostLayout() {
		mHotelReceiptMini.showTotalCostLayout();
	}

	public void showMiniDetailsLayout() {
		mHotelReceiptMini.showMiniDetailsLayout();
	}

	// private methods

	private void reset() {
		// Clear current data
		// TODO: If this ever becomes a performance issue, we could start caching
		// views and re-using them.
		mDetailsLayout.removeAllViews();
		mExtrasLayout.removeAllViews();
		mExtrasLayout.setVisibility(View.GONE);
		mExtraSectionDivider.setVisibility(View.GONE);
		mHotelReceiptMini.reset();
	}

	/**
	 * This adds a row, using snippet_booking_detail_text, where the LEFT column has a
	 * width of wrap_content and the RIGHT column is wrapped if too long.
	 */
	private View addTextRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, getContext().getString(labelStrId), value, R.layout.snippet_booking_detail_text);
	}

	/**
	 * This adds a row, using snippet_booking_detail_rate, where the RIGHT column has a
	 * width of wrap_content and the LEFT column is wrapped if too long.
	 */
	private View addRateRow(ViewGroup parent, int labelStrId, CharSequence value) {
		return addRow(parent, getContext().getString(labelStrId), value, R.layout.snippet_booking_detail_rate);
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

		View detailRow = mInflater.inflate(layoutResId, parent, false);
		TextView labelView = (TextView) detailRow.findViewById(R.id.label_text_view);
		labelView.setText(label);
		TextView valueView = (TextView) detailRow.findViewById(R.id.value_text_view);
		valueView.setText(value);
		parent.addView(detailRow);
		return detailRow;
	}

	private View addExtra(ViewGroup parent, int stringResId) {
		return addExtra(parent, getContext().getString(stringResId));
	}

	private View addExtra(ViewGroup parent, CharSequence label) {
		mExtrasLayout.setVisibility(View.VISIBLE);
		mExtraSectionDivider.setVisibility(View.VISIBLE);

		View extraRow = mInflater.inflate(R.layout.snippet_hotel_receipt_extra, parent, false);
		TextView labelView = (TextView) extraRow.findViewById(R.id.extra_label);
		labelView.setText(label);
		parent.addView(extraRow);
		return extraRow;
	}

	private String formatCheckInOutDate(Calendar cal) {
		DateFormat medDf = android.text.format.DateFormat.getMediumDateFormat(getContext());
		medDf.setTimeZone(CalendarUtils.getFormatTimeZone());
		return DateUtils.getDayOfWeekString(cal.get(Calendar.DAY_OF_WEEK), DateUtils.LENGTH_MEDIUM) + ", "
				+ medDf.format(cal.getTime());
	}

	private void addSpace(ViewGroup parent, int spaceInDp) {
		int height = (int) getContext().getResources().getDisplayMetrics().density * spaceInDp;

		View v = new View(getContext());
		v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

		parent.addView(v);
	}
}
