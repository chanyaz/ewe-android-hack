package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.TwoLevelImageCache;

public class HotelConfirmationActivity extends SherlockFragmentActivity {

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The app will get in to this state if being restored after background kill. In this case let's just be a good
		// guy and send them to the itin screen.
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.d("HotelConfirmationActivity launched without confirmation data, sending to itin");
			NavUtils.goToItin(this);
			finish();
			return;
		}

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (savedInstanceState == null) {
			if (Db.getBookingResponse().succeededWithErrors()) {
				showSucceededWithErrorsDialog();
			}

			// Add guest itin to itin manager
			if (Db.getBookingResponse() != null && Db.getBillingInfo() != null && !User.isLoggedIn(this)) {
				String email = Db.getBillingInfo().getEmail();
				String tripId = Db.getBookingResponse().getItineraryId();
				ItineraryManager.getInstance().addGuestTrip(email, tripId);
			}

			// Track page load
			Rate selectedRate = Db.getHotelSearch().getBookingRate();
			OmnitureTracking.trackAppHotelsCheckoutConfirmation(this, Db.getHotelSearch().getSearchParams(),
					Db.getHotelSearch().getSelectedProperty(), Db.getBillingInfo(),
					selectedRate, Db.getBookingResponse());
		}

		setContentView(R.layout.activity_hotel_confirmation);
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
			// #953: Kick off deep refresh for newly booked hotel
			BookingResponse response = Db.getBookingResponse();
			if (response != null) {
				ItineraryManager.getInstance().deepRefreshTrip(response.getItineraryId(), true);
			}

			// Clear out data
			Db.setBillingInfo(null);
			Db.setBookingResponse(null);
			Db.getHotelSearch().setCreateTripResponse(null);
			Db.getHotelSearch().resetSearchData();

			// Clear the cache just to be safe
			TwoLevelImageCache.clearMemoryCache();
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

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_confirmation, menu);
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

	//////////////////////////////////////////////////////////////////////////
	// Error handling
	//
	// Sometimes we can succeed in the booking, but there is some error
	// that still happened (that is minor).  Inform the user

	public void showSucceededWithErrorsDialog() {
		FragmentManager fm = getSupportFragmentManager();
		String dialogTag = getString(R.string.tag_simple_dialog);
		if (fm.findFragmentByTag(dialogTag) == null) {
			String title = getString(R.string.error_booking_title);
			String message = getString(R.string.error_booking_succeeded_with_errors, Db.getBookingResponse()
					.gatherErrorMessage(this));

			SimpleSupportDialogFragment.newInstance(title, message).show(getSupportFragmentManager(), dialogTag);
		}
	}

}
