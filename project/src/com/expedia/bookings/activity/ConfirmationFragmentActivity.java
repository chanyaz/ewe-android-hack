package com.expedia.bookings.activity;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager;
import com.expedia.bookings.server.ExpediaServices;
import com.google.android.maps.MapActivity;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class ConfirmationFragmentActivity extends MapActivity {

	public InstanceFragment mInstance;
	private Context mContext;
	public EventManager mEventManager = new EventManager();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		Intent intent = getIntent();

		FragmentManager fm = getFragmentManager();
		mInstance = (InstanceFragment) fm.findFragmentByTag(InstanceFragment.TAG);
		if (mInstance == null) {
			mInstance = InstanceFragment.newInstance();
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(mInstance, InstanceFragment.TAG);
			ft.commit();

			// Load data from Intent
			mInstance.mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
					SearchParams.class);
			mInstance.mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
			mInstance.mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);
			mInstance.mBillingInfo = (BillingInfo) JSONUtils.parseJSONableFromIntent(intent, Codes.BILLING_INFO,
					BillingInfo.class);
			mInstance.mBookingResponse = (BookingResponse) JSONUtils.parseJSONableFromIntent(intent,
					Codes.BOOKING_RESPONSE, BookingResponse.class);
			mInstance.mPropertyInfoResponse = (PropertyInfoResponse) JSONUtils.parseJSONableFromIntent(intent,
					Codes.PROPERTY_INFO_RESPONSE, PropertyInfoResponse.class);

			// This code allows us to test the ConfirmationFragmentActivity standalone, for layout purposes.
			// Just point the default launcher activity towards this instead of SearchActivity
			if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
				try {
					mInstance.mSearchParams = new SearchParams();
					mInstance.mSearchParams.fillWithTestData();
					mInstance.mProperty = new Property();
					mInstance.mProperty.fillWithTestData();
					mInstance.mRate = new Rate();
					mInstance.mRate.fillWithTestData();
					mInstance.mBookingResponse = new BookingResponse();
					mInstance.mBookingResponse.fillWithTestData();
					mInstance.mBillingInfo = new BillingInfo();
					mInstance.mBillingInfo.fillWithTestData();
				}
				catch (JSONException e) {
					Log.e("Couldn't create dummy data!", e);
				}
			}
		}

		setContentView(R.layout.activity_confirmation_fragment);
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
		if (bd.isDownloading(BookingFragmentActivity.KEY_PROPERTY_INFO)) {
			bd.registerDownloadCallback(BookingFragmentActivity.KEY_PROPERTY_INFO, mPropertyInfoCallback);
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
		bd.unregisterDownloadCallback(BookingFragmentActivity.KEY_PROPERTY_INFO, mPropertyInfoCallback);
	}

	//////////////////////////////////////////////////////////////////////////
	// ActionBar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_fragment_standard, menu);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
		actionBar.setDisplayHomeAsUpEnabled(true);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			newSearch();
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

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(BookingFragmentActivity.KEY_PROPERTY_INFO);
		bd.startDownload(BookingFragmentActivity.KEY_PROPERTY_INFO, mPropertyInfoDownload, mPropertyInfoCallback);
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
				mEventManager.notifyEventHandlers(BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_ERROR, null);
			}
			else {
				mEventManager.notifyEventHandlers(BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_COMPLETE, null);
			}
		}
	};

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
		public Rate mRate;
		public BillingInfo mBillingInfo;
		public BookingResponse mBookingResponse;
		public PropertyInfoResponse mPropertyInfoResponse;
		public String mPropertyInfoStatus;
	}

	//////////////////////////////////////////////////////////////////////////
	// Actions

	public void newSearch() {
		Intent intent = new Intent(this, SearchFragmentActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////
	// MapActivity

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
