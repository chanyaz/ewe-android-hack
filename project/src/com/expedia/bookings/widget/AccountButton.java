package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;

public class AccountButton extends LinearLayout {
	private Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	private View mLogoutContainer;
	private View mErrorContainer;

	public AccountButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public AccountButton(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		mAccountLoadingContainer = findViewById(R.id.account_loading_container);
		mLoginContainer = findViewById(R.id.account_login_container);
		mLogoutContainer = findViewById(R.id.account_logout_container);
		mErrorContainer = findViewById(R.id.error_container);

		final OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					mListener.accountLoginClicked();
				}
			}
		};
		View loginButton = (View) mLoginContainer.findViewById(R.id.expedia_account_login);
		if (loginButton == null) {
			mLoginContainer.setOnClickListener(clickListener);
		}
		else {
			loginButton.setOnClickListener(clickListener);
		}

		View logoutButton = mLogoutContainer.findViewById(R.id.account_logout_logout_button);
		View loadingLogoutButton = mAccountLoadingContainer.findViewById(R.id.account_loading_logout_button);

		OnClickListener logoutListener = new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					mListener.accountLogoutClicked();
				}
			}
		};

		logoutButton.setOnClickListener(logoutListener);
		loadingLogoutButton.setOnClickListener(logoutListener);
	}

	public void setListener(AccountButtonClickListener listener) {
		mListener = listener;
	}

	public void bind(boolean isLoading, boolean isLoggedIn, User u) {
		Traveler traveler = null;
		if (u != null) {
			traveler = u.getPrimaryTraveler();
		}

		mErrorContainer.setVisibility(View.GONE);
		if (isLoading) {
			mAccountLoadingContainer.setVisibility(View.VISIBLE);
			mLoginContainer.setVisibility(View.GONE);
			mLogoutContainer.setVisibility(View.GONE);
		}
		else if (isLoggedIn) {
			ImageView card = (ImageView) mLogoutContainer.findViewById(R.id.card_icon);
			TextView top = (TextView) mLogoutContainer.findViewById(R.id.account_top_textview);
			TextView bottom = (TextView) mLogoutContainer.findViewById(R.id.account_bottom_textview);
			if (traveler.getLoyaltyMembershipNumber() == null) {
				// Normal user
				card.setImageResource(R.drawable.ic_logged_in_no_rewards);
				top.setText(mContext.getString(R.string.logged_in_as));
				bottom.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
			}
			else {
				// Rewards user
				card.setImageResource(R.drawable.ic_logged_in_with_rewards);
				top.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
				bottom.setText(mContext.getString(R.string.enrolled_in_expedia_rewards));
			}
			mLogoutContainer.setVisibility(View.VISIBLE);
			mAccountLoadingContainer.setVisibility(View.GONE);
			mLoginContainer.setVisibility(View.GONE);
		}
		else {
			mLoginContainer.setVisibility(View.VISIBLE);
			mLogoutContainer.setVisibility(View.GONE);
			mAccountLoadingContainer.setVisibility(View.GONE);
		}
	}

	public void error() {
		mAccountLoadingContainer.setVisibility(View.GONE);
		mLogoutContainer.setVisibility(View.GONE);

		// Show error and let user re-login easily
		mErrorContainer.setVisibility(View.VISIBLE);
		mLoginContainer.setVisibility(View.VISIBLE);
	}

	public interface AccountButtonClickListener {
		public void accountLoginClicked();

		public void accountLogoutClicked();
	}
}
