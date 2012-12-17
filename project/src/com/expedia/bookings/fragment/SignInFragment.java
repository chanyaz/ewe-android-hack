package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FacebookLinkActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

public class SignInFragment extends DialogFragment {
	private static final String KEY_SIGNIN = "KEY_SIGNIN";

	private Context mContext;

	private TextView mLoginFailed;
	private EditText mUsernameEditText;
	private EditText mPasswordEditText;
	private Button mLogInButton;
	private View mFacebookButton;

	private ProgressDialog mProgressDialog;

	private boolean mLoginClicked = false;
	private boolean mEmptyUsername = true;
	private boolean mEmptyPassword = true;

	private boolean mIsFlights = false;

	private final String SIGNIN_LOGIN_CLICKED = "SIGNIN_LOGIN_CLICKED";
	private final String SIGNIN_EMPTY_USERNAME = "SIGNIN_EMPTY_USERNAME";
	private final String SIGNIN_EMPTY_PASSWORD = "SIGNIN_EMPTY_PASSWORD";

	public static SignInFragment newInstance(boolean isFlights) {
		SignInFragment dialog = new SignInFragment();
		dialog.mIsFlights = isFlights;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_sign_in, null);

		int themeResId = AndroidUtils.isTablet(mContext) ? R.style.Theme_Light_Fullscreen_Panel
				: R.style.ExpediaLoginDialog;
		Dialog dialog = new Dialog(getActivity(), themeResId);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setContentView(view);

		mLoginFailed = (TextView) view.findViewById(R.id.login_failed_textview);
		mUsernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
		mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text);

		mPasswordEditText.setTypeface(Typeface.DEFAULT);
		mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

		mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {
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

		TextView forgotLink = (TextView) view.findViewById(R.id.forgot_your_password_link);
		forgotLink.setText(Html.fromHtml(String.format("<a href=\"http://www.%s/pub/agent.dll?qscr=apwd\">%s</a>",
				PointOfSale.getPointOfSale().getUrl(), mContext.getString(R.string.forgot_your_password))));
		forgotLink.setMovementMethod(LinkMovementMethod.getInstance());
		forgotLink.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mIsFlights) {
					OmnitureTracking.trackLinkFlightCheckoutLoginForgot(mContext);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginForgot(mContext);
				}
			}
		});

		mProgressDialog = new ProgressDialog(mContext);
		mProgressDialog.setMessage(getString(R.string.logging_in));
		mProgressDialog.setCancelable(false);
		if (savedInstanceState != null) {
			mLoginClicked = savedInstanceState.getBoolean(SIGNIN_LOGIN_CLICKED, false);
			mEmptyUsername = savedInstanceState.getBoolean(SIGNIN_EMPTY_USERNAME, true);
			mEmptyPassword = savedInstanceState.getBoolean(SIGNIN_EMPTY_PASSWORD, true);
		}

		mLogInButton = (Button) view.findViewById(R.id.log_in_button);
		mLogInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mLoginFailed.setVisibility(View.GONE);
				mLoginClicked = true;
				startOrResumeDownload();
			}
		});
		mLogInButton.setEnabled(false);
		View cancelButton = view.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();

				if (mIsFlights) {
					OmnitureTracking.trackLinkFlightCheckoutLoginCancel(mContext);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginCancel(mContext);
				}
			}
		});
		
		
		mFacebookButton = Ui.findView(view, R.id.facebook_connect_button);
		mFacebookButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent facebookLoginIntent = new Intent(getActivity(), FacebookLinkActivity.class);
				startActivity(facebookLoginIntent);
				SignInFragment.this.dismiss();
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
				mLogInButton.setEnabled(!(mEmptyUsername || mEmptyPassword));
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
				mLogInButton.setEnabled(!(mEmptyUsername || mEmptyPassword));
			}
		};
		mUsernameEditText.addTextChangedListener(usernameWatcher);
		mPasswordEditText.addTextChangedListener(passwordWatcher);

		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(SIGNIN_LOGIN_CLICKED, mLoginClicked);
		outState.putBoolean(SIGNIN_EMPTY_USERNAME, mEmptyUsername);
		outState.putBoolean(SIGNIN_EMPTY_PASSWORD, mEmptyPassword);
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_SIGNIN, mLoginCallback);
	}

	@Override
	public void onResume() {
		super.onResume();
		startOrResumeDownload();

		//Show the soft keyboard
		if (getDialog() != null) {
			View focused = this.getDialog().getCurrentFocus();
			if (focused == null || !(focused instanceof EditText)) {
				focused = Ui.findView(this.getDialog(), R.id.username_edit_text);
			}
			final View finalFocused = focused;
			if (finalFocused != null && finalFocused instanceof EditText) {
				finalFocused.postDelayed(new Runnable() {
					@Override
					public void run() {
						//Dumb but effective - show the keyboard by emulating a click on the view
						finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0));
						finalFocused.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
								SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0));
					}
				}, 200);
			}
		}

	}

	public void startOrResumeDownload() {
		if (mLoginClicked) {
			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (bd.isDownloading(KEY_SIGNIN)) {
				bd.registerDownloadCallback(KEY_SIGNIN, mLoginCallback);
			}
			else {
				bd.startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof SignInFragmentListener)) {
			throw new RuntimeException("Activity must implement SignInFragmentListener!");
		}
		mContext = (Context) activity;
	}

	private final Download<SignInResponse> mLoginDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			String email = mUsernameEditText.getText().toString();
			String password = mPasswordEditText.getText().toString();

			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SIGNIN, services);
			return services.signIn(email, password, ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mLoginCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse response) {
			mProgressDialog.dismiss();
			mLoginClicked = false;
			if (response == null || response.hasErrors()) {
				mPasswordEditText.setText("");
				mLoginFailed.setVisibility(View.VISIBLE);
				((SignInFragmentListener) getActivity()).onLoginFailed();
			}
			else {
				User user = response.getUser();
				Db.setUser(user);
				AdTracker.trackLogin();
				user.save(mContext);
				((SignInFragmentListener) getActivity()).onLoginCompleted();
				dismiss();

				if (mIsFlights) {
					OmnitureTracking.trackLinkFlightCheckoutLoginSuccess(mContext);
				}
				else {
					OmnitureTracking.trackLinkHotelsCheckoutLoginSuccess(mContext);
				}
			}
		}
	};

	public interface SignInFragmentListener {
		public void onLoginStarted();

		public void onLoginCompleted();

		public void onLoginFailed();
	}
}
