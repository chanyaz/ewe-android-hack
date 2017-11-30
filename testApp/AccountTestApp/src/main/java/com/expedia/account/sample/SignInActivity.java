package com.expedia.account.sample;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.expedia.account.AccountService;
import com.expedia.account.AccountView;
import com.expedia.account.Config;
import com.expedia.account.PanningImageView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import timber.log.Timber;

public class SignInActivity extends FragmentActivity {

	@InjectView(R.id.account_view)
	public MockAccountView vAccountView;

	@InjectView(R.id.background)
	public PanningImageView vBackground;

	@InjectView(R.id.white_background)
	public View vWhiteBackground;

	@InjectView(R.id.build_date)
	public TextView vBuildDate;

	private Config config;

	private DialogInterface.OnClickListener endpointListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

			config = getAccountViewConfig();
			int siteId = 0;
			int langId = 0;
			String clientId = "accountstest.phone.android";

			switch (getResources().getStringArray(R.array.endpoints)[which]) {
			case "Production":
				config.setService(new OkHttpClient(), "https://www.expedia.com/", siteId, langId, clientId);
				break;
			case "Integration":
				config.setService(InsecureHttpClient.newInstance(),
					"https://wwwexpediacom.integration.sb.karmalab.net/", siteId, langId, clientId);
				break;
			case "Mock Mode":
				config.setService(new AccountService(new MockExpediaAccountApi(), siteId, langId, clientId));
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
			if (getResources().getStringArray(R.array.signuppath)[which].equals("Single Page")) {
				config.setEnableSinglePageSignUp(true);
			}
			else {
				config.setEnableSinglePageSignUp(false);
			}
			vAccountView.configure(config);
		}
	};

	private Config getAccountViewConfig() {
		Config config = Config.build()
			.setBackgroundImageView(vBackground)
			.setPOSEnableSpamByDefault(true)
			.setPOSShowSpamOptIn(true)
			.setAutoEnrollUserInRewards(false)
			.setUserRewardsEnrollmentCheck(true)
			.setEnableFacebookButton(true)
			.setEnableSignInMessaging(true)
			.setTOSText(Html.fromHtml(getString(R.string.terms_of_service)))
			.setMarketingText(getString(R.string.agree_to_spam))
			.setSignInMessagingText(getString(R.string.sign_in_messaging))
			.setFacebookAppId(getString(R.string.facebook_app_id))
			.setRewardsText(Html.fromHtml(getString(R.string.loyalty_terms_of_service)))
			.setListener(mAccountViewListener)
			.setAnalyticsListener(null)
			.setInitialState(Config.InitialState.SignIn)
			.setParentActivity(this);
		return config;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_expedia_sign_in);
		showTransparentStatusBar();
		ButterKnife.inject(this);

		try {
			ApplicationInfo ai = this.getPackageManager().getApplicationInfo(this.getPackageName(), 0);
			ZipFile zf = new ZipFile(ai.sourceDir);
			ZipEntry ze = zf.getEntry("classes.dex");
			long time = ze.getTime();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
			sdf.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));

			vBuildDate.setText(String.format("Built %s in San Francisco", sdf.format(new Date(time))));
		}
		catch (Exception ignored) {
		}

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

	@Override
	public void onBackPressed() {
		boolean isStatusAlreadyShown = vAccountView.getVisibility() == View.VISIBLE;
		if (!vAccountView.back() && !isStatusAlreadyShown) {
			super.onBackPressed();
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
		vAccountView.onActivityResult(requestCode, resultCode, data);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void showTransparentStatusBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
				WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	AccountView.Listener mAccountViewListener = new AccountView.Listener() {
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
		public void onFacebookClicked(){
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
			String packageName = SignInActivity.class.getPackage().getName();
			PackageInfo info = getPackageManager().getPackageInfo(
				packageName, PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Timber.d("Facebook Info: "
					+ "Package: " + packageName + ", "
					+ "KeyHash: " + Base64.encodeToString(md.digest(), Base64.DEFAULT));
			}
		}
		catch (PackageManager.NameNotFoundException e) {
		}
		catch (NoSuchAlgorithmException e) {
		}
	}


}