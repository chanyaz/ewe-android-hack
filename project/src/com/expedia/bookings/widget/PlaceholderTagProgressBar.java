package com.expedia.bookings.widget;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlaceholderTagProgressBar {

	private LinearLayout mContainer;
	private ProgressBar mProgressBar;
	private TextView mProgressTextView;

	public PlaceholderTagProgressBar(LinearLayout container, ProgressBar progressBar, TextView progressTextView) {
		mContainer = container;
		mProgressBar = progressBar;
		mProgressTextView = progressTextView;

		container.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Devour clicks so they can't get to underlying Activities
			}
		});
	}

	public void setShowProgress(boolean show) {
		if (show) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
		else {
			mProgressBar.setVisibility(View.INVISIBLE);
		}
	}

	public void setText(int textId) {
		mProgressTextView.setText(textId);
	}

	public void setText(String text) {
		mProgressTextView.setText(text);
	}

	public void setVisibility(int gone) {
		mContainer.setVisibility(gone);
	}
}
