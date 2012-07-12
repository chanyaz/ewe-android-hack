package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingFormFragment.BookingFormFragmentListener;
import com.expedia.bookings.fragment.SignInFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.Amobee;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.validation.ValidationError;

public class BookingInfoActivity extends FragmentActivity implements BookingFormFragmentListener,
		SignInFragmentListener {

	public static final String BOOKING_DOWNLOAD_KEY = BookingInfoActivity.class.getName() + ".BOOKING";

	private static final int DIALOG_CLEAR_PRIVATE_DATA = 4;

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes

	private Context mContext;

	private BookingFormFragment mBookingFragment;

	private long mLastResumeTime = -1;

	//////////////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		// This code allows us to test the BookingInfoActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		Intent intent = getIntent();
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			Db.loadTestData(this);
		}

		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		mBookingFragment = Ui.findOrAddSupportFragment(this, BookingFormFragment.class,
				getString(R.string.tag_booking_form));
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Haxxy fix for #13798, only required on pre-Honeycomb
		if (AndroidUtils.getSdkVersion() <= 10 && ConfirmationUtils.hasSavedConfirmationData(this)) {
			finish();
			return;
		}

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		// If we were booking, re-hook the download 
		BackgroundDownloader downloader = BackgroundDownloader.getInstance();
		if (downloader.isDownloading(BOOKING_DOWNLOAD_KEY)) {
			downloader.registerDownloadCallback(BOOKING_DOWNLOAD_KEY, mCheckoutCallback);
			showDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(BOOKING_DOWNLOAD_KEY);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			Db.setCreateTripResponse(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Menus

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_booking, menu);
		DebugMenu.onCreateOptionsMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_private_data:
			showDialog(DIALOG_CLEAR_PRIVATE_DATA);
			break;
		}

		if (DebugMenu.onOptionsItemSelected(this, item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Dialogs

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case BookingInfoUtils.DIALOG_BOOKING_PROGRESS: {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setMessage(getString(R.string.booking_loading));
			pd.setCancelable(false);
			pd.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
						return true;
					}
					return false;
				}
			});

			return pd;
		}
		case BookingInfoUtils.DIALOG_BOOKING_NULL: {
			return DialogUtils.createSimpleDialog(this, BookingInfoUtils.DIALOG_BOOKING_NULL,
					R.string.error_booking_title, R.string.error_booking_null);
		}
		case BookingInfoUtils.DIALOG_BOOKING_ERROR: {
			String errorMsg = Db.getBookingResponse().gatherErrorMessage(this);

			return DialogUtils.createSimpleDialog(this, BookingInfoUtils.DIALOG_BOOKING_ERROR,
					getString(R.string.error_booking_title), errorMsg);
		}
		case DIALOG_CLEAR_PRIVATE_DATA: {
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.dialog_clear_private_data_title);
			if (User.isLoggedIn(mContext)) {
				builder.setMessage(R.string.dialog_log_out_and_clear_private_data_msg);
			}
			else {
				builder.setMessage(R.string.dialog_clear_private_data_msg);
			}
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Delete private data
					Db.deleteBillingInfo(mContext);

					// Clear form
					mBookingFragment.clearBillingInfo();

					User.signOut(mContext);

					// Inform the men
					Toast.makeText(mContext, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
					finish();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}
		}

		return super.onCreateDialog(id);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Downloads

	private final Download<BookingResponse> mCheckoutDownload = new Download<BookingResponse>() {
		@Override
		public BookingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			String tripId = null;
			String userId = null;
			Long tuid = null;

			if (Db.getCreateTripResponse() != null) {
				tripId = Db.getCreateTripResponse().getTripId();
				userId = Db.getCreateTripResponse().getUserId();
			}

			if (Db.getUser() != null) {
				tuid = Db.getUser().getTuid();
			}

			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo(), tripId, userId, tuid);
		}
	};

	private final OnDownloadComplete<BookingResponse> mCheckoutCallback = new OnDownloadComplete<BookingResponse>() {
		@Override
		public void onDownload(BookingResponse response) {
			removeDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);

			if (response == null) {
				showDialog(BookingInfoUtils.DIALOG_BOOKING_NULL);
				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			Db.setBookingResponse(response);

			if (!response.isSuccess() && !response.succeededWithErrors()) {
				showDialog(BookingInfoUtils.DIALOG_BOOKING_ERROR);

				// Highlight erroneous fields, if that exists
				List<ValidationError> errors = response.checkForInvalidFields(getWindow(), Db.getBillingInfo()
						.getStoredCard() != null);
				if (errors != null && errors.size() > 0) {
					mBookingFragment.handleFormErrors(errors);
				}

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			// Track successful booking with Amobee
			final String currency = Db.getSelectedRate().getDisplayRate().getCurrency();
			final Integer duration = Db.getSearchParams().getStayDuration();
			final Double totalPrice = Db.getSelectedRate().getTotalAmountAfterTax().getAmount();
			final Integer daysRemaining = (int) ((Db.getSearchParams().getCheckInDate().getTime().getTime() - new Date()
					.getTime()) / (24 * 60 * 60 * 1000));

			Amobee.trackBooking(currency, totalPrice, duration, daysRemaining);

			if (Db.getCreateTripResponse() != null) {
				Db.setCouponDiscountRate(Db.getCreateTripResponse().getNewRate());
			}

			startActivity(ConfirmationFragmentActivity.createIntent(mContext));
		}
	};

	//////////////////////////////////////////////////////////////////////////////////
	// BookingFormFragmentListener

	@Override
	public void onCheckout() {
		showDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);
		BackgroundDownloader.getInstance().startDownload(BOOKING_DOWNLOAD_KEY, mCheckoutDownload, mCheckoutCallback);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SignInFragmentListener

	@Override
	public void onLoginStarted() {
		if (getSupportFragmentManager().findFragmentByTag(getString(R.string.tag_signin)) == null) {
			SignInFragment.newInstance().show(getSupportFragmentManager(), getString(R.string.tag_signin));
		}
	}

	@Override
	public void onLoginCompleted() {
		BookingFormFragment bookingFormFragment = Ui.findOrAddSupportFragment(this, BookingFormFragment.class,
				getString(R.string.tag_booking_form));
		bookingFormFragment.loginCompleted();
	}

	@Override
	public void onLoginFailed() {
		//NOTE: If SignInFragment takes care of failure for us we should never see this
	}
}
