package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.BookingOverviewFragment;
import com.expedia.bookings.fragment.BookingOverviewFragment.BookingOverviewFragmentListener;
import com.expedia.bookings.fragment.LoginFragment.LogInListener;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.SlideToWidget.ISlideToListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class BookingOverviewActivity extends SherlockFragmentActivity implements BookingOverviewFragmentListener,
		LogInListener, ISlideToListener {

	private BookingOverviewFragment mBookingOverviewFragment;
	private MenuItem mCheckoutMenuItem;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Db.getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		loadCachedData(false);

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		setContentView(R.layout.activity_booking_overview);

		mBookingOverviewFragment = (BookingOverviewFragment) getSupportFragmentManager().findFragmentById(
				R.id.booking_overview_fragment);
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
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
	public void onBackPressed() {
		if (mBookingOverviewFragment.isInCheckout()) {
			mBookingOverviewFragment.endCheckout();
			mCheckoutMenuItem.setVisible(true);

			return;
		}

		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_booking_overview, menu);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);

		ViewGroup titleView = (ViewGroup) getLayoutInflater().inflate(R.layout.actionbar_hotel_name_with_stars, null);

		Property property = Db.getSelectedProperty();
		if (property == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return false;
		}

		((TextView) titleView.findViewById(R.id.title)).setText(property.getName());
		((RatingBar) titleView.findViewById(R.id.rating)).setRating((float) property.getHotelRating());

		actionBar.setCustomView(titleView);

		Button tv = (Button) getLayoutInflater().inflate(R.layout.actionbar_checkout, null);
		ViewUtils.setAllCaps(tv);
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionsItemSelected(mCheckoutMenuItem);
			}
		});

		mCheckoutMenuItem = menu.findItem(R.id.menu_checkout);
		mCheckoutMenuItem.setActionView(tv);

		if (mBookingOverviewFragment != null) {
			mCheckoutMenuItem.setVisible(!mBookingOverviewFragment.isInCheckout());
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			if (mBookingOverviewFragment.isInCheckout()) {
				mBookingOverviewFragment.endCheckout();
				return true;
			}

			onBackPressed();
			return true;
		}
		case R.id.menu_checkout: {
			mBookingOverviewFragment.startCheckout();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	// Private methods

	private void loadCachedData(boolean wait) {
		if (!Db.hasBillingInfo()) {
			CheckoutDataLoader.CheckoutDataLoadedListener listener = new CheckoutDataLoader.CheckoutDataLoadedListener() {
				@Override
				public void onCheckoutDataLoaded(boolean wasSuccessful) {
					Runnable refreshDataRunner = new Runnable() {
						@Override
						public void run() {
							if (mBookingOverviewFragment != null) {
								mBookingOverviewFragment.refreshData();
							}
						}
					};
					runOnUiThread(refreshDataRunner);
				}

			};
			CheckoutDataLoader.getInstance().loadCheckoutData(this, true, true, listener, wait);
		}
	}

	//BookingOverviewFragmentListener implementation

	@Override
	public void checkoutStarted() {
		if (mCheckoutMenuItem != null) {
			mCheckoutMenuItem.setVisible(false);
		}
	}

	@Override
	public void checkoutEnded() {
		if (mCheckoutMenuItem != null) {
			mCheckoutMenuItem.setVisible(true);
		}
	}

	// LogInListener implementation

	@Override
	public void onLoginStarted() {
	}

	@Override
	public void onLoginCompleted() {
		mBookingOverviewFragment.onLoginCompleted();
	}

	@Override
	public void onLoginFailed() {
	}

	// ISlideToListener implementation

	@Override
	public void onSlideStart() {
	}

	@Override
	public void onSlideAllTheWay() {
		Db.getBillingInfo().save(this);
		startActivity(new Intent(this, HotelBookingActivity.class));
	}

	@Override
	public void onSlideAbort() {
	}
}
