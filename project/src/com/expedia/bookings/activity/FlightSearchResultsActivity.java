package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.expedia.bookings.R;
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

public class FlightSearchResultsActivity extends FragmentActivity implements FlightListFragmentListener {

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	private FlightListFragment mListFragment;

	private FlightAdapter mAdapter;

	// Current leg being displayed
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
						mListFragment.setHeaderDrawable(null);

						ExpediaServices services = new ExpediaServices(FlightSearchResultsActivity.this);
						return services.flightSearch(Db.getFlightSearchParams(), 0);
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

			// DELETE EVENTUALLY: For now, just set the header to always be SF
			mListFragment.setHeaderDrawable(getResources().getDrawable(R.drawable.san_francisco));
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
