package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
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
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.ServicesUtil;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.TextView;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class AccountLibActivity extends AppCompatActivity
	implements UserAccountRefresher.IUserAccountRefreshListener, LoginExtenderListener {
	public static final String ARG_BUNDLE = "ARG_BUNDLE";
	public static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	public static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";

	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private static final String NET_LINK_NEW_USER = "NET_LINK_NEW_USER";
	private static final String NET_LINK_EXISTING_USER = "NET_LINK_EXISTING_USER";
	private static final String DIALOG_LOADING = "DIALOG_LOADING";

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
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(NET_LINK_EXISTING_USER)) {
			setLoadingText(getString(R.string.linking_your_accounts));
			setIsLoading(true);
			bd.startDownload(NET_LINK_EXISTING_USER, mFbLinkExistingUserDownload,
				mFbLinkExistingUserHandler);
		}
	}

	@OnClick(R.id.cancel_link_accounts_button)
	public void onCancelLinkFacebook() {

		BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);

		//goto previous state...
		showLinkFacebook(false);
	}

	@OnClick(R.id.try_facebook_again)
	public void onTryFacebookAgain() {
		// Do facebook things!!!
		Session currentSession = Session.getActiveSession();
		List<String> permissions = new ArrayList<String>();
		permissions.add("email");
		Session.NewPermissionsRequest request = new Session.NewPermissionsRequest(this, permissions);
		currentSession.requestNewReadPermissions(request);
	}

	@OnClick(R.id.try_facebook_again_cancel)
	public void onTryFacebookAgainCancel() {
		Session.setActiveSession(null);
		showEmailDenied(false);
	}

	private String mFbUserId;
	private String mFbUserEmail;
	private String mFbUserName;
	private ThrobberDialog mLoadingFragment;

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
	protected void onPause() {
		super.onPause();
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().removeCallback(mFacebookStatusCallback);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		accountView.setListener(listener);
		accountView.setAnalyticsListener(analyticsListener);
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().addCallback(mFacebookStatusCallback);
		}
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
			doFacebookLogin();
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}

	/** Facebook garbage, will remove with Account Lib v2 **/

	/**
	 * Login with facebook.
	 * <p/>
	 * This uses the facebook app if it is installed.
	 * If the fb app isn't installed it should use a webpage.
	 */
	protected void doFacebookLogin() {
		Log.d("FB: doFacebookLogin");
		setIsLoading(true);
		setLoadingText(getString(R.string.fetching_facebook_info));
		setStatusText(R.string.Sign_in_with_Facebook, true);

		// start Facebook Login
		Session currentSession = Session.getActiveSession();
		if (currentSession == null || currentSession.getState().isClosed()) {
			Session session = new Session.Builder(this).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		if (!currentSession.isOpened()) {
			Log.d("FB: doFacebookLogin - !currentSession.isOpened()");
			Session.OpenRequest openRequest = new Session.OpenRequest(this);

			//We need an email address to do any sort of Expedia account creation/linking
			List<String> permissions = new ArrayList<String>();
			permissions.add("email");

			if (openRequest != null) {
				openRequest.setPermissions(permissions);
				currentSession.addCallback(mFacebookStatusCallback);
				currentSession.openForRead(openRequest);
			}
		}
		else {
			Log.d("FB: doFacebookLogin - currentSession.isOpened()");
			if (hasRequiredInfoFromFB(currentSession)) {
				fetchFacebookUserInfo(currentSession);
			}
			else {
				setFBEmailDeniedState();
			}
		}

	}

	private void setFBEmailDeniedState() {
		setIsLoading(false);
		setStatusText(R.string.user_denied_permission_email_heading, true);
		showEmailDenied(true);
	}

	private boolean hasRequiredInfoFromFB(Session session) {
		if (session.isPermissionGranted("email")) {
			return true;
		}
		return false;
	}

	/**
	 * Ok so we have a users facebook session, but we need the users information for that to be useful so lets get that
	 *
	 * @param session
	 */
	protected void fetchFacebookUserInfo(Session session) {
		Log.d("FB: fetchFacebookUserInfo");

		// make request to the /me API
		Request.newMeRequest(session, new Request.GraphUserCallback() {

			// callback after Graph API response with user object
			@Override
			public void onCompleted(GraphUser user, Response response) {
				Log.d("FB: executeMeRequestAsync response:" + response.toString());
				if (user != null && response.getError() == null) {

					Log.d("FB: executeMeRequestAsync response - user != null && response.getError() == null");
					setFbUserVars(user);
					setStatusTextFbInfoLoaded(mFbUserName);
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_AUTO_LOGIN)) {
						bd.startDownload(NET_AUTO_LOGIN, mFbLinkAutoLoginDownload, mFbLinkAutoLoginHandler);
					}
				}
				else {
					Log.d("FB: executeMeRequestAsync response - user == null || response.getError() != null");
					setStatusText(R.string.unable_to_sign_into_facebook, false);
					setIsLoading(false);
				}
			}
		}).executeAsync();
	}

	/**
	 * Facebook returns us stuff, here is where we determine what that stuff means
	 *
	 * @param session
	 * @param state
	 * @param exception
	 */
	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		Log.d("FB: handleFacebookResponse", exception);
		if (session == null || state == null || exception != null
			|| state.equals(SessionState.CLOSED)
			|| state.equals(SessionState.CLOSED_LOGIN_FAILED)) {
			setStatusText(R.string.unable_to_sign_into_facebook, false);
			goBack();
		}
		else if (session.isOpened()) {
			fetchFacebookUserInfo(session);
		}
		else {
			Log.d("FB: handleFacebookResponse - else");
		}

	}

	/**
	 * When the facebook login status changes, this gets called
	 */
	Session.StatusCallback mFacebookStatusCallback = new Session.StatusCallback() {
		// callback when session changes state
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			handleFacebookResponse(session, state, exception);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse> mFbLinkAutoLoginHandler = new BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse>() {
		@Override
		public void onDownload(FacebookLinkResponse results) {
			if (results != null && results.getFacebookLinkResponseCode() != null) {
				Log.d("onDownload: mFbLinkAutoLoginHandler:" + results.getFacebookLinkResponseCode().name());
				if (results.isSuccess()) {
					userAccountRefresher.ensureAccountIsRefreshed();
				}
				else if (results.getFacebookLinkResponseCode()
					.compareTo(FacebookLinkResponse.FacebookLinkResponseCode.nofbdatafound) == 0 && TextUtils
					.isEmpty(mFbUserEmail)) {
					setFBEmailDeniedState();
				}
				else {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_LINK_NEW_USER)) {
						//setLoadingText(R.string.linking_your_accounts);
						setIsLoading(true);
						bd.startDownload(NET_LINK_NEW_USER, mFbLinkNewUserDownload, mFbLinkNewUserHandler);
					}
				}
			}
			else {
				//TODO:Better error message
				setStatusText(R.string.unspecified_error, false);
				setIsLoading(false);
			}
		}
	};

	/**
	 * Create a new user based on facebook creds
	 */
	private final BackgroundDownloader.Download<FacebookLinkResponse> mFbLinkNewUserDownload = new BackgroundDownloader.Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkNewUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
				return null;
			}

			setLoadingText(getString(R.string.attempting_to_sign_in_with_facebook));
			ExpediaServices services = new ExpediaServices(AccountLibActivity.this);
			return services.facebookLinkNewUser(mFbUserId, fbSession.getAccessToken(), mFbUserEmail);
		}
	};


	/**
	 * This attmpts to hand our facebook info to expedia and tries to auto login based on that info.
	 * This will only succeed if the user has at some point granted Expedia access to fbconnect.
	 */
	private final BackgroundDownloader.Download<FacebookLinkResponse> mFbLinkAutoLoginDownload = new BackgroundDownloader.Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkAutoLoginDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
			}

			ExpediaServices services = new ExpediaServices(AccountLibActivity.this);
			return services.facebookAutoLogin(mFbUserId, fbSession.getAccessToken());
		}
	};

	protected void setFbUserVars(GraphUser user) {
		setFbUserVars(user.getName(), user.getId(), user.getProperty("email") == null ? null : user
			.getProperty("email").toString());
	}

	protected void setFbUserVars(String fbUserName, String fbUserId, String fbUserEmail) {
		this.mFbUserName = fbUserName;
		this.mFbUserId = fbUserId;
		this.mFbUserEmail = fbUserEmail;
	}

	protected void setIsLoading(boolean loading) {
		String message = getString(R.string.fetching_facebook_info);
		ThrobberDialog ldf = (ThrobberDialog) getSupportFragmentManager().findFragmentByTag(DIALOG_LOADING);
		if (loading) {
			if (ldf == null) {
				ldf = ThrobberDialog.newInstance(message);
			}
			else {
				ldf.setText(message);
			}
			if (!ldf.isAdded()) {
				ldf.show(getSupportFragmentManager(), DIALOG_LOADING);
			}
			mLoadingFragment = ldf;
		}
		else {
			if (ldf != null) {
				ldf.dismiss();
			}
			if (mLoadingFragment != null) {
				mLoadingFragment.dismiss();
			}
		}
	}

	private final BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse> mFbLinkNewUserHandler = new BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse>() {
		@Override
		public void onDownload(FacebookLinkResponse results) {
			if (results != null && results.getFacebookLinkResponseCode() != null) {
				Log.d("onDownload: mFbLinkNewUserHandler");
				if (results.isSuccess()) {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_AUTO_LOGIN)) {
						bd.startDownload(NET_AUTO_LOGIN, mFbLinkAutoLoginDownload, mFbLinkAutoLoginHandler);
					}
				}
				else if (results.getFacebookLinkResponseCode()
					.compareTo(FacebookLinkResponse.FacebookLinkResponseCode.existing) == 0 ||
					results.getFacebookLinkResponseCode()
						.compareTo(FacebookLinkResponse.FacebookLinkResponseCode.loginFailed) == 0) {
					setStatusTextExpediaAccountFound(mFbUserName);
					setIsLoading(false);
				}
				else {
					//TODO:Better error message
					setStatusText(R.string.unspecified_error, false);
					setIsLoading(false);
				}
			}
			else {
				//TODO:Better error message
				setStatusText(R.string.unspecified_error, false);
				setIsLoading(false);
			}
		}
	};

	/**
	 * This is for associating a facebook account with an existing expedia account
	 */
	private final BackgroundDownloader.Download<FacebookLinkResponse> mFbLinkExistingUserDownload = new BackgroundDownloader.Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkExistingUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
				return null;
			}

			setLoadingText(getString(R.string.linking_your_accounts));
			String expediaPw = linkPassword.getText().toString();
			ExpediaServices services = new ExpediaServices(AccountLibActivity.this);
			return services.facebookLinkExistingUser(mFbUserId, fbSession.getAccessToken(), mFbUserEmail, expediaPw);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse> mFbLinkExistingUserHandler = new BackgroundDownloader.OnDownloadComplete<FacebookLinkResponse>() {
		@Override
		public void onDownload(FacebookLinkResponse results) {
			if (results != null && results.getFacebookLinkResponseCode() != null) {
				Log.d("onDownload: mFbLinkExistingUserHandler");
				if (results.isSuccess()) {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_AUTO_LOGIN)) {
						bd.startDownload(NET_AUTO_LOGIN, mFbLinkAutoLoginDownload, mFbLinkAutoLoginHandler);
					}
				}
				else if (results.getFacebookLinkResponseCode().compareTo(
					FacebookLinkResponse.FacebookLinkResponseCode.loginFailed) == 0) {
					setStatusText(R.string.sign_in_failed_try_again, false);
					showLinkFacebook(true);
					setIsLoading(false);
				}
				else {
					//TODO: Something...
					setStatusText(R.string.unspecified_error, false);
					setIsLoading(false);
				}
			}
			else {
				//TODO:Better error message
				setStatusText(R.string.unspecified_error, false);
				setIsLoading(false);
			}
		}
	};

	public void goBack() {
		//Cancel all the current downloads....
		BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().removeCallback(mFacebookStatusCallback);
		}

		setIsLoading(false);
	}

	protected void setStatusText(int resId, boolean isHeading) {
		String str = getString(resId);
		setStatusText(str, isHeading);
	}

	private void setStatusText(String text, boolean isHeading) {
		Snackbar.make(accountView, Html.fromHtml(text).toString(), Snackbar.LENGTH_LONG).show();
	}

	protected void setLoadingText(final String text) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mLoadingFragment != null) {
					mLoadingFragment.setText(text);
				}
			}
		};
		runOnUiThread(runner);
	}

	protected void setStatusTextExpediaAccountFound(String name) {
		String str = String.format(
			Phrase.from(this, R.string.facebook_weve_found_your_account_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.put("name", name).format().toString());
		statusText.setText(Html.fromHtml(str).toString());
		showLinkFacebook(true);
	}

	protected void setStatusTextFbInfoLoaded(String name) {
		String str = String.format(getString(R.string.facebook_weve_fetched_your_info), name);
		setStatusText(str, false);
	}

	private void showLinkFacebook(boolean visible) {
		accountView.setVisibility(visible ? View.GONE : View.VISIBLE);
		faceBookLinkContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	private void showEmailDenied(boolean visible) {
		accountView.setVisibility(visible ? View.GONE : View.VISIBLE);
		facebookEmailDeniedContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
	}
}
