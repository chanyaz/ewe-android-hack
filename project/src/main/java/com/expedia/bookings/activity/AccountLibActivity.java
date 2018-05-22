package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import com.crashlytics.android.Crashlytics;
import com.expedia.account.AccountSignInListener;
import com.expedia.account.AccountView;
import com.expedia.account.AnalyticsListener;
import com.expedia.account.Config;
import com.expedia.account.NewAccountView;
import com.expedia.account.PanningImageView;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.services.IClientLogServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.AppStartupTimeClientLog;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.tracking.RouterToSignInTimeLogger;
import com.expedia.bookings.marketing.carnival.CarnivalUtils;
import com.expedia.bookings.utils.FeatureUtilKt;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.utils.navigation.NavUtils;
import com.expedia.bookings.widget.TextView;
import com.squareup.phrase.Phrase;
import javax.inject.Inject;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class AccountLibActivity extends AppCompatActivity implements UserAccountRefresher.IUserAccountRefreshListener, LoginExtenderListener {
	private static final String ARG_BUNDLE = "ARG_BUNDLE";
	private static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	private static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";
	private static final String ARG_INITIAL_STATE = "ARG_INITIAL_STATE";
	private static final String ARG_INITIAL_TAB = "ARG_INITIAL_TAB";

	@InjectView(R.id.parallax_view)
	public PanningImageView background;

	@InjectView(R.id.white_background)
	public View whiteBackground;

	@InjectView(R.id.account_view)
	public AccountView accountView;

	@InjectView(R.id.new_account_view)
	public NewAccountView newAccountView;

	@InjectView(R.id.login_extension_container)
	public LinearLayout loginExtenderContainer;

	@InjectView(R.id.extender_status)
	public TextView extenderStatus;

	@Inject
	RouterToSignInTimeLogger routerToSignInTimeLogger;

	@Inject
	IClientLogServices clientLogServices;

	private UserStateManager userStateManager;
	private LineOfBusiness lob = LineOfBusiness.HOTELS;
	private LoginExtender loginExtender;
	private Config.InitialState startState = Config.InitialState.SignIn;
	@VisibleForTesting
	protected NewAccountView.AccountTab initialTab = NewAccountView.AccountTab.SIGN_IN;
	private UserAccountRefresher userAccountRefresher;
	private boolean userLoggedInWithFacebook = false;
	private Listener listener = new Listener();
	private NavigationListener navigationListener = new NavigationListener();

	public static Intent createIntent(Context context, Bundle bundle) {
		Intent loginIntent = new Intent(context, AccountLibActivity.class);
		if (bundle != null) {
			loginIntent.putExtra(ARG_BUNDLE, bundle);
		}
		return loginIntent;
	}

	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, LoginExtender extender) {
		return createArgumentsBundle(pathMode, Config.InitialState.SignIn, extender);
	}

	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, Config.InitialState startState, LoginExtender extender) {
		Bundle bundle = new Bundle();
		bundle.putString(ARG_PATH_MODE, pathMode.name());
		bundle.putString(ARG_INITIAL_STATE, startState.name());
		if (extender != null) {
			bundle.putBundle(ARG_LOGIN_FRAGMENT_EXTENDER, extender.buildStateBundle());
		}
		return bundle;
	}

	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, NewAccountView.AccountTab initialTab, LoginExtender extender) {
		Bundle bundle = new Bundle();
		bundle.putString(ARG_PATH_MODE, pathMode.name());
		bundle.putString(ARG_INITIAL_TAB, initialTab.name());
		if (extender != null) {
			bundle.putBundle(ARG_LOGIN_FRAGMENT_EXTENDER, extender.buildStateBundle());
		}
		return bundle;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (routerToSignInTimeLogger.getShouldGoToSignIn()) {
			routerToSignInTimeLogger.setEndTime();
			AppStartupTimeClientLog.trackTimeLogger(routerToSignInTimeLogger, clientLogServices);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		newAccountView.cancelLoading();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Ui.getApplication(this).appComponent().inject(this);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		userStateManager = Ui.getApplication(this).appComponent().userStateManager();

		CarnivalUtils.getInstance().toggleNotifications(false);
		Intent intent = getIntent();
		if (intent.hasExtra(ARG_BUNDLE)) {
			Bundle args = intent.getBundleExtra(ARG_BUNDLE);
			if (args.containsKey(ARG_PATH_MODE)) {
				lob = LineOfBusiness.valueOf(args.getString(ARG_PATH_MODE));
			}
			if (args.containsKey(ARG_INITIAL_STATE)) {
				startState = Config.InitialState.valueOf(args.getString(ARG_INITIAL_STATE));
			}
			if (args.containsKey(ARG_LOGIN_FRAGMENT_EXTENDER)) {
				loginExtender = LoginExtender.buildLoginExtenderFromState(args.getBundle(ARG_LOGIN_FRAGMENT_EXTENDER));
			}
			if (args.containsKey(ARG_INITIAL_TAB)) {
				initialTab = NewAccountView.AccountTab.valueOf(args.getString(ARG_INITIAL_TAB));
			}
		}

		if (lob == LineOfBusiness.CARS || lob == LineOfBusiness.LX) {
			overridePendingTransition(R.anim.expand, R.anim.unexpand);
		}

		setContentView(R.layout.account_lib_activity);
		ButterKnife.inject(this);
		setupAccountViewBasedOnBucketing();

		userAccountRefresher = new UserAccountRefresher(this, lob, this);
		OmnitureTracking.trackLoginScreen();
		userLoggedInWithFacebook = false;
	}

	private void setupAccountViewBasedOnBucketing() {
		if (FeatureUtilKt.isNewSignInEnabled(this)) {
			newAccountView.setVisibility(View.VISIBLE);
			newAccountView.setNavigationOnClickListener(navigationListener);
			getWindow().setStatusBarColor(getResources().getColor(R.color.brand_primary_dark));
			newAccountView.setupConfig(buildConfigBasedOnBucketing());
			accountView.setVisibility(View.GONE);
		}
		else {
			Ui.showTransparentStatusBar(this);
			int statusBarHeight = Ui.getStatusBarHeight(this);
			accountView.setVisibility(View.VISIBLE);
			background.setVisibility(View.VISIBLE);
			accountView.setPadding(accountView.getPaddingLeft(), statusBarHeight, accountView.getPaddingRight(), accountView.getPaddingBottom());
			int backgroundDrawableResId = R.drawable.bg_account_creation;
			new PicassoHelper.Builder(background)
					.setPlaceholder(backgroundDrawableResId)
					.build()
					.load(backgroundDrawableResId);
			accountView.setWhiteBackgroundFromActivity(whiteBackground);
			accountView.configure(buildConfigBasedOnBucketing());
			newAccountView.setVisibility(View.GONE);
		}
	}

	private Config buildConfigBasedOnBucketing() {
		Config config;
		if (FeatureUtilKt.isNewSignInEnabled(this)) {
			config = Config.build()
					.setInitialTab(initialTab)
					.setService(ServicesUtil.generateAccountService(this))
					.setPOSEnableSpamByDefault(false)
					.setPOSShowSpamOptIn(PointOfSale.getPointOfSale().shouldShowMarketingOptIn())
					.setEnableFacebookButton(
							ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled())
					.setListener(listener)
					.setMarketingText(PointOfSale.getPointOfSale().getMarketingText())
					.setAnalyticsListener(analyticsListener)
					.setFacebookAppId(getString(R.string.facebook_app_id))
					.setNewTermsText(StrUtils.generateNewTermsRewardLegalLink(this));
		}
		else {
			config = Config.build()
					.setService(ServicesUtil.generateAccountService(this))
					.setBackgroundImageView(background)
					.setPOSEnableSpamByDefault(PointOfSale.getPointOfSale().shouldEnableMarketingOptIn())
					.setPOSShowSpamOptIn(PointOfSale.getPointOfSale().shouldShowMarketingOptIn())
					.setEnableFacebookButton(
							ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled())
					.setListener(listener)
					.setTOSText(StrUtils.generateAccountCreationLegalLink(this))
					.setMarketingText(PointOfSale.getPointOfSale().getMarketingText())
					.setAnalyticsListener(analyticsListener)
					.setFacebookAppId(getString(R.string.facebook_app_id))
					.setInitialState(startState)
					.setUserRewardsEnrollmentCheck(ProductFlavorFeatureConfiguration.getInstance().showUserRewardsEnrollmentCheck())
					.setRewardsText(StrUtils.generateLoyaltyRewardsLegalLink(this))
					.setSignupString(Phrase.from(this, R.string.account_signup_TEMPLATE).put("brand", BuildConfig.brand).format().toString())
					.setNewTermsText(StrUtils.generateNewTermsRewardLegalLink(this));
		}
		if (shouldEnableRecaptcha()) {
			config.setEnableRecaptcha(true).setRecaptchaAPIKey(getString(R.string.recaptcha_sdk_site_key));
		}
		return config;
	}

	private boolean shouldEnableRecaptcha() {
		return ProductFlavorFeatureConfiguration.getInstance().isRecaptchaEnabled()
				&& isRecaptchaSatelliteEnabled() && isRecaptchaABTestEnabled() && !ExpediaBookingApp.isAutomation();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		CarnivalUtils.getInstance().toggleNotifications(true);
	}

	@Override
	public void onBackPressed() {
		if (FeatureUtilKt.isNewSignInEnabled(this)) {
			if (newAccountView.isOnSignInPage()) {
				super.onBackPressed();
			}
			else {
				newAccountView.cancelFacebookLinkAccountsView();
			}
		}
		else {
			if (!accountView.back()) {
				super.onBackPressed();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Required for Facebook
		if (FeatureUtilKt.isNewSignInEnabled(this)) {
			newAccountView.onActivityResult(requestCode, resultCode, data);
		}
		else {
			accountView.onActivityResult(requestCode, resultCode, data);
		}
	}

	// TODO - talk to Mohit (as he is the tracking dude) about this. Doesn't seem right
	@Override
	public void onUserAccountRefreshed() {
		User user = userStateManager.getUserSource().getUser();
		userStateManager.addUserToAccountManager(user);

		if (userStateManager.isUserAuthenticated()) {
			if (userLoggedInWithFacebook) {
				OmnitureTracking.trackSignInSuccess();
				Db.sharedInstance.setSignInType(Db.SignInTypeEnum.FACEBOOK_SIGN_IN);
			}
			else {
				Db.sharedInstance.setSignInType(Db.SignInTypeEnum.BRAND_SIGN_IN);
			}
			AdTracker.trackLogin();
			if (loginExtender != null) {
				loginExtenderContainer.setVisibility(View.VISIBLE);
				loginExtender.onLoginComplete(this, this, loginExtenderContainer);
				return;
			}

			if (user != null) {
				CarnivalUtils.getInstance().setUserInfo(user.getTuidString(),user.getPrimaryTraveler().getEmail());
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

	@VisibleForTesting
	public AnalyticsListener analyticsListener = new AnalyticsListener() {

		@Override
		public void signInButtonClicked() {
			OmnitureTracking.trackSignInButtonClicked();
		}

		@Override
		public void facebookSignInButtonClicked() {
			OmnitureTracking.trackFacebookSignInClicked();
		}

		@Override
		public void createButtonClicked() {
			OmnitureTracking.trackCreateAccountButtonClicked();
		}

		@Override
		public void newSignInTabClicked() {
			OmnitureTracking.trackSignInTabClicked();
		}

		@Override
		public void newCreateAccountTabClicked() {
			OmnitureTracking.trackCreateAccountTabClicked();
		}

		@Override
		public void signInSucceeded() {
			OmnitureTracking.trackSignInSuccess();
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
		public void userViewedSinglePage() {
			OmnitureTracking.trackSinglePage();
		}

		@Override
		public void userExplicitlyModifiedMarketingOptIn(boolean b) {
			OmnitureTracking.trackMarketingOptIn(b);
		}

		@Override
		public void userSucceededInCreatingAccount() {
			OmnitureTracking.trackAccountCreateSuccess();
		}

		@Override
		public void userReceivedErrorOnSignInAttempt(String s) {
			OmnitureTracking.trackSignInError(s);
		}

		@Override
		public void userReceivedErrorOnAccountCreationAttempt(String failureReason) {
			OmnitureTracking.trackAccountCreationError(failureReason);
		}
	};

	public class NavigationListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}

	public class Listener implements AccountSignInListener {

		@Override
		public void onSignInSuccessful() {
			// Do stuff with User
			userAccountRefresher.forceAccountRefresh();

			Intent intent = getIntent();

			if (intent != null) {
				if (intent.hasExtra(ARG_BUNDLE)) {
					if (intent.getBundleExtra(ARG_BUNDLE).containsKey(Codes.DEALS)) {
						NavUtils.goToMemberPricing(getBaseContext());
					}
				}
			}
		}

		@Override
		public void onFacebookSignInSuccess() {
			userLoggedInWithFacebook = true;
		}

		@Override
		public void onSignInCancelled() {
			// e.g. close this activity
			finish();
		}

		@Override
		public void onFacebookRequested() {
		}

		@Override
		public void onForgotPassword() {
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(AccountLibActivity.this);
			builder.setUrl(PointOfSale.getPointOfSale().getForgotPasswordUrl());
			builder.setInjectExpediaCookies(true);
			builder.setTitle(getString(R.string.title_forgot_password));
			startActivity(builder.getIntent());
		}

		@Override
		public void onRecaptchaError(Throwable e) {
			((ExpediaBookingApp)getBaseContext().getApplicationContext()).setCrashlyticsMetadata();
			Crashlytics.logException(e);
		}
	}

	private boolean isRecaptchaSatelliteEnabled() {
		// Returns true if satellite enabled OR if satellite config isn't valid. (i.e. "fail on")
		return SatelliteFeatureConfigManager.isABTestEnabled(this, AbacusUtils.EBAndroidAppAccountRecaptcha.getKey())
				|| !SatelliteFeatureConfigManager.configValid(this);
	}

	private boolean isRecaptchaABTestEnabled() {
		// Returns true if AB test enabled or abacus is missing the response. (i.e. "fail on")
		return AbacusFeatureConfigManager.isBucketedForTest(this, AbacusUtils.EBAndroidAppAccountRecaptcha)
				||	Db.sharedInstance.getAbacusResponse().testForKey(AbacusUtils.EBAndroidAppAccountRecaptcha) == null;
	}

}
