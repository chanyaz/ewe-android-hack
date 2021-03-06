package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.account.AccountView;
import com.expedia.account.AnalyticsListener;
import com.expedia.account.Config;
import com.expedia.account.PanningImageView;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AccountLibActivity extends AppCompatActivity
	implements UserAccountRefresher.IUserAccountRefreshListener, LoginExtenderListener {
	public static final String ARG_BUNDLE = "ARG_BUNDLE";
	public static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	public static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";

	@InjectView(R.id.parallax_view)
	public PanningImageView background;

	@InjectView(R.id.account_view)
	public AccountView accountView;

	@InjectView(R.id.login_extension_container)
	public LinearLayout loginExtenderContainer;

	@InjectView(R.id.extender_status)
	public TextView extenderStatus;

	private LineOfBusiness lob = LineOfBusiness.HOTELS;
	private LoginExtender loginExtender;
	private UserAccountRefresher userAccountRefresher;
	private boolean loginWithFacebook = false;
	private Listener listener = new Listener();
	private boolean isUserBucketedForSignInMessagingTest = Db.getAbacusResponse()
		.isUserBucketedForTest(AbacusUtils.EBAndroidAppSignInMessagingTest);
	private int signInMessagingTestVariate = Db.getAbacusResponse()
		.variateForTest(AbacusUtils.EBAndroidAppSignInMessagingTest);


	public static Intent createIntent(Context context, Bundle bundle) {
		Intent loginIntent = new Intent(context, AccountLibActivity.class);
		if (bundle != null) {
			loginIntent.putExtra(ARG_BUNDLE, bundle);
		}
		return loginIntent;
	}

	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, LoginExtender extender) {
		Bundle bundle = new Bundle();
		bundle.putString(AccountLibActivity.ARG_PATH_MODE, pathMode.name());
		if (extender != null) {
			bundle.putBundle(ARG_LOGIN_FRAGMENT_EXTENDER, extender.buildStateBundle());
		}
		return bundle;
	}

	@Override
	protected void onResume() {
		super.onResume();
		accountView.setListener(listener);
		accountView.setAnalyticsListener(analyticsListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (savedInstanceState == null) {
			AdTracker.trackSignInUpStarted();
		}

		Intent intent = getIntent();
		if (intent.hasExtra(ARG_BUNDLE)) {
			Bundle args = intent.getBundleExtra(ARG_BUNDLE);
			if (args.containsKey(ARG_PATH_MODE)) {
				lob = LineOfBusiness.valueOf(args.getString(ARG_PATH_MODE));
			}
			if (args.containsKey(ARG_LOGIN_FRAGMENT_EXTENDER)) {
				loginExtender = LoginExtender.buildLoginExtenderFromState(args.getBundle(ARG_LOGIN_FRAGMENT_EXTENDER));
			}
		}

		if (lob == LineOfBusiness.CARS || lob == LineOfBusiness.LX) {
			overridePendingTransition(R.anim.expand, R.anim.unexpand);
		}

		setContentView(R.layout.account_lib_activity);
		Ui.showTransparentStatusBar(this);
		ButterKnife.inject(this);

		int statusBarHeight = Ui.getStatusBarHeight(this);
		accountView.setPadding(accountView.getPaddingLeft(), statusBarHeight, accountView.getPaddingRight(),
			accountView.getPaddingBottom());

		int backgroundDrawableResId = Ui.obtainThemeResID(this, R.attr.skin_accountCreationBackgroundDrawable);
		new PicassoHelper.Builder(background).setPlaceholder(backgroundDrawableResId).build().load(
			backgroundDrawableResId);

		String signInMessage = "";
		if (signInMessagingTestVariate == AbacusUtils.HotelSignInMessagingVariate.EXCLUSIVE_MEMBER_MESSAGE.ordinal()) {
			signInMessage = getString(R.string.sign_in_messaging);
		}
		else if (signInMessagingTestVariate == AbacusUtils.HotelSignInMessagingVariate.TRIPLE_POINT_MESSAGE.ordinal()) {
			signInMessage = getString(R.string.triple_point_messaging);
		}
		else if (signInMessagingTestVariate == AbacusUtils.HotelSignInMessagingVariate.TRIP_ALERT_MESSAGE.ordinal()) {
			signInMessage = getString(R.string.trip_alert_messaging);
		}

		accountView.configure(Config.build()
				.setService(ServicesUtil.generateAccountService(this))
				.setBackgroundImageView(background)
				.setPOSEnableSpamByDefault(PointOfSale.getPointOfSale().shouldEnableMarketingOptIn())
				.setPOSShowSpamOptIn(PointOfSale.getPointOfSale().shouldShowMarketingOptIn())
				.setEnableSignInMessaging(isUserBucketedForSignInMessagingTest)
				.setSignInMessagingText(signInMessage)
				.setEnableFacebookButton(
					ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled())
				.setListener(listener)
				.setTOSText(StrUtils.generateAccountCreationLegalLink(this))
				.setMarketingText(PointOfSale.getPointOfSale().getMarketingText())
				.setAnalyticsListener(analyticsListener)
				.setFacebookAppId(getString(R.string.facebook_app_id))
		);

		userAccountRefresher = new UserAccountRefresher(this, lob, this);

		OmnitureTracking.trackLoginScreen();
	}

	@Override
	public void onBackPressed() {
		if (!accountView.back()) {
			super.onBackPressed();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Required for Facebook
		accountView.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onUserAccountRefreshed() {
		User.addUserToAccountManager(this, Db.getUser());
		if (User.isLoggedIn(this)) {
			if (loginWithFacebook) {
				OmnitureTracking.trackLoginSuccess();
			}
			AdTracker.trackLogin();
			if (loginExtender != null) {
				loginExtenderContainer.setVisibility(View.VISIBLE);
				loginExtender.onLoginComplete(this, this, loginExtenderContainer);
				return;
			}
		}

		finish();
	}

	@Override
	public void loginExtenderWorkComplete(LoginExtender extender) {
		finish();
	}

	@Override
	public void setExtenderStatus(String status) {
		extenderStatus.setText(status);
	}

	private AnalyticsListener analyticsListener = new AnalyticsListener() {
		@Override
		public void signInSucceeded() {
			OmnitureTracking.trackLoginSuccess();
			//Don't track the adtracker login here, as it happens once we fetch the profile
		}

		@Override
		public void contactsAccessRequested() {
			OmnitureTracking.trackLoginContactAccess();
		}

		@Override
		public void contactsAccessResponse(boolean b) {
			OmnitureTracking.trackAllowContactAccess(b);
		}

		@Override
		public void emailsQueried() {
			OmnitureTracking.trackLoginEmailsQueried();
		}

		@Override
		public void accountCreationAttemptWithPreexistingEmail(boolean useExisting, boolean createNew) {
			OmnitureTracking.trackEmailPrompt();
			OmnitureTracking.trackEmailPromptChoice(useExisting);
		}

		@Override
		public void userViewedNameEntering() {
			OmnitureTracking.trackLoginCreateUsername();
		}

		@Override
		public void userViewedPasswordEntering() {
			OmnitureTracking.trackLoginCreatePassword();
		}

		@Override
		public void userViewedTosPage() {
			OmnitureTracking.trackLoginTOS();
		}

		@Override
		public void userExplicitlyModifiedMarketingOptIn(boolean b) {
			OmnitureTracking.trackMarketingOptIn(b);
		}

		@Override
		public void userSucceededInCreatingAccount() {
			OmnitureTracking.trackAccountCreateSuccess();
			AdTracker.trackAccountCreated();
			//Don't track the adtracker login here, as it happens once we fetch the profile
		}

		@Override
		public void userReceivedErrorOnSignInAttempt(String s) {
			OmnitureTracking.trackAccountCreateError(s);
		}
	};

	public class Listener extends AccountView.Listener {

		@Override
		public void onSignInSuccessful() {
			loginWithFacebook = false;
			// Do stuff with User
			userAccountRefresher.ensureAccountIsRefreshed();
		}

		@Override
		public void onSignInCancelled() {
			// e.g. close this activity
			finish();
		}

		@Override
		public void onFacebookRequested() {
			loginWithFacebook = true;
		}

		@Override
		public void onForgotPassword() {
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(AccountLibActivity.this);
			builder.setUrl(PointOfSale.getPointOfSale().getForgotPasswordUrl());
			builder.setInjectExpediaCookies(true);
			builder.setTheme(R.style.HotelWebViewTheme);
			builder.setTitle(getString(R.string.title_forgot_password));
			startActivity(builder.getIntent());
		}
	}

	public interface LogInListener {
		void onLoginCompleted();
	}
}
