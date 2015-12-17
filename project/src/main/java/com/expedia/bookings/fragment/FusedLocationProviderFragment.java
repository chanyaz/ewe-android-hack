package com.expedia.bookings.fragment;

import java.util.LinkedList;
import java.util.Queue;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.expedia.bookings.utils.Ui;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.mobiata.android.Log;

/**
 * This is a non-ui fragment that makes using the FusedLocationProvider easy.
 * <p/>
 * To use it, add this fragment and then call <code>find()</code> passing in a Listener:
 * <p/>
 * <pre>
 *   mLocationFragment = FusedLocationProviderFragment.getInstance(this);
 *   ...
 *   mLocationFragment.find(new Listener() {
 *       public void onFound(Location currentLocation) {
 *           //TODO: something with the current location
 *       }
 *       public void onError(Location lastKnownLocation) {
 *           //TODO: something to indicate error
 *       }
 *   });
 * </pre>
 */
public class FusedLocationProviderFragment extends Fragment implements
	GoogleApiClient.ConnectionCallbacks,
	GoogleApiClient.OnConnectionFailedListener {

	// Use this for the FragmentManager too
	private static final String TAG = FusedLocationProviderFragment.class.getSimpleName();

	private GoogleApiClient mGoogleApiClient;

	private Queue<FusedLocationProviderListener> mListeners = new LinkedList<>();

	//////////////////////////////////////////////////////////////////////////
	// Static methods

	/**
	 * Finds or creates a FusedLocationProviderFragment using the FragmentManager
	 *
	 * @return
	 */
	public static FusedLocationProviderFragment getInstance(Fragment host) {
		FusedLocationProviderFragment frag = Ui.findSupportFragment(host, FusedLocationProviderFragment.TAG);
		if (frag == null) {
			frag = new FusedLocationProviderFragment();
			FragmentTransaction ft = host.getFragmentManager().beginTransaction();
			ft.add(frag, FusedLocationProviderFragment.TAG);
			ft.commit();
		}
		return frag;
	}

	/**
	 * Finds or creates a FusedLocationProviderFragment using the FragmentManager
	 *
	 * @return
	 */
	public static FusedLocationProviderFragment getInstance(FragmentActivity host) {
		FusedLocationProviderFragment frag = Ui.findSupportFragment(host, FusedLocationProviderFragment.TAG);
		if (frag == null) {
			frag = new FusedLocationProviderFragment();
			FragmentTransaction ft = host.getSupportFragmentManager().beginTransaction();
			ft.add(frag, FusedLocationProviderFragment.TAG);
			ft.commit();
		}
		return frag;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGoogleApiClient = new GoogleApiClient.Builder(getActivity(), this, this)
			.addApi(LocationServices.API)
			.build();
	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Connect the client.
		mGoogleApiClient.connect();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	public void onStop() {
		// Disconnecting the client invalidates it.
		mGoogleApiClient.disconnect();

		super.onStop();
	}

	//////////////////////////////////////////////////////////////////////////
	// ConnectionCallbacks

	@Override
	public void onConnected(Bundle connectionHint) {
		Log.d(TAG, "onConnected(" + connectionHint + ")");
		deliverLocation();
	}

	@Override
	public void onConnectionSuspended(int i) {
		Log.d(TAG, "onConnectionSuspended()");
	}

	//////////////////////////////////////////////////////////////////////////
	// OnConnectionFailedListener

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.w(TAG, "onConnectionFailed(" + result + ")");
	}

	//////////////////////////////////////////////////////////////////////////
	// Finally, just get the current location

	public void find(FusedLocationProviderListener listener) {
		Log.d(TAG, "getCurrentLocation");
		mListeners.add(listener);
		deliverLocation();
	}

	public void stop() {
		mListeners.clear();
	}

	private synchronized void deliverLocation() {
		if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
			return;
		}

		Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
		while (!mListeners.isEmpty()) {
			if (location == null) {
				Log.d(TAG, "location error");
				mListeners.poll().onError();
			}
			else {
				Log.d(TAG, "location found");
				mListeners.poll().onFound(location);
			}
		}
	}

	public boolean isLocationEnabled() {
		return true;
		//TODO: is it ever false sometimes? This replaces LocationServices.areProvidersEnabled
	}

	//////////////////////////////////////////////////////////////////////////
	// For asynchronous callbacks

	public interface FusedLocationProviderListener {
		public void onFound(Location currentLocation);

		public void onError();
	}
}
