package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
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

	private boolean mLoginClicked = false;

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

		Button button = (Button) view.findViewById(R.id.log_in_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				mLoginFailed.setVisibility(View.GONE);
				mLoginClicked = true;
				BackgroundDownloader.getInstance().startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		});
		View cancelButton = view.findViewById(R.id.cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				dismiss();
			}
		});

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
			if (response == null || response.hasErrors()) {
				//Dialog d = DialogUtils.createSimpleDialog(getActivity(), 0, mContext.getString(R.string.error_booking_title), response.getErrors().get(0).getPresentableMessage(mContext));
				//d.show();
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

