package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.expedia.account.AccountView;
import com.expedia.account.AnalyticsListener;
import com.expedia.account.Config;
import com.expedia.account.PanningImageView;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.TextView;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

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

	/** Facebook garbage **/

	@InjectView(R.id.login_status_textview)
	public TextView statusText;

	@InjectView(R.id.facebook_button_container)
	public LinearLayout faceBookLinkContainer;

	@InjectView(R.id.link_password_edit_text)
	public EditText linkPassword;

	@InjectView(R.id.user_denied_permission_email_message)
	public TextView userDeniedPermissionEmailMessage;

	@InjectView(R.id.facebook_email_denied_container)
	public LinearLayout facebookEmailDeniedContainer;

	@OnClick(R.id.link_accounts_button)
	public void onLinkFacebook() {
		// waiting for accountlib to support facebook
	}

	@OnClick(R.id.cancel_link_accounts_button)
	public void onCancelLinkFacebook() {
		// waiting for accountlib to support facebook
	}

	@OnClick(R.id.try_facebook_again)
	public void onTryFacebookAgain() {
		// waiting for accountlib to support facebook
	}

	@OnClick(R.id.try_facebook_again_cancel)
	public void onTryFacebookAgainCancel() {
		// waiting for accountlib to support facebook
	}

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
		accountView.setPadding(accountView.getPaddingLeft(), statusBarHeight, accountView.getPaddingRight(), accountView.getPaddingBottom());
		facebookEmailDeniedContainer.setPadding(facebookEmailDeniedContainer.getPaddingLeft(), statusBarHeight, facebookEmailDeniedContainer.getPaddingRight(), facebookEmailDeniedContainer.getPaddingBottom());
		faceBookLinkContainer.setPadding(faceBookLinkContainer.getPaddingLeft(), statusBarHeight, faceBookLinkContainer.getPaddingRight(), faceBookLinkContainer.getPaddingBottom());

		int backgroundDrawableResId = Ui.obtainThemeResID(this, R.attr.skin_accountCreationBackgroundDrawable);
		new PicassoHelper.Builder(background).setPlaceholder(backgroundDrawableResId).build().load(backgroundDrawableResId);

		accountView.configure(Config.build()
				.setEndpoint(Ui.getApplication(this).appComponent().okHttpClient(),
					Ui.getApplication(this).appComponent().endpointProvider().getE3EndpointUrl())
				.setSiteId(PointOfSale.getPointOfSale().getSiteId())
				.setLangId(PointOfSale.getPointOfSale().getDualLanguageId())
				.setBackgroundImageView(background)
				.setPOSEnableSpamByDefault(PointOfSale.getPointOfSale().shouldEnableMarketingOptIn())
				.setPOSShowSpamOptIn(PointOfSale.getPointOfSale().shouldShowMarketingOptIn())
				.setEnableFacebookButton(
					ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled())
				.setClientId(ServicesUtil.generateClientId(this))
				.setListener(listener)
				.setTOSText(StrUtils.generateAccountCreationLegalLink(this))
				.setMarketingText(PointOfSale.getPointOfSale().getMarketingText())
				.setAnalyticsListener(analyticsListener)
		);

		userAccountRefresher = new UserAccountRefresher(this, lob, this);

		linkPassword.setHint(Phrase.from(this, R.string.brand_password_hint_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.format());

		userDeniedPermissionEmailMessage.setText(
			Phrase.from(this, R.string.user_denied_permission_email_message_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format());

		FontCache.setTypeface(statusText, FontCache.Font.ROBOTO_REGULAR);
		OmnitureTracking.trackLoginScreen();
	}

	@Override
	public void onBackPressed() {
		if (!accountView.back()) {
			super.onBackPressed();
		}
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
