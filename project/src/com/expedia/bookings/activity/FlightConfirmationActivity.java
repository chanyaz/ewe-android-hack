package com.expedia.bookings.activity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.BitmapDrawable;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightCheckoutResponse;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class FlightConfirmationActivity extends FragmentActivity {

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	private ImageView mBgImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The app will get in to this state if being restored after background kill. In this case let's just be a good
		// guy and send them to the itin screen.
		if (Db.getFlightCheckout() == null) {
			Log.d("FlightConfirmationActivity launched without confirmation data, sending to itin");
			NavUtils.goToItin(this);
			finish();
			return;
		}

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (savedInstanceState == null) {
			// Add guest itin to ItinManager
			if (!User.isLoggedIn(this)) {
				String email = Db.getBillingInfo().getEmail();
				String tripId = Db.getTripBucket().getFlight().getItinerary().getItineraryNumber();
				ItineraryManager.getInstance().addGuestTrip(email, tripId);
			}
		}

		setContentView(R.layout.activity_flight_confirmation);
		getWindow().setBackgroundDrawable(null);

		mBgImageView = Ui.findView(this, R.id.background_bg_view);

		Point portrait = Ui.getPortraitScreenSize(this);
		final String code = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
		final String url = new Akeakamai(Images.getFlightDestination(code)) //
			.resizeExactly(portrait.x, portrait.y) //
			.build();
		Bitmap bitmap = L2ImageCache.sDestination.getImage(url, true /*blurred*/, true /*checkDisk*/);
		if (bitmap != null) {
			onBitmapLoaded(bitmap);
		}
		else {
			onBitmapLoadFailed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			Db.setBillingInfo(null);
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

	@Override
	public void finish() {
		// #953: Kick off deep refresh for newly booked flight
		final FlightCheckoutResponse response = Db.getFlightCheckout();
		if (response != null) {
			String tripId = Db.getTripBucket().getFlight().getItinerary().getItineraryNumber();
			ItineraryManager.getInstance().deepRefreshTrip(tripId, true);
		}

		super.finish();
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_confirmation, menu);
		ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_done);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_done:
			NavUtils.goToItin(this);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onBitmapLoaded(Bitmap bitmap) {
		BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
		mBgImageView.setImageDrawable(drawable);
	}

	public void onBitmapLoadFailed() {
		Bitmap bitmap = L2ImageCache.sDestination.getImage(getResources(), R.drawable.default_flights_background, true /*blurred*/);
		onBitmapLoaded(bitmap);
	}
}
