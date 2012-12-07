package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.List;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FacebookLinkResponse;
import com.expedia.bookings.data.FacebookLinkResponse.FacebookLinkResponseCode;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.FlightTripPriceFragment.LoadingDetailsDialogFragment;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.utils.Ui;
import com.facebook.FacebookActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class FacebookLinkActivity extends FacebookActivity {

	private static final String NET_AUTO_LOGIN = "NET_AUTO_LOGIN";
	private static final String NET_LINK_NEW_USER = "NET_LINK_NEW_USER";
	private static final String NET_LINK_EXISTING_USER = "NET_LINK_EXISTING_USER";
	private static final String NET_SIGN_IN = "NET_SIGN_IN";

	private static final String STATE_FB_USER_ID = "STATE_FB_USER_ID";
	private static final String STATE_FB_EMAIL = "STATE_FB_EMAIL";
	private static final String STATE_FB_USER_NAME = "STATE_FB_USER_NAME";
	private static final String STATE_IS_LOADING = "STATE_IS_LOADING";
	private static final String STATE_SHOW_LINK_STUFF = "STATE_SHOW_LINK_STUFF";
	private static final String STATE_STATUS_TEXT = "STATE_STATUS_TEXT";
	private static final String STATE_LOADING_TEXT = "STATE_LOADING_TEXT";

	private String mFbUserId;
	private String mFbUserEmail;
	private String mFbUserName;
	private String mStatusText;//Text next to expedia icon
	private String mLoadingText;//Loading spinner text
	private boolean mIsLoading = true;
	private boolean mShowLinkAccountStuff = false;

	private TextView mFacebookStatusTv;
	private View mLinkAccountsBtn;
	private EditText mPasswordTv;

	private LoadingDialogFragment mLoadingFragment;

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook_link);

		mFacebookStatusTv = Ui.findView(this, R.id.facebook_status_textview);
		mLinkAccountsBtn = Ui.findView(this, R.id.link_accounts_button);
		mPasswordTv = Ui.findView(this, R.id.password_edit_text);

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
			if (savedInstanceState.containsKey(STATE_SHOW_LINK_STUFF)) {
				mShowLinkAccountStuff = savedInstanceState.getBoolean(STATE_SHOW_LINK_STUFF);
			}
			if (savedInstanceState.containsKey(STATE_STATUS_TEXT)) {
				mStatusText = savedInstanceState.getString(STATE_STATUS_TEXT);
			}
			if (savedInstanceState.containsKey(STATE_LOADING_TEXT)) {
				mLoadingText = savedInstanceState.getString(STATE_LOADING_TEXT);
			}
		}

		mLinkAccountsBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(NET_LINK_EXISTING_USER)) {
					FacebookLinkActivity.this.setLoadingText(R.string.linking_your_accounts);
					FacebookLinkActivity.this.setIsLoading(true);
					bd.startDownload(NET_LINK_EXISTING_USER, mFbLinkExistingUserDownload,
							mFbLinkExistingUserHandler);
				}
			}

		});

		mPasswordTv.addTextChangedListener(new TextWatcher() {
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

		//NOTE: Facebook Activity does not use ABS - for now we are just going to ignore this fact...
		this.setTitle(R.string.link_accounts);
		if (AndroidUtils.isHoneycombVersionOrHigher()) {
			android.app.ActionBar actionBar = this.getActionBar();
			if (actionBar != null) {
				actionBar.setDisplayHomeAsUpEnabled(true);
			}
		}

	}

	@Override
	protected void onPause() {
		super.onPause();

		if (isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().cancelDownload(NET_SIGN_IN);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_AUTO_LOGIN);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_EXISTING_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_LINK_NEW_USER);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(NET_SIGN_IN);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mStatusText != null) {
			this.setStatusText(mStatusText);
		}
		else {
			this.setStatusText(R.string.fetching_facebook_info);
		}

		this.showLinkAccountsStuff(mShowLinkAccountStuff);

		//Do facebook things!!!
		if (this.getSessionState() == null || this.getSessionState().isClosed()) {
			setIsLoading(true);
			setLoadingText(R.string.fetching_facebook_info);
			List<String> fbPermissions = new ArrayList<String>();
			fbPermissions.add("email");
			String fbAppId = ExpediaServices.getFacebookAppId(this);
			this.openSessionForRead(fbAppId, fbPermissions);
		}
		else if (mFbUserName == null) {
			setIsLoading(true);
			setLoadingText(R.string.fetching_facebook_info);
			getFacebookInfo();
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
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
	protected void onSaveInstanceState(Bundle outState) {
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
		outState.putBoolean(STATE_IS_LOADING, mIsLoading);
		outState.putBoolean(STATE_SHOW_LINK_STUFF, this.mShowLinkAccountStuff);

	}

	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) {
		// user has either logged in or not ...
		if (state.isOpened()) {
			getFacebookInfo();
		}
		else {
			//facebook connection is not open, forget having any fun at all
			setIsLoading(false);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void showLinkAccountsStuff(boolean show) {
		this.mPasswordTv.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		this.mLinkAccountsBtn.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
		mShowLinkAccountStuff = show;
	}

	protected void getFacebookInfo() {
		// make request to the /me API
		Request request = Request.newMeRequest(
				this.getSession(),
				new Request.GraphUserCallback() {
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
				}
				);
		Request.executeBatchAsync(request);
	}

	protected void setIsLoading(boolean loading) {
		mIsLoading = loading;
		mLinkAccountsBtn.setEnabled(!loading);

		String message = mLoadingText != null ? mLoadingText : getString(R.string.fetching_facebook_info);
		LoadingDialogFragment ldf = (LoadingDialogFragment) getSupportFragmentManager().findFragmentByTag(
				LoadingDialogFragment.TAG);
		if (loading) {
			if (ldf == null) {
				ldf = LoadingDialogFragment.getInstance(message);
			}
			else {
				ldf.setText(message);
			}
			if (!ldf.isAdded()) {
				ldf.show(getSupportFragmentManager(), LoadingDialogFragment.TAG);
			}
			mLoadingFragment = ldf;
		}
		else {
			if (ldf != null) {
				ldf.dismiss();
			}
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
				if (mFacebookStatusTv != null) {
					mFacebookStatusTv.setText(Html.fromHtml(text));
				}
			}
		};
		mStatusText = text;
		runOnUiThread(runner);

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
		runOnUiThread(runner);

	}

	protected void setLoadingText(int resId) {
		String str = getString(resId);
		setLoadingText(str);
	}

	protected void clearPasswordField() {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mPasswordTv != null) {
					mPasswordTv.setText("");
				}
			}
		};
		runOnUiThread(runner);
	}

	private final Download<FacebookLinkResponse> mFbLinkAutoLoginDownload = new Download<FacebookLinkResponse>() {
		@Override
		public FacebookLinkResponse doDownload() {
			Log.d("doDownload: mFbLinkAutoLoginDownload");
			Session fbSession = Session.getActiveSession();
			if (fbSession == null || fbSession.isClosed()) {
				Log.e("fbState invalid");
			}

			setLoadingText(R.string.attempting_to_log_in_with_facebook);
			ExpediaServices services = new ExpediaServices(FacebookLinkActivity.this);
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
			ExpediaServices services = new ExpediaServices(FacebookLinkActivity.this);
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
			String expediaPw = mPasswordTv.getText().toString();
			ExpediaServices services = new ExpediaServices(FacebookLinkActivity.this);
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
						FacebookLinkActivity.this.setLoadingText(R.string.linking_your_accounts);
						FacebookLinkActivity.this.setIsLoading(true);
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
					FacebookLinkActivity.this.setStatusTextExpediaAccountFound(FacebookLinkActivity.this.mFbUserName);
					FacebookLinkActivity.this.showLinkAccountsStuff(true);
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
			ExpediaServices services = new ExpediaServices(FacebookLinkActivity.this);
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
				Ui.showToast(FacebookLinkActivity.this, R.string.failure_to_update_user);
				setIsLoading(false);
				FacebookLinkActivity.this.finish();
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(FacebookLinkActivity.this);
				Log.d("User saved!");
				//TODO: Omniture Tracking...

				setIsLoading(false);
				finish();
			}

		}
	};

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
