package com.expedia.bookings.invaders;

import android.app.Activity;
import android.os.Bundle;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
@SuppressWarnings("ResourceType")
public class InvaderActivity extends Activity {

	private static final int VIEW_ID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_invaders);
	}
}
