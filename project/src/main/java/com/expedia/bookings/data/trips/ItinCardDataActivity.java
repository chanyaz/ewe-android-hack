package com.expedia.bookings.data.trips;

import java.util.List;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Activity;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.JodaUtils;

public class ItinCardDataActivity extends ItinCardData {
	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	// SimpleDateFormat equiv: MMM d; eg Mar 15
	private static final int DETAIL_SHORT_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			| DateUtils.FORMAT_ABBREV_MONTH;
	// SimpleDateFormat equiv: MMMM d; eg March 15
	private static final int DETAIL_LONG_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR;
	// SimpleDateFormat equiv: EEEE, MMMM d, yyyy; eg Friday, March 15, 2012
	private static final int SHARE_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private final Activity mActivity;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataActivity(TripComponent tripComponent) {
		super(tripComponent);
		mActivity = ((TripActivity) tripComponent).getActivity();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public String getTitle() {
		return mActivity.getTitle();
	}

	public DateTime getValidDate() {
		return getTripComponent().getParentTrip().getStartDate();
	}

	public DateTime getExpirationDate() {
		return getTripComponent().getParentTrip().getEndDate();
	}

	public String getFormattedShareValidDate(Context context) {
		return JodaUtils.formatDateTime(context, getValidDate(), SHARE_DATE_FLAGS);
	}

	public String getFormattedShareExpiresDate(Context context) {
		return JodaUtils.formatDateTime(context, getExpirationDate(), SHARE_DATE_FLAGS);
	}

	public String getLongFormattedValidDate(Context context) {
		return JodaUtils.formatDateTime(context, getValidDate(), DETAIL_LONG_DATE_FLAGS);
	}

	public String getFormattedValidDate(Context context) {
		return JodaUtils.formatDateTime(context, getValidDate(), DETAIL_SHORT_DATE_FLAGS);
	}

	public String getFormattedExpirationDate(Context context) {
		return JodaUtils.formatDateTime(context, getExpirationDate(), DETAIL_SHORT_DATE_FLAGS);
	}

	public int getGuestCount() {
		return mActivity.getGuestCount();
	}

	public String getImageUrl() {
		return mActivity.getImageUrl();
	}

	public String getFormattedGuestCount() {
		return String.valueOf(getGuestCount());
	}

	public List<Traveler> getTravelers() {
		return mActivity.getTravelers();
	}

	public String getVoucherPrintUrl() {
		return mActivity.getVoucherPrintUrl();
	}

	public String getBestSupportPhoneNumber(Context context) {
		TripActivity tripActivity = (TripActivity) getTripComponent();
		if (tripActivity == null) {
			return null;
		}

		Trip trip = tripActivity.getParentTrip();
		if (trip == null) {
			return null;
		}

		CustomerSupport support = trip.getCustomerSupport();
		if (support == null) {
			return null;
		}

		if (PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.UNITED_STATES
				&& !TextUtils.isEmpty(support.getSupportPhoneNumberDomestic())) {
			return support.getSupportPhoneNumberDomestic();
		}

		return support.getSupportPhoneNumberInternational();
	}

	public Intent buildRedeemIntent(Context context) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
		builder.setUrl(getVoucherPrintUrl());
		builder.setTitle(R.string.webview_title_print_vouchers);
		builder.setAllowMobileRedirects(false);
		builder.setInjectExpediaCookies(true);
		return builder.getIntent();
	}

}
