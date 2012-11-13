package com.expedia.bookings.activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.TextUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.ConfirmationState;
import com.expedia.bookings.data.ConfirmationState.Type;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Policy;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.RateBreakdown;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.fragment.BookingConfirmationFragment.BookingConfirmationFragmentListener;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;

public class ConfirmationFragmentActivity extends SherlockFragmentMapActivity implements
		BookingConfirmationFragmentListener {

	private Context mContext;

	private ConfirmationState mConfState;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		mConfState = new ConfirmationState(this, Type.HOTEL);

		if (AndroidUtils.isTablet(this)) {
			setTheme(R.style.Theme_Tablet_Confirmation);
			NavUtils.sendKillActivityBroadcast(mContext);
		}
		else {
			setTheme(R.style.Theme_Phone);
		}

		if (savedInstanceState == null) {
			if (mConfState.hasSavedData()) {
				// Load saved data from disk
				if (!mConfState.load()) {
					// If we failed to load the saved confirmation data, we should
					// delete the file and go back (since we are only here if we were called
					// directly from a startup).
					mConfState.delete();
					finish();
					return;
				}
			}
			else {
				// Start a background thread to save this data to the disk
				new Thread(new Runnable() {
					public void run() {
						Rate discountRate = null;
						if (Db.getCreateTripResponse() != null) {
							discountRate = Db.getCreateTripResponse().getNewRate();
						}
						BillingInfo billingInfo = new BillingInfo(Db.getBillingInfo());

						Traveler primaryTraveler = Db.getUser() == null ? null : Db.getUser().getPrimaryTraveler();
						mConfState.save(Db.getSearchParams(),
								Db.getSelectedProperty(), Db.getSelectedRate(), billingInfo,
								Db.getBookingResponse(), discountRate, primaryTraveler);
					}
				}).start();
			}
		}

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		setContentView(R.layout.activity_confirmation_fragment);

		// We don't want to display the "succeeded with errors" dialog box if:
		// 1. It's not the first launch of the activity (savedInstanceState != null)
		// 2. We're re-launching the activity with saved confirmation data
		if (Db.getBookingResponse().succeededWithErrors() && savedInstanceState == null
				&& !mConfState.hasSavedData()) {
			showSucceededWithErrorsDialog();
		}

		// Track page load
		if (savedInstanceState == null) {
			Tracker.trackAppHotelsCheckoutConfirmation(this, Db.getSearchParams(), Db.getSelectedProperty(),
					Db.getBillingInfo(), Db.getSelectedRate(), Db.getBookingResponse());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			Db.setBillingInfo(null);
			Db.setBookingResponse(null);
			Db.setCreateTripResponse(null);
			Db.setCouponDiscountRate(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (AndroidUtils.isTablet(this)) {
			getSupportMenuInflater().inflate(R.menu.menu_fragment_standard, menu);
		}
		else {
			getSupportMenuInflater().inflate(R.menu.menu_confirmation, menu);
		}

		// Configure the ActionBar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(true);

		// We want the phones to have an up button for the launch screen
		actionBar.setDisplayHomeAsUpEnabled(!AndroidUtils.isTablet(this));
		actionBar.setHomeButtonEnabled(!AndroidUtils.isTablet(this));
		actionBar.setDisplayUseLogoEnabled(!AndroidUtils.isTablet(this));

		actionBar.setTitle(getString(R.string.booking_complete));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.goToLaunchScreen(this);
			return true;

		case R.id.menu_share:
			onShareBooking();
			return true;

		case R.id.menu_show_on_map:
			onShowOnMap();
			return true;

		case R.id.menu_new_search:
			onNewSearch();
			return true;

		case R.id.menu_about:
			Intent intent = new Intent(this, TabletAboutActivity.class);
			startActivity(intent);
			return true;

		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Actions

	public void showSucceededWithErrorsDialog() {
		FragmentManager fm = getSupportFragmentManager();
		String dialogTag = getString(R.string.tag_simple_dialog);
		if (fm.findFragmentByTag(dialogTag) == null) {
			String title = getString(R.string.error_booking_title);
			String message = getString(R.string.error_booking_succeeded_with_errors, Db.getBookingResponse()
					.gatherErrorMessage(this));

			SimpleSupportDialogFragment.newInstance(title, message).show(getSupportFragmentManager(), dialogTag);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingConfirmationFragmentListener

	@Override
	public void onNewSearch() {
		Tracker.trackNewSearch(this);

		// Ensure we can't come back here again
		mConfState.delete();
		Db.clear();

		Intent intent = ExpediaBookingApp.useTabletInterface(this)
				? SearchFragmentActivity.createIntent(this, true)
				: PhoneSearchActivity.createIntent(this, true);

		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		startActivity(intent);
		finish();
	}

	@Override
	public void onShareBooking() {
		share(Db.getSearchParams(), Db.getSelectedProperty(), Db.getBookingResponse(),
				Db.getBillingInfo(), Db.getSelectedRate(), Db.getCouponDiscountRate());
	}

	@Override
	public void onShowOnMap() {
		Tracker.trackViewOnMap(this);

		Intent newIntent = new Intent(Intent.ACTION_VIEW);
		String queryAddress = StrUtils.formatAddress(Db.getSelectedProperty().getLocation()).replace("\n", " ");
		newIntent.setData(Uri.parse("geo:0,0?q=" + queryAddress));
		startActivity(newIntent);
	}

	//////////////////////////////////////////////////////////////////////////
	// Sharing

	private void share(SearchParams searchParams, Property property, BookingResponse bookingResponse,
			BillingInfo billingInfo, Rate rate, Rate discountRate) {
		Context context = this;
		Resources res = getResources();

		DateFormat dateFormatter = new SimpleDateFormat("MM/dd");
		dateFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());
		DateFormat fullDateFormatter = android.text.format.DateFormat.getMediumDateFormat(context);
		fullDateFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());
		DateFormat dayFormatter = new SimpleDateFormat("EEE");
		dayFormatter.setTimeZone(CalendarUtils.getFormatTimeZone());

		Date checkIn = searchParams.getCheckInDate().getTime();
		Date checkOut = searchParams.getCheckOutDate().getTime();

		// Create the subject
		String dateStart = dateFormatter.format(checkIn);
		String dateEnd = dateFormatter.format(checkOut);
		String subject = context.getString(R.string.share_subject_template, property.getName(), dateStart, dateEnd);

		// Create the body 
		StringBuilder body = new StringBuilder();
		body.append(context.getString(R.string.share_body_start));
		body.append("\n\n");

		body.append(property.getName());
		body.append("\n");
		body.append(StrUtils.formatAddress(property.getLocation()));
		body.append("\n\n");

		if (!TextUtils.isEmpty(bookingResponse.getHotelConfNumber())) {
			appendLabelValue(context, body, R.string.confirmation_number, bookingResponse.getHotelConfNumber());
			body.append("\n");
		}
		appendLabelValue(context, body, R.string.itinerary_number, bookingResponse.getItineraryId());
		body.append("\n\n");

		if (mConfState.getPrimaryTraveler() != null) {
			appendLabelValue(context, body, R.string.name,
					context.getString(R.string.name_template, mConfState.getPrimaryTraveler().getFirstName(), mConfState.getPrimaryTraveler().getLastName()));
		}
		else {
			appendLabelValue(context, body, R.string.name,
					context.getString(R.string.name_template, billingInfo.getFirstName(), billingInfo.getLastName()));
		}
		body.append("\n");
		appendLabelValue(context, body, R.string.CheckIn,
				dayFormatter.format(checkIn) + ", " + fullDateFormatter.format(checkIn));
		body.append("\n");
		appendLabelValue(context, body, R.string.CheckOut,
				dayFormatter.format(checkOut) + ", " + fullDateFormatter.format(checkOut));
		body.append("\n");
		int numDays = searchParams.getStayDuration();
		appendLabelValue(context, body, R.string.stay_duration,
				res.getQuantityString(R.plurals.length_of_stay, numDays, numDays));
		body.append("\n\n");

		appendLabelValue(context, body, R.string.room_type, Html.fromHtml(rate.getRoomDescription()).toString());
		body.append("\n");
		appendLabelValue(context, body, R.string.bed_type, rate.getRatePlanName());
		body.append("\n");
		appendLabelValue(context, body, R.string.adults, searchParams.getNumAdults() + "");
		body.append("\n");
		appendLabelValue(context, body, R.string.children, searchParams.getNumChildren() + "");
		body.append("\n\n");

		if (rate.getRateBreakdownList() != null) {
			for (RateBreakdown breakdown : rate.getRateBreakdownList()) {
				Date date = breakdown.getDate().getCalendar().getTime();
				String dateStr = dayFormatter.format(date) + ", " + fullDateFormatter.format(date);
				Money amount = breakdown.getAmount();
				if (amount.isZero()) {
					appendLabelValue(body, context.getString(R.string.room_rate_template, dateStr),
							context.getString(R.string.free));
				}
				else {
					appendLabelValue(body, context.getString(R.string.room_rate_template, dateStr),
							amount.getFormattedMoney());
				}
				body.append("\n");
			}
			body.append("\n\n");
		}

		if (rate.getTotalAmountBeforeTax() != null) {
			appendLabelValue(context, body, R.string.subtotal, rate.getTotalAmountBeforeTax().getFormattedMoney());
			body.append("\n");
		}

		Money totalSurcharge = new Money(rate.getTotalSurcharge());
		Money extraGuestFee = rate.getExtraGuestFee();
		if (extraGuestFee != null) {
			appendLabelValue(context, body, R.string.extra_guest_charge, extraGuestFee.getFormattedMoney());
			body.append("\n");
			if (totalSurcharge != null) {
				totalSurcharge = totalSurcharge.copy();
				totalSurcharge.subtract(extraGuestFee);
			}
		}
		if (totalSurcharge != null) {
			appendLabelValue(context, body, R.string.TaxesAndFees, totalSurcharge.getFormattedMoney());
			body.append("\n");
		}

		if (discountRate != null) {
			Money discount = new Money(discountRate.getTotalAmountAfterTax());
			discount.subtract(rate.getTotalAmountAfterTax());
			appendLabelValue(context, body, R.string.discount, discount.getFormattedMoney());
			body.append("\n");
			appendLabelValue(context, body, R.string.Total, discountRate.getTotalAmountAfterTax().getFormattedMoney());
			body.append("\n");
		}
		else {
			if (rate.getTotalAmountAfterTax() != null) {
				body.append("\n");
				appendLabelValue(context, body, R.string.Total, rate.getTotalAmountAfterTax().getFormattedMoney());
			}
		}

		Policy cancellationPolicy = rate.getRateRules().getPolicy(Policy.TYPE_CANCEL);
		if (cancellationPolicy != null) {
			body.append("\n\n");
			body.append(context.getString(R.string.cancellation_policy));
			body.append("\n");
			body.append(Html.fromHtml(cancellationPolicy.getDescription()));
		}

		body.append("\n\n");
		body.append(ConfirmationUtils.determineContactText(this));

		SocialUtils.email(context, subject, body.toString());

		// Track the share
		Log.d("Tracking \"CKO.CP.ShareBooking\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.ShareBooking");
	}

	private void appendLabelValue(Context context, StringBuilder sb, int labelStrId, String value) {
		appendLabelValue(sb, context.getString(labelStrId), value);
	}

	private void appendLabelValue(StringBuilder sb, String label, String value) {
		sb.append(label);
		sb.append(": ");
		sb.append(value);
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
