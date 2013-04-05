package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FacebookLinkResponse.FacebookLinkResponseCode;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender.LoginExtenderListener;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.animation.AnimatorProxy;

/**
 * This fragment is for logging into expedia accounts via the standard method AND facebook connect.
 *
 * It should be noted that the facebook connect expedia api is totally undocumented, and totally useless in the case of errors.
 * In fact, the expedia fbconnect apis return nothing more than success or failure, and it is up to us to make due.
 *
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
 *
 */
public class LoginFragment extends Fragment implements LoginExtenderListener {
	private static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	private static final String ARG_EXTENDER_OBJECT = "ARG_EXTENDER_OBJECT";

	private static final String NET_MANUAL_LOGIN = "NET_MANUAL_SIGNIN";
	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private static final String NET_LINK_NEW_USER = "NET_LINK_NEW_USER";
	private static final String NET_LINK_EXISTING_USER = "NET_LINK_EXISTING_USER";
	private static final String NET_LOG_IN = "NET_LOG_IN";

	private static final String STATE_FB_USER_ID = "STATE_FB_USER_ID";
	private static final String STATE_FB_EMAIL = "STATE_FB_EMAIL";
	private static final String STATE_FB_USER_NAME = "STATE_FB_USER_NAME";
	private static final String STATE_IS_LOADING = "STATE_IS_LOADING";
	private static final String STATE_STATUS_TEXT = "STATE_STATUS_TEXT";
	private static final String STATE_LOADING_TEXT = "STATE_LOADING_TEXT";
	private static final String STATE_VISIBILITY_STATE = "STATE_VISIBILITY_STATE";
	private static final String STATE_PATH_MODE = "STATE_PATH_MODE";
	private static final String STATE_EMPTY_EXP_USERNAME = "STATE_EMPTY_EXP_USERNAME";
	private static final String STATE_EMPTY_EXP_PASSWORD = "STATE_EMPTY_EXP_PASSWORD";
	private static final String STATE_DO_LOGIN_EXTENDER_WORK = "STATE_DO_LOGIN_EXTENDER_WORK";
	private static final String STATE_LOGIN_EXTENDER = "STATE_LOGIN_EXTENDER";

	private static final String DIALOG_LOADING = "DIALOG_LOADING";

	private static final int ANIM_BUTTON_FLIP_DURATION = 200;

	private Activity mContext;
	private TitleSettable mTitleSetter;
	private LoginExtender mLoginExtender;

	//UI ELEMENTS
	private ViewGroup mExpediaSigninContainer;
	private ViewGroup mOrFacebookContainer;
	private ViewGroup mSigninWithExpediaButtonContainer;
	private ViewGroup mFacebookSigninContainer;
	private ViewGroup mFacebookButtonContainer;
	private ViewGroup mLoginExtenderContainer;
	private LinearLayout mOuterContainer;

	private TextView mStatusMessageTv;
	private Button mLogInWithFacebookBtn;
	private Button mSignInWithExpediaBtn;
	private TextView mForgotYourPasswordTv;
	private Button mLinkAccountsBtn;
	private Button mCancelLinkAccountsBtn;

	private EditText mExpediaUserName;
	private EditText mExpediaPassword;
	private EditText mLinkPassword;

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
	private boolean mIsLoading = false;
	private boolean mEmptyUsername = true;
	private boolean mEmptyPassword = true;
	private boolean mDoLoginExtenderWork = false;
	private VisibilityState mVisibilityState = VisibilityState.EXPEDIA_WTIH_FB_BUTTON;
	private LineOfBusiness mLob = LineOfBusiness.HOTELS;

	// Boolean for OmnitureTracking related purposes. false means user logged in manually
	private boolean loginWithFacebook = false;

	private enum VisibilityState {
		FACEBOOK_LINK, EXPEDIA_WTIH_FB_BUTTON, EXPEDIA_WITH_EXPEDIA_BUTTON
	}

	public static LoginFragment newInstance(LineOfBusiness mode) {
		LoginFragment frag = new LoginFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PATH_MODE, mode.name());
		frag.setArguments(args);
		return frag;
	}

	public static LoginFragment newInstance(LineOfBusiness mode, LoginExtender extender) {
		LoginFragment frag = new LoginFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PATH_MODE, mode.name());
		args.putParcelable(ARG_EXTENDER_OBJECT, extender);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_log_in, container, false);

		if (this.getArguments() != null && this.getArguments().containsKey(ARG_PATH_MODE)) {
			mLob = LineOfBusiness.valueOf(this.getArguments().getString(ARG_PATH_MODE));
		}
		if (this.getArguments() != null && this.getArguments().containsKey(ARG_EXTENDER_OBJECT)) {
			mLoginExtender = this.getArguments().getParcelable(ARG_EXTENDER_OBJECT);
			//We now control this from saved state...
			this.getArguments().remove(ARG_EXTENDER_OBJECT);
		}

		mOuterContainer = Ui.findView(v, R.id.outer_container);
		mExpediaSigninContainer = Ui.findView(v, R.id.expedia_signin_container);
		mOrFacebookContainer = Ui.findView(v, R.id.or_facebook_container);
		mSigninWithExpediaButtonContainer = Ui.findView(v, R.id.log_in_with_expedia_button_container);
		mFacebookSigninContainer = Ui.findView(v, R.id.facebook_signin_container);
		mFacebookButtonContainer = Ui.findView(v, R.id.facebook_button_container);
		mLoginExtenderContainer = Ui.findView(v, R.id.login_extension_container);

		mStatusMessageTv = Ui.findView(v, R.id.login_status_textview);
		mLogInWithFacebookBtn = Ui.findView(v, R.id.log_in_with_facebook_btn);
		mSignInWithExpediaBtn = Ui.findView(v, R.id.log_in_with_expedia_btn);
		mForgotYourPasswordTv = Ui.findView(v, R.id.forgot_your_password_link);
		mLinkAccountsBtn = Ui.findView(v, R.id.link_accounts_button);
		mCancelLinkAccountsBtn = Ui.findView(v, R.id.cancel_link_accounts_button);
		mExpediaUserName = Ui.findView(v, R.id.username_edit_text);
		mExpediaPassword = Ui.findView(v, R.id.password_edit_text);
		mLinkPassword = Ui.findView(v, R.id.link_password_edit_text);

		FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mLogInWithFacebookBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mSignInWithExpediaBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mForgotYourPasswordTv, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mLinkAccountsBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mCancelLinkAccountsBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mExpediaUserName, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mExpediaPassword, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mLinkPassword, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(v, R.id.or_tv, Font.ROBOTO_LIGHT);

		loadSavedState(savedInstanceState);

		if (mStatusText == null || mStatusText.equalsIgnoreCase(getString(R.string.expedia_account))) {
			setStatusText(R.string.expedia_account, true);
		}
		else if (mStatusText != null) {
			setStatusText(mStatusText, false);
		}

		initOnClicks();
		setVisibilityState(mVisibilityState, false);

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (this.mFbUserId != null) {
			outState.putString(STATE_FB_USER_ID, mFbUserId);
		}
		if (this.mFbUserEmail != null) {
			outState.putString(STATE_FB_EMAIL, mFbUserEmail);
		}
		if (this.mFbUserName != null) {
			outState.putString(STATE_FB_USER_NAME, mFbUserName);
		}
		if (this.mStatusText != null) {
			outState.putString(STATE_STATUS_TEXT, mStatusText);
		}
		if (this.mLoadingText != null) {
			outState.putString(STATE_LOADING_TEXT, mLoadingText);
		}
		if (this.mVisibilityState != null) {
			outState.putString(STATE_VISIBILITY_STATE, mVisibilityState.name());
		}
		if (this.mLob != null) {
			outState.putString(STATE_PATH_MODE, mLob.name());
		}
		if (mLoginExtender != null) {
			outState.putParcelable(STATE_LOGIN_EXTENDER, mLoginExtender);
		}

		outState.putBoolean(STATE_IS_LOADING, mIsLoading);
		outState.putBoolean(STATE_EMPTY_EXP_USERNAME, mEmptyUsername);
		outState.putBoolean(STATE_EMPTY_EXP_PASSWORD, mEmptyPassword);
		outState.putBoolean(STATE_DO_LOGIN_EXTENDER_WORK, mDoLoginExtenderWork);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (Session.getActiveSession() != null) {
			Session.getActiveSession().removeCallback(mFacebookStatusCallback);
		}

		if (mContext.isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(NET_MANUAL_LOGIN);
			BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_LOG_IN);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_MANUAL_LOGIN);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LOG_IN);
		}

		if (mLoginExtender != null) {
			mLoginExtender.cleanUp();
		}
	}

	@Override
	public void onResume() {
		super.onResume();

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
		else {
			View focused = this.getView().findFocus();
			if (focused == null || !(focused instanceof EditText)) {
				focused = mExpediaUserName;
			}
			if (focused != null && focused instanceof EditText) {
				FocusViewRunnable.focusView(this, focused);
			}
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		mTitleSetter = (TitleSettable) mContext;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("FB: onActivityResult");
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	}

	private void loginWorkComplete() {
		if (mLoginExtender != null) {
			doLoginExtenderWork();
		}
		else {
			finishParentWithResult();
		}
	}

	public void doLoginExtenderWork() {
		mDoLoginExtenderWork = true;
		setEditTextsEnabled(false);
		setLoginExtenderEnabled(true, false);
		mLoginExtenderContainer.setVisibility(View.VISIBLE);
		if (User.isLoggedIn(mContext) && Db.getUser() != null) {
			mLoginExtender.onLoginComplete(getActivity(), this, mLoginExtenderContainer);
		}
		else {
			//If we arent logged in, then our extender is considered complete
			loginExtenderWorkComplete(mLoginExtender);
		}
	}

	@Override
	public void loginExtenderWorkComplete(LoginExtender extender) {
		finishParentWithResult();
	}

	@Override
	public void setExtenderStatus(String status) {
		setStatusText(status, true);
	}

	private void finishParentWithResult() {
		if (mContext != null) {
			if (User.isLoggedIn(mContext) && Db.getUser() != null) {
				mContext.setResult(Activity.RESULT_OK);
			}
			else {
				mContext.setResult(Activity.RESULT_CANCELED);
			}
			mContext.finish();
		}
	}

	private void loadSavedState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(STATE_FB_USER_ID)) {
				mFbUserId = savedInstanceState.getString(STATE_FB_USER_ID);
			}
			if (savedInstanceState.containsKey(STATE_FB_EMAIL)) {
				mFbUserEmail = savedInstanceState.getString(STATE_FB_EMAIL);
			}
			if (savedInstanceState.containsKey(STATE_FB_USER_NAME)) {
				mFbUserName = savedInstanceState.getString(STATE_FB_USER_NAME);
			}
			if (savedInstanceState.containsKey(STATE_VISIBILITY_STATE)) {
				mVisibilityState = VisibilityState.valueOf(savedInstanceState.getString(STATE_VISIBILITY_STATE));
			}
			if (savedInstanceState.containsKey(STATE_PATH_MODE)) {
				mLob = LineOfBusiness.valueOf(savedInstanceState.getString(STATE_PATH_MODE));
			}
			if (savedInstanceState.containsKey(STATE_STATUS_TEXT)) {
				mStatusText = savedInstanceState.getString(STATE_STATUS_TEXT);
			}
			if (savedInstanceState.containsKey(STATE_LOADING_TEXT)) {
				mLoadingText = savedInstanceState.getString(STATE_LOADING_TEXT);
			}
			if (savedInstanceState.containsKey(STATE_LOGIN_EXTENDER)) {
				mLoginExtender = savedInstanceState.getParcelable(STATE_LOGIN_EXTENDER);
			}

			mEmptyUsername = savedInstanceState.getBoolean(STATE_EMPTY_EXP_USERNAME, true);
			mEmptyPassword = savedInstanceState.getBoolean(STATE_EMPTY_EXP_PASSWORD, true);
			mDoLoginExtenderWork = savedInstanceState.getBoolean(STATE_DO_LOGIN_EXTENDER_WORK, false);
		}
	}

	private void initOnClicks() {

		mSignInWithExpediaBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(NET_MANUAL_LOGIN)) {
					setLoadingText(R.string.logging_in);
					setIsLoading(true);
					bd.startDownload(NET_MANUAL_LOGIN, mManualLoginDownload, mManualLoginCallback);
				}
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

				setVisibilityState(VisibilityState.FACEBOOK_LINK, false);
			}
		});

		mForgotYourPasswordTv.setText(Html.fromHtml(String.format(
				"<a href=\"http://www.%s/pub/agent.dll?qscr=apwd\">%s</a>",
				PointOfSale.getPointOfSale(mContext).getUrl(), mContext.getString(R.string.forgot_your_password))));
		mForgotYourPasswordTv.setMovementMethod(LinkMovementMethod.getInstance());
		mForgotYourPasswordTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLob.equals(LineOfBusiness.FLIGHTS)) {
					OmnitureTracking.trackLinkFlightCheckoutLoginForgot(mContext);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginForgot(mContext);
				}
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

				if (mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WTIH_FB_BUTTON)) {
					setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, true);
				}
				else if (!mEmptyUsername && !mVisibilityState.equals(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON)) {
					setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON, true);
				}
			}
		};
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
		mExpediaUserName.addTextChangedListener(usernameWatcher);
		mExpediaPassword.addTextChangedListener(passwordWatcher);

		mExpediaPassword.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE
						|| actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_GO
						|| actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
					InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
							Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					return true;
				}
				else {
					Log.d("EditorInfo IME_ACTION unrecognized actionId=" + actionId);
					return false;
				}
			}
		});

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

				setStatusText(R.string.expedia_account, true);
			}
		});
	}

	private void updateButtonState() {
		if (Session.getActiveSession() != null && Session.getActiveSession().isOpened() && !mIsLoading) {
			mLinkAccountsBtn.setEnabled(true);
		}
		else {
			mLinkAccountsBtn.setEnabled(false);
		}
	}

	private void setVisibilityState(VisibilityState state, boolean animate) {
		mVisibilityState = state;
		switch (mVisibilityState) {
		case FACEBOOK_LINK:
			mExpediaSigninContainer.setVisibility(View.GONE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.VISIBLE);
			mFacebookButtonContainer.setVisibility(View.VISIBLE);
			mTitleSetter.setActionBarTitle(getResources().getString(R.string.link_accounts));
			break;
		case EXPEDIA_WITH_EXPEDIA_BUTTON:
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mTitleSetter.setActionBarTitle(getResources().getString(R.string.Log_In));
			toggleLoginButtons(false, animate);
			break;
		case EXPEDIA_WTIH_FB_BUTTON:
		default:
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mTitleSetter.setActionBarTitle(getResources().getString(R.string.Log_In));
			toggleLoginButtons(true, animate);
			break;
		}

		updateButtonState();
	}

	public void setLoginExtenderEnabled(boolean enabled, boolean animate) {
		if (enabled) {
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			mTitleSetter.setActionBarTitle(getResources().getString(R.string.link_accounts));
			hideKeyboard();
			mOuterContainer.setGravity(Gravity.CENTER);
		}
		else {
			mOuterContainer.setGravity(Gravity.CENTER_HORIZONTAL);
			setVisibilityState(mVisibilityState, animate);
		}
	}

	public void hideKeyboard() {
		View focused = this.getActivity().getCurrentFocus();
		if (focused instanceof EditText) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
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
			this.setStatusText(R.string.expedia_account, true);
			if (TextUtils.isEmpty(mExpediaUserName.getText())) {
				setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON, false);
			}
			else {
				setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON, false);
			}
			break;
		default:
			finishParentWithResult();
			break;
		}
	}

	//////////////////////////////////
	// User message control stuff

	protected void setIsLoading(boolean loading) {
		mIsLoading = loading;

		if (mContext == null || !this.isAdded()) {
			return;
		}

		if (mIsLoading) {
			//TODO: DISABLE ALL BUTTONS?
		}

		String message = mLoadingText != null ? mLoadingText : getString(R.string.fetching_facebook_info);
		ThrobberDialog ldf = (ThrobberDialog) getFragmentManager().findFragmentByTag(DIALOG_LOADING);
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
				ldf.show(getFragmentManager(), DIALOG_LOADING);
			}
			mLoadingFragment = ldf;
		}
		else {
			if (ldf != null) {
				ldf.dismiss();
			}
		}
	}

	protected void setStatusTextExpediaAccountFound(String name) {
		if (mContext != null && this.isAdded()) {
			String str = String.format(getString(R.string.facebook_weve_found_your_account), name);
			setStatusText(str, false);
		}
	}

	protected void setStatusTextFbInfoLoaded(String name) {
		if (mContext != null && this.isAdded()) {
			String str = String.format(getString(R.string.facebook_weve_fetched_your_info), name);
			setStatusText(str, false);
		}
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
		if (mContext != null && this.isAdded()) {
			mContext.runOnUiThread(runner);
		}
		updateButtonState();
	}

	protected void setStatusTextMode(boolean heading) {
		LayoutParams lp = (LayoutParams) mStatusMessageTv.getLayoutParams();
		Resources resources = getResources();

		if (heading) {
			float textSize = resources.getDimensionPixelSize(R.dimen.login_header_text_size);
			mStatusMessageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
			int marginpx = resources.getDimensionPixelSize(R.dimen.login_header_text_size);
			lp.leftMargin = marginpx;
			lp.rightMargin = marginpx;
		}
		else {
			float textSize = resources.getDimensionPixelSize(R.dimen.login_header_text_small_size);
			mStatusMessageTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_REGULAR);
			int marginpx = resources.getDimensionPixelSize(R.dimen.login_header_text_small_size);
			lp.leftMargin = marginpx;
			lp.rightMargin = marginpx;
		}
		mStatusMessageTv.setLayoutParams(lp);
	}

	protected void setStatusText(int resId, boolean isHeading) {
		if (mContext != null && this.isAdded()) {
			String str = getString(resId);
			setStatusText(str, isHeading);
		}
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
		if (mContext != null && this.isAdded()) {
			mContext.runOnUiThread(runner);
		}

	}

	protected void setLoadingText(int resId) {
		if (mContext != null && this.isAdded()) {
			String str = getString(resId);
			setLoadingText(str);
		}
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
		if (mContext != null && this.isAdded()) {
			mContext.runOnUiThread(runner);
		}
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
	 *
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

		setViewAlpha(mOrFacebookContainer, fbAlpha);
		setViewAlpha(mSigninWithExpediaButtonContainer, expAlpha);

		setViewRotationX(mOrFacebookContainer, fbRotationX);
		setViewRotationX(mSigninWithExpediaButtonContainer, expRotationX);
	}

	//////////////////////////////////
	// Animation Helpers

	@SuppressLint("NewApi")
	private void setViewAlpha(View v, float alpha) {
		if (!AnimatorProxy.NEEDS_PROXY) {
			v.setAlpha(alpha);
		}
		else {
			AnimatorProxy.wrap(v).setAlpha(alpha);
		}
	}

	@SuppressLint("NewApi")
	private void setViewRotationX(View v, float rotationX) {
		if (!AnimatorProxy.NEEDS_PROXY) {
			v.setRotationX(rotationX);
		}
		else {
			AnimatorProxy.wrap(v).setRotationX(rotationX);
		}
	}

	//////////////////////////////////
	// Downloads

	/**
	 * Good old fashioned expedia login, nothing too complicated.
	 */
	private final Download<SignInResponse> mManualLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			String email = mExpediaUserName.getText().toString();
			String password = mExpediaPassword.getText().toString();

			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(NET_MANUAL_LOGIN, services);
			return services.signIn(email, password, ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mManualLoginCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse response) {
			setIsLoading(false);
			if (response == null || response.hasErrors()) {
				mExpediaPassword.setText("");
				setStatusText(R.string.login_failed_try_again, false);
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(mContext);
				loginWorkComplete();

				OmnitureTracking.trackLoginSuccess(mContext, mLob, loginWithFacebook, user.isRewardsUser());
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
			ExpediaServices services = new ExpediaServices(mContext);
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
			ExpediaServices services = new ExpediaServices(mContext);
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
			ExpediaServices services = new ExpediaServices(mContext);
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
				else if (results.getFacebookLinkResponseCode().compareTo(FacebookLinkResponseCode.existing) == 0) {
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
			ExpediaServices services = new ExpediaServices(mContext);
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
				Ui.showToast(mContext, R.string.failure_to_update_user);
				setIsLoading(false);
				loginWorkComplete();
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(mContext);
				Log.d("User saved!");

				OmnitureTracking.trackLoginSuccess(mContext, mLob, loginWithFacebook, user.isRewardsUser());

				setIsLoading(false);
				loginWorkComplete();
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
	 * @param session
	 * @param state
	 * @param exception
	 */
	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		Log.d("FB: handleFacebookResponse", exception);
		if (session == null || state == null || exception != null || state.equals(SessionState.CLOSED)
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
	 * @param session
	 */
	protected void fetchFacebookUserInfo(Session session) {
		Log.d("FB: fetchFacebookUserInfo");

		// make request to the /me API
		Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

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
		});
	}

	/**
	 * Login with facebook.
	 *
	 *  This uses the facebook app if it is installed.
	 *  If the fb app isn't installed it should use a webpage.
	 */
	protected void doFacebookLogin() {
		Log.d("FB: doFacebookLogin");

		setIsLoading(true);
		setLoadingText(R.string.fetching_facebook_info);
		setStatusText(R.string.Log_in_with_Facebook, true);

		// start Facebook Login
		Session currentSession = Session.getActiveSession();
		if (currentSession == null || currentSession.getState().isClosed()) {
			Session session = new Session.Builder(getActivity()).setApplicationId(
					ExpediaServices.getFacebookAppId(mContext)).build();
			Session.setActiveSession(session);
			currentSession = session;
		}
		if (!currentSession.isOpened()) {
			Log.d("FB: doFacebookLogin - !currentSession.isOpened()");
			Session.OpenRequest openRequest = null;

			openRequest = new Session.OpenRequest(this);

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
			fetchFacebookUserInfo(currentSession);
		}

	}

	/////////////////////////////
	//Interfaces

	public interface TitleSettable {
		public void setActionBarTitle(String title);
	}

	//This is here for compatibility with the old SignInFragment.SignInFragmentListener
	public interface LogInListener {
		public void onLoginStarted();

		public void onLoginCompleted();

		public void onLoginFailed();
	}

	/**
	 * This interface is for classes that want to do something after login. It was developed for itin so we can wait for itins on the login screen.
	 * NOTE: This is not a view, and it does not have to contain a gui component (although if it is long running it should display an indicator at the very least)
	 */
	public interface LoginExtender extends Parcelable {
		/**
		 * The login has finished successfully, now is your time to do some stuff. If we have gui components add them to the extenderContainer
		 * @param fragment - the login fragment
		 * @param extenderContainer - a container set aside for views if the extender has a gui component
		 */
		public void onLoginComplete(Context context, LoginExtenderListener listener, ViewGroup extenderContainer);

		/**
		 * We dont want to leak memory so we give you a chance to cleanup when the login fragment thinks cleaning is smart to do.
		 * At the time of this writting this gets called in onPause
		 */
		public void cleanUp();

		/**
		 * Set status message from Extender
		 */
		public void setExtenderStatus(String status);
	}
}
