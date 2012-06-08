package com.expedia.bookings.activity;

import java.util.List;

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

	private static final String KEY_BOOKING = "KEY_BOOKING";

	public static final String EXTRA_SPECIFIC_RATE = "EXTRA_SPECIFIC_RATE";

	//////////////////////////////////////////////////////////////////////////
	// Member vars

	private Context mContext;

	private BookingInfoFragment mBookingInfoFragment;

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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			Db.setCreateTripResponse(null);
		}
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
	// Booking

	private final Download<BookingResponse> mBookingDownload = new Download<BookingResponse>() {
		@Override
		public BookingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			String userId = null;
			String tripId = null;
			if (Db.getCreateTripResponse() != null) {
				tripId = Db.getCreateTripResponse().getTripId();
				userId = Db.getCreateTripResponse().getUserId();
			}
			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo(), tripId, userId);
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
				List<ValidationError> errors = response.checkForInvalidFields(bookingFormFragment.getDialog()
						.getWindow(), Db.getBillingInfo().getStoredCard() != null);
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
			startActivity(ConfirmationFragmentActivity.createIntent(mContext));
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
			SignInFragment.newInstance().show(getSupportFragmentManager(), getString(R.string.tag_signin));
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
		dialog.setCancelable(false);
		dialog.show(getSupportFragmentManager(), getString(R.string.tag_booking_progress));
		
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_BOOKING);
		bd.startDownload(KEY_BOOKING, mBookingDownload, mBookingCallback);
	}
}
