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
		
		if(mRootView != null) {
			Rect bounds = new Rect();
			mRootView.getHitRect(bounds);
			if(!bounds.contains((int) ev.getX(), (int) ev.getY())) {
				finish();
			}
		}
		return super.dispatchTouchEvent(ev);
	}
}
