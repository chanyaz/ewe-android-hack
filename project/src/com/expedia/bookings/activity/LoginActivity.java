package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.LoginFragment;
import com.expedia.bookings.fragment.LoginFragment.LoginExtender;
import com.expedia.bookings.fragment.LoginFragment.TitleSettable;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;

public class LoginActivity extends SherlockFragmentActivity implements TitleSettable {

	public static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	public static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";

	private static final String TAG_LOGIN_FRAGMENT = "TAG_LOGIN_FRAGMENT";
	private static final String STATE_TITLE = "STATE_TITLE";

	private ImageView mBgImageView;
	private View mBgShadeView;

	private LoginFragment mLoginFragment;
	private String mTitle;
	private LineOfBusiness mLob = LineOfBusiness.HOTELS;
	private LoginExtender mLoginExtender;

	public static Intent createIntent(Context context, LineOfBusiness pathMode, LoginExtender extender) {
		Intent loginIntent = new Intent(context, LoginActivity.class);
		loginIntent.putExtra(LoginActivity.ARG_PATH_MODE, pathMode.name());
		if (extender != null) {
			loginIntent.putExtra(ARG_LOGIN_FRAGMENT_EXTENDER, extender);
		}
		return loginIntent;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		mBgImageView = Ui.findView(this, R.id.background_image_view);
		mBgShadeView = Ui.findView(this, R.id.background_shade);

		// Set up theming stuff
		Intent intent = getIntent();
		if (intent.hasExtra(ARG_PATH_MODE)) {
			mLob = LineOfBusiness.valueOf(intent.getStringExtra(ARG_PATH_MODE));
		}
		if (intent.hasExtra(ARG_LOGIN_FRAGMENT_EXTENDER)) {
			mLoginExtender = intent.getParcelableExtra(ARG_LOGIN_FRAGMENT_EXTENDER);
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_TITLE)) {
				setTitle(savedInstanceState.getString(STATE_TITLE));
			}
		}

		// Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		switch (mLob) {
		case HOTELS:
			actionBar.setIcon(R.drawable.ic_logo_hotels);
			actionBar.setDisplayUseLogoEnabled(false);
			break;
		case FLIGHTS:
			actionBar.setIcon(R.drawable.ic_logo_flights);
			actionBar.setDisplayUseLogoEnabled(false);
			break;
		default:
			actionBar.setDisplayUseLogoEnabled(true);
			break;
		}
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (mTitle != null) {
			setTitle(mTitle);
		}
		else {
			setTitle(R.string.sign_in);
		}

		// Set the background (based on mode)
		if (mLob.equals(LineOfBusiness.FLIGHTS)) {
			mBgImageView.setImageBitmap(Db.getBackgroundImage(this, true));
			mBgShadeView.setBackgroundColor(getResources().getColor(R.color.login_shade_flights));
		}

		// Create/grab the login fragment
		mLoginFragment = Ui.findSupportFragment(this, TAG_LOGIN_FRAGMENT);
		if (mLoginFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			if (mLoginExtender != null) {
				mLoginFragment = LoginFragment.newInstance(mLob, mLoginExtender);
			}
			else {
				mLoginFragment = LoginFragment.newInstance(mLob);
			}

			ft.add(R.id.login_fragment_container, mLoginFragment, TAG_LOGIN_FRAGMENT);
			ft.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mTitle != null) {
			outState.putString(STATE_TITLE, mTitle);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
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
