package com.expedia.bookings.activity;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.expedia.bookings.R;

public class TabletPreferenceActivity extends ExpediaBookingPreferenceActivity {

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
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (!isFinishing()) {
			Rect bounds = new Rect();
			getWindow().getDecorView().getHitRect(bounds);

			if (!bounds.contains((int) ev.getX(), (int) ev.getY())) {
				finish();
			}
		}

		return super.dispatchTouchEvent(ev);
	}

}
