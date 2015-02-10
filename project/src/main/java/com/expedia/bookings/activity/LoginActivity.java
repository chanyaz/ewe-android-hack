package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.LoginFragment;
import com.expedia.bookings.fragment.LoginFragment.TitleSettable;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.Ui;

public class LoginActivity extends FragmentActivity implements TitleSettable {

	public static final String ARG_BUNDLE = "ARG_BUNDLE";
	public static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	public static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";

	private static final String FRAG_TAG_IMAGE_FRAG = "FRAG_TAG_IMAGE_FRAG";

	private static final String TAG_LOGIN_FRAGMENT = "TAG_LOGIN_FRAGMENT";
	private static final String STATE_TITLE = "STATE_TITLE";

	private ViewGroup mLoginContentContainer;
	private ImageView mBgImageView;
	private View mBgShadeView;

	private LoginFragment mLoginFragment;
	private String mTitle;
	private LineOfBusiness mLob = LineOfBusiness.HOTELS;
	private LoginExtender mLoginExtender;

	/**
	 * Please don't use this. SRSLY. If you want to sign into expedia,
	 * please use User.signIn(contex, bundle).
	 * @param context
	 *
	 * @param bundle
	 * @return
	 */
	public static Intent createIntent(Context context, Bundle bundle) {
		Intent loginIntent = new Intent(context, LoginActivity.class);
		if (bundle != null) {
			loginIntent.putExtra(ARG_BUNDLE, bundle);
		}
		return loginIntent;
	}

	/**
	 * This generates the arguments bundle for LoginActivity.
	 * The Bundle generated is suitable for passing into User.signIn(context,BUNDLE)
	 *
	 * @param pathMode
	 * @param extender
	 * @return
	 */
	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, LoginExtender extender) {
		Bundle bundle = new Bundle();
		bundle.putString(LoginActivity.ARG_PATH_MODE, pathMode.name());
		if (extender != null) {
			bundle.putBundle(ARG_LOGIN_FRAGMENT_EXTENDER, extender.buildStateBundle());
		}
		return bundle;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.activity_login);

		mLoginContentContainer = Ui.findView(this, R.id.login_fragment_container);
		mBgImageView = Ui.findView(this, R.id.background_image_view);
		mBgShadeView = Ui.findView(this, R.id.background_shade);

		// Set up theming stuff
		Intent intent = getIntent();
		if (intent.hasExtra(ARG_BUNDLE)) {
			Bundle args = intent.getBundleExtra(ARG_BUNDLE);
			if (args.containsKey(ARG_PATH_MODE)) {
				mLob = LineOfBusiness.valueOf(args.getString(ARG_PATH_MODE));
			}
			if (args.containsKey(ARG_LOGIN_FRAGMENT_EXTENDER)) {
				mLoginExtender = LoginExtender.buildLoginExtenderFromState(args.getBundle(ARG_LOGIN_FRAGMENT_EXTENDER));
			}
		}

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_TITLE)) {
				setTitle(savedInstanceState.getString(STATE_TITLE));
			}
		}

		// Actionbar
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		//defaults to login
		setActionBarTitle(null);

		switch (mLob) {
		case HOTELS:
			actionBar.setIcon(Ui.obtainThemeResID(this, R.attr.skin_hotelLoginActionBarIcon));
			break;
		case FLIGHTS:
			actionBar.setIcon(Ui.obtainThemeResID(this, R.attr.skin_flightLoginActionBarIcon));
			break;
		default:
			actionBar.setDisplayUseLogoEnabled(true);
			break;
		}

		// Set the background (based on mode)
		if (mLob.equals(LineOfBusiness.FLIGHTS)) {
			final String code = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
			if (ExpediaBookingApp.useTabletInterface(this)) {
				ResultsBackgroundImageFragment frag = Ui.findSupportFragment(this, FRAG_TAG_IMAGE_FRAG);
				if (frag == null) {
					frag = ResultsBackgroundImageFragment.newInstance(code, true);
					getSupportFragmentManager().beginTransaction()
						.add(R.id.background_image_container, frag, FRAG_TAG_IMAGE_FRAG).commit();
				}
			}
			else {
				Point portrait = Ui.getPortraitScreenSize(this);
				final String url = new Akeakamai(Images.getFlightDestination(code)) //
					.resizeExactly(portrait.x, portrait.y) //
					.build();
				new PicassoHelper.Builder(mBgImageView).applyBlurTransformation(true).setPlaceholder(
					R.drawable.default_flights_background_blurred).build().load(url);
			}
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

	/**
	 * Set the actionbar title to the provided string.
	 * If supplied string is null, revert to default "Log In"
	 */
	@Override
	public void setActionBarTitle(String title) {
		mTitle = title;
		ActionBar actionBar = getActionBar();
		if (mTitle == null) {
			actionBar.setTitle(R.string.Log_In);
		}
		else {
			actionBar.setTitle(mTitle);
		}
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

	///////////////////////////////////////////////////////////////
	// OnBitmapLoaded

}
