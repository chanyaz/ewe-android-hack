package com.expedia.bookings.activity;

import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.RoomsAndRatesFragment;
import com.expedia.bookings.fragment.RoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public class RoomsAndRatesListActivity extends SherlockFragmentActivity implements RoomsAndRatesFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.booking.details.offer.full";

	private static final long RESUME_TIMEOUT = 1000 * 60 * 20; // 20 minutes

	private RoomsAndRatesFragment mRoomsAndRatesFragment;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private long mLastResumeTime = -1;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	//////////////////////////////////////////////////////////////////////////////////////////
	// Static Methods
	//////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Create intent to open this activity in a standard way.
	 * @param context
	 * @return
	 */
	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, RoomsAndRatesListActivity.class);
		return intent;
	}

	//////////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////////

	//----------------------------------
	// LIFECYCLE EVENTS
	//----------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		setContentView(R.layout.activity_rooms_and_rates);
		getWindow().setBackgroundDrawable(null);

		mRoomsAndRatesFragment = Ui.findSupportFragment(this, getString(R.string.tag_rooms_and_rates));

		// Format the header
		Property property = Db.getSelectedProperty();
		SearchParams searchParams = Db.getSearchParams();
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			UrlBitmapDrawable.loadImageView(property.getThumbnail().getUrl(), thumbnailView, R.drawable.ic_image_placeholder);
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setTitle(R.string.select_a_room_instruction);

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		TextView locationView = (TextView) findViewById(R.id.location_text_view);
		locationView.setText(StrUtils.formatAddressShort(property.getLocation()));

		RatingBar hotelRating = (RatingBar) findViewById(R.id.hotel_rating_bar);
		hotelRating.setRating((float) property.getHotelRating());

		// Only display nights header if orientation landscape
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			TextView nightsView = (TextView) findViewById(R.id.nights_text_view);
			int numNights = Math.max(1, searchParams.getStayDuration());
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
			OmnitureTracking.trackAppHotelsRoomsRates(this, property, null);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadHotelsRoomsRates(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DOWNLOAD_KEY)) {
			mRoomsAndRatesFragment.showProgress();
			bd.registerDownloadCallback(DOWNLOAD_KEY, mCallback);
		}

		OmnitureTracking.onResume(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go back
			onBackPressed();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (!isFinishing()) {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
		}
		else {
			BackgroundDownloader.getInstance().cancelDownload(DOWNLOAD_KEY);
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();

		mWasStopped = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	private boolean checkFinishConditionsAndFinish() {
		// #13365: If the Db expired, finish out of this activity
		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}

		// #14135, set a 1 hour timeout on this screen
		if (mLastResumeTime != -1 && mLastResumeTime + RESUME_TIMEOUT < Calendar.getInstance().getTimeInMillis()) {
			finish();
			return true;
		}
		mLastResumeTime = Calendar.getInstance().getTimeInMillis();

		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloading rooms & rates

	private final Download<AvailabilityResponse> mDownload = new Download<AvailabilityResponse>() {
		@Override
		public AvailabilityResponse doDownload() {
			ExpediaServices services = new ExpediaServices(RoomsAndRatesListActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(DOWNLOAD_KEY, services);
			return services.availability(Db.getSearchParams(), Db.getSelectedProperty());
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

		Intent intent = new Intent(this, BookingOverviewActivity.class);
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
