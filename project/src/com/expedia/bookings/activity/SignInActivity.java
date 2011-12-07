package com.expedia.bookings.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.DialogUtils;

public class SignInActivity extends Activity {

	private static final int DIALOG_PROGRESS = 1;
	private static final int DIALOG_ERROR = 2;

	private static final String KEY_SIGNIN = "KEY_SIGNIN";

	private Context mContext;

	private EditText mUsernameEditText;
	private EditText mPasswordEditText;

	private String mError;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		setContentView(R.layout.activity_sign_in);

		mUsernameEditText = (EditText) findViewById(R.id.username_edit_text);
		mPasswordEditText = (EditText) findViewById(R.id.password_edit_text);

		// TODO: DELETE THIS
		// Temporarily enter our test username/password so this isn't a pain to test
		mUsernameEditText.setText("mobiatatest@gmail.com");
		mPasswordEditText.setText("testpassword");

		Button loginButton = (Button) findViewById(R.id.log_in_button);
		loginButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_PROGRESS);
				BackgroundDownloader.getInstance().startDownload(KEY_SIGNIN, mLoginDownload, mLoginCallback);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();

		BackgroundDownloader.getInstance().registerDownloadCallback(KEY_SIGNIN, mLoginCallback);
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_SIGNIN);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS: {
			ProgressDialog pd = new ProgressDialog(this);
			pd.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					BackgroundDownloader.getInstance().cancelDownload(KEY_SIGNIN);
				}
			});
			pd.setMessage(getString(R.string.logging_in));
			return pd;
		}
		case DIALOG_ERROR: {
			return DialogUtils.createSimpleDialog(this, DIALOG_ERROR, null, mError);
		}
		}
		return super.onCreateDialog(id);
	}

	private final Download mLoginDownload = new Download() {
		public Object doDownload() {
			String email = mUsernameEditText.getText().toString();
			String password = mPasswordEditText.getText().toString();

			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_SIGNIN, services);
			return services.signIn(SignInActivity.this, email, password);
		}
	};

	private final OnDownloadComplete mLoginCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			dismissDialog(DIALOG_PROGRESS);

			SignInResponse response = (SignInResponse) results;

			if (response == null) {
				mError = "Got a null log in response.";
				showDialog(DIALOG_ERROR);
			}
			else if (response.hasErrors()) {
				mError = response.getErrors().get(0).getPresentableMessage(mContext);
				showDialog(DIALOG_ERROR);
			}
			else {
				Toast.makeText(mContext, R.string.login_success, Toast.LENGTH_LONG).show();
				finish();
			}
		}
	};
}
