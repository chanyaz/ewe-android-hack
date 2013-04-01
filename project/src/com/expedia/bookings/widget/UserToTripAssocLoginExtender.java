package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AssociateUserToTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.LoginFragment.LoginExtender;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender.LoginExtenderListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public class UserToTripAssocLoginExtender implements LoginExtender {

	private static final String NET_ASSOCIATE_USER_TO_TRIP = "NET_ASSOCIATE_USER_TO_TRIP";

	private View mView;
	private LoginExtenderListener mListener;
	private Context mContext;
	private String mTripId;

	public UserToTripAssocLoginExtender(String tripId) {
		mTripId = tripId;
	}

	private UserToTripAssocLoginExtender(Parcel in) {
		mTripId = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mTripId);
	}

	public static final Parcelable.Creator<UserToTripAssocLoginExtender> CREATOR = new Parcelable.Creator<UserToTripAssocLoginExtender>() {
		public UserToTripAssocLoginExtender createFromParcel(Parcel in) {
			return new UserToTripAssocLoginExtender(in);
		}

		public UserToTripAssocLoginExtender[] newArray(int size) {
			return new UserToTripAssocLoginExtender[size];
		}
	};

	@Override
	public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer) {
		mListener = listener;
		mContext = context;

		extenderContainer.removeAllViews();

		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

		setExtenderStatus(mContext.getString(R.string.loading_your_rewards_points));

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

	private Download<AssociateUserToTripResponse> mAssociateUserAndTripDownload = new Download<AssociateUserToTripResponse>() {
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
				Db.getFlightSearch().getSelectedFlightTrip().setRewardsPoints(results.getRewardsPoints());
			}

			if (mListener != null) {
				mListener.loginExtenderWorkComplete(UserToTripAssocLoginExtender.this);
			}
		}
	};

}
