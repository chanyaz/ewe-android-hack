package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Amobee;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;

public class SignInFragment extends DialogFragment {
	private static final String KEY_SIGNIN = "KEY_SIGNIN";

	private Context mContext;

	private TextView mLoginFailed;
	private EditText mUsernameEditText;
	private EditText mPasswordEditText;
	private Button mLogInButton;

	private ProgressDialog mProgressDialog;

	private boolean mLoginClicked = false;
	private boolean mEmptyUsername = true;
	private boolean mEmptyPassword = true;

	private final String SIGNIN_LOGIN_CLICKED = "SIGNIN_LOGIN_CLICKED";
	private final String SIGNIN_EMPTY_USERNAME = "SIGNIN_EMPTY_USERNAME";
	private final String SIGNIN_EMPTY_PASSWORD = "SIGNIN_EMPTY_PASSWORD";

	public static SignInFragment newInstance() {
		SignInFragment dialog = new SignInFragment();
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_sign_in, null);

		Dialog dialog;
		if (AndroidUtils.isTablet(mContext)) {
			dialog = new Dialog(getActivity(), R.style.Theme_Light_Fullscreen_Panel);
			dialog.requestWindowFeature(STYLE_NO_TITLE);
		}
		else {
			dialog = new Dialog(getActivity(), R.style.ExpediaLoginDialog);
		}
		dialog.setTitle(R.string.expedia_account);
		dialog.setContentView(view);

		mLoginFailed = (TextView) view.findViewById(R.id.login_failed_textview);
		mUsernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
		mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text);

		mPasswordEditText.setTypeface(Typeface.DEFAULT);
		mPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

		TextView forgotLink = (TextView) view.findViewById(R.id.forgot_your_password_link);
		forgotLink.setText(Html.fromHtml(String.format("<a href=\"http://www.%s/pub/agent.dll?qscr=apwd\">%s</a>",
				LocaleUtils.getPointOfSale(mContext), mContext.getString(R.string.forgot_your_password))));
		forgotLink.setMovementMethod(LinkMovementMethod.getInstance());

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
			return services.signIn(email, password);
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
				Db.setUser(response.getUser());
				Amobee.trackLogin();
				ExpediaServices.persistUserIsLoggedIn(mContext);
				((SignInFragmentListener) getActivity()).onLoginCompleted();
				dismiss();
			}
		}
	};

	public interface SignInFragmentListener {
		public void onLoginStarted();

		public void onLoginCompleted();

		public void onLoginFailed();
	}
}
