package com.expedia.bookings.activity;

import android.content.Context;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.server.ExpediaServices;

public class SearchMapActivity extends MapActivity implements SearchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;

	private MapView mMapView;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_map);

		// Configure the map
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setSatellite(false);

		mParent = (SearchActivity) getParent();
		if (mParent == null) {
			// Testing code - this allows the SearchMapActivity to run standalone as a test.
			final Context context = this;
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			Download download = new Download() {
				@Override
				public Object doDownload() {
					SearchParams params = new SearchParams();
					params.setFreeformLocation("Minneapolis");

					return ExpediaServices.searchExpedia(context, params);
				}
			};

			OnDownloadComplete callback = new OnDownloadComplete() {
				@Override
				public void onDownload(Object results) {
					onSearchCompleted((SearchResponse) results);
				}
			};

			downloader.startDownload("mykey", download, callback);
		}
		else {
			mParent.addSearchListener(this);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		clearResults();
	}

	@Override
	public void onSearchProgress(int strId) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchFailed(String message) {
		// Do nothing.  SearchActivity should handle the display of search progress.
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean hasSearchResults() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearResults() {
		// TODO Auto-generated method stub

	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

}
