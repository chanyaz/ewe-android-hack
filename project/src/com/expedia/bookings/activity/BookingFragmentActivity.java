package com.expedia.bookings.activity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.server.AvailabilityResponseHandler;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleDialogFragment;
import com.mobiata.android.json.JSONUtils;
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

	public InstanceFragment mInstance;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		FragmentManager fm = getFragmentManager();
		mInstance = (InstanceFragment) fm.findFragmentByTag(InstanceFragment.TAG);
		if (mInstance == null) {
			mInstance = InstanceFragment.newInstance();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(mInstance, InstanceFragment.TAG);
			ft.commit();

			// Construct data for activity 
			Intent intent = getIntent();
			mInstance.mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
					SearchParams.class);
			mInstance.mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
			mInstance.mAvailabilityResponse = (AvailabilityResponse) JSONUtils.parseJSONableFromIntent(intent,
					Codes.AVAILABILITY_RESPONSE, AvailabilityResponse.class);
			mInstance.mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);

			// This code allows us to test the BookingFragmentActivity standalone, for layout purposes.
			// Just point the default launcher activity towards this instead of SearchActivity
			if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
				try {
					mInstance.mSearchParams = new SearchParams();
					mInstance.mSearchParams.fillWithTestData();
					mInstance.mProperty = new Property();
					mInstance.mProperty.fillWithTestData();
					mInstance.mRate = new Rate();
					mInstance.mRate.fillWithTestData();

					JSONObject obj = new JSONObject(getString(R.string.sample_availability_response));
					AvailabilityResponseHandler handler = new AvailabilityResponseHandler(this,
							mInstance.mSearchParams, mInstance.mProperty);
					mInstance.mAvailabilityResponse = (AvailabilityResponse) handler.handleJson(obj);
				}
				catch (JSONException e) {
					Log.e("Couldn't create dummy data!", e);
				}
			}

			// Attempt to load the saved billing info
			mInstance.mBillingInfo = new BillingInfo();
			mInstance.mBillingInfo.load(this);
		}

		setContentView(R.layout.activity_booking_fragment);

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));

		if (savedInstanceState == null) {
			String referrer = getIntent().getBooleanExtra(EXTRA_SPECIFIC_RATE, false) ? "App.Hotels.ViewSpecificRoom"
					: "App.Hotels.ViewAllRooms";

			Tracker.trackAppHotelsRoomsRates(this, mInstance.mProperty, referrer);
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
	// InstanceFragment

	public static final class InstanceFragment extends Fragment {
		public static final String TAG = "INSTANCE";

		public static InstanceFragment newInstance() {
			InstanceFragment fragment = new InstanceFragment();
			fragment.setRetainInstance(true);
			return fragment;
		}

		public SearchParams mSearchParams;
		public Property mProperty;

		public AvailabilityResponse mAvailabilityResponse;
		public Rate mRate;

		public BillingInfo mBillingInfo;
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
		mInstance.mRate = rate;

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
			return services.reservation(mInstance.mSearchParams, mInstance.mProperty, mInstance.mRate,
					mInstance.mBillingInfo);
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
			intent.putExtra(Codes.SEARCH_PARAMS, mInstance.mSearchParams.toJson().toString());
			intent.putExtra(Codes.PROPERTY, mInstance.mProperty.toJson().toString());
			intent.putExtra(Codes.RATE, mInstance.mRate.toJson().toString());
			intent.putExtra(Codes.BOOKING_RESPONSE, response.toJson().toString());
			intent.putExtra(Codes.BILLING_INFO, mInstance.mBillingInfo.toJson().toString());
			startActivity(intent);
		}
	};

	private void showErrorDialog(String errorMsg) {
		SimpleDialogFragment.newInstance(getString(R.string.error_booking_title), errorMsg).show(getFragmentManager(),
				getString(R.string.tag_booking_error));
	}
}
