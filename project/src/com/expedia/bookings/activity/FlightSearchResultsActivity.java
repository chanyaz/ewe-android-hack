package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.fragment.FlightListFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightAdapter.FlightAdapterListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class FlightSearchResultsActivity extends FragmentActivity implements FlightAdapterListener {

	public static final String EXTRA_LEG_POSITION = "EXTRA_LEG_POSITION";

	private static final String DOWNLOAD_KEY = "com.expedia.bookings.flights";

	private FlightListFragment mListFragment;

	// Current leg being displayed
	private int mLegPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLegPosition = getIntent().getIntExtra(EXTRA_LEG_POSITION, 0);

		mListFragment = Ui.findOrAddSupportFragment(this, FlightListFragment.class, "listFragment");

		// Configure the title based on which leg the user is selecting
		FlightSearchParams params = Db.getFlightSearch().getSearchParams();
		int titleStrId = (mLegPosition == 0) ? R.string.outbound_TEMPLATE : R.string.inbound_TEMPLATE;
		String airportCode = (mLegPosition == 0) ? params.getArrivalAirportCode() : params.getDepartureAirportCode();
		setTitle(getString(titleStrId, FlightStatsDbUtils.getAirport(airportCode).mCity));
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (Db.getFlightSearch().getSearchResponse() == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (bd.isDownloading(DOWNLOAD_KEY)) {
				mListFragment.showProgress();
				bd.registerDownloadCallback(DOWNLOAD_KEY, mDownloadCallback);
			}
			else {
				Download<FlightSearchResponse> download = new Download<FlightSearchResponse>() {
					@Override
					public FlightSearchResponse doDownload() {
						mListFragment.setHeaderDrawable(null);
						mListFragment.showProgress();

						ExpediaServices services = new ExpediaServices(FlightSearchResultsActivity.this);
						return services.flightSearch(Db.getFlightSearch().getSearchParams(), 0);
					}
				};

				bd.startDownload(DOWNLOAD_KEY, download, mDownloadCallback);
			}
		}
		else {
			mDownloadCallback.onDownload(Db.getFlightSearch().getSearchResponse());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(DOWNLOAD_KEY);
	}

	//////////////////////////////////////////////////////////////////////////
	// Downloads

	private OnDownloadComplete<FlightSearchResponse> mDownloadCallback = new OnDownloadComplete<FlightSearchResponse>() {

		@Override
		public void onDownload(FlightSearchResponse response) {
			Log.i("Finished flights download!");

			FlightSearch search = Db.getFlightSearch();
			search.setSearchResponse(response);

			if (response.hasErrors()) {
				mListFragment.showError(getString(R.string.error_loading_flights_TEMPLATE, response.getErrors().get(0)
						.getPresentableMessage(FlightSearchResultsActivity.this)));
			}
			else if (response.getTripCount() == 0) {
				mListFragment.showError(getString(R.string.error_no_flights_found));
			}
			else {
				mListFragment.setLegPosition(mLegPosition);
				mListFragment.setFlights(search.getTrips(mLegPosition));

				// DELETE EVENTUALLY: For now, just set the header to always be SF
				mListFragment.setHeaderDrawable(getResources().getDrawable(R.drawable.san_francisco));
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// FlightAdapterListener

	@Override
	public void onDetailsClick(FlightTrip trip, FlightLeg leg, int position) {
		// TODO: This should probably not be based on array position/leg position.
		Intent intent = new Intent(this, FlightDetailsActivity.class);
		intent.putExtra(FlightDetailsActivity.EXTRA_STARTING_POSITION, position);
		intent.putExtra(FlightDetailsActivity.EXTRA_LEG_POSITION, mLegPosition);
		startActivity(intent);
	}

	@Override
	public void onSelectClick(FlightTrip trip, FlightLeg leg, int position) {
		// TODO: Implement selecting a leg
		Toast.makeText(this, "TODO: Implement select button press", Toast.LENGTH_SHORT).show();
	}

}
