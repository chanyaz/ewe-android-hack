package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.expedia.bookings.R;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.widget.Panel;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.server.ExpediaServices;

public class SearchActivity extends ActivityGroup {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	private static final String ACTIVITY_SEARCH_LIST = SearchListActivity.class.getCanonicalName();
	private static final String ACTIVITY_SEARCH_MAP = SearchMapActivity.class.getCanonicalName();

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	// Views

	private FrameLayout mContent;
	private EditText mSearchEditText;
	
	private Panel mPanel;
	private View mSortLayout;
	private RadioButton mSortPopularRadioButton;
	private RadioButton mSortPriceRadioButton;
	
	private ImageButton mViewButton;
	private Button mSearchButton;

	// Others

	private Context mContext = this;
	private LocalActivityManager mLocalActivityManager;
	private String mTag;
	private Intent mIntent;
	private View mLaunchedView;

	private List<SearchListener> mSearchListeners;

	private SearchParams mSearchParams;
	private Filter mFilter;

	// Threads / callbacks

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			return ExpediaServices.searchExpedia(mContext, mSearchParams);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			SearchResponse response = (SearchResponse) results;
			response.setFilter(mFilter);
			
			broadcastSearchCompleted(response);
		}
	};

	//////////////////////////////////////////////////////////////////////////////////
	// Overrides

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mLocalActivityManager = getLocalActivityManager();

		initializeViews();

		// Load both activites
		showActivity(SearchMapActivity.class);
		showActivity(SearchListActivity.class);

		setViewButtonImage();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void addSearchListener(SearchListener searchListener) {
		if (mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}

		mSearchListeners.add(searchListener);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	// Broadcast methods

	private void broadcastSearchCompleted(SearchResponse response) {
		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchCompleted(response);
			}
		}
	}

	// Other methods

	private void initializeViews() {
		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mSearchEditText = (EditText) findViewById(R.id.search_edit_text);
		
		mPanel = (Panel) findViewById(R.id.drawer_panel);
		mSortLayout = (View) findViewById(R.id.sort_layout);
		mSortPopularRadioButton = (RadioButton) findViewById(R.id.sort_popular_button);
		mSortPriceRadioButton = (RadioButton) findViewById(R.id.sort_price_button);

		mViewButton = (ImageButton) findViewById(R.id.view_button);
		mSearchButton = (Button) findViewById(R.id.search_button);

		// Listeners
		mPanel.setInterpolator(new AccelerateInterpolator());
		mViewButton.setOnClickListener(mViewButtonClickListener);
		mSearchButton.setOnClickListener(mSearchButtonClickListener);
	}

	private void setDrawerViews() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			mSortLayout.setVisibility(View.VISIBLE);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			mSortLayout.setVisibility(View.GONE);
		}

	}

	private void setViewButtonImage() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			//mViewButton.setImageResource(R.drawable.btn_map);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			//mViewButton.setImageResource(R.drawable.btn_list);
		}
	}

	private void showActivity(Class<?> activity) {
		mIntent = new Intent(this, activity);
		mTag = activity.getCanonicalName();

		final Window w = mLocalActivityManager.startActivity(mTag, mIntent);
		final View wd = w != null ? w.getDecorView() : null;
		if (mLaunchedView != wd && mLaunchedView != null) {
			if (mLaunchedView.getParent() != null) {
				mContent.removeView(mLaunchedView);
			}
		}
		mLaunchedView = wd;

		if (mLaunchedView != null) {
			mLaunchedView.setVisibility(View.VISIBLE);
			mLaunchedView.setFocusableInTouchMode(true);
			((ViewGroup) mLaunchedView).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

			mContent.addView(mLaunchedView);
		}
	}

	private void switchResultsView() {
		if (mTag.equals(ACTIVITY_SEARCH_LIST)) {
			showActivity(SearchMapActivity.class);
		}
		else if (mTag.equals(ACTIVITY_SEARCH_MAP)) {
			showActivity(SearchListActivity.class);
		}

		setViewButtonImage();
		setDrawerViews();
	}

	// Searching methods

	private void startSearch() {
		mSearchParams = new SearchParams();
		mSearchParams.setFreeformLocation(mSearchEditText.getText().toString());

		mSearchDownloader.startDownload("mykey", mSearchDownload, mSearchCallback);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listeners

	View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchResultsView();
		}
	};

	View.OnClickListener mSearchButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			startSearch();
		}
	};
}