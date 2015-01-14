package com.expedia.bookings.activity;

import org.joda.time.DateTime;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.HotelRoomsAndRatesFragment;
import com.expedia.bookings.fragment.HotelRoomsAndRatesFragment.RoomsAndRatesFragmentListener;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.DateFormatUtils;
import com.expedia.bookings.utils.DebugMenu;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class HotelRoomsAndRatesActivity extends FragmentActivity implements RoomsAndRatesFragmentListener {

	private static final long RESUME_TIMEOUT = 20 * DateUtils.MINUTE_IN_MILLIS;
	private static final String INSTANCE_LAST_SEARCH_TIME = "INSTANCE_LAST_SEARCH_TIME";

	private HotelRoomsAndRatesFragment mRoomsAndRatesFragment;

	private DateTime mLastSearchTime;

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
		Intent intent = new Intent(context, HotelRoomsAndRatesActivity.class);
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

		setContentView(R.layout.activity_rooms_and_rates);
		getWindow().setBackgroundDrawable(null);

		mRoomsAndRatesFragment = Ui.findSupportFragment(this, getString(R.string.tag_rooms_and_rates));

		// Format the header
		Property property = Db.getHotelSearch().getSelectedProperty();
		HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
		ImageView thumbnailView = (ImageView) findViewById(R.id.thumbnail_image_view);
		if (property.getThumbnail() != null) {
			thumbnailView.setVisibility(View.VISIBLE);
			property.getThumbnail().fillImageView(thumbnailView,
					Ui.obtainThemeResID(this, R.attr.skin_hotelImagePlaceHolderDrawable));
		}
		else {
			thumbnailView.setVisibility(View.GONE);
		}

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayShowTitleEnabled(true);
		ab.setTitle(R.string.select_a_room_instruction);

		TextView nameView = (TextView) findViewById(R.id.name_text_view);
		nameView.setText(property.getName());

		TextView locationView = (TextView) findViewById(R.id.location_text_view);
		locationView.setText(StrUtils.formatAddressShort(property.getLocation()));

		RatingBar hotelRating;
		if (property.shouldShowCircles()) {
			hotelRating = Ui.findView(this, R.id.hotel_rating_bar_circles);
		}
		else {
			hotelRating = Ui.findView(this, R.id.hotel_rating_bar_stars);
		}
		hotelRating.setVisibility(View.VISIBLE);
		hotelRating.setRating((float) property.getHotelRating());

		// Only display nights header if orientation landscape
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			TextView nightsView = (TextView) findViewById(R.id.nights_text_view);
			int numNights = Math.max(1, searchParams.getStayDuration());
			nightsView.setText(getResources().getQuantityString(R.plurals.staying_nights, numNights, numNights));

			TextView datesView = (TextView) findViewById(R.id.dates_text_view);
			datesView.setText(DateFormatUtils.formatDateRange(this, searchParams));
		}
		else {
			findViewById(R.id.nights_container).setVisibility(View.GONE);
		}

		if (savedInstanceState != null) {
			mLastSearchTime = (DateTime) savedInstanceState.getSerializable(INSTANCE_LAST_SEARCH_TIME);
		}

		if (mLastSearchTime == null) {
			mLastSearchTime = DateTime.now();
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(INSTANCE_LAST_SEARCH_TIME, mLastSearchTime);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Property property = Db.getHotelSearch().getSelectedProperty();
		OmnitureTracking.trackAppHotelsRoomsRates(this, property, null);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (checkFinishConditionsAndFinish()) {
			return;
		}

		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		if (Db.getHotelSearch().getHotelOffersResponse(selectedId) != null) {
			mRoomsAndRatesFragment.notifyAvailabilityLoaded();
		}
		else {
			getHotelOffers();
		}

		OmnitureTracking.onResume(this);
	}

	private void getHotelOffers() {
		mLastSearchTime = DateTime.now();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(CrossContextHelper.KEY_INFO_DOWNLOAD)) {
			mRoomsAndRatesFragment.showProgress();
			bd.registerDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD, mCallback);
		}
		else {
			bd.startDownload(CrossContextHelper.KEY_INFO_DOWNLOAD,
				CrossContextHelper.getHotelOffersDownload(this, CrossContextHelper.KEY_INFO_DOWNLOAD),
				mCallback);
		}
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
			BackgroundDownloader.getInstance().unregisterDownloadCallback(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}
		else {
			BackgroundDownloader.getInstance().cancelDownload(CrossContextHelper.KEY_INFO_DOWNLOAD);
		}

		OmnitureTracking.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}
	}

	private boolean checkFinishConditionsAndFinish() {

		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Db.loadHotelSearchFromDisk(this);
		}

		// #13365: If the Db expired, finish out of this activity
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return true;
		}

		// #14135, set a 1 hour timeout on this screen
		if (JodaUtils.isExpired(mLastSearchTime, RESUME_TIMEOUT)) {
			//Instead of finishing the activity, lets refresh the data
			getHotelOffers();
			return true;
		}

		return false;
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloading rooms & rates

	private final OnDownloadComplete<HotelOffersResponse> mCallback = new OnDownloadComplete<HotelOffersResponse>() {
		@Override
		public void onDownload(HotelOffersResponse response) {
			Db.getHotelSearch().updateFrom(response);
			Db.kickOffBackgroundHotelSearchSave(HotelRoomsAndRatesActivity.this);
			mRoomsAndRatesFragment.notifyAvailabilityLoaded();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// RoomsAndRatesFragmentListener

	@Override
	public void onRateSelected(Rate rate) {
		String selectedId = Db.getHotelSearch().getSelectedPropertyId();
		Db.getHotelSearch().getAvailability(selectedId).setSelectedRate(rate);

		Db.getTripBucket().clearHotel();
		Db.getTripBucket().add(Db.getHotelSearch(), rate, Db.getHotelSearch().getAvailability(selectedId));
		Db.saveTripBucket(this);

		Intent intent = new Intent(this, HotelOverviewActivity.class);
		startActivity(intent);

		OmnitureTracking.trackAddAirAttachHotel(this);
	}

	@Override
	public void noRatesAvailable() {
		// ignore
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
