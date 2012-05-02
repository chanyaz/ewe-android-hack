package com.expedia.bookings.activity;

import java.util.List;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingFormFragment.BookingFormFragmentListener;
import com.expedia.bookings.fragment.SignInFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.validation.ValidationError;

public class BookingInfoActivity extends FragmentActivity implements BookingFormFragmentListener,
		SignInFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.booking";

	private static final int DIALOG_CLEAR_PRIVATE_DATA = 4;

	private Context mContext;

	private BookingFormFragment mBookingFragment;

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

		mBookingFragment = Ui.findOrAddSupportFragment(this, BookingFormFragment.class,
				getString(R.string.tag_booking_form));
	}

	@Override
	protected void onResume() {
		super.onResume();

		// If we were booking, re-hook the download 
		BackgroundDownloader downloader = BackgroundDownloader.getInstance();
		if (downloader.isDownloading(DOWNLOAD_KEY)) {
			downloader.registerDownloadCallback(DOWNLOAD_KEY, mCheckoutCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
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
			builder.setMessage(R.string.dialog_clear_private_data_msg);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Delete private data
					Db.deleteBillingInfo(mContext);

					// Clear form
					mBookingFragment.clearBillingInfo();

					// Inform the men
					Toast.makeText(mContext, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
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

	private Download mCheckoutDownload = new Download() {
		@Override
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.reservation(Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate(),
					Db.getBillingInfo());
		}
	};

	private OnDownloadComplete mCheckoutCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			removeDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);

			if (results == null) {
				showDialog(BookingInfoUtils.DIALOG_BOOKING_NULL);
				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			BookingResponse response = (BookingResponse) results;
			Db.setBookingResponse(response);

			if (!response.isSuccess() && !response.succeededWithErrors()) {
				showDialog(BookingInfoUtils.DIALOG_BOOKING_ERROR);

				// Highlight erroneous fields, if that exists
				List<ValidationError> errors = response.checkForInvalidFields(getWindow());
				if (errors != null && errors.size() > 0) {
					mBookingFragment.handleFormErrors(errors);
				}

				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			// TODO: Have ConfirmationActivity rely on Db, instead of filling this intent with everything it needs
			Intent intent = new Intent(mContext, ConfirmationActivity.class);
			intent.putExtra(Codes.PROPERTY, Db.getSelectedProperty().toJson().toString());
			intent.putExtra(Codes.SEARCH_PARAMS, Db.getSearchParams().toJson().toString());
			intent.putExtra(Codes.RATE, Db.getSelectedRate().toJson().toString());
			intent.putExtra(Codes.BOOKING_RESPONSE, response.toJson().toString());

			// Create a BillingInfo that lacks the user's security code (for safety)
			JSONObject billingJson = Db.getBillingInfo().toJson();
			billingJson.remove("securityCode");
			intent.putExtra(Codes.BILLING_INFO, billingJson.toString());

			startActivity(intent);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////
	// BookingFormFragmentListener

	@Override
	public void onCheckout() {
		showDialog(BookingInfoUtils.DIALOG_BOOKING_PROGRESS);
		BackgroundDownloader.getInstance().startDownload(DOWNLOAD_KEY, mCheckoutDownload, mCheckoutCallback);
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
