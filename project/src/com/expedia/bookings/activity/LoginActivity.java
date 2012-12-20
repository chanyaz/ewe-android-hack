package com.expedia.bookings.activity;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.LoginFragment;
import com.expedia.bookings.fragment.LoginFragment.PathMode;
import com.expedia.bookings.fragment.LoginFragment.TitleSettable;
import com.expedia.bookings.utils.Ui;
import com.facebook.Session;

import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

public class LoginActivity extends SherlockFragmentActivity implements TitleSettable {

	public static final String ARG_PATH_MODE = "TAG_PATH_MODE";

	private static final String TAG_LOGIN_FRAGMENT = "TAG_LOGIN_FRAGMENT";
	private static final String STATE_TITLE = "STATE_TITLE";

	private LoginFragment mLoginFragment;
	private String mTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		//Set up theming stuff
		if (this.getIntent().getStringExtra(ARG_PATH_MODE) == null)
		{
			//Default to hotels mode...
			this.getIntent().putExtra(ARG_PATH_MODE, PathMode.HOTELS.name());
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_TITLE)) {
				setTitle(savedInstanceState.getString(STATE_TITLE));
			}
		}

		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (mTitle != null) {
			setTitle(mTitle);
		}
		else {
			setTitle(getString(R.string.sign_in));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mLoginFragment = Ui.findSupportFragment(this, TAG_LOGIN_FRAGMENT);
		if (mLoginFragment == null) {
			mLoginFragment = LoginFragment.newInstance(PathMode.valueOf(getIntent().getStringExtra(ARG_PATH_MODE)));
		}
		if (!mLoginFragment.isAdded()) {
			ft.add(R.id.login_fragment_container, mLoginFragment, TAG_LOGIN_FRAGMENT);
			ft.commit();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mTitle != null) {
			outState.putString(STATE_TITLE, mTitle);
		}
	}

	@Override
	public void setActionBarTitle(String title) {
		mTitle = title;
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (mLoginFragment != null) {
				mLoginFragment.goBack();
				return true;
			}
			else {
				return super.onOptionsItemSelected(item);
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		if (mLoginFragment != null) {
			mLoginFragment.goBack();
		}
		else {
			super.onBackPressed();
		}
	}

}
