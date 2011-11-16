package com.expedia.bookings.activity;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.google.android.maps.MapActivity;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;

public class ConfirmationFragmentActivity extends MapActivity {

	public InstanceFragment mInstance;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
