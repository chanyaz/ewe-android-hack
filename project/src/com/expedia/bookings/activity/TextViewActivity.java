package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class TextViewActivity extends SherlockActivity {
	public static Intent getIntent(Context context, String data) {
		Intent intent = new Intent(context, TextViewActivity.class);
		intent.putExtra(Codes.CONTENT_STRING, data);
		return intent;
	}

	@Override
	public void onCreate(Bundle onSaveInstanceState) {
		super.onCreate(onSaveInstanceState);
		setContentView(R.layout.activity_textview);
		TextView textview = (TextView) findViewById(R.id.text);
		textview.setMovementMethod(LinkMovementMethod.getInstance());

		String content_string = getIntent().getStringExtra(Codes.CONTENT_STRING);
		textview.setText(Html.fromHtml(content_string));

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setLogo(Ui.obtainThemeResID(this, R.attr.webViewPreferencesActionBarLogo));
		getSupportActionBar().setTitle("");
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}
}
