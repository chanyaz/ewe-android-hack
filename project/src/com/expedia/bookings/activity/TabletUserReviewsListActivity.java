package com.expedia.bookings.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.expedia.bookings.R;

public class TabletUserReviewsListActivity extends UserReviewsListActivity {

	private View mRootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRootView = findViewById(R.id.user_reviews_view);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isFinishing() && mRootView != null) {
			Rect bounds = new Rect();
			mRootView.getHitRect(bounds);

			// 11354: ensure to check the key press so that the reviews container is not dismissed
			// when merely scrolling
			if (ev.getAction() == MotionEvent.ACTION_UP && !bounds.contains((int) ev.getX(), (int) ev.getY())) {
				finish();
			}
		}

		return super.dispatchTouchEvent(ev);
	}
}
