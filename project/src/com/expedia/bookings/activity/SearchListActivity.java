package com.expedia.bookings.activity;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.HotelAdapter;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Codes;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.server.ExpediaServices;

public class SearchListActivity extends ListActivity implements SearchListener {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private SearchActivity mParent;
	private HotelAdapter mAdapter;

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_list);

		mParent = (SearchActivity) getParent();
		if (mParent == null) {
			// Testing code - this allows the SearchListActivity to run standalone as a test.
			final Context context = this;
			BackgroundDownloader downloader = BackgroundDownloader.getInstance();
			Download download = new Download() {
				@Override
				public Object doDownload() {
					SearchParams params = new SearchParams();
					params.setFreeformLocation("Minneapolis");

					return ExpediaServices.search(context, params, Codes.F_SORT_BY_DISTANCE);
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
			mParent.addSearchListner(this);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////
	// SearchListener implementation

	@Override
	public void onSearchStarted() {
		// TODO: Implement hanging doorknob

	}

	@Override
	public void onSearchProgress(int strId) {
		// TODO: Implement hanging doorknob (will it have status updates?)
	}

	@Override
	public void onSearchFailed(String message) {
		// TODO: Implement an error screen
	}

	@Override
	public void onSearchCompleted(SearchResponse response) {
		mAdapter = new HotelAdapter(this, response);
		setListAdapter(mAdapter);
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
