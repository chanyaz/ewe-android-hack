package com.expedia.bookings.activity;

import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.BookingOverviewFragment;
import com.expedia.bookings.fragment.BookingOverviewFragment.BookingOverviewFragmentListener;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class BookingOverviewActivity extends SherlockFragmentActivity implements BookingOverviewFragmentListener,
		SignInFragmentListener {

	public static final String STATE_TAG_LOADED_DB_INFO = "STATE_TAG_LOADED_DB_INFO";

	//We only want to load from disk once: when the activity is first started
	private boolean mLoadedDbInfo = false;

	private BookingOverviewFragment mBookingOverviewFragment;
	private MenuItem mCheckoutMenuItem;

	// To make up for a lack of FLAG_ACTIVITY_CLEAR_TASK in older Android versions
	private ActivityKillReceiver mKillReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mLoadedDbInfo = savedInstanceState.getBoolean(STATE_TAG_LOADED_DB_INFO, false);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				loadCachedData();
			}
		}).start();

		mKillReceiver = new ActivityKillReceiver(this);
		mKillReceiver.onCreate();

		setContentView(R.layout.activity_booking_overview);

		mBookingOverviewFragment = (BookingOverviewFragment) getSupportFragmentManager().findFragmentById(
				R.id.booking_overview_fragment);
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
		if (mBookingOverviewFragment.getInCheckout()) {
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
			mCheckoutMenuItem.setVisible(!mBookingOverviewFragment.getInCheckout());
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			if (mBookingOverviewFragment.getInCheckout()) {
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

	@Override
	public void onSaveInstanceState(Bundle out) {
		super.onSaveInstanceState(out);
		out.putBoolean(STATE_TAG_LOADED_DB_INFO, mLoadedDbInfo);
	}

	// Private methods

	private void loadCachedData() {
		if (!mLoadedDbInfo) {

			Db.loadBillingInfo(this);
			BillingInfo billingInfo = Db.getBillingInfo();

			//Load billing info (only if we don't have a valid card already)
			if (billingInfo == null || TextUtils.isEmpty(billingInfo.getNumber())) {
				billingInfo.load(this);
				StoredCreditCard stored = billingInfo.getStoredCard();
				if (stored != null) {
					if (User.isLoggedIn(this)) {
						if (Db.getUser() == null) {
							Db.loadUser(this);
						}
						List<StoredCreditCard> usrCards = Db.getUser().getStoredCreditCards();
						boolean cardFound = false;
						for (int i = 0; i < usrCards.size(); i++) {
							if (stored.getId().compareTo(usrCards.get(i).getId()) == 0) {
								cardFound = true;
								break;
							}
						}
						//If the storedcard is not part of the user's collection of stored cards, we can't use it
						if (!cardFound) {
							Db.resetBillingInfo();
						}
					}
					else {
						//If we have an expedia account card, but we aren't logged in, we get rid of it
						Db.resetBillingInfo();
					}
				}
			}

			//Load traveler info (only if we don't have traveler info already)
			if (Db.getTravelers() == null || Db.getTravelers().size() == 0 || !Db.getTravelers().get(0).hasName()) {
				Db.loadTravelers(this);
				List<Traveler> travelers = Db.getTravelers();
				if (travelers != null && travelers.size() > 0) {
					if (User.isLoggedIn(this)) {
						//If we are logged in, we need to ensure that any expedia account users are associated with the currently logged in account
						if (Db.getUser() == null) {
							Db.loadUser(this);
						}
						List<Traveler> userTravelers = Db.getUser().getAssociatedTravelers();
						for (int i = 0; i < travelers.size(); i++) {
							Traveler trav = travelers.get(i);
							if (trav.hasTuid()) {
								boolean travFound = false;
								for (int j = 0; j < userTravelers.size(); j++) {
									Traveler usrTrav = userTravelers.get(j);
									if (usrTrav.getTuid().compareTo(trav.getTuid()) == 0) {
										travFound = true;
										break;
									}
								}
								if (!travFound) {
									travelers.set(i, new Traveler());
								}
							}
						}
					}
					else {
						//Remove logged in travelers (because the user is not logged in)
						for (int i = 0; i < travelers.size(); i++) {
							Traveler trav = travelers.get(i);
							if (trav.hasTuid()) {
								travelers.set(i, new Traveler());
							}
						}
					}
				}
			}

			if (mBookingOverviewFragment != null) {
				mBookingOverviewFragment.refreshData();
			}

			//We only load from disk once
			mLoadedDbInfo = true;
		}
	}

	// SignInFragmentListener implementation

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
}
