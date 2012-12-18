package com.expedia.bookings.activity;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.LoginFragment;
import com.facebook.Session;

import android.support.v4.app.FragmentActivity;
import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends FragmentActivity {

	LoginFragment frag;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		 frag = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.login_fragment);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	  super.onActivityResult(requestCode, resultCode, data);
	  Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}
	
}
