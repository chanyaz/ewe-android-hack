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
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.mobiata.android.Log;

/**
 * This is a non-ui fragment that makes using the FusedLocationProvider easy.
 *
 * To use it, add this fragment and then call <code>find()</code> passing in a Listener:
 * 
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
 * 
 */
public class FusedLocationProviderFragment extends Fragment implements ConnectionCallbacks, OnConnectionFailedListener {

	// Use this for the FragmentManager too
	private static final String TAG = FusedLocationProviderFragment.class.getSimpleName();

	private LocationClient mLocationClient;

	// Global variable to hold the current location
	private Location mCurrentLocation;

	private Queue<FusedLocationProviderListener> mListeners = new LinkedList<FusedLocationProviderListener>();

	//////////////////////////////////////////////////////////////////////////
	// Static methods

	/**
	 * Finds or creates a FusedLocationProviderFragment using the FragmentManager
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

		mLocationClient = new LocationClient(getActivity(), this, this);
	}

	/*
	 * Called when the Activity becomes visible.
	 */
	@Override
	public void onStart() {
		super.onStart();

		// Connect the client.
		mLocationClient.connect();
	}

	/*
	 * Called when the Activity is no longer visible.
	 */
	@Override
	public void onStop() {
		// Disconnecting the client invalidates it.
		mLocationClient.disconnect();

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
	public void onDisconnected() {
		Log.d(TAG, "onDisconnected()");
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
		if (mLocationClient == null || !mLocationClient.isConnected()) {
			return;
		}

		mCurrentLocation = mLocationClient.getLastLocation();
		while (!mListeners.isEmpty()) {
			if (mCurrentLocation == null) {
				Log.d(TAG, "location error");
				mListeners.poll().onError();
			}
			else {
				Log.d(TAG, "location found");
				mListeners.poll().onFound(mCurrentLocation);
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
