package com.expedia.bookings.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.ExpediaServicesFragment;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ExpediaServicesFragmentListener;
import com.expedia.bookings.fragment.ExpediaServicesFragment.ServiceType;
import com.expedia.bookings.fragment.TabletSearchFragment;
import com.expedia.bookings.fragment.TabletSearchFragment.SearchFragmentListener;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.fragment.base.MeasurableFragmentListener;
import com.expedia.bookings.fragment.debug.ColorFragment;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.app.SimpleProgressDialogFragment;

public class TabletLaunchActivity extends FragmentActivity implements MeasurableFragmentListener,
		SearchFragmentListener, ExpediaServicesFragmentListener {

	// On top when search params covers up everything
	private static final String BACKSTACK_SEARCH_PARAMS = "BACKSTACK_SEARCH_PARAMS";

	private MeasurableFragment mTopFragment;
	private Fragment mBottomFragment;
	private TabletSearchFragment mSearchFragment;

	// TODO: REMOVE LATER, THIS IS DEV ONLY
	// We're loading all results here right now, until we figure out where we need to load it later.
	private static final String TAG_SERVICES = "TAG_SERVICES";
	private static final String TAG_LOAD_SEARCH_DIALOG = "TAG_LOAD_SEARCH_DIALOG";
	private ExpediaServicesFragment mServicesFragment;
	private SimpleProgressDialogFragment mLoadSearchDialogFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_launch);

		getWindow().setBackgroundDrawable(null);

		FragmentManager fm = getSupportFragmentManager();
		if (savedInstanceState == null) {
			mTopFragment = ColorFragment.newInstance(Color.BLUE);
			mBottomFragment = ColorFragment.newInstance(Color.GREEN);
			mSearchFragment = new TabletSearchFragment();
			mServicesFragment = new ExpediaServicesFragment();

			FragmentTransaction ft = fm.beginTransaction();
			ft.add(R.id.top_container, mTopFragment);
			ft.add(R.id.bottom_container, mBottomFragment);
			ft.add(R.id.search_container, mSearchFragment);
			ft.add(mServicesFragment, TAG_SERVICES);
			ft.commit();
		}
		else {
			mTopFragment = Ui.findSupportFragment(this, R.id.top_container);
			mBottomFragment = Ui.findSupportFragment(this, R.id.bottom_container);
			mSearchFragment = Ui.findSupportFragment(this, R.id.search_container);
			mServicesFragment = Ui.findSupportFragment(this, TAG_SERVICES);
			mLoadSearchDialogFragment = Ui.findSupportFragment(this, TAG_LOAD_SEARCH_DIALOG);

			if (BACKSTACK_SEARCH_PARAMS.equals(getTopBackStackName())) {
				mSearchFragment.expand();
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (!mSearchFragment.onBackPressed()) {
			super.onBackPressed();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Back stack utils

	public String getTopBackStackName() {
		FragmentManager fm = getSupportFragmentManager();
		int backStackEntryCount = fm.getBackStackEntryCount();
		if (backStackEntryCount > 0) {
			return fm.getBackStackEntryAt(backStackEntryCount - 1).getName();
		}
		return "";
	}

	//////////////////////////////////////////////////////////////////////////
	// MeasureableFragmentListener

	@Override
	public void canMeasure(Fragment fragment) {
		if ((fragment == mTopFragment || fragment == mSearchFragment) && mTopFragment.isMeasurable()
				&& mSearchFragment.isMeasurable()) {
			mSearchFragment.setInitialTranslationY(mTopFragment.getView().getHeight());
			mSearchFragment.collapse();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// SearchFragmentListener

	@Override
	public void onFinishExpand() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.detach(mTopFragment);
		ft.detach(mBottomFragment);
		ft.addToBackStack(BACKSTACK_SEARCH_PARAMS);
		ft.commit();
	}

	@Override
	public void onSearch(SearchParams searchParams) {
		Db.getHotelSearch().setSearchResponse(null);
		Db.getFlightSearch().setSearchResponse(null);

		mLoadSearchDialogFragment = SimpleProgressDialogFragment.newInstance("Loading results...");
		mLoadSearchDialogFragment.show(getSupportFragmentManager(), TAG_LOAD_SEARCH_DIALOG);

		mServicesFragment.startHotelSearch(searchParams, false);
		mServicesFragment.startFlightSearch(searchParams, false);

		Db.getHotelSearch().setSearchParams(searchParams.toHotelSearchParams());
		Db.getFlightSearch().setSearchParams(searchParams.toFlightSearchParams());

		Log.i("Starting search with params: " + searchParams);
	}

	//////////////////////////////////////////////////////////////////////////
	// ExpediaServicesFragmentListener

	@Override
	public void onExpediaServicesDownload(ServiceType type, Response response) {
		switch (type) {
		case HOTEL_SEARCH:
			Db.getHotelSearch().setSearchResponse((HotelSearchResponse) response);
			break;
		case FLIGHT_SEARCH:
			Db.getFlightSearch().setSearchResponse((FlightSearchResponse) response);
			break;
		}

		// Update progress based on new results
		HotelSearchResponse hotelSearchResponse = Db.getHotelSearch().getSearchResponse();
		FlightSearchResponse flighSearchResponse = Db.getFlightSearch().getSearchResponse();

		if (hotelSearchResponse != null && !hotelSearchResponse.hasErrors() && flighSearchResponse != null
				&& !flighSearchResponse.hasErrors()) {
			// TODO: Start results activity
			mLoadSearchDialogFragment.setMessage("ALL LOADED!");
		}
		else {
			mLoadSearchDialogFragment.setMessage(checkResponse(hotelSearchResponse, "Hotel search")
					+ checkResponse(flighSearchResponse, "Flight search"));
		}
	}

	private String checkResponse(Response response, String prefix) {
		if (response == null) {
			return prefix + " response is null; ";
		}
		else if (response.hasErrors()) {
			return prefix + " response has errors; ";
		}
		else {
			return prefix + " response loaded; ";
		}
	}
}
