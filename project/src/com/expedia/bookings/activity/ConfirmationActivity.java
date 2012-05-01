package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.ReceiptWidget;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.Log;
import com.mobiata.android.MapUtils;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.DialogUtils;
import com.mobiata.android.util.IoUtils;

public class ConfirmationActivity extends MapActivity {

	private static final int INSTANCE_PROPERTY = 1;
	private static final int INSTANCE_SEARCH_PARAMS = 2;
	private static final int INSTANCE_RATE = 3;
	private static final int INSTANCE_BILLING_INFO = 4;
	private static final int INSTANCE_BOOKING_RESPONSE = 5;

	private Context mContext;

	private ReceiptWidget mReceiptWidget;

	private SearchParams mSearchParams;
	private Property mProperty;
	private Rate mRate;
	private BillingInfo mBillingInfo;
	private BookingResponse mBookingResponse;

	private String mContactText;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_confirmation);

		// Retrieve data to build this with
		boolean loadedData = false;
		SparseArray<Object> instance = (SparseArray<Object>) getLastNonConfigurationInstance();
		if (instance != null) {
			mSearchParams = (SearchParams) instance.get(INSTANCE_SEARCH_PARAMS);
			mProperty = (Property) instance.get(INSTANCE_PROPERTY);
			mRate = (Rate) instance.get(INSTANCE_RATE);
			mBillingInfo = (BillingInfo) instance.get(INSTANCE_BILLING_INFO);
			mBookingResponse = (BookingResponse) instance.get(INSTANCE_BOOKING_RESPONSE);
			loadedData = true;
		}
		else if (ConfirmationUtils.hasSavedConfirmationData(this)) {
			if (loadSavedConfirmationData()) {
				loadedData = true;
			}
			else {
				// If we failed to load the saved confirmation data, we should
				// delete the file and go back (since we are only here if we were called
				// directly from a startup).
				ConfirmationUtils.deleteSavedConfirmationData(this);
				finish();
			}
		}

		final Intent intent = getIntent();
		if (!loadedData) {
			mSearchParams = (SearchParams) JSONUtils.parseJSONableFromIntent(intent, Codes.SEARCH_PARAMS,
					SearchParams.class);
			mProperty = (Property) JSONUtils.parseJSONableFromIntent(intent, Codes.PROPERTY, Property.class);
			mRate = (Rate) JSONUtils.parseJSONableFromIntent(intent, Codes.RATE, Rate.class);
			mBillingInfo = (BillingInfo) JSONUtils.parseJSONableFromIntent(intent, Codes.BILLING_INFO,
					BillingInfo.class);
			mBookingResponse = (BookingResponse) JSONUtils.parseJSONableFromIntent(intent, Codes.BOOKING_RESPONSE,
					BookingResponse.class);

			// Start a background thread to save this data to the disk
			new Thread(new Runnable() {
				public void run() {
					ConfirmationUtils.saveConfirmationData(ConfirmationActivity.this, mSearchParams, mProperty, mRate,
							mBillingInfo, mBookingResponse);
				}
			}).start();
		}

		// This code allows us to test the ConfirmationActivity standalone, for layout purposes.
		// Just point the default launcher activity towards this instead of SearchActivity
		if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
			Db.loadTestData(this);
			mSearchParams = Db.getSearchParams();
			mProperty = Db.getSelectedProperty();
			mRate = Db.getSelectedRate();
			mBookingResponse = Db.getBookingResponse();
			mBillingInfo = Db.getBillingInfo();
		}

		// We don't want to display the "succeeded with errors" dialog box if:
		// 1. It's not the first launch of the activity (savedInstanceState != null)
		// 2. We're re-launching the activity with saved confirmation data
		if (mBookingResponse.succeededWithErrors() && savedInstanceState == null
				&& !ConfirmationUtils.hasSavedConfirmationData(this)) {
			showDialog(BookingInfoUtils.DIALOG_BOOKING_ERROR);
		}

		mReceiptWidget = new ReceiptWidget(this, findViewById(R.id.receipt), true);
		mReceiptWidget.restoreInstanceState(savedInstanceState);

		//////////////////////////////////////////////////
		// Screen configuration

		// Show on the map where the hotel is
		MapView mapView = MapUtils.createMapView(this);
		ViewGroup mapContainer = (ViewGroup) findViewById(R.id.map_layout);
		mapContainer.addView(mapView);

		List<Property> properties = new ArrayList<Property>(1);
		properties.add(mProperty);
		List<Overlay> overlays = mapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(this, properties, mapView);
		overlays.add(overlay);
		MapController mc = mapView.getController();
		GeoPoint center = overlay.getCenter();
		GeoPoint offsetCenter = new GeoPoint(center.getLatitudeE6() + 1000, center.getLongitudeE6() - 8000);
		mc.setCenter(offsetCenter);
		mc.setZoom(15);
		// disabling the map so that it does not respond to touch events 
		mapView.setEnabled(false);

		// Ratings
		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) mProperty.getHotelRating());
		RatingBar userRating = (RatingBar) findViewById(R.id.user_rating_bar);
		userRating.setRating((float) mProperty.getAverageExpediaRating());

		// Receipt pricing info (daily rates, taxes, total, etc.)
		mReceiptWidget.updateData(mProperty, mSearchParams, mRate, mBookingResponse, mBillingInfo);

		// Cancellation policy (at the bottom)
		View confirmationContainer = findViewById(R.id.confirmation_content_container);
		ConfirmationUtils.determineCancellationPolicy(mRate, confirmationContainer);

		// Reservation support contact info
		TextView contactView = (TextView) findViewById(R.id.contact_text_view);
		mContactText = ConfirmationUtils.determineContactText(this);
		ConfirmationUtils.configureContactView(this, contactView, mContactText);

		//////////////////////////////////////////////////
		// Button bar configuration

		ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ConfirmationUtils.share(ConfirmationActivity.this, mSearchParams, mProperty, mBookingResponse,
						mBillingInfo, mRate, mContactText);
			}
		});

		ImageButton mapButton = (ImageButton) findViewById(R.id.map_button);
		mapButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Tracker.trackViewOnMap(mContext);
				startActivity(ConfirmationUtils.generateIntentToShowPropertyOnMap(mProperty));
			}
		});

		Button newSearchButton = (Button) findViewById(R.id.new_search_button);
		newSearchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.i("User is initiating a new search.");

				onClickNewSearch();

				// Ensure we can't come back here again
				ConfirmationUtils.deleteSavedConfirmationData(mContext);

				Intent intent = new Intent(mContext, PhoneSearchActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra(Codes.EXTRA_NEW_SEARCH, true);
				startActivity(intent);
				finish();
			}
		});

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		SparseArray<Object> instance = new SparseArray<Object>();

		instance.put(INSTANCE_SEARCH_PARAMS, mSearchParams);
		instance.put(INSTANCE_PROPERTY, mProperty);
		instance.put(INSTANCE_RATE, mRate);
		instance.put(INSTANCE_BILLING_INFO, mBillingInfo);
		instance.put(INSTANCE_BOOKING_RESPONSE, mBookingResponse);

		return instance;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		mReceiptWidget.saveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			onPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// #7090: A user should remain on the confirmation page until they explicitly press the
		// "new search" key.  This is the easiest way to get out of this - send the user back
		// to the start, then finish that activity, when the user presses back.
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			finish();
			Intent i = new Intent(mContext, PhoneSearchActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i.putExtra(Codes.EXTRA_FINISH, true);
			startActivity(i);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case BookingInfoUtils.DIALOG_BOOKING_ERROR: {
			String errorMsg = getString(R.string.error_booking_succeeded_with_errors,
					mBookingResponse.gatherErrorMessage(this));

			return DialogUtils.createSimpleDialog(this, BookingInfoUtils.DIALOG_BOOKING_ERROR,
					getString(R.string.error_booking_title), errorMsg);
		}
		}
		return super.onCreateDialog(id);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Breadcrumb (reloading activity)

	public boolean loadSavedConfirmationData() {
		Log.i("Loading saved confirmation data...");
		try {
			JSONObject data = new JSONObject(IoUtils.readStringFromFile(ConfirmationUtils.CONFIRMATION_DATA_FILE, this));
			mSearchParams = (SearchParams) JSONUtils.getJSONable(data, Codes.SEARCH_PARAMS, SearchParams.class);
			mProperty = (Property) JSONUtils.getJSONable(data, Codes.PROPERTY, Property.class);
			mRate = (Rate) JSONUtils.getJSONable(data, Codes.RATE, Rate.class);
			mBillingInfo = (BillingInfo) JSONUtils.getJSONable(data, Codes.BILLING_INFO, BillingInfo.class);
			mBookingResponse = (BookingResponse) JSONUtils.getJSONable(data, Codes.BOOKING_RESPONSE,
					BookingResponse.class);
			return true;
		}
		catch (Exception e) {
			Log.e("Could not load ConfirmationActivity state.", e);
			return false;
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Tracker.trackAppHotelsCheckoutConfirmation(this, mSearchParams, mProperty, mBillingInfo, mRate,
				mBookingResponse);
	}

	public void onClickNewSearch() {
		Tracker.trackNewSearch(this);
	}
}
