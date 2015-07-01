package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.expedia.account.AccountView;
import com.expedia.account.Config;
import com.expedia.account.PanningImageView;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.utils.LoginExtender;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.expedia.bookings.widget.TextView;
import com.facebook.Session;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AccountLibActivity extends AppCompatActivity
	implements UserAccountRefresher.IUserAccountRefreshListener, LoginExtenderListener {
	public static final String ARG_BUNDLE = "ARG_BUNDLE";
	public static final String ARG_PATH_MODE = "ARG_PATH_MODE";
	public static final String ARG_LOGIN_FRAGMENT_EXTENDER = "ARG_LOGIN_FRAGMENT_EXTENDER";

	@InjectView(R.id.parallax_view)
	public PanningImageView background;

	@InjectView(R.id.account_view)
	public AccountView accountView;

	@InjectView(R.id.login_extension_container)
	public LinearLayout loginExtenderContainer;

	@InjectView(R.id.extender_status)
	public TextView extenderStatus;

	private LineOfBusiness lob = LineOfBusiness.HOTELS;
	private LoginExtender loginExtender;
	private UserAccountRefresher userAccountRefresher;

	public static Intent createIntent(Context context, Bundle bundle) {
		Intent loginIntent = new Intent(context, AccountLibActivity.class);
		if (bundle != null) {
			loginIntent.putExtra(ARG_BUNDLE, bundle);
		}
		return loginIntent;
	}

	public static Bundle createArgumentsBundle(LineOfBusiness pathMode, LoginExtender extender) {
		Bundle bundle = new Bundle();
		bundle.putString(AccountLibActivity.ARG_PATH_MODE, pathMode.name());
		if (extender != null) {
			bundle.putBundle(ARG_LOGIN_FRAGMENT_EXTENDER, extender.buildStateBundle());
		}
		return bundle;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		setContentView(R.layout.account_lib_activity);
		ButterKnife.inject(this);

		Intent intent = getIntent();
		if (intent.hasExtra(ARG_BUNDLE)) {
			Bundle args = intent.getBundleExtra(ARG_BUNDLE);
			if (args.containsKey(ARG_PATH_MODE)) {
				lob = LineOfBusiness.valueOf(args.getString(ARG_PATH_MODE));
			}
			if (args.containsKey(ARG_LOGIN_FRAGMENT_EXTENDER)) {
				loginExtender = LoginExtender.buildLoginExtenderFromState(args.getBundle(ARG_LOGIN_FRAGMENT_EXTENDER));
			}
		}

		accountView.configure(Config.build()
				.setApi(Ui.getApplication(this).appComponent().accountApi())
				.setSiteId(PointOfSale.getPointOfSale().getSiteId())
				.setLangId(PointOfSale.getPointOfSale().getDualLanguageId())
				.setBackgroundImageView(background)
				.setPOSEnableSpamByDefault(true)
				.setPOSShowSpamOptIn(true)
				.setClientId("accountstest.phone.android")
				.setListener(new Listener())
				.setAnalyticsListener(null)
		);

		userAccountRefresher = new UserAccountRefresher(this, lob, this);
	}

	@Override
	public void onUserAccountRefreshed() {
		User.addUserToAccountManager(this, Db.getUser());
		if (User.isLoggedIn(this) && loginExtender != null) {
			loginExtenderContainer.setVisibility(View.VISIBLE);
			loginExtender.onLoginComplete(this, this, loginExtenderContainer);
		}
		else {
			finish();
		}
	}

	@Override
	public void loginExtenderWorkComplete(LoginExtender extender) {
		finish();
	}

	@Override
	public void setExtenderStatus(String status) {
		extenderStatus.setText(status);
	}

	public class Listener extends AccountView.Listener {

		@Override
		public void onSignInSuccessful() {
			// Do stuff with User
			userAccountRefresher.ensureAccountIsRefreshed();
		}

		@Override
		public void onSignInCancelled() {
			// e.g. close this activity
			finish();
		}

		@Override
		public void onFacebookRequested() {
		}

		@Override
		public void onForgotPassword() {
			// This is called when the "Forgot your password" button is tapped
		}
	}

	public interface LogInListener {
		void onLoginCompleted();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}
}
