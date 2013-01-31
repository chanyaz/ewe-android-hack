package com.expedia.bookings.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;

public class TextViewActivity extends SherlockActivity {
	@Override
	public void onCreate(Bundle onSaveInstanceState) {
		super.onCreate(onSaveInstanceState);
		setContentView(R.layout.activity_textview);
		TextView textview = (TextView) findViewById(R.id.text);

		String content_string = getIntent().getStringExtra(Codes.CONTENT_STRING);
		textview.setText(content_string);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setLogo(R.drawable.ic_action_bar_expedia_logo);
		getSupportActionBar().setTitle("");
	}
}

