package com.expedia.bookings.activity;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.fragment.FlightListFragment.FlightListFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class FlightSearchActivity extends FragmentActivity implements FlightListFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	private FlightListFragment mListFragment;

	private FlightAdapter mAdapter;

	// Search parameters
	// CURRENTLY FILLED WIH TEST PARAMS
	private Calendar mDepartureDate = new GregorianCalendar(2012, 8, 15);
	private Calendar mReturnDate = new GregorianCalendar(2012, 8, 20);
	private String mDepartureAirportCode = "MSP";
	private String mArrivalAirportCode = "SFO";
	private int mLegPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mListFragment = Ui.findOrAddSupportFragment(this, FlightListFragment.class, "listFragment");
		mAdapter = new FlightAdapter(this);
		mListFragment.setListAdapter(mAdapter);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Db.getFlightSearchResponse() == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (bd.isDownloading(DOWNLOAD_KEY)) {
				bd.registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);
			}
			else {
				Download download = new Download() {

					@Override
					public Object doDownload() {
						ExpediaServices services = new ExpediaServices(FlightSearchActivity.this);
						return services.flightSearch(mDepartureDate, mReturnDate, mDepartureAirportCode,
								mArrivalAirportCode, 0);
					}
				};

				bd.startDownload(DOWNLOAD_KEY, download, mDownloadCallback);
			}
		}
		else {
			mDownloadCallback.onDownload(Db.getFlightSearchResponse());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloads

	private OnDownloadComplete mDownloadCallback = new OnDownloadComplete() {

		@Override
		public void onDownload(Object results) {
			Log.i("Finished flights download!");

			FlightSearchResponse response = (FlightSearchResponse) results;
			Db.setFlightSearchResponse(response);
			mAdapter.setLegPosition(mLegPosition);
			mAdapter.setFlights(response);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	@Override
	public void onFlightClick(int position) {
		Intent intent = new Intent(this, FlightDetailsActivity.class);
		intent.putExtra(FlightDetailsActivity.EXTRA_STARTING_POSITION, position);
		intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, mLegPosition);
		startActivity(intent);
	}
}
