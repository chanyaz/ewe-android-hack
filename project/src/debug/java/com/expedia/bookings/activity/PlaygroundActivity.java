package com.expedia.bookings.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;

import com.expedia.bookings.R;

public class PlaygroundActivity extends Activity {

	private static final String KEY_LAYOUT_RES = "KEY_LAYOUT_RES";

	public static Intent createIntent(Context context, @LayoutRes int layoutResId) {
		Intent intent = new Intent(context, PlaygroundActivity.class);
		intent.putExtra(KEY_LAYOUT_RES, layoutResId);
		return intent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getIntent().getIntExtra(KEY_LAYOUT_RES, R.layout.widget_test_car_search));
	}
}
