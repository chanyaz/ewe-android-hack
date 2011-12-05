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
			int cushion_for_container = getResources().getInteger(R.integer.cushion_for_reviews_container);
			Rect bounds = new Rect();
			mRootView.getHitRect(bounds);
			
			// 11354: add cushion so that reviews are not mistakenly
			// or easily dismissed
			bounds.bottom += cushion_for_container;
			bounds.top -= cushion_for_container;
			bounds.left -= cushion_for_container;
			bounds.right += cushion_for_container;
			
			if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
				finish();
			}
		}

		return super.dispatchTouchEvent(ev);
	}
}
