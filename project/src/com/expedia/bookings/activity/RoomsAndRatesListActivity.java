package com.expedia.bookings.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.RoomsAndRatesFragment;
import com.expedia.bookings.fragment.RoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.Tracker;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;

public class RoomsAndRatesListActivity extends FragmentActivity implements RoomsAndRatesFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.booking.details.offer.full";

	private RoomsAndRatesFragment mRoomsAndRatesFragment;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// This code allows us to test the RoomsAndRatesListActivity standalone, for layout purposes.
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

		setContentView(R.layout.activity_rooms_and_rates);

		mRoomsAndRatesFragment = Ui.findSupportFragment(this, getString(R.string.tag_rooms_and_rates));

		// Format the header
		Property property = Db.getSelectedProperty();
		SearchParams searchParams = Db.getSearchParams();
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			ImageCache.loadImage(property.getThumbnail().getUrl(), thumbnailView);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		TextView locationView = (TextView) findViewById(R.id.location_text_view);
		locationView.setText(StrUtils.formatAddressShort(property.getLocation()));

		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());

		// Only display nights header if orientation landscape
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			TextView nightsView = (TextView) findViewById(R.id.nights_text_view);
			int numNights = searchParams.getStayDuration();
			nightsView.setText(getResources().getQuantityString(R.plurals.staying_nights, numNights, numNights));

			TextView datesView = (TextView) findViewById(R.id.dates_text_view);
			datesView.setText(CalendarUtils.formatDateRange(this, searchParams));
		}
		else {
			findViewById(R.id.nights_container).setVisibility(View.GONE);
		}

		// Load the initial data from net
		AvailabilityResponse response = Db.getSelectedAvailabilityResponse();
		if (response != null && !response.canRequestMoreData()) {
			mRoomsAndRatesFragment.notifyAvailabilityLoaded();
		}
		else {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(DOWNLOAD_KEY)) {
				bd.startDownload(DOWNLOAD_KEY, mDownload, mCallback);
			}
		}

		if (savedInstanceState == null) {
			Tracker.trackAppHotelsRoomsRates(this, property, null);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if (mWasStopped) {
			Tracker.trackAppHotelsRoomsRates(this, Db.getSelectedProperty(), null);
			mWasStopped = false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DOWNLOAD_KEY)) {
			mRoomsAndRatesFragment.showProgress();
			bd.registerDownloadCallback(DOWNLOAD_KEY, mCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloading rooms & rates

	private final Download<AvailabilityResponse> mDownload = new Download<AvailabilityResponse>() {
		@Override
		public AvailabilityResponse doDownload() {
			ExpediaServices services = new ExpediaServices(RoomsAndRatesListActivity.this);
			return services.availability(Db.getSearchParams(), Db.getSelectedProperty(),
					ExpediaServices.F_EXPENSIVE);
		}
	};


	private final OnDownloadComplete<AvailabilityResponse> mCallback = new OnDownloadComplete<AvailabilityResponse>() {
		@Override
		public void onDownload(AvailabilityResponse response) {
			Db.addAvailabilityResponse(response);
			mRoomsAndRatesFragment.notifyAvailabilityLoaded();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// RoomsAndRatesFragmentListener

	@Override
	public void onRateSelected(Rate rate) {
		Db.setSelectedRate(rate);

		Intent intent = new Intent(this, BookingInfoActivity.class);
		startActivity(intent);
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// Options menu (just for debug)

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		DebugMenu.onCreateOptionsMenu(this, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		DebugMenu.onPrepareOptionsMenu(this, menu);
		return super.onPrepareOptionsMenu(menu);
	}
}
