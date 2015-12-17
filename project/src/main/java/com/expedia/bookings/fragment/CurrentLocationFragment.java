package com.expedia.bookings.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.interfaces.IExpediaServicesListener;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class CurrentLocationFragment extends Fragment
	implements IExpediaServicesListener,
	FusedLocationProviderFragment.FusedLocationProviderListener,
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	//Errors for reporting to the listener
	public static final int ERROR_LOCATION_SERVICE = 1;//Service reports an error.
	public static final int ERROR_LOCATION_DATA = 2;//We get back bad/missing data from location frag
	public static final int ERROR_SUGGEST_SERVICE = 4;//Networking service failure
	public static final int ERROR_SUGGEST_DATA = 8;//Invalid data returned from suggest service

	//Frag tags
	private static final String FTAG_CURLOCFRAG_LOCATION = "FTAG_CURLOCFRAG_LOCATION";
	private static final String FTAG_CURLOCFRAG_LOC_SUG = "FTAG_CURLOCFRAG_LOC_SUG";


	public interface ICurrentLocationListener {
		void onCurrentLocation(Location location, SuggestionV2 suggestion);

		void onCurrentLocationError(int errorCode);
	}

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
	public void onAttach(Context context) {
		super.onAttach(context);
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
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		if (FTAG_CURLOCFRAG_LOCATION.equals(tag)) {
			return mLocationFragment;
		}
		else if (FTAG_CURLOCFRAG_LOC_SUG.equals(tag)) {
			return mLocationSuggestionFragment;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (FTAG_CURLOCFRAG_LOCATION.equals(tag)) {
			return new FusedLocationProviderFragment();
		}
		else if (FTAG_CURLOCFRAG_LOC_SUG.equals(tag)) {
			return new LocationSuggestionsDownloadFragment();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FTAG_CURLOCFRAG_LOCATION.equals(tag)) {
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
		manager.executePendingTransactions();
		FragmentTransaction transaction = manager.beginTransaction();
		mLocationFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(attached, FTAG_CURLOCFRAG_LOCATION, manager,
				transaction, this, 0, true);
		transaction.commit();
	}

	private void setServicesFragmentAttached(boolean attached) {
		FragmentManager manager = getChildFragmentManager();
		manager.executePendingTransactions();
		FragmentTransaction transaction = manager.beginTransaction();
		mLocationSuggestionFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(attached, FTAG_CURLOCFRAG_LOC_SUG, manager,
				transaction, this, 0, true);
		transaction.commit();
	}


	protected void onLocationSuggestion(SuggestionV2 suggestion) {
		if (suggestion != null) {
			mLocationSuggestion = suggestion;
			mLocationSuggestion.setResultType(SuggestionV2.ResultType.CURRENT_LOCATION);
			mListener.onCurrentLocation(mLastLocation, mLocationSuggestion);
		}
		else {
			Log.e("onLocationSuggestion received a null suggestion");
			mListener.onCurrentLocationError(ERROR_SUGGEST_DATA);
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
			Log.e("onLocation received a null location");
			mListener.onCurrentLocationError(ERROR_LOCATION_DATA);
		}
	}

	@Override
	public void onError() {
		Log.e("Fused Location Provider - onError()");
		mListener.onCurrentLocationError(ERROR_LOCATION_SERVICE);
	}

	@Override
	public void onExpediaServicesDownload(ServiceType type, Response response) {
		if (type == ServiceType.SUGGEST_NEARBY) {
			if (response != null && !response.hasErrors() && response instanceof SuggestionResponse) {
				SuggestionResponse suggestResponse = (SuggestionResponse) response;
				if (suggestResponse.getSuggestions() != null && suggestResponse.getSuggestions().size() > 0) {
					onLocationSuggestion(suggestResponse.getSuggestions().get(0));
				}
				else {
					Log.e("Suggestion for nearby search returned no results");
					mListener.onCurrentLocationError(ERROR_SUGGEST_DATA);
				}
			}
			else {
				Log.e(
					"Suggestion for nearby search returned a null response, an error response, or the response is not a SuggestionResponse");
				mListener.onCurrentLocationError(ERROR_SUGGEST_SERVICE);
			}
		}
	}
}
