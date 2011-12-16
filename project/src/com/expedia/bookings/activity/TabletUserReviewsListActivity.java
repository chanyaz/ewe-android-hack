package com.expedia.bookings.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

import com.expedia.bookings.R;

public class TabletUserReviewsListActivity extends UserReviewsListActivity {

	private View mRootView;

	private GestureDetector mDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRootView = findViewById(R.id.user_reviews_view);
		mDetector = new GestureDetector(this, new CloseGestureDetector());
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mDetector.onTouchEvent(ev);

		return super.dispatchTouchEvent(ev);
	}

	private class CloseGestureDetector extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapUp(MotionEvent ev) {
			if (!isFinishing() && mRootView != null) {
				Rect bounds = new Rect();
				mRootView.getHitRect(bounds);

				if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
					finish();
				}
			}

			return true;
		}
	}
}
