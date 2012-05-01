package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public class SignInFragment extends DialogFragment {
	private static final String KEY_SIGNIN = "KEY_SIGNIN";

	private Context mContext;

	private EditText mUsernameEditText;
	private EditText mPasswordEditText;

	public static SignInFragment newInstance() {
		SignInFragment dialog = new SignInFragment();
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.fragment_sign_in, null);

		Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Fullscreen_Panel);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setContentView(view);

		mUsernameEditText = (EditText) view.findViewById(R.id.username_edit_text);
		mPasswordEditText = (EditText) view.findViewById(R.id.password_edit_text);

		Button button = (Button) view.findViewById(R.id.log_in_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick (View v) {
				BackgroundDownloader.getInstance().startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		});

		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (!(activity instanceof SignInFragmentListener)) {
			throw new RuntimeException("Activity must implement SignInFragmentListener!");
		}
		mContext = (Context) activity;
	}

	private final Download mLoginDownload = new Download() {
		public Object doDownload() {
			String email = mUsernameEditText.getText().toString();
			String password = mPasswordEditText.getText().toString();

			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SIGNIN, services);
			return services.signIn(email, password);
		}
	};

	private final OnDownloadComplete mLoginCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			SignInResponse response = (SignInResponse) results;

			if (response == null || response.hasErrors()) {
				((SignInFragmentListener) getActivity()).onLoginFailed();
			}
			else {
				Db.setUser(response.getUser());
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

