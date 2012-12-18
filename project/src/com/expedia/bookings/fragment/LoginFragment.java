package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog; 
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FacebookLinkResponse.FacebookLinkResponseCode;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightTripPriceFragment.LoadingDetailsDialogFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LocaleUtils;
import com.expedia.bookings.utils.Ui;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class LoginFragment extends Fragment {
	private static final String ARG_PATH_MODE = "ARG_PATH_MODE";

	private static final String NET_MANUAL_LOGIN = "NET_MANUAL_SIGNIN";
	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private static final String NET_LINK_NEW_USER = "NET_LINK_NEW_USER";
	private static final String NET_LINK_EXISTING_USER = "NET_LINK_EXISTING_USER";
	private static final String NET_SIGN_IN = "NET_SIGN_IN";

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

	private Activity mContext;

	//UI ELEMENTS
	private ViewGroup mExpediaSigninContainer;
	private ViewGroup mSigninButtonContainer;
	private ViewGroup mOrFacebookContainer;
	private ViewGroup mSigninWithExpediaButtonContainer;
	private ViewGroup mFacebookSigninContainer;
	private ViewGroup mFacebookButtonContainer;

	private TextView mStatusMessageTv;
	private View mConnectWithFacebookBtn;
	private View mSignInWithExpediaBtn;
	private TextView mForgotYourPasswordTv;
	private View mLinkAccountsBtn;
	private View mCancelLinkAccountsBtn;

	private EditText mExpediaUserName;
	private EditText mExpediaPassword;
	private EditText mLinkPassword;

	private LoadingDialogFragment mLoadingFragment;

	//STATE
	private String mFbUserId;
	private String mFbUserEmail;
	private String mFbUserName;
	private String mStatusText;//Text next to expedia icon
	private String mLoadingText;//Loading spinner text
	private boolean mIsLoading = false;
	private boolean mEmptyUsername = true;
	private boolean mEmptyPassword = true;
	private VisibilityState mVisibilityState = VisibilityState.EXPEDIA_WTIH_FB_BUTTON;
	private PathMode mPathMode = PathMode.HOTELS;

	private enum VisibilityState {
		FACEBOOK_LINK, EXPEDIA_WTIH_FB_BUTTON, EXPEDIA_WITH_EXPEDIA_BUTTON
	}

	public enum PathMode {
		HOTELS, FLIGHTS
	}

	public static LoginFragment newInstance(PathMode mode) {
		LoginFragment frag = new LoginFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PATH_MODE, mode.name());
		frag.setArguments(args);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_log_in, container, false);

		if (this.getArguments() != null && this.getArguments().containsKey(ARG_PATH_MODE)) {
			mPathMode = PathMode.valueOf(this.getArguments().getString(ARG_PATH_MODE));
		}
		
		mExpediaSigninContainer = Ui.findView(v, R.id.expedia_signin_container);
		mSigninButtonContainer = Ui.findView(v, R.id.sign_in_with_expedia_button_container);
		mOrFacebookContainer = Ui.findView(v, R.id.or_facebook_container);
		mSigninWithExpediaButtonContainer = Ui.findView(v, R.id.sign_in_with_expedia_button_container);
		mFacebookSigninContainer = Ui.findView(v, R.id.facebook_signin_container);
		mFacebookButtonContainer = Ui.findView(v, R.id.facebook_button_container);

		mStatusMessageTv = Ui.findView(v, R.id.login_status_textview);
		mConnectWithFacebookBtn = Ui.findView(v, R.id.connect_with_facebook_btn);
		mSignInWithExpediaBtn = Ui.findView(v, R.id.sign_in_with_expedia_btn);
		mForgotYourPasswordTv = Ui.findView(v, R.id.forgot_your_password_link);
		mLinkAccountsBtn = Ui.findView(v, R.id.link_accounts_button);
		mCancelLinkAccountsBtn = Ui.findView(v, R.id.cancel_link_accounts_button);
		mExpediaUserName = Ui.findView(v, R.id.username_edit_text);
		mExpediaPassword = Ui.findView(v, R.id.password_edit_text);
		mLinkPassword = Ui.findView(v, R.id.link_password_edit_text);

		loadSavedState(savedInstanceState);

		initOnClicks();
		setVisibilityState(mVisibilityState);

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
		if (this.mPathMode != null) {
			outState.putString(STATE_PATH_MODE, mPathMode.name());
		}
		outState.putBoolean(STATE_IS_LOADING, mIsLoading);
		outState.putBoolean(STATE_EMPTY_EXP_USERNAME, mEmptyUsername);
		outState.putBoolean(STATE_EMPTY_EXP_PASSWORD, mEmptyPassword);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mContext.isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(NET_MANUAL_LOGIN);
			BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_SIGN_IN);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_MANUAL_LOGIN);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_SIGN_IN);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

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
		if (bd.isDownloading(NET_SIGN_IN)) {
			bd.registerDownloadCallback(NET_SIGN_IN, mLoginHandler);
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
	}
	
	private void loginComplete() {
		if (mContext != null) {
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
				mPathMode = PathMode.valueOf(savedInstanceState.getString(STATE_PATH_MODE));
			}
			if (savedInstanceState.containsKey(STATE_STATUS_TEXT)) {
				mStatusText = savedInstanceState.getString(STATE_STATUS_TEXT);
			}
			if (savedInstanceState.containsKey(STATE_LOADING_TEXT)) {
				mLoadingText = savedInstanceState.getString(STATE_LOADING_TEXT);
			}
			mEmptyUsername = savedInstanceState.getBoolean(STATE_EMPTY_EXP_USERNAME, true);
			mEmptyPassword = savedInstanceState.getBoolean(STATE_EMPTY_EXP_PASSWORD, true);
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

		mConnectWithFacebookBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Cancel regular login download if it is happening...
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (bd.isDownloading(NET_MANUAL_LOGIN)) {
					bd.cancelDownload(NET_MANUAL_LOGIN);
				}

				//Do facebook things!!!
				Session session = Session.getActiveSession();
				if (session == null || session.getState() == null || session.getState().isClosed()
						|| mFbUserName == null) {
					if (session == null) {
						session = new Session.Builder(mContext).setApplicationId(
								ExpediaServices.getFacebookAppId(mContext)).build();
						Session.setActiveSession(session);
					}
					setIsLoading(true);
					setLoadingText(R.string.fetching_facebook_info);
					getFacebookInfo();
				}

				setVisibilityState(VisibilityState.FACEBOOK_LINK);
			}
		});

		mForgotYourPasswordTv.setText(Html.fromHtml(String.format(
				"<a href=\"http://www.%s/pub/agent.dll?qscr=apwd\">%s</a>",
				LocaleUtils.getPointOfSale(mContext), mContext.getString(R.string.forgot_your_password))));
		mForgotYourPasswordTv.setMovementMethod(LinkMovementMethod.getInstance());
		mForgotYourPasswordTv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPathMode.equals(PathMode.FLIGHTS)) {
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

				if (mEmptyUsername) {
					setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON);
				}
				else {
					setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON);
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
				BackgroundDownloader.getInstance().cancelDownload(NET_SIGN_IN);

				//goto previous state...
				if (TextUtils.isEmpty(mExpediaUserName.getText())) {
					setVisibilityState(VisibilityState.EXPEDIA_WTIH_FB_BUTTON);
				}
				else {
					setVisibilityState(VisibilityState.EXPEDIA_WITH_EXPEDIA_BUTTON);
				}
			}
		});
	}

	private void setVisibilityState(VisibilityState state) {
		mVisibilityState = state;
		switch (mVisibilityState) {
		case FACEBOOK_LINK:
			mExpediaSigninContainer.setVisibility(View.GONE);
			mSigninButtonContainer.setVisibility(View.GONE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.VISIBLE);
			mFacebookButtonContainer.setVisibility(View.VISIBLE);
			break;
		case EXPEDIA_WITH_EXPEDIA_BUTTON:
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mSigninButtonContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.GONE);
			mSigninWithExpediaButtonContainer.setVisibility(View.VISIBLE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
			break;
		case EXPEDIA_WTIH_FB_BUTTON:
		default:
			mExpediaSigninContainer.setVisibility(View.VISIBLE);
			mSigninButtonContainer.setVisibility(View.VISIBLE);
			mOrFacebookContainer.setVisibility(View.VISIBLE);
			mSigninWithExpediaButtonContainer.setVisibility(View.GONE);
			mFacebookSigninContainer.setVisibility(View.GONE);
			mFacebookButtonContainer.setVisibility(View.GONE);
		}
	}

	//////////////////////////////////
	// User message control stuff

	protected void setIsLoading(boolean loading) {
		mIsLoading = loading;

		if (mIsLoading) {
			//TODO: DISABLE ALL BUTTONS?
		}

		String message = mLoadingText != null ? mLoadingText : getString(R.string.fetching_facebook_info);
		LoadingDialogFragment ldf = (LoadingDialogFragment) getFragmentManager().findFragmentByTag(
				LoadingDialogFragment.TAG);
		if (loading) {
			if (ldf == null) {
				ldf = LoadingDialogFragment.getInstance(message);
			}
			else {
				ldf.setText(message);
			}
			if (!ldf.isAdded()) {
				ldf.show(getFragmentManager(), LoadingDialogFragment.TAG);
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
		String str = String.format(getString(R.string.facebook_weve_found_your_account), name);
		setStatusText(str);
	}

	protected void setStatusTextFbInfoLoaded(String name) {
		String str = String.format(getString(R.string.facebook_weve_fetched_your_info), name);
		setStatusText(str);
	}

	protected void setStatusText(final String text) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mStatusMessageTv != null) {
					mStatusMessageTv.setText(Html.fromHtml(text));
				}
			}
		};
		mStatusText = text;
		if (mContext != null) {
			mContext.runOnUiThread(runner);
		}

	}

	protected void setStatusText(int resId) {
		String str = getString(resId);
		setStatusText(str);
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
		if (mContext != null) {
			mContext.runOnUiThread(runner);
		}

	}

	protected void setLoadingText(int resId) {
		String str = getString(resId);
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
		if (mContext != null) {
			mContext.runOnUiThread(runner);
		}
	}

	protected void setFbUserVars(GraphUser user) {
		//TODO: REMOVE!!!
		if (user.getProperty("email") == null) {
			user.setProperty("email", "jrdrotos@gmail.com");
		}
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
	// Downloads

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
				setStatusText(R.string.login_failed_try_again);
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(mContext);
				loginComplete();

				if (mPathMode.equals(PathMode.FLIGHTS)) {
					OmnitureTracking.trackLinkFlightCheckoutLoginSuccess(mContext);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginSuccess(mContext);
				}
			}
		}
	};

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

	private final Download<FacebookLinkResponse> mFbLinkNewUserDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkNewUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
			}

			setLoadingText(R.string.attempting_to_log_in_with_facebook);
			ExpediaServices services = new ExpediaServices(mContext);
			return services.facebookLinkNewUser(mFbUserId, fbSession.getAccessToken(), mFbUserEmail);
		}
	};

	private final Download<FacebookLinkResponse> mFbLinkExistingUserDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkExistingUserDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
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
					if (!bd.isDownloading(NET_SIGN_IN)) {
						bd.startDownload(NET_SIGN_IN, mLoginDownload, mLoginHandler);
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
				setStatusText(R.string.unspecified_error);
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
					//mContext.showLinkAccountsStuff(true);
					setIsLoading(false);
				}
				else {
					//TODO:Better error message
					setStatusText(R.string.unspecified_error);
					setIsLoading(false);
				}
			}
			else {
				//TODO:Better error message
				setStatusText(R.string.unspecified_error);
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
					setStatusText(R.string.login_failed_try_again);
					clearPasswordField();
					setIsLoading(false);
				}
				else {
					//TODO: Something...
					setStatusText(R.string.unspecified_error);
					setIsLoading(false);
				}
			}
			else {
				//TODO:Better error message
				setStatusText(R.string.unspecified_error);
				setIsLoading(false);
			}
		}
	};

	private final Download<SignInResponse> mLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			Log.d("doDownload: mLoginDownload");
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(NET_SIGN_IN, services);

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
				mContext.finish();
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(mContext);
				Log.d("User saved!");
				//TODO: Omniture Tracking...

				setIsLoading(false);
				loginComplete();
			}

		}
	};

	Session.StatusCallback mFacebookStatusCallback = new Session.StatusCallback() {

		// callback when session changes state
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			handleFacebookResponse(session, state, exception);
		}
	};

	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		if (session.isOpened()) {

			if (!session.getPermissions().contains("email")) {
				List<String> permissions = new ArrayList<String>();
				permissions.add("email");
				NewPermissionsRequest permissionRequest = new NewPermissionsRequest(mContext,
						permissions);
				session.addCallback(mFacebookStatusCallback);
				session.requestNewReadPermissions(permissionRequest);
			}
			else {

				// make request to the /me API
				Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {

					// callback after Graph API response with user object
					@Override
					public void onCompleted(GraphUser user, Response response) {
						Log.d("FB RESPONSE:" + response.toString());
						if (user != null) {
							setFbUserVars(user);
							setStatusTextFbInfoLoaded(mFbUserName);
							BackgroundDownloader bd = BackgroundDownloader.getInstance();
							if (!bd.isDownloading(NET_AUTO_LOGIN)) {
								bd.startDownload(NET_AUTO_LOGIN, mFbLinkAutoLoginDownload, mFbLinkAutoLoginHandler);
							}
						}
						else {
							setStatusText(R.string.unable_to_sign_into_facebook);
							setIsLoading(false);
						}
					}
				});
			}
		}
		else {
			setStatusText(R.string.unable_to_sign_into_facebook);
			setIsLoading(false);
		}
	}

	protected void getFacebookInfo() {

		// start Facebook Login
		Session.openActiveSession(mContext, true, mFacebookStatusCallback);

	}

	//////////////////////////////////////////////////////////////////////////
	// Progress dialog

	public static class LoadingDialogFragment extends DialogFragment {
		public static final String TAG = LoadingDetailsDialogFragment.class.getName();
		private static final String ARG_MESSAGE = "ARG_MESSAGE";

		public static LoadingDialogFragment getInstance(String message) {
			LoadingDialogFragment frag = new LoadingDialogFragment();
			Bundle args = new Bundle();
			args.putCharSequence(ARG_MESSAGE, message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog pd = new ProgressDialog(getActivity());
			pd.setMessage(this.getArguments().getCharSequence(ARG_MESSAGE));
			pd.setCanceledOnTouchOutside(false);
			return pd;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);

			// If the dialog is canceled without finishing loading, don't show this page.
			getActivity().finish();
		}

		public void setText(String text) {
			ProgressDialog pd = (ProgressDialog) this.getDialog();
			if (pd != null) {
				pd.setMessage(text);
			}
			this.getArguments().putCharSequence(ARG_MESSAGE, text);
		}
	}

}
