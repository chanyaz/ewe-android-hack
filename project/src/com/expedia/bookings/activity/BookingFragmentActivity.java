package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingFormFragment.BookingFormFragmentListener;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.BookingInfoFragment;
import com.expedia.bookings.fragment.BookingInfoFragment.BookingInfoFragmentListener;
import com.expedia.bookings.fragment.RoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.fragment.SignInFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.validation.ValidationError;

public class BookingFragmentActivity extends FragmentActivity implements RoomsAndRatesFragmentListener,
		BookingInfoFragmentListener, SignInFragmentListener, BookingFormFragmentListener {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final String BOOKING_DOWNLOAD_KEY = BookingFragmentActivity.class.getName() + ".BOOKING";

	public static final String EXTRA_SPECIFIC_RATE = "EXTRA_SPECIFIC_RATE";

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;

	private BookingInfoFragment mBookingInfoFragment;

	private long mLastResumeTime = -1;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		mContext = this;

		setContentView(R.layout.activity_booking_fragment);

		mBookingInfoFragment = Ui.findSupportFragment(this, getString(R.string.tag_booking_info));

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		if (savedInstanceState == null) {
			String referrer = getIntent().getBooleanExtra(EXTRA_SPECIFIC_RATE, false) ? "App.Hotels.ViewSpecificRoom"
					: "App.Hotels.ViewAllRooms";

			Tracker.trackAppHotelsRoomsRates(this, Db.getSelectedProperty(), referrer);
		}
	}

	@TargetApi(11)
	@Override
	protected void onStart() {
		super.onStart();

		// Configure the ActionBar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.booking_information_title);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			bd.registerDownloadCallback(BOOKING_DOWNLOAD_KEY, mBookingCallback);
			DialogFragment dialog = BookingInProgressDialogFragment.newInstance();
			dialog.show(getSupportFragmentManager(), getString(R.string.tag_booking_progress));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(BOOKING_DOWNLOAD_KEY, mBookingCallback);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			Db.setCreateTripResponse(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@TargetApi(11)
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

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
	// Booking

	private final Download<BookingResponse> mBookingDownload = new Download<BookingResponse>() {
		@Override
		public BookingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			String userId = null;
			String tripId = null;
			Long tuid = null;

			if (Db.getCreateTripResponse() != null) {
				tripId = Db.getCreateTripResponse().getTripId();
				userId = Db.getCreateTripResponse().getUserId();
			}

			if (Db.getUser() != null) {
				tuid = Db.getUser().getPrimaryTraveler().getTuid();
			}

			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo(), tripId, userId, tuid);
		}
	};

	private final OnDownloadComplete<BookingResponse> mBookingCallback = new OnDownloadComplete<BookingResponse>() {
		@Override
		public void onDownload(BookingResponse response) {
			DialogFragment bookingProgressFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(
					getString(R.string.tag_booking_progress));
			if (bookingProgressFragment != null) {
				bookingProgressFragment.dismiss();
			}

			if (response == null) {
				showErrorDialog(getString(R.string.error_booking_null));

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");

				return;
			}

			Db.setBookingResponse(response);

			BookingFormFragment bookingFormFragment = (BookingFormFragment) getSupportFragmentManager()
					.findFragmentByTag(getString(R.string.tag_booking_form));

			if (!response.isSuccess() && !response.succeededWithErrors()) {
				String errorMsg = response.gatherErrorMessage(BookingFragmentActivity.this);
				showErrorDialog(errorMsg);

				// Highlight erroneous fields, if that exists
				boolean isStoredCard = Db.getBillingInfo() != null && Db.getBillingInfo().getStoredCard() != null;
				List<ValidationError> errors = response.checkForInvalidFields(bookingFormFragment.getDialog()
						.getWindow(), isStoredCard);
				if (errors != null && errors.size() > 0) {
					if (bookingFormFragment != null) {
						bookingFormFragment.handleFormErrors(errors);
					}
				}

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");

				return;
			}

			// Track successful booking with Amobee
			AdTracker.trackBooking();

			if (Db.getCreateTripResponse() != null) {
				Db.setCouponDiscountRate(Db.getCreateTripResponse().getNewRate());
			}

			if (bookingFormFragment != null) {
				bookingFormFragment.dismiss();
			}

			// Start the conf activity
			startActivity(new Intent(mContext, ConfirmationFragmentActivity.class));
		}
	};

	private void showErrorDialog(String errorMsg) {
		SimpleDialogFragment.newInstance(getString(R.string.error_booking_title), errorMsg).show(getFragmentManager(),
				getString(R.string.tag_booking_error));
	}

	//////////////////////////////////////////////////////////////////////////
	// RoomsAndRatesFragmentListener

	@Override
	public void onRateSelected(Rate rate) {
		Db.setSelectedRate(rate);

		mBookingInfoFragment.notifyRateSelected();
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingInfoFragmentListener

	@Override
	public void onEnterBookingInfoClick() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_booking_form)) == null) {
			BookingFormFragment.newInstance().show(getSupportFragmentManager(), getString(R.string.tag_booking_form));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SigninFragmentListener

	@Override
	public void onLoginStarted() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.findFragmentByTag(getString(R.string.tag_signin)) == null) {
			SignInFragment.newInstance(false).show(getSupportFragmentManager(), getString(R.string.tag_signin));
		}
	}

	@Override
	public void onLoginCompleted() {
		BookingFormFragment bookingFormFragment = (BookingFormFragment) getSupportFragmentManager().findFragmentByTag(
				getString(R.string.tag_booking_form));
		bookingFormFragment.loginCompleted();
	}

	@Override
	public void onLoginFailed() {
		//NOTE: If SignInFragment takes care of failure for us we should never see this
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingFormFragmentListener

	@Override
	public void onCheckout() {
		DialogFragment dialog = BookingInProgressDialogFragment.newInstance();
		dialog.show(getSupportFragmentManager(), getString(R.string.tag_booking_progress));

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(BOOKING_DOWNLOAD_KEY);
		bd.startDownload(BOOKING_DOWNLOAD_KEY, mBookingDownload, mBookingCallback);
	}
}
