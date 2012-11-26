package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.utils.LocaleUtils;
import com.mobiata.android.util.Ui;

public class AccountButton extends LinearLayout {
	private Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	private View mLogoutContainer;
	private View mErrorContainer;
	private View mRewardsContainer;

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
		mRewardsContainer = findViewById(R.id.account_rewards_container);

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

	public void bind(boolean isLoading, boolean isLoggedIn, User u, boolean isFlights) {
		Traveler traveler = null;
		if (u != null) {
			traveler = u.getPrimaryTraveler();
		}

		mErrorContainer.setVisibility(View.GONE);
		if (mRewardsContainer != null) {
			mRewardsContainer.setVisibility(View.GONE);
		}

		if (isLoading) {
			mAccountLoadingContainer.setVisibility(View.VISIBLE);
			mLoginContainer.setVisibility(View.GONE);
			mLogoutContainer.setVisibility(View.GONE);
		}
		else if (isLoggedIn) {
			ImageView card = (ImageView) mLogoutContainer.findViewById(R.id.card_icon);
			TextView top = (TextView) mLogoutContainer.findViewById(R.id.account_top_textview);
			TextView bottom = (TextView) mLogoutContainer.findViewById(R.id.account_bottom_textview);

			if (isFlights) {
				//Flights
				if (traveler.getLoyaltyMembershipNumber() == null) {
					card.setImageResource(R.drawable.ic_expedia_logo);
					mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_logged_in);
					top.setText(mContext.getString(R.string.logged_in_as));
					bottom.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
				}
				else {
					card.setImageResource(R.drawable.ic_expedia_logo);
					top.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
					bottom.setText(mContext.getString(R.string.enrolled_in_expedia_rewards));
					if (mRewardsContainer != null && Db.getFlightSearch().getSelectedFlightTrip() != null
							&& !TextUtils.isEmpty(Db.getFlightSearch().getSelectedFlightTrip().getRewardsPoints())
							&& LocaleUtils.getPointOfSale().endsWith(".com")) {
						String rewardsString = String.format(
								getResources().getString(R.string.youll_earn_points_TEMPLATE), Db.getFlightSearch()
										.getSelectedFlightTrip().getRewardsPoints());
						TextView rewards = Ui.findView(mRewardsContainer, R.id.account_rewards_textview);
						rewards.setText(rewardsString);
						mRewardsContainer.setVisibility(View.VISIBLE);
						mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
					}
				}
			}
			else {
				//Hotels
				if (traveler.getLoyaltyMembershipNumber() == null) {
					card.setImageResource(R.drawable.ic_logged_in_no_rewards);
					top.setText(mContext.getString(R.string.logged_in_as));
					bottom.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
				}
				else {
					card.setImageResource(R.drawable.ic_expedia_logo);
					top.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
					bottom.setText(mContext.getString(R.string.enrolled_in_expedia_rewards));
				}
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

	public void bind(boolean isLoading, boolean isLoggedIn, User u) {
		bind(isLoading, isLoggedIn, u, false);
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
