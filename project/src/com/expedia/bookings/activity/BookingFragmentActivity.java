package com.expedia.bookings.activity;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.validation.ValidationError;

public class BookingFragmentActivity extends Activity {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_RATE_SELECTED = 4;

	private static final String KEY_BOOKING = "KEY_BOOKING";

	public static final String EXTRA_SPECIFIC_RATE = "EXTRA_SPECIFIC_RATE";

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;

	public EventManager mEventManager = new EventManager();

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		if (savedInstanceState == null) {
			loadSavedBillingInfo();
		}

		setContentView(R.layout.activity_booking_fragment);

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		if (savedInstanceState == null) {
			String referrer = getIntent().getBooleanExtra(EXTRA_SPECIFIC_RATE, false) ? "App.Hotels.ViewSpecificRoom"
					: "App.Hotels.ViewAllRooms";

			Tracker.trackAppHotelsRoomsRates(this, Db.getSelectedProperty(), referrer);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Configure the ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_action_bar));
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_BOOKING)) {
			bd.registerDownloadCallback(KEY_BOOKING, mBookingCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_BOOKING, mBookingCallback);
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE,
				ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_TITLE);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.booking_information_title);

		DebugMenu.onCreateOptionsMenu(this, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.menu_about: {
			Intent intent = new Intent(this, TabletAboutActivity.class);
			startActivity(intent);
			return true;
		}
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////
	// Actions

	public void rateSelected(Rate rate) {
		Db.setSelectedRate(rate);

		mEventManager.notifyEventHandlers(EVENT_RATE_SELECTED, rate);
	}

	public void enterBookingInfo() {
		FragmentManager fm = getFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_booking_form)) == null) {
			BookingFormFragment.newInstance().show(getFragmentManager(), getString(R.string.tag_booking_form));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Booking

	public void bookingCompleted() {
		BookingInProgressDialogFragment.newInstance().show(getFragmentManager(),
				getString(R.string.tag_booking_progress));
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_BOOKING);
		bd.startDownload(KEY_BOOKING, mBookingDownload, mBookingCallback);
	}

	private Download mBookingDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo());
		}
	};

	private OnDownloadComplete mBookingCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			DialogFragment bookingProgressFragment = (DialogFragment) getFragmentManager().findFragmentByTag(
					getString(R.string.tag_booking_progress));
			if (bookingProgressFragment != null) {
				bookingProgressFragment.dismiss();
			}

			if (results == null) {
				showErrorDialog(getString(R.string.error_booking_null));

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");

				return;
			}

			BookingResponse response = (BookingResponse) results;

			BookingFormFragment bookingFormFragment = (BookingFormFragment) getFragmentManager().findFragmentByTag(
					getString(R.string.tag_booking_form));

			if (!response.isSuccess() && !response.succeededWithErrors()) {
				String errorMsg = response.gatherErrorMessage(BookingFragmentActivity.this);
				showErrorDialog(errorMsg);

				// Highlight erroneous fields, if that exists
				List<ValidationError> errors = response.checkForInvalidFields(bookingFormFragment.getDialog()
						.getWindow());
				if (errors != null && errors.size() > 0) {
					if (bookingFormFragment != null) {
						bookingFormFragment.handleFormErrors(errors);
					}
				}

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");

				return;
			}

			if (bookingFormFragment != null) {
				bookingFormFragment.dismiss();
			}

			// Start the conf activity
			Intent intent = new Intent(mContext, ConfirmationFragmentActivity.class);
			intent.putExtra(Codes.SEARCH_PARAMS, Db.getSearchParams().toJson().toString());
			intent.putExtra(Codes.PROPERTY, Db.getSelectedProperty().toJson().toString());
			intent.putExtra(Codes.RATE, Db.getSelectedRate().toJson().toString());
			intent.putExtra(Codes.BOOKING_RESPONSE, response.toJson().toString());
			intent.putExtra(Codes.BILLING_INFO, Db.getBillingInfo().toJson().toString());
			startActivity(intent);
		}
	};

	private void showErrorDialog(String errorMsg) {
		SimpleDialogFragment.newInstance(getString(R.string.error_booking_title), errorMsg).show(getFragmentManager(),
				getString(R.string.tag_booking_error));
	}

	private boolean loadSavedBillingInfo() {
		// Attempt to load the saved billing info
		// TODO: revisit this whole section
		if (Db.loadBillingInfo(this)) {

			BillingInfo billingInfo = Db.getBillingInfo();

			// When upgrading from 1.2.1 to 1.3, country code isn't present. So let's just use the default country.
			if (billingInfo.getTelephoneCountryCode() == null) {

				Resources r = getResources();
				String[] countryCodes = r.getStringArray(R.array.country_codes);
				String[] countryNames = r.getStringArray(R.array.country_names);
				int[] countryPhoneCodes = r.getIntArray(R.array.country_phone_codes);

				String defaultCountryName = getString(LocaleUtils.getDefaultCountryResId(this));

				for (int n = 0; n < countryCodes.length; n++) {
					if (defaultCountryName.equals(countryNames[n])) {
						billingInfo.setTelephoneCountry(countryCodes[n]);
						billingInfo.setTelephoneCountryCode(Integer.toString(countryPhoneCodes[n]));
						break;
					}
				}
			}

			return true;
		}

		return false;
	}
}
