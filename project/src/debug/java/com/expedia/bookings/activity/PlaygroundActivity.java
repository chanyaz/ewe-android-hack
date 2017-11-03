package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class PlaygroundActivity extends AppCompatActivity {

	private static final String KEY_LAYOUT_RES = "KEY_LAYOUT_RES";
	private static final String KEY_THEME_RES = "KEY_THEME_RES";

	public static Intent createIntent(Context context, @LayoutRes int layoutResId) {
		Intent intent = new Intent(context, PlaygroundActivity.class);
		addData(intent, layoutResId);
		return intent;
	}

	public static Intent addData(Intent intent, @LayoutRes int layoutResId) {
		intent.putExtra(KEY_LAYOUT_RES, layoutResId);
		return intent;
	}

	public static Intent addTheme(Intent intent, @StyleRes int styleResId) {
		intent.putExtra(KEY_THEME_RES, styleResId);
		return intent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).defaultLXComponents();
		setTheme(getIntent().getIntExtra(KEY_THEME_RES, R.style.V2_Theme_Packages));
		setContentView(getIntent().getIntExtra(KEY_LAYOUT_RES, R.layout.package_activity));
	}
}
