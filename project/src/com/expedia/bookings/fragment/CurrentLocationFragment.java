package com.expedia.bookings.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class CurrentLocationFragment extends Fragment
	implements ExpediaServicesFragment.ExpediaServicesFragmentListener,
	FusedLocationProviderFragment.FusedLocationProviderListener,
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	public interface ICurrentLocationListener {
		public void onCurrentLocation(Location location, SuggestionV2 suggestion);
	}

	private static final String FTAG_LOCATION = "FTAG_LOCATION";
	private static final String FTAG_LOCATION_SUGGEST = "FTAG_LOCATION_SUGGEST";

	//Frags
	private FusedLocationProviderFragment mLocationFragment;
	private LocationSuggestionsDownloadFragment mLocationSuggestionFragment;

	//Location
	private static final long LOCATION_EXPIRATION_MS = 1000 * 60 * 15;//15 minutes
	private Location mLastLocation;
	private SuggestionV2 mLocationSuggestion;
	private long mLastLocationTime;

	private boolean mFetchOnResume = false;

	private ICurrentLocationListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, ICurrentLocationListener.class);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mFetchOnResume) {
			getCurrentLocation();
			mFetchOnResume = false;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLocationFragment != null) {
			mLocationFragment.stop();
		}
	}

	public void getCurrentLocation() {
		if (getActivity() == null) {
			mFetchOnResume = true;
		}
		else {
			if (needFreshLocation()) {
				fetchLocation();
			}
			else {
				mListener.onCurrentLocation(mLastLocation, mLocationSuggestion);
			}
		}
	}


	/**
	 * FRAGMENT PROVIDER
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_LOCATION) {
			return mLocationFragment;
		}
		else if (tag == FTAG_LOCATION_SUGGEST) {
			return mLocationSuggestionFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_LOCATION) {
			return new FusedLocationProviderFragment();
		}
		else if (tag == FTAG_LOCATION_SUGGEST) {
			return new LocationSuggestionsDownloadFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_LOCATION) {
			((FusedLocationProviderFragment) frag).find(this);
		}
	}

	/**
	 * LOCATION SEARCH
	 */
	protected void fetchLocation() {
		if (getActivity() == null) {
			mFetchOnResume = true;
		}
		else {
			setLocationFragAttached(true);
		}
	}

	protected boolean needFreshLocation() {
		if (mLocationSuggestion == null || mLastLocation == null
			|| System.currentTimeMillis() - mLastLocationTime > LOCATION_EXPIRATION_MS) {
			return true;
		}
		return false;
	}

	private void setLocationFragAttached(boolean attached) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mLocationFragment = FragmentAvailabilityUtils.setFragmentAvailability(attached, FTAG_LOCATION, manager,
			transaction, this, 0, true);
		transaction.commit();
	}

	private void setServicesFragmentAttached(boolean attached) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mLocationSuggestionFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(attached, FTAG_LOCATION_SUGGEST, manager,
				transaction, this, 0, true);
		transaction.commit();
	}


	protected void onLocationSuggestion(SuggestionV2 suggestion) {
		if (suggestion != null) {
			mLocationSuggestion = suggestion;
			mListener.onCurrentLocation(mLastLocation, mLocationSuggestion);
		}
		else {
			Log.e("onLocationSuggestion received a null suggestion");
		}
	}


	@Override
	public void onFound(final Location currentLocation) {
		if (currentLocation != null) {
			setLocationFragAttached(false);

			mLastLocation = currentLocation;
			mLastLocationTime = System.currentTimeMillis();
			mLocationSuggestion = null;

			setServicesFragmentAttached(true);
			mLocationSuggestionFragment.setLatLon(currentLocation.getLatitude(), currentLocation.getLongitude());
			mLocationSuggestionFragment.startOrRestart();
		}
		else {
			//TODO: Try to fetch location again or something...
			Log.e("onLocation received a null location");
		}
	}

	@Override
	public void onError() {
		Log.e("Fused Location Provider - onError()");
	}

	@Override
	public void onExpediaServicesDownload(ExpediaServicesFragment.ServiceType type, Response response) {
		if (type == ExpediaServicesFragment.ServiceType.SUGGEST_NEARBY) {
			if (response != null && !response.hasErrors() && response instanceof SuggestionResponse) {
				SuggestionResponse suggestResponse = (SuggestionResponse) response;
				if (suggestResponse.getSuggestions() != null && suggestResponse.getSuggestions().size() > 0) {
					onLocationSuggestion(suggestResponse.getSuggestions().get(0));
				}
				else {
					Log.e("Suggestion for nearby search returned no results");
				}
			}
			else {
				Log.e(
					"Suggestion for nearby search returned a null response, an error response, or the response is not a SuggestionResponse");
			}
		}
	}

}
