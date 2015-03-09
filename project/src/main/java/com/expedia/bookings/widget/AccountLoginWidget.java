package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FacebookLinkResponse.FacebookLinkResponseCode;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.Ui;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This fragment is for logging into expedia accounts via the standard method AND facebook connect.
 * <p/>
 * It should be noted that the facebook connect expedia api is totally undocumented, and totally useless in the case of errors.
 * In fact, the expedia fbconnect apis return nothing more than success or failure, and it is up to us to make due.
 * <p/>
 * The general workflow for FBConnect is as follows:
 * - Login with facebook (or used stored token)
 * - Attempt auto login (with fb token)
 * - If autologin succeeds we are logged in, hooray!
 * - If autologin fails
 * -- Attempt to create expedia account with fb credentials (e.g. their facebook email)
 * -- If creating an account succeeds then we try to autologin again.
 * -- If creating an expedia account fails
 * ---- We assume here that it is because an account with that email already exists, so we let them enter a password and associate the existing account
 * ---- If associating fails we let them try again, however since our api response is mostly worthless we sort of just get stuck here.
 */
public class AccountLoginWidget extends ExpandableCardView implements LoginExtenderListener,
	AccountButtonV2.AccountButtonClickListener {

	private static final String NET_MANUAL_LOGIN = "NET_MANUAL_SIGNIN";
	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private static final String NET_LINK_NEW_USER = "NET_LINK_NEW_USER";
	private static final String NET_LINK_EXISTING_USER = "NET_LINK_EXISTING_USER";
	private static final String NET_LOG_IN = "NET_LOG_IN";

	private static final String DIALOG_LOADING = "DIALOG_LOADING";

	private static final int ANIM_BUTTON_FLIP_DURATION = 200;

	private LoginExtender mLoginExtender;
	private LogInStatusListener mLogInStatusListener;

	//UI ELEMENTS
	private ViewGroup mExpediaSigninContainer;
	private ViewGroup mOrFacebookContainer;
	private ViewGroup mSigninWithExpediaButtonContainer;
	private ViewGroup mFacebookSigninContainer;
	private ViewGroup mFacebookButtonContainer;
	private ViewGroup mLoginExtenderContainer;
	private ViewGroup mFacebookEmailDeniedContainer;
	private LinearLayout mOuterContainer;

	private TextView mStatusMessageTv;
	private Button mLogInWithFacebookBtn;
	private Button mSignInWithExpediaBtn;
	private TextView mForgotYourPasswordTv;
	private Button mLinkAccountsBtn;
	private Button mCancelLinkAccountsBtn;
	private Button mTryFacebookAgain;
	private Button mTryFacebookAgainCancel;

	private EditText mExpediaUserName;
	private EditText mExpediaPassword;
	private EditText mLinkPassword;

	private AccountButtonV2 mAccountButton;

	private ThrobberDialog mLoadingFragment;

	//ANIMATION
	private Semaphore mButtonToggleSemaphore = new Semaphore(1);
	private Animator mLastButtonToggleAnimator;
	private float mLastButtonTogglePercentage = -1f;

	//STATE
	private String mFbUserId;
	private String mFbUserEmail;
	private String mFbUserName;
	private String mStatusText;//Text next to expedia icon
	private String mLoadingText;//Loading spinner text
	private String mStatusTextContent; //Text cached when loaded
	private boolean mIsLoading = false;
	private boolean mEmptyUsername = true;
	private boolean mEmptyPassword = true;
	private boolean mDoLoginExtenderWork = false;
	private VisibilityState mVisibilityState = VisibilityState.SIGN_IN;
	private LineOfBusiness mLob = LineOfBusiness.HOTELS;
	private boolean mFacebookExpectingClose = false;//If we are logging out

	// Boolean for OmnitureTracking related purposes. false means user logged in manually
	private boolean loginWithFacebook = false;

	public AccountLoginWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	private enum VisibilityState {
		FACEBOOK_LINK,
		EXPEDIA_WTIH_FB_BUTTON,
		EXPEDIA_WITH_EXPEDIA_BUTTON,
		LOGGED_IN,
		FACEBOOK_EMAIL_DENIED,
		SIGN_IN
	}

	@InjectView(R.id.login_text)
	TextView loginText;

	@InjectView(R.id.login_container)
	ScrollView loginContainer;

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.login_widget, this);
		ButterKnife.inject(this);
		View v = this;

		mOuterContainer = Ui.findView(v, R.id.outer_container);
		mExpediaSigninContainer = Ui.findView(v, R.id.expedia_signin_container);
		mOrFacebookContainer = Ui.findView(v, R.id.or_facebook_container);
		mSigninWithExpediaButtonContainer = Ui.findView(v, R.id.log_in_with_expedia_button_container);
		mFacebookSigninContainer = Ui.findView(v, R.id.facebook_signin_container);
		mFacebookButtonContainer = Ui.findView(v, R.id.facebook_button_container);
		mLoginExtenderContainer = Ui.findView(v, R.id.login_extension_container);
		mFacebookEmailDeniedContainer = Ui.findView(v, R.id.facebook_email_denied_container);

		mStatusMessageTv = Ui.findView(v, R.id.login_status_textview);
		mLogInWithFacebookBtn = Ui.findView(v, R.id.log_in_with_facebook_btn);
		mSignInWithExpediaBtn = Ui.findView(v, R.id.log_in_with_expedia_btn);
		mForgotYourPasswordTv = Ui.findView(v, R.id.forgot_your_password_link);
		mLinkAccountsBtn = Ui.findView(v, R.id.link_accounts_button);
		mCancelLinkAccountsBtn = Ui.findView(v, R.id.cancel_link_accounts_button);
		mExpediaUserName = Ui.findView(v, R.id.username_edit_text);
		mExpediaPassword = Ui.findView(v, R.id.password_edit_text);
		mLinkPassword = Ui.findView(v, R.id.link_password_edit_text);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mTryFacebookAgain = Ui.findView(v, R.id.try_facebook_again);
		mTryFacebookAgainCancel = Ui.findView(v, R.id.try_facebook_again_cancel);

		mExpediaUserName.setOnFocusChangeListener(this);
		mExpediaPassword.setOnFocusChangeListener(this);

		FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mLogInWithFacebookBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mSignInWithExpediaBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mForgotYourPasswordTv, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mTryFacebookAgain, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mTryFacebookAgainCancel, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mLinkAccountsBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mCancelLinkAccountsBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mExpediaUserName, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mExpediaPassword, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mLinkPassword, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.or_tv, Font.ROBOTO_LIGHT);

		if (Session.getActiveSession() != null) {
			Session.getActiveSession().addCallback(mFacebookStatusCallback);
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(NET_MANUAL_LOGIN)) {
			bd.registerDownloadCallback(NET_MANUAL_LOGIN, mManualLoginCallback);
		}
		if (bd.isDownloading(NET_AUTO_LOGIN)) {
			bd.registerDownloadCallback(NET_AUTO_LOGIN, mFbLinkAutoLoginHandler);
		}
		if (bd.isDownloading(NET_LINK_EXISTING_USER)) {
			bd.registerDownloadCallback(NET_LINK_EXISTING_USER, mFbLinkExistingUserHandler);
		}
		if (bd.isDownloading(NET_LINK_NEW_USER)) {
			bd.registerDownloadCallback(NET_LINK_NEW_USER, mFbLinkNewUserHandler);
		}
		if (bd.isDownloading(NET_LOG_IN)) {
			bd.registerDownloadCallback(NET_LOG_IN, mLoginHandler);
		}

		if (mDoLoginExtenderWork) {
			doLoginExtenderWork();
		}

		setupView();
	}

	public void setupView() {

		initOnClicks();

		if (User.isLoggedIn(getContext())) {
			if (Db.getUser() == null) {
				Db.loadUser(getContext());
			}

			if (!mDoLoginExtenderWork && Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
				&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				//We have a user (either from memory, or loaded from disk)
				mAccountButton.bind(false, true, Db.getUser(), LineOfBusiness.FLIGHTS);
				mVisibilityState = VisibilityState.LOGGED_IN;
			}
		}

		setVisibilityState(mVisibilityState, false);
	}


	private void loginWorkComplete() {
		User.addUserToAccountManager(getContext(), Db.getUser());

		if (mLogInStatusListener != null) {
			mLogInStatusListener.onLoginCompleted();
		}
		if (mLoginExtender != null) {
			doLoginExtenderWork();
		}
	}

	public void doLoginExtenderWork() {
		mDoLoginExtenderWork = true;
		setEditTextsEnabled(false);
		setLoginExtenderEnabled(true, false);
		mLoginExtenderContainer.setVisibility(View.VISIBLE);
		if (User.isLoggedIn(getContext()) && Db.getUser() != null) {
			mLoginExtender.onLoginComplete(getContext(), this, mLoginExtenderContainer);
		}
		else {
			//If we arent logged in, then our extender is considered complete
			loginExtenderWorkComplete(mLoginExtender);
		}
	}

	@Override
	public void loginExtenderWorkComplete(LoginExtender extender) {

	}

	@Override
	public void setExtenderStatus(String status) {
		setStatusText(status, true);
	}

	private void initOnClicks() {

		mSignInWithExpediaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				initiateLoginWithExpedia();
			}
		});

		final String forgotPwdUrl = PointOfSale.getPointOfSale().getForgotPasswordUrl();
		final String anchorTag = String.format("<a href=\"%s\">%s</a>", forgotPwdUrl, getContext().getString(
			R.string.forgot_your_password));

		mForgotYourPasswordTv.setText(Html.fromHtml(anchorTag));
		mForgotYourPasswordTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Context context = getContext();
				if (mLob.equals(LineOfBusiness.FLIGHTS)) {
					OmnitureTracking.trackLinkFlightCheckoutLoginForgot(context);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginForgot(context);
				}

				// Open link in the app's webview instead of default browser.
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
				builder.setUrl(forgotPwdUrl);
				builder.setInjectExpediaCookies(true);
				builder.setTheme(R.style.HotelWebViewTheme);
				builder.setTitle(getContext().getString(R.string.title_forgot_password));
				getContext().startActivity(builder.getIntent());
			}
		});

		final TextWatcher usernameWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				mEmptyUsername = TextUtils.isEmpty(s);
				mSignInWithExpediaBtn.setEnabled(!(mEmptyUsername || mEmptyPassword));

				if (ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled()) {
					if (mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WTIH_FB_BUTTON)) {
						setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, true);
					}
					else if (!mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON)) {
						setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON, true);
					}
				}
			}
		};
		mExpediaUserName.addTextChangedListener(usernameWatcher);

		if (ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled()) {
			mLinkPassword.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable arg0) {
					if (arg0.length() > 0) {
						mLinkAccountsBtn.setEnabled(true);
					}
					else {
						mLinkAccountsBtn.setEnabled(false);
					}
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}

				@Override
				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				}
			});

			mTryFacebookAgain.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					//Cancel regular login download if it is happening...
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (bd.isDownloading(NET_MANUAL_LOGIN)) {
						bd.cancelDownload(NET_MANUAL_LOGIN);
					}

					// Do facebook things!!!
					loginWithFacebook = true;
					Session currentSession = Session.getActiveSession();
					List<String> permissions = new ArrayList<String>();
					permissions.add("email");
					Session.NewPermissionsRequest request = new Session.NewPermissionsRequest((Activity) getContext(),
						permissions);
					currentSession.requestNewReadPermissions(request);
				}
			});

			mTryFacebookAgainCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					Session.setActiveSession(null);
					setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, false);
				}
			});

			mLogInWithFacebookBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//Cancel regular login download if it is happening...
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (bd.isDownloading(NET_MANUAL_LOGIN)) {
						bd.cancelDownload(NET_MANUAL_LOGIN);
					}

					// Do facebook things!!!
					loginWithFacebook = true;

					doFacebookLogin();
				}
			});

			mLinkAccountsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_LINK_EXISTING_USER)) {
						setLoadingText(R.string.linking_your_accounts);
						setIsLoading(true);
						bd.startDownload(NET_LINK_EXISTING_USER, mFbLinkExistingUserDownload,
							mFbLinkExistingUserHandler);
					}
				}
			});

			mCancelLinkAccountsBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					//Cancel fbconnect downloads
					BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
					BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
					BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
					BackgroundDownloader.getInstance().cancelDownload(NET_LOG_IN);

					//goto previous state...
					if (TextUtils.isEmpty(mExpediaUserName.getText())) {
						setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, false);
					}
					else {
						setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON, false);
					}

					setStatusText(mStatusTextContent, true);
				}
			});
		}

		final TextWatcher passwordWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// Do nothing
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			@Override
			public void afterTextChanged(Editable s) {
				mEmptyPassword = TextUtils.isEmpty(s);
				mSignInWithExpediaBtn.setEnabled(!(mEmptyUsername || mEmptyPassword));
			}
		};

		mExpediaPassword.addTextChangedListener(passwordWatcher);

		mExpediaPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					initiateLoginWithExpedia();
					return true;
				}
				if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_SEARCH ||
					actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					Ui.hideKeyboard((Activity) getContext());
					return true;
				}
				else {
					Log.d("EditorInfo IME_ACTION unrecognized actionId=" + actionId);
					return false;
				}
			}
		});

		mAccountButton.setListener(this);
	}

	private void updateButtonState() {
		if (Session.getActiveSession() != null && Session.getActiveSession().isOpened() && !mIsLoading) {
			mLinkAccountsBtn.setEnabled(true);
		}
		else {
			mLinkAccountsBtn.setEnabled(false);
		}

		if (User.isLoggedIn(getContext())) {
			mAccountButton.bind(false, true, Db.getUser(), LineOfBusiness.FLIGHTS);
		}
		else {
			mAccountButton.bind(false, false, null, LineOfBusiness.FLIGHTS);
		}
	}

	private void setVisibilityState(VisibilityState state, boolean animate) {
		mVisibilityState = state;
		switch (mVisibilityState) {
		case LOGGED_IN:
			loginText.setVisibility(GONE);
			loginContainer.setVisibility(VISIBLE);
			setStatusTextVisibility(View.GONE);
			mExpediaSigninContainer.setVisibility(View.GONE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mAccountButton.setVisibility(View.VISIBLE);
			mFacebookEmailDeniedContainer.setVisibility(View.GONE);
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getResources().getString(R.string.already_logged_in));
				if (animate) {
					mToolbarListener.onWidgetClosed();
				}
			}
			break;
		case FACEBOOK_LINK:
			loginText.setVisibility(GONE);
			loginContainer.setVisibility(VISIBLE);
			setStatusTextVisibility(View.VISIBLE);
			mExpediaSigninContainer.setVisibility(View.GONE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.VISIBLE);
			mFacebookButtonContainer.setVisibility(View.VISIBLE);
			mAccountButton.setVisibility(View.GONE);
			mFacebookEmailDeniedContainer.setVisibility(View.GONE);
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getResources().getString(R.string.link_accounts));
			}
			break;
		case FACEBOOK_EMAIL_DENIED:
			loginText.setVisibility(GONE);
			loginContainer.setVisibility(VISIBLE);
			setStatusTextVisibility(View.VISIBLE);
			mExpediaSigninContainer.setVisibility(View.GONE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mAccountButton.setVisibility(View.GONE);
			mFacebookEmailDeniedContainer.setVisibility(View.VISIBLE);
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getResources().getString(R.string.Facebook));
			}
			break;
		case EXPEDIA_WITH_EXPEDIA_BUTTON:
			loginText.setVisibility(GONE);
			loginContainer.setVisibility(VISIBLE);
			setStatusTextVisibility(View.VISIBLE);
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mAccountButton.setVisibility(View.GONE);
			mFacebookEmailDeniedContainer.setVisibility(View.GONE);
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getResources().getString(R.string.Log_In));
			}
			toggleLoginButtons(false, animate);
			break;
		case SIGN_IN:
			loginText.setVisibility(VISIBLE);
			loginContainer.setVisibility(GONE);
			setStatusTextVisibility(View.VISIBLE);
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mAccountButton.setVisibility(View.GONE);
			mFacebookEmailDeniedContainer.setVisibility(View.GONE);
			break;
		case EXPEDIA_WTIH_FB_BUTTON:
		default:
			loginText.setVisibility(GONE);
			loginContainer.setVisibility(VISIBLE);
			setStatusTextVisibility(View.VISIBLE);
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mAccountButton.setVisibility(View.GONE);
			mFacebookEmailDeniedContainer.setVisibility(View.GONE);
			if (mToolbarListener != null) {
				mToolbarListener.setActionBarTitle(getResources().getString(R.string.Log_In));
			}
			toggleLoginButtons(true, animate);
			break;
		}

		updateButtonState();
	}

	public void setLoginExtenderEnabled(boolean enabled, boolean animate) {
		if (enabled) {
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mToolbarListener.setActionBarTitle(null);
			Ui.hideKeyboardIfEditText((Activity) getContext());
			mOuterContainer.setGravity(Gravity.CENTER);
		}
		else {
			mOuterContainer.setGravity(Gravity.CENTER_HORIZONTAL);
			setVisibilityState(mVisibilityState, animate);
		}
	}

	public void setEditTextsEnabled(boolean enabled) {
		mExpediaUserName.setEnabled(enabled);
		mExpediaPassword.setEnabled(enabled);
		mLinkPassword.setEnabled(enabled);
	}

	public void goBack() {
		//Cancel all the current downloads....
		BackgroundDownloader.getInstance().cancelDownload(NET_MANUAL_LOGIN);
		BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
		BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
		BackgroundDownloader.getInstance().cancelDownload(NET_LOG_IN);
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().removeCallback(mFacebookStatusCallback);
		}

		setIsLoading(false);

		switch (mVisibilityState) {
		case FACEBOOK_LINK:
			this.setStatusText(mStatusTextContent, true);
			if (TextUtils.isEmpty(mExpediaUserName.getText())) {
				setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, false);
			}
			else {
				setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON, false);
			}
			break;
		default:

			break;
		}
	}

	//////////////////////////////////
	// User message control stuff

	protected void setIsLoading(boolean loading) {
		mIsLoading = loading;

		if (mIsLoading) {
			//TODO: DISABLE ALL BUTTONS?
		}

		String message = mLoadingText != null ? mLoadingText : getContext().getString(R.string.fetching_facebook_info);
		ThrobberDialog ldf = (ThrobberDialog) ((ActionBarActivity) getContext()).getSupportFragmentManager()
			.findFragmentByTag(
				DIALOG_LOADING);
		if (loading) {
			if (ldf == null) {
				ldf = ThrobberDialog.newInstance(message);
				ldf.setCancelListener(new ThrobberDialog.CancelListener() {
					@Override
					public void onCancel() {
						loginWorkComplete();
					}
				});
			}
			else {
				ldf.setText(message);
			}
			if (!ldf.isAdded()) {
				ldf.show(((ActionBarActivity) getContext()).getSupportFragmentManager(), DIALOG_LOADING);
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

	protected void setStatusTextExpediaAccountFound(String name) {
		String str = String.format(getContext().getString(R.string.facebook_weve_found_your_account), name);
		setStatusText(str, false);
	}

	protected void setStatusTextFbInfoLoaded(String name) {
		String str = String.format(getContext().getString(R.string.facebook_weve_fetched_your_info), name);
		setStatusText(str, false);
	}

	protected void setStatusTextVisibility(int visibility) {
		mStatusMessageTv.setVisibility(visibility);
	}

	protected void setStatusText(final String text, final boolean isHeading) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mStatusMessageTv != null) {
					mStatusMessageTv.setText(Html.fromHtml(text));
					setStatusTextMode(isHeading);
				}
			}
		};
		mStatusText = text;
		((Activity) getContext()).runOnUiThread(runner);
		updateButtonState();
	}

	protected void setStatusTextMode(boolean heading) {
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mStatusMessageTv.getLayoutParams();
		Resources resources = getResources();

		if (heading) {
			float textSize = resources.getDimensionPixelSize(R.dimen.login_header_text_size);
			mStatusMessageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		}
		else {
			float textSize = resources.getDimensionPixelSize(R.dimen.login_header_text_small_size);
			mStatusMessageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_REGULAR);
		}
		mStatusMessageTv.setLayoutParams(lp);
	}

	protected void setStatusText(int resId, boolean isHeading) {
		String str = getContext().getString(resId);
		setStatusText(str, isHeading);
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
		mLoadingText = text;
		((Activity) getContext()).runOnUiThread(runner);

	}

	protected void setLoadingText(int resId) {
		String str = getContext().getString(resId);
		setLoadingText(str);
	}

	protected void clearPasswordField() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mLinkPassword != null) {
					mLinkPassword.setText("");
				}
			}
		};
		((Activity) getContext()).runOnUiThread(runner);

	}

	protected void setFbUserVars(GraphUser user) {
		setFbUserVars(user.getName(), user.getId(), user.getProperty("email") == null ? null : user
			.getProperty("email").toString());
	}

	protected void setFbUserVars(String fbUserName, String fbUserId, String fbUserEmail) {
		this.mFbUserName = fbUserName;
		this.mFbUserId = fbUserId;
		this.mFbUserEmail = fbUserEmail;
	}

	protected void clearFbUserVars() {
		this.mFbUserName = null;
		this.mFbUserId = null;
		this.mFbUserEmail = null;
	}

	//////////////////////////////////
	// Animations

	private void toggleLoginButtons(final boolean toggleToFacebook, final boolean animate) {
		if (!animate) {
			if (toggleToFacebook) {
				mOrFacebookContainer.setVisibility(View.VISIBLE);
				mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
				setButtonFlipPercentage(0f);
			}
			else {
				mOrFacebookContainer.setVisibility(View.GONE);
				mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
				setButtonFlipPercentage(1f);
			}
		}
		else {
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);

			Animator anim = getToggleAnimator(toggleToFacebook);
			anim.setDuration(ANIM_BUTTON_FLIP_DURATION);
			anim.addListener(new AnimatorListener() {

				boolean wasCancelled = false;

				@Override
				public void onAnimationCancel(Animator arg0) {
					wasCancelled = true;
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					if (!wasCancelled) {
						//We only cancel when another animation starts, so we dont want to do anything with visibility now
						mOrFacebookContainer.setVisibility(toggleToFacebook ? View.VISIBLE : View.GONE);
						mSigninWithExpediaButtonContainer.setVisibility(toggleToFacebook ? View.GONE : View.VISIBLE);
					}
					mButtonToggleSemaphore.release();
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationStart(Animator arg0) {
					if (mButtonToggleSemaphore.tryAcquire()) {
						mLastButtonToggleAnimator = arg0;
					}
					else {
						if (mLastButtonToggleAnimator != null && mLastButtonToggleAnimator.isStarted()) {
							mLastButtonToggleAnimator.cancel();
						}
						try {
							mButtonToggleSemaphore.acquire();
							mLastButtonToggleAnimator = arg0;
						}
						catch (Exception ex) {
							Log.e("Exception starting animation", ex);
						}
					}
				}
			});
			anim.start();
		}
	}

	private Animator getToggleAnimator(boolean toggleToFacebook) {

		float start = toggleToFacebook ? 1f : 0f;
		float end = toggleToFacebook ? 0f : 1f;
		if (mLastButtonTogglePercentage >= 0f && mLastButtonTogglePercentage <= 1) {
			start = mLastButtonTogglePercentage;
		}

		ValueAnimator animator = ValueAnimator.ofFloat(start, end);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator anim) {
				Float f = (Float) anim.getAnimatedValue();
				setButtonFlipPercentage(f.floatValue());
			}
		});

		return animator;
	}

	/**
	 * Set the button flip animation percentage
	 * <p/>
	 * 0 = Facebook button showing, Login with Expedia is not showing
	 * 1 = Login with Expedia showing, Login with facebook not showing.
	 *
	 * @param percentage
	 */
	private void setButtonFlipPercentage(float percentage) {
		if (percentage > 1f) {
			percentage = 1f;
		}
		if (percentage < 0f) {
			percentage = 0f;
		}

		mLastButtonTogglePercentage = percentage;

		int maxDegrees = 180;
		float fbAlpha = 0;
		float expAlpha = 0;
		float fbRotationX = 0;
		float expRotationX = 0;

		if (percentage < 0.5f) {
			//Facebook dominates
			fbAlpha = 1f - percentage;
			expAlpha = 0f;

			fbRotationX = percentage * maxDegrees;
		}
		else {
			//Expedia dominates
			fbAlpha = 0f;
			expAlpha = percentage;

			expRotationX = percentage * maxDegrees + 180;
		}

		mOrFacebookContainer.setAlpha(fbAlpha);
		mSigninWithExpediaButtonContainer.setAlpha(expAlpha);

		mOrFacebookContainer.setRotationX(fbRotationX);
		mSigninWithExpediaButtonContainer.setRotationX(expRotationX);
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		// We never show the account button if we are logged out, so login clicked should never be called.
		throw new RuntimeException(
			"The AccountButton in LoginFragment has fired accountLoginClicked, which should never happen.");
	}

	@Override
	public void accountLogoutClicked() {
		mFacebookExpectingClose = true;
		User.signOut(getContext());
		// Let's clear expedia username and password on logout.
		mExpediaUserName.setText("");
		mExpediaPassword.setText("");
		if (mLogInStatusListener != null) {
			mLogInStatusListener.onLogout();
		}
		setVisibilityState(VisibilityState.SIGN_IN, true);
	}

	//////////////////////////////////
	// Downloads

	private void initiateLoginWithExpedia() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (!bd.isDownloading(NET_MANUAL_LOGIN)) {
			setLoadingText(R.string.logging_in);
			setIsLoading(true);
			bd.startDownload(NET_MANUAL_LOGIN, mManualLoginDownload, mManualLoginCallback);
		}
	}

	/**
	 * Good old fashioned expedia login, nothing too complicated.
	 */
	private final Download<SignInResponse> mManualLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			String email = mExpediaUserName.getText().toString();
			String password = mExpediaPassword.getText().toString();

			ExpediaServices services = new ExpediaServices(getContext());
			BackgroundDownloader.getInstance().addDownloadListener(NET_MANUAL_LOGIN, services);
			return services.signIn(email, password, ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private boolean hasResetError(SignInResponse response) {
		if (response == null) {
			return false;
		}
		for (ServerError error : response.getErrors()) {
			if (error.getMessage() != null && error.getMessage().equalsIgnoreCase("AuthenticationFailedAtMTTWeb")) {
				return true;
			}
		}
		return false;
	}

	private final OnDownloadComplete<SignInResponse> mManualLoginCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse response) {
			setIsLoading(false);
			if (response == null || response.hasErrors()) {
				if (hasResetError(response)) {
					mExpediaPassword.setText("");
					setStatusText(R.string.login_reset_password, false);
					return;
				}

				mExpediaPassword.setText("");
				setStatusText(R.string.login_failed_try_again, false);
			}
			else {
				User user = response.getUser();
				user.setIsFacebookUser(loginWithFacebook);
				Db.setUser(user);
				user.save(getContext());
				loginWorkComplete();
				setVisibilityState(VisibilityState.LOGGED_IN, true);
				OmnitureTracking.trackLoginSuccess(getContext(), mLob, loginWithFacebook, user.isRewardsUser());
				AdTracker.trackLogin();
			}
		}
	};

	/**
	 * This attmpts to hand our facebook info to expedia and tries to auto login based on that info.
	 * This will only succeed if the user has at some point granted Expedia access to fbconnect.
	 */
	private final Download<FacebookLinkResponse> mFbLinkAutoLoginDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkAutoLoginDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
			}

			setLoadingText(R.string.attempting_to_log_in_with_facebook);
			ExpediaServices services = new ExpediaServices(getContext());
			return services.facebookAutoLogin(mFbUserId, fbSession.getAccessToken());
		}
	};

	/**
	 * Create a new user based on facebook creds
	 */
	private final Download<FacebookLinkResponse> mFbLinkNewUserDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkNewUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
				return null;
			}

			setLoadingText(R.string.attempting_to_log_in_with_facebook);
			ExpediaServices services = new ExpediaServices(getContext());
			return services.facebookLinkNewUser(mFbUserId, fbSession.getAccessToken(), mFbUserEmail);
		}
	};

	/**
	 * This is for associating a facebook account with an existing expedia account
	 */
	private final Download<FacebookLinkResponse> mFbLinkExistingUserDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkExistingUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
				return null;
			}

			setLoadingText(R.string.linking_your_accounts);
			String expediaPw = mLinkPassword.getText().toString();
			ExpediaServices services = new ExpediaServices(getContext());
			return services.facebookLinkExistingUser(mFbUserId, fbSession.getAccessToken(), mFbUserEmail, expediaPw);
		}
	};

	private final OnDownloadComplete<FacebookLinkResponse> mFbLinkAutoLoginHandler = new OnDownloadComplete<FacebookLinkResponse>() {
		@Override
		public void onDownload(FacebookLinkResponse results) {
			if (results != null && results.getFacebookLinkResponseCode() != null) {
				Log.d("onDownload: mFbLinkAutoLoginHandler:" + results.getFacebookLinkResponseCode().name());
				if (results.isSuccess()) {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_LOG_IN)) {
						bd.startDownload(NET_LOG_IN, mLoginDownload, mLoginHandler);
					}
				}
				else if (results.getFacebookLinkResponseCode().compareTo(FacebookLinkResponseCode.nofbdatafound) == 0
					&& TextUtils.isEmpty(mFbUserEmail)) {
					setFBEmailDeniedState();
				}
				else {
					BackgroundDownloader bd = BackgroundDownloader.getInstance();
					if (!bd.isDownloading(NET_LINK_NEW_USER)) {
						setLoadingText(R.string.linking_your_accounts);
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

	private final OnDownloadComplete<FacebookLinkResponse> mFbLinkNewUserHandler = new OnDownloadComplete<FacebookLinkResponse>() {
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
				else if (results.getFacebookLinkResponseCode().compareTo(FacebookLinkResponseCode.existing) == 0 ||
					results.getFacebookLinkResponseCode().compareTo(FacebookLinkResponseCode.loginFailed) == 0) {
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

	private final OnDownloadComplete<FacebookLinkResponse> mFbLinkExistingUserHandler = new OnDownloadComplete<FacebookLinkResponse>() {
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
				else if (results.getFacebookLinkResponseCode().compareTo(FacebookLinkResponseCode.loginFailed) == 0) {
					setStatusText(R.string.login_failed_try_again, false);
					clearPasswordField();
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

	/**
	 * Just try to sign in with the cookies we already have.
	 */
	private final Download<SignInResponse> mLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			Log.d("doDownload: mLoginDownload");
			ExpediaServices services = new ExpediaServices(getContext());
			BackgroundDownloader.getInstance().addDownloadListener(NET_LOG_IN, services);

			return services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mLoginHandler = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse response) {
			Log.d("onDownload: mLoginHandler");
			if (response == null || response.hasErrors()) {
				//TODO: set better error
				Ui.showToast(getContext(), R.string.failure_to_update_user);
				setIsLoading(false);
				loginWorkComplete();
			}
			else {
				User user = response.getUser();
				user.setIsFacebookUser(loginWithFacebook);
				Db.setUser(user);
				user.save(getContext());
				setIsLoading(false);
				loginWorkComplete();
				setVisibilityState(VisibilityState.LOGGED_IN, true);
				Log.d("User saved!");

				OmnitureTracking.trackLoginSuccess(getContext(), mLob, loginWithFacebook, user.isRewardsUser());
				AdTracker.trackLogin();
			}

		}
	};

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

	/**
	 * Facebook returns us stuff, here is where we determine what that stuff means
	 *
	 * @param session
	 * @param state
	 * @param exception
	 */
	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		Log.d("FB: handleFacebookResponse", exception);
		if (mFacebookExpectingClose && state != null && state.isClosed()) {
			mFacebookExpectingClose = false;
		}
		else if (session == null || state == null || exception != null
			|| state.equals(SessionState.CLOSED)
			|| state.equals(SessionState.CLOSED_LOGIN_FAILED)) {
			setStatusText(R.string.unable_to_log_into_facebook, false);
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
					setStatusText(R.string.unable_to_log_into_facebook, false);
					setIsLoading(false);
				}
			}
		}).executeAsync();
	}

	private boolean hasRequiredInfoFromFB(Session session) {
		if (session.isPermissionGranted("email")) {
			return true;
		}
		return false;
	}

	/**
	 * Login with facebook.
	 * <p/>
	 * This uses the facebook app if it is installed.
	 * If the fb app isn't installed it should use a webpage.
	 */
	protected void doFacebookLogin() {
		Log.d("FB: doFacebookLogin");

		setIsLoading(true);
		setLoadingText(R.string.fetching_facebook_info);
		setStatusText(R.string.Log_in_with_Facebook, true);

		// start Facebook Login
		Session currentSession = Session.getActiveSession();
		if (currentSession == null || currentSession.getState().isClosed()) {
			Session session = new Session.Builder(getContext()).setApplicationId(
				ExpediaServices.getFacebookAppId(getContext())).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		if (!currentSession.isOpened()) {
			Log.d("FB: doFacebookLogin - !currentSession.isOpened()");
			Session.OpenRequest openRequest = null;

			openRequest = new Session.OpenRequest((Activity) getContext());

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
		setVisibilityState(VisibilityState.FACEBOOK_EMAIL_DENIED, false);
	}

	public interface LogInStatusListener {
		public void onLoginStarted();

		public void onLoginCompleted();

		public void onLoginFailed();

		public void onLogout();
	}

	public void setLoginStatusListener(LogInStatusListener listener) {
		mLogInStatusListener = listener;
	}

	@Override
	public void setExpanded(boolean expand, boolean animate) {
		super.setExpanded(expand, animate);
		if (!expand) {
			if (User.isLoggedIn(getContext())) {
				setVisibilityState(VisibilityState.LOGGED_IN, animate);
			}
			else {
				setVisibilityState(VisibilityState.SIGN_IN, animate);
			}
			return;
		}
		if (mToolbarListener != null) {
			mToolbarListener.setActionBarTitle(getActionBarTitle());
		}
		VisibilityState state = VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON;
		if (ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled()) {
			if (mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WTIH_FB_BUTTON)) {
				state = VisibilityState.EXPEDIA_WTIH_FB_BUTTON;
			}
			else if (!mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON)) {
				state = VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON;
			}
		}
		setVisibilityState(state, animate);
	}

	@Override
	public boolean getDoneButtonFocus() {
		if (mExpediaPassword != null) {
			return mExpediaPassword.hasFocus();
		}
		return false;
	}

	@Override
	public void onDonePressed() {
		initiateLoginWithExpedia();
	}

	@Override
	public void onLogin() {

	}

	@Override
	public void onLogout() {

	}

	@Override
	public String getActionBarTitle() {
		return getResources().getString(R.string.Log_In);
	}

	@Override
	public boolean isComplete() {
		return true;
	}
}
