package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.server.ExpediaServices;

public class AccountButton {
	private Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	private View mLogoutContainer;

	public AccountButton (Context context, AccountButtonClickListener listener, View rootView) {
		mContext = context;
		mListener = listener;
		mAccountLoadingContainer = rootView.findViewById(R.id.account_loading_container);
		mLoginContainer = rootView.findViewById(R.id.account_login_container);
		mLogoutContainer = rootView.findViewById(R.id.account_logout_container);

		final OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				mListener.accountLoginClicked();
			}
		};
		TextView loginButton = (TextView) mLoginContainer.findViewById(R.id.expedia_login_textview);
		if (loginButton == null) {
			mLoginContainer.setOnClickListener(clickListener);
		}
		else {
			loginButton.setOnClickListener(clickListener);
		}

		View logoutButton = mLogoutContainer.findViewById(R.id.logout_button);

		OnClickListener logoutListener = new OnClickListener() {
			public void onClick(View v) {
				mListener.accountLogoutClicked();
			}
		};

		logoutButton.setOnClickListener(logoutListener);
	}

	public void update(boolean isLoading) {
		mAccountLoadingContainer.setVisibility(View.GONE);
		mLoginContainer.setVisibility(View.GONE);
		mLogoutContainer.setVisibility(View.GONE);

		if (isLoading) {
			mAccountLoadingContainer.setVisibility(View.VISIBLE);
			return;
		}

		if (ExpediaServices.isLoggedIn(mContext)) {
			User u = Db.getUser();
			ImageView card = (ImageView) mLogoutContainer.findViewById(R.id.card_icon);
			TextView top = (TextView) mLogoutContainer.findViewById(R.id.account_top_textview);
			TextView bottom = (TextView) mLogoutContainer.findViewById(R.id.account_bottom_textview);
			if (u.getLoyaltyMembershipNumber() == null) {
				// Normal user
				card.setImageResource(R.drawable.ic_logged_in_no_rewards);
				top.setText(mContext.getString(R.string.logged_in_as));
				bottom.setText(Html.fromHtml("<b>" + u.getEmail() + "</b>"));
			}
			else {
				// Rewards user
				card.setImageResource(R.drawable.ic_logged_in_with_rewards);
				top.setText(Html.fromHtml("<b>" + u.getEmail() + "</b>"));
				bottom.setText(mContext.getString(R.string.enrolled_in_expedia_rewards));
			}
			mLogoutContainer.setVisibility(View.VISIBLE);
		}
		else {
			mLoginContainer.setVisibility(View.VISIBLE);
		}
	}

	public interface AccountButtonClickListener {
		public void accountLoginClicked();
		public void accountLogoutClicked();
	}
}
