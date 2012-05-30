package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.DialogUtils;

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
			dialog = new Dialog(getActivity());
		}
		dialog.setTitle(R.string.expedia_account);
		dialog.setContentView(view);

		mLoginFailed = (TextView) view.findViewById(R.id.login_failed_textview);
		mUsernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
		mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text);

		TextView forgotLink = (TextView) view.findViewById(R.id.forgot_your_password_link);
		forgotLink.setText(Html.fromHtml(String.format("<a href=\"http://www.%s/pub/agent.dll?qscr=apwd\">%s</a>", LocaleUtils.getPointOfSale(mContext), mContext.getString(R.string.forgot_your_password))));
		forgotLink.setMovementMethod(LinkMovementMethod.getInstance());

		mLogInButton = (Button) view.findViewById(R.id.log_in_button);
		mLogInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				mLoginFailed.setVisibility(View.GONE);
				mLoginClicked = true;
				mProgressDialog = new ProgressDialog(mContext);
				mProgressDialog.setMessage(getString(R.string.logging_in));
				mProgressDialog.setCancelable(false);
				mProgressDialog.show();
				BackgroundDownloader.getInstance().startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		});
		mLogInButton.setEnabled(false);
		View cancelButton = view.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
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
	public void onPause() {
		super.onPause();
		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_SIGNIN, mLoginCallback);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mLoginClicked) {
			BackgroundDownloader.getInstance().registerDownloadCallback(KEY_SIGNIN, mLoginCallback);
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
			if (response == null || response.hasErrors()) {
				mLoginFailed.setVisibility(View.VISIBLE);
				((SignInFragmentListener) getActivity()).onLoginFailed();
			}
			else {
				Db.setUser(response.getUser());
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

