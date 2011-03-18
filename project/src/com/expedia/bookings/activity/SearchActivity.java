package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.mobiata.android.widget.Panel;
import com.mobiata.hotellib.app.SearchListener;

public class SearchActivity extends ActivityGroup {
	//////////////////////////////////////////////////////////////////////////////////
	// Constants

	//////////////////////////////////////////////////////////////////////////////////
	// Private members

	private FrameLayout mContent;

	private EditText mSearchEditText;
	private Panel mPanel;
	private Button mViewButton;

	private LocalActivityManager mLocalActivityManager;
	private String mTag;
	private Intent mIntent;
	private View mLaunchedView;

	private List<SearchListener> mSearchListeners;

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

		setViewButtonText();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Public methods

	public void addSearchListner(SearchListener searchListener) {
		if (mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}

		mSearchListeners.add(searchListener);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Private methods

	private void initializeViews() {
		mContent = (FrameLayout) findViewById(R.id.content_layout);
		mSearchEditText = (EditText) findViewById(R.id.search_text);
		mPanel = (Panel) findViewById(R.id.drawer);
		mViewButton = (Button) findViewById(R.id.view_button);

		mPanel.setInterpolator(new AccelerateInterpolator());
		mViewButton.setOnClickListener(mViewButtonClickListener);
	}

	private void setViewButtonText() {
		if (mTag.equals(SearchListActivity.class.getCanonicalName())) {
			mViewButton.setText("Map");
		}
		else if (mTag.equals(SearchMapActivity.class.getCanonicalName())) {
			mViewButton.setText("List");
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
		if (mTag.equals(SearchListActivity.class.getCanonicalName())) {
			showActivity(SearchMapActivity.class);
		}
		else if (mTag.equals(SearchMapActivity.class.getCanonicalName())) {
			showActivity(SearchListActivity.class);
		}

		setViewButtonText();
	}

	//////////////////////////////////////////////////////////////////////////////////
	// Listeners

	View.OnClickListener mViewButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switchResultsView();
		}
	};
}