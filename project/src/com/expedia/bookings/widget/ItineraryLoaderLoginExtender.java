package com.expedia.bookings.widget;

import java.util.Collection;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.ItineraryManager.ItinerarySyncListener;
import com.expedia.bookings.data.trips.ItineraryManager.SyncError;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.fragment.LoginExtender;
import com.expedia.bookings.fragment.LoginExtenderListener;
import com.mobiata.android.util.Ui;

public class ItineraryLoaderLoginExtender extends LoginExtender implements ItinerarySyncListener {

	private View mView;
	private ProgressBar mProgress;
	private ViewGroup mErrorContainer;
	private Button mRetryButton;
	private TextView mErrorMessage;
	private LoginExtenderListener mListener;
	private Context mContext;

	public ItineraryLoaderLoginExtender() {
		super(null);
	}

	public ItineraryLoaderLoginExtender(Bundle state) {
		super(state);
	}

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		mListener = listener;
		mContext = context;
		extenderContainer.removeAllViews();

		LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(R.layout.snippet_itin_progress_message_retry, null);
		mProgress = Ui.findView(mView, R.id.itinerary_loading_progress);
		mErrorContainer = Ui.findView(mView, R.id.error_container);
		mRetryButton = Ui.findView(mView, R.id.no_trips_try_again_button);
		mErrorMessage = Ui.findView(mView, R.id.no_trips_error_message);

		mRetryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ItineraryManager.getInstance().startSync(true);
				if (mProgress != null) {
					mProgress.setVisibility(View.VISIBLE);
				}
				if (mErrorContainer != null) {
					mErrorContainer.setVisibility(View.GONE);
				}
			}

		});

		ItineraryManager.getInstance().addSyncListener(this);
		ItineraryManager.getInstance().startSync(true);
		setExtenderStatus(mContext.getString(R.string.fetching_your_itinerary));

		extenderContainer.addView(mView);
	}

	@Override
	public void cleanUp() {
		ItineraryManager.getInstance().removeSyncListener(this);
		mView = null;
		mProgress = null;
		mErrorContainer = null;
		mRetryButton = null;
		mErrorMessage = null;
		mListener = null;
		mContext = null;
	}

	@Override
	public void setExtenderStatus(String status) {
		if (mListener != null) {
			mListener.setExtenderStatus(status);
		}

	}

	/////////////////////////////////
	//ITIN SYNC

	@Override
	public void onTripAdded(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripUpdated(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripUpdateFailed(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTripRemoved(Trip trip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSyncFailure(final SyncError error) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				mProgress.setVisibility(View.GONE);
				mErrorContainer.setVisibility(View.VISIBLE);
				mErrorMessage.setText(R.string.itinerary_fetch_error);
			}
		};
		mView.post(runner);
	}

	@Override
	public void onSyncFinished(Collection<Trip> trips) {
		if (mListener != null) {
			mListener.loginExtenderWorkComplete(ItineraryLoaderLoginExtender.this);
		}
		ItineraryManager.getInstance().removeSyncListener(this);
	}

	@Override
	public LoginExtenderType getExtenderType() {
		return LoginExtenderType.ITINERARY_LOADER;
	}

	@Override
	protected Bundle getStateBundle() {
		//No real state
		return new Bundle();
	}

	@Override
	protected void restoreStateFromBundle(Bundle state) {
		//No real state
	}

}
