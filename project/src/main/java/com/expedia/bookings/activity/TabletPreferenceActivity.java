package com.expedia.bookings.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;

public class TabletPreferenceActivity extends ExpediaBookingPreferenceActivity {

	private GestureDetector mDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_tablet_preferences);

		Button button = (Button) findViewById(R.id.ok_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

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
			if (!isFinishing()) {
				Rect bounds = new Rect();
				getWindow().getDecorView().getHitRect(bounds);

				if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
					finish();
				}
			}

			return true;
		}
	}

}
