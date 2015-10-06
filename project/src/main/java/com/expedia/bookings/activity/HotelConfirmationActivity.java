package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.tracking.AdImpressionTracking;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.Log;

public class HotelConfirmationActivity extends FragmentActivity {

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The app will get in to this state if being restored after background kill. In this case let's just be a good
		// guy and send them to the itin screen.
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		HotelBookingResponse bookingResponse = hotel == null ? null : hotel.getBookingResponse();
		if (bookingResponse == null) {
			Log.d("HotelConfirmationActivity launched without confirmation data, sending to itin");
			NavUtils.goToItin(this);
			finish();
			return;
		}

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		if (savedInstanceState == null) {
			if (bookingResponse.succeededWithErrors()) {
				showSucceededWithErrorsDialog();
			}

			// Add guest itin to itin manager
			if (Db.getBillingInfo() != null && !User.isLoggedIn(this)) {
				String email = Db.getBillingInfo().getEmail();
				String tripId = bookingResponse.getItineraryId();
				ItineraryManager.getInstance().addGuestTrip(email, tripId);
			}

			// Track page load
			Property property = Db.getTripBucket().getHotel().getProperty();
			HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
			Rate selectedRate = Db.getTripBucket().getHotel().getRate();

			CreateTripResponse tripResponse = hotel.getCreateTripResponse();

			OmnitureTracking.trackAppHotelsCheckoutConfirmation(this, params, property, tripResponse.getSupplierType(),
				selectedRate, bookingResponse);

			if (tripResponse != null) {
				AdImpressionTracking.trackAdConversion(this, tripResponse.getTripId());
			}
		}

		setContentView(R.layout.activity_hotel_confirmation);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			// #953: Kick off deep refresh for newly booked hotel
			HotelBookingResponse response = Db.getTripBucket().getHotel().getBookingResponse();
			if (response != null) {
				ItineraryManager.getInstance().deepRefreshTrip(response.getItineraryId(), true);
			}

			// Clear out data
			Db.setBillingInfo(null);
			Db.getHotelSearch().resetSearchData();
			Db.getHotelSearch().resetSearchParams();

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mKillReceiver != null) {
			mKillReceiver.onDestroy();
		}

		if (isFinishing()) {
			Db.getHotelSearch().resetSearchData();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Action bar

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_confirmation, menu);
		ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_done);
		TextView actionButton = (TextView) menu.findItem(R.id.menu_done).getActionView();
		actionButton.setText(R.string.button_confirmation_done);
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
			String err = Db.getTripBucket().getHotel().getBookingResponse().gatherErrorMessage(this);
			String message = getString(R.string.error_booking_succeeded_with_errors, err);

			SimpleSupportDialogFragment.newInstance(title, message).show(getSupportFragmentManager(), dialogTag);
		}
	}

}
