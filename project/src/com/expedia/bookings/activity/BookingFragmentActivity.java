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
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.Session;
import com.expedia.bookings.fragment.BookingErrorDialogFragment;
import com.expedia.bookings.fragment.BookingFormFragment;
import com.expedia.bookings.fragment.BookingInProgressDialogFragment;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.server.AvailabilityResponseHandler;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.json.JSONUtils;

public class BookingFragmentActivity extends Activity {

	//////////////////////////////////////////////////////////////////////////
	// Constants

	public static final int EVENT_PROPERTY_INFO_QUERY_STARTED = 1;
	public static final int EVENT_PROPERTY_INFO_QUERY_COMPLETE = 2;
	public static final int EVENT_PROPERTY_INFO_QUERY_ERROR = 3;
	public static final int EVENT_RATE_SELECTED = 4;

	public static final String KEY_PROPERTY_INFO = "KEY_PROPERTY_INFO";
	private static final String KEY_BOOKING = "KEY_BOOKING";

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
			mInstance.mSession = (Session) JSONUtils.parseJSONableFromIntent(intent, Codes.SESSION, Session.class);
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

			// Initialize some variables in the instance for later use
			mInstance.mBookingInfoValidation = new BookingInfoValidation();

			// Attempt to load the saved billing info
			mInstance.mBillingInfo = new BillingInfo();
			mInstance.mBillingInfo.load(this);
		}

		setContentView(R.layout.activity_booking_fragment);

		// Need to set this BG from code so we can make it just repeat vertically
		findViewById(R.id.search_results_list_shadow).setBackgroundDrawable(LayoutUtils.getDividerDrawable(this));
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
		if (bd.isDownloading(KEY_PROPERTY_INFO)) {
			bd.registerDownloadCallback(KEY_PROPERTY_INFO, mPropertyInfoCallback);
		}
		else if (mInstance.mPropertyInfoResponse != null) {
			mPropertyInfoCallback.onDownload(mInstance.mPropertyInfoResponse);
		}
		else {
			startPropertyInfoDownload(mInstance.mProperty);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.unregisterDownloadCallback(KEY_PROPERTY_INFO, mPropertyInfoCallback);
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

		public Session mSession;

		public SearchParams mSearchParams;
		public Property mProperty;

		public AvailabilityResponse mAvailabilityResponse;
		public Rate mRate;

		public String mPropertyInfoStatus;
		public PropertyInfoResponse mPropertyInfoResponse;

		public BillingInfo mBillingInfo;
		public BookingInfoValidation mBookingInfoValidation;

		public BookingResponse mBookingResponse;
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Hotel Info

	private void startPropertyInfoDownload(Property property) {
		//clear out previous results
		mInstance.mPropertyInfoResponse = null;
		mInstance.mPropertyInfoStatus = null;
		mEventManager.notifyEventHandlers(EVENT_PROPERTY_INFO_QUERY_STARTED, null);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_PROPERTY_INFO);
		bd.startDownload(KEY_PROPERTY_INFO, mPropertyInfoDownload, mPropertyInfoCallback);

	}

	private Download mPropertyInfoDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			return services.info(mInstance.mProperty);
		}
	};

	private OnDownloadComplete mPropertyInfoCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			mInstance.mPropertyInfoResponse = (PropertyInfoResponse) results;
			mInstance.mPropertyInfoStatus = null;

			if (mInstance.mPropertyInfoResponse == null) {
				mInstance.mPropertyInfoStatus = getString(R.string.error_room_type_load);
				mEventManager.notifyEventHandlers(EVENT_PROPERTY_INFO_QUERY_ERROR, null);
			}
			else {
				mEventManager.notifyEventHandlers(EVENT_PROPERTY_INFO_QUERY_COMPLETE, null);
			}
		}
	};

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
		mInstance.mBookingResponse = null;
		BookingInProgressDialogFragment.newInstance().show(getFragmentManager(),
				getString(R.string.tag_booking_progress));
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		bd.cancelDownload(KEY_BOOKING);
		bd.startDownload(KEY_BOOKING, mBookingDownload, mBookingCallback);
	}

	private Download mBookingDownload = new Download() {
		public Object doDownload() {
			ExpediaServices services = new ExpediaServices(mContext, mInstance.mSession);
			return services.reservation(BookingFragmentActivity.this, mInstance.mSearchParams, mInstance.mProperty,
					mInstance.mRate, mInstance.mBillingInfo);
		}
	};

	private OnDownloadComplete mBookingCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			((DialogFragment) getFragmentManager().findFragmentByTag(getString(R.string.tag_booking_progress)))
					.dismiss();

			if (results == null) {
				BookingErrorDialogFragment.newInstance(getString(R.string.error_booking_null)).show(
						getFragmentManager(), getString(R.string.tag_booking_error));
				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			BookingResponse response = mInstance.mBookingResponse = (BookingResponse) results;

			if (response.getSession() != null) {
				mInstance.mSession = response.getSession();
			}

			if (response.hasErrors()) {
				// Gather the error message
				String errorMsg = "";
				int numErrors = response.getErrors().size();
				List<ServerError> errors = response.getErrors();
				for (int a = 0; a < numErrors; a++) {
					if (a > 0) {
						errorMsg += "\n";
					}
					errorMsg += errors.get(a).getPresentableMessage(BookingFragmentActivity.this);
				}

				BookingErrorDialogFragment.newInstance(errorMsg).show(getFragmentManager(),
						getString(R.string.tag_booking_error));
				TrackingUtils.trackErrorPage(mContext, "ReservationRequestFailed");
				return;
			}

			// Start the conf activity
			Intent intent = new Intent(mContext, ConfirmationFragmentActivity.class);
			intent.putExtra(Codes.SESSION, mInstance.mSession.toJson().toString());
			intent.putExtra(Codes.SEARCH_PARAMS, mInstance.mSearchParams.toJson().toString());
			intent.putExtra(Codes.PROPERTY, mInstance.mProperty.toJson().toString());
			intent.putExtra(Codes.RATE, mInstance.mRate.toJson().toString());
			intent.putExtra(Codes.BOOKING_RESPONSE, mInstance.mBookingResponse.toJson().toString());
			intent.putExtra(Codes.BILLING_INFO, mInstance.mBillingInfo.toJson().toString());
			if (mInstance.mPropertyInfoResponse != null) {
				intent.putExtra(Codes.PROPERTY_INFO_RESPONSE, mInstance.mPropertyInfoResponse.toJson().toString());
			}
			startActivity(intent);
		}
	};
}
