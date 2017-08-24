package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class UserToTripAssocLoginExtender extends LoginExtender {

	private static final String NET_ASSOCIATE_USER_TO_TRIP = "NET_ASSOCIATE_USER_TO_TRIP";
	private static final String STATE_TRIP_ID = "STATE_TRIP_ID";

	private View mView;
	private LoginExtenderListener mListener;
	private Context mContext;
	private String mTripId;

	public UserToTripAssocLoginExtender(String tripId) {
		super(null);
		mTripId = tripId;
	}

	public UserToTripAssocLoginExtender(Bundle state) {
		super(state);
	}

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		mListener = listener;
		mContext = context;

		extenderContainer.removeAllViews();
		LayoutInflater inflater = LayoutInflater.from(mContext);
		mView = inflater.inflate(R.layout.snippet_assoc_user_to_trip_loading, null);
		extenderContainer.addView(mView);

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(NET_ASSOCIATE_USER_TO_TRIP)) {
			bd.startDownload(NET_ASSOCIATE_USER_TO_TRIP, mAssociateUserAndTripDownload,
				mAssociateUserAndTripCompleteHandler);
		}
		else {
			bd.registerDownloadCallback(NET_ASSOCIATE_USER_TO_TRIP, mAssociateUserAndTripCompleteHandler);
		}

		setExtenderStatus(mContext.getString(R.string.signing_in));

	}

	@Override
	public void cleanUp() {
		mView = null;
		mListener = null;
		mContext = null;
	}

	@Override
	public void setExtenderStatus(String status) {
		if (mListener != null) {
			mListener.setExtenderStatus(status);
		}
	}

	private final Download<AssociateUserToTripResponse> mAssociateUserAndTripDownload = new Download<AssociateUserToTripResponse>() {
		@Override
		public AssociateUserToTripResponse doDownload() {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(NET_ASSOCIATE_USER_TO_TRIP, services);
			return services.associateUserToTrip(mTripId, 0);
		}
	};

	private OnDownloadComplete<AssociateUserToTripResponse> mAssociateUserAndTripCompleteHandler = new OnDownloadComplete<AssociateUserToTripResponse>() {
		@Override
		public void onDownload(AssociateUserToTripResponse results) {
			if (results != null && results.isSuccess() && !TextUtils.isEmpty(results.getRewardsPoints())) {
				Db.getTripBucket().getFlight().getFlightTrip().setRewardsPoints(results.getRewardsPoints());
			}
			else {
				Log.w("Failed to associate user to trip");
			}

			if (mListener != null) {
				mListener.loginExtenderWorkComplete(UserToTripAssocLoginExtender.this);
			}
		}
	};

	@Override
	public LoginExtenderType getExtenderType() {
		return LoginExtenderType.USER_TO_TRIP_ASSOC;
	}

	@Override
	protected Bundle getStateBundle() {
		Bundle bundle = new Bundle();
		bundle.putString(STATE_TRIP_ID, mTripId);
		return bundle;
	}

	@Override
	protected void restoreStateFromBundle(Bundle state) {
		mTripId = state.getString(STATE_TRIP_ID);
	}

}
