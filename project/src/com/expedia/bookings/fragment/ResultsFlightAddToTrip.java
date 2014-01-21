package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightAddToTrip: The add to trip fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightAddToTrip extends Fragment {

	public static ResultsFlightAddToTrip newInstance() {
		ResultsFlightAddToTrip frag = new ResultsFlightAddToTrip();
		return frag;
	}

	// Views
	private ViewGroup mRootC;
	private ViewGroup mAddToTripRowC;
	private ViewGroup mAddingToTripLoadingC;

	private FlightLegSummarySectionTablet mFlightCard;

	private IAddToTripListener mAddToTripListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_flight_add_to_trip, null);
		mAddToTripRowC = Ui.findView(mRootC, R.id.add_to_trip_row);
		mAddingToTripLoadingC = Ui.findView(mRootC, R.id.add_to_trip_loading_message_container);
		mFlightCard = Ui.findView(mRootC, R.id.flight_row);
		return mRootC;
	}

	public Rect getRowRect() {
		return ScreenPositionUtils.getGlobalScreenPosition(mAddToTripRowC);
	}

	public void beginOrResumeAddToTrip() {
		mAddToTripListener.beginAddToTrip("FLIGHTS", getRowRect(), Color.TRANSPARENT);
		mAddToTripRowC.setVisibility(View.VISIBLE);
		mAddingToTripLoadingC.setVisibility(View.VISIBLE);
		bindFlightCard();
		doAddToTripDownloadStuff();
	}

	private void bindFlightCard() {
		mFlightCard.bindForTripBucket(Db.getFlightSearch(), true);
	}

	/**
	 * ADD TO TRIP DOWNLOAD....
	 */
	// NOTE THIS IS JUST A PLACEHOLDER SO THAT WE GET THE FLOW IDEA
	private Runnable mDownloadRunner;

	private void doAddToTripDownloadStuff() {
		if (mDownloadRunner == null) {
			mDownloadRunner = new Runnable() {
				@Override
				public void run() {
					if (getActivity() != null) {
						mAddToTripListener.performTripHandoff();
						mAddToTripRowC.setVisibility(View.INVISIBLE);
						mAddingToTripLoadingC.setVisibility(View.INVISIBLE);
					}
					mDownloadRunner = null;
				}
			};
			mRootC.postDelayed(mDownloadRunner, 2000);
		}
	}

}
