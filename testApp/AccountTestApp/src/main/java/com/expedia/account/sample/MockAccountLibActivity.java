package com.expedia.account.sample;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import com.expedia.account.AccountService;
import com.expedia.account.AccountSignInListener;
import com.expedia.account.AnalyticsListener;
import com.expedia.account.Config;
import com.expedia.account.MockAccountService;
import com.expedia.account.NewAccountView;
import com.expedia.account.PanningImageView;
import com.mobiata.android.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;

public class MockAccountLibActivity extends FragmentActivity {

	@InjectView(R.id.account_view)
	public MockAccountView vAccountView;

	@InjectView(R.id.new_account_view)
	public MockNewAccountView newAccountView;

	@InjectView(R.id.background)
	public PanningImageView vBackground;

	@InjectView(R.id.white_background)
	public View vWhiteBackground;

	@InjectView(R.id.build_date)
	public TextView vBuildDate;

	private Config config;

	private boolean isNewSignInEnabled = false;

	private NavigationListener navigationListener = new NavigationListener();

	private DialogInterface.OnClickListener endpointListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

			config = getAccountViewConfig();
			int siteId = 0;
			int langId = 0;
			String clientId = "accountstest.phone.android";
			String userAgent = "AccountSampleApp/1.0";

			switch (getResources().getStringArray(R.array.endpoints)[which]) {
			case "Production":
				config.setService(new AccountService(new OkHttpClient(),
					"https://www.expedia.com/", siteId, langId, clientId, userAgent,
					Schedulers.io(), AndroidSchedulers.mainThread()));
				break;
			case "Integration":
				config.setService(new AccountService(InsecureHttpClient.newInstance(),
					"https://wwwexpediacom.integration.sb.karmalab.net/", siteId, langId, clientId, userAgent,
					Schedulers.io(), AndroidSchedulers.mainThread()));
				break;
			case "Mock Mode":
				config.setService(new MockAccountService(siteId, langId, clientId));
				newAccountView.setMockMode(true);
				vAccountView.setMockMode(true);
				break;
			default:
				throw new RuntimeException("Endpoint not handled");
			}

			informFacebookKeyHash();

		}
	};

	private DialogInterface.OnClickListener signupPathListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			if (getResources().getStringArray(R.array.signuppath)[which].equals("New Account Page")) {
				config.setInitialTab(NewAccountView.AccountTab.SIGN_IN);
				newAccountView.setVisibility(View.VISIBLE);
				getWindow().setStatusBarColor(getResources().getColor(R.color.brand_primary_dark));
				newAccountView.setNavigationOnClickListener(navigationListener);
				config.setInitialTab(NewAccountView.AccountTab.SIGN_IN);
				newAccountView.setupConfig(config);
				isNewSignInEnabled = true;
			} else {
				vAccountView.setVisibility(View.VISIBLE);
				vAccountView.configure(config);
			}
		}
	};

	private Config getAccountViewConfig() {
		return Config.build()
			.setBackgroundImageView(vBackground)
			.setPOSEnableSpamByDefault(true)
			.setPOSShowSpamOptIn(true)
			.setUserRewardsEnrollmentCheck(true)
			.setEnableFacebookSignIn(true)
			.setEnableSignInMessaging(true)
			.setTOSText(Html.fromHtml(getString(R.string.terms_of_service)))
			.setMarketingText(getString(R.string.agree_to_spam))
			.setSignInMessagingText(getString(R.string.sign_in_messaging))
			.setFacebookAppId(getString(R.string.facebook_app_id))
			.setRewardsText(Html.fromHtml(getString(R.string.loyalty_terms_of_service)))
			.setNewTermsText(Html.fromHtml(getString(R.string.new_account_terms_text)))
			.setListener(mAccountViewListener)
			.setAnalyticsListener(analyticsListener)
			.setInitialState(Config.InitialState.SignIn)
			.setInitialTab(NewAccountView.AccountTab.SIGN_IN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_expedia_sign_in);
		ButterKnife.inject(this);

		String timeString = getAppBuildTimeString();
		vBuildDate.setText(String.format("Built %s", timeString));

		new AlertDialog.Builder(this)
			.setTitle("Sign Up Path")
			.setItems(R.array.signuppath, signupPathListener)
			.setCancelable(false)
			.create()
			.show();

		new AlertDialog.Builder(this)
			.setTitle(R.string.pick_server)
			.setItems(R.array.endpoints, endpointListener)
			.setCancelable(false)
			.create()
			.show();

		vAccountView.setWhiteBackgroundFromActivity(vWhiteBackground);
	}

	private String getAppBuildTimeString() {
		SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm z", Locale.US);

		try {
			ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();

			return dateTimeFormatter.format(time);
		}
		catch (Exception e) {
			return "with love";
		}
	}

	@Override
	public void onBackPressed() {
		if (isNewSignInEnabled) {
			if (newAccountView.isOnSignInPage()) {
				super.onBackPressed();
			} else {
				newAccountView.cancelFacebookLinkAccountsView();
			}
		} else {
			boolean isStatusAlreadyShown = vAccountView.getVisibility() == View.VISIBLE;
			if (!vAccountView.back() && !isStatusAlreadyShown) {
				super.onBackPressed();
			}
		}

	}

	@OnClick(R.id.button_done)
	public void onClickButtonDone() {
		finish();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Required for Facebook
		if (isNewSignInEnabled) {
			newAccountView.onActivityResult(requestCode, resultCode, data);
		} else {
			vAccountView.onActivityResult(requestCode, resultCode, data);
		}
	}

	AccountSignInListener mAccountViewListener = new AccountSignInListener() {
		@Override
		public void onSignInSuccessful() {
			showStatus(R.string.Success, R.string.Youre_all_signed_in);
		}

		@Override
		public void onFacebookSignInSuccess() {
			// do nothing
		}

		@Override
		public void onSignInCancelled() {
			showStatus(R.string.Cancelled, R.string.Sign_in_was_cancelled);
			findViewById(R.id.button_signout).setVisibility(View.INVISIBLE);
		}

		@Override
		public void onFacebookRequested() {
			showStatus(R.string.Facebook, R.string.Facebook_requested);
		}

		@Override
		public void onForgotPassword() {
			showStatus(R.string.acct__Password, R.string.Password_was_forgotten);
		}

		@Override
		public void onRecaptchaError(Throwable e) {
		}
	};

	private void showStatus(@StringRes int title, @StringRes int message) {
		vAccountView.setVisibility(View.INVISIBLE);
		findViewById(R.id.sign_in_status_frame).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.sign_in_status_title)).setText(title);
		((TextView) findViewById(R.id.sign_in_status_message)).setText(message);
		findViewById(R.id.sign_in_status_thumbs_up).setRotation(((float) (Math.random() * 360)));
	}

	private void informFacebookKeyHash() {
		// Add code to print out the key hash and stick that here:
		// https://developers.facebook.com/settings/developer/sample-app/
		try {
			String packageName = MockAccountLibActivity.class.getPackage().getName();
			PackageInfo info = getPackageManager().getPackageInfo(
				packageName, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.d("Facebook Info: "
					+ "Package: " + packageName + ", "
					+ "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		}
		catch (PackageManager.NameNotFoundException e) {
		}
		catch (NoSuchAlgorithmException e) {
		}
	}

	public class NavigationListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			finish();
		}
	}

	private AnalyticsListener analyticsListener = new AnalyticsListener() {
		@Override
		public void signInButtonClicked() { }

		@Override
		public void facebookSignInButtonClicked() { }

		@Override
		public void googleSignInButtonClicked() {
		}

		@Override
		public void createButtonClicked() { }

		@Override
		public void newSignInTabClicked() { }

		@Override
		public void newCreateAccountTabClicked() { }

		@Override
		public void signInSucceeded() { }

		@Override
		public void accountCreationAttemptWithPreexistingEmail(boolean useExisting, boolean createNew) { }

		@Override
		public void userViewedNameEntering() { }

		@Override
		public void userViewedPasswordEntering() { }

		@Override
		public void userViewedTosPage() { }

		@Override
		public void userViewedSinglePage() { }

		@Override
		public void userExplicitlyModifiedMarketingOptIn(boolean optIn) { }

		@Override
		public void userSucceededInCreatingAccount(boolean userIsEnrolledForRewards) { }

		@Override
		public void userReceivedErrorOnSignInAttempt(String failureReason) { }

		@Override
		public void userReceivedErrorOnAccountCreationAttempt(String failureReason) { }
	};
}
