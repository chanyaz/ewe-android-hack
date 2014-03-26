package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.Resources;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

public class AccountButton extends LinearLayout {
	private Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	private TextView mLoginTextView;
	private View mLogoutContainer;
	private View mErrorContainer;
	private View mRewardsContainer;
	private View mLogoutButton;
	private View mLoadingLogoutButton;
	private ImageView mExpediaLogo;

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
		mLoginTextView = Ui.findView(this, R.id.login_text_view);
		mLogoutContainer = findViewById(R.id.account_logout_container);
		mErrorContainer = findViewById(R.id.error_container);
		mRewardsContainer = findViewById(R.id.account_rewards_container);
		mExpediaLogo = Ui.findView(this, R.id.card_icon);

		final OnClickListener clickListener = new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					mListener.accountLoginClicked();
				}
			}
		};

		mLoginContainer.setOnClickListener(clickListener);

		mLogoutButton = mLogoutContainer.findViewById(R.id.account_logout_logout_button);
		mLoadingLogoutButton = mAccountLoadingContainer.findViewById(R.id.account_loading_logout_button);

		OnClickListener logoutListener = new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					mListener.accountLogoutClicked();
				}
			}
		};

		mLogoutButton.setOnClickListener(logoutListener);
		mLoadingLogoutButton.setOnClickListener(logoutListener);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		Ui.setEnabled(mLoginContainer, enabled);
		Ui.setEnabled(mLogoutButton, enabled);
		Ui.setEnabled(mLoadingLogoutButton, enabled);
	}

	public void setListener(AccountButtonClickListener listener) {
		mListener = listener;
	}

	public void bind(boolean isLoading, boolean isLoggedIn, User u, LineOfBusiness lob) {
		Traveler traveler = null;
		if (u != null) {
			traveler = u.getPrimaryTraveler();
		}

		boolean isElitePlusMember = isLoggedIn && traveler != null && traveler.getIsElitePlusMember();

		// Errors container
		if (mErrorContainer != null) {
			mErrorContainer.setVisibility(View.GONE);
		}

		// Rewards container
		if (mRewardsContainer != null) {
			mRewardsContainer.setVisibility(View.GONE);
			if (isLoggedIn && !isElitePlusMember) {
				mRewardsContainer.setBackgroundResource(R.drawable.bg_checkout_information_rewards_tab);
			}
		}

		// Loading container
		mAccountLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		// Login container
		mLoginContainer.setVisibility(!isLoading && !isLoggedIn ? View.VISIBLE : View.GONE);
		if (!isLoggedIn) {
			bindLoginContainer(lob);
		}

		// Logout container
		mLogoutContainer.setVisibility(!isLoading && isLoggedIn ? View.VISIBLE : View.GONE);
		if (isLoggedIn) {
			bindLogoutContainer(traveler, lob, isElitePlusMember);
		}
	}

	// Do some runtime styling, based on whether this is a hotels or flights button
	private void bindLoginContainer(LineOfBusiness lob) {
		boolean isFlights = lob == LineOfBusiness.FLIGHTS;
		boolean isTablet = AndroidUtils.isTablet(getContext());

		Resources res = getResources();
		if (isTablet) {
			mLoginContainer.setBackgroundResource(R.drawable.bg_checkout_information_single);
			Ui.findView(mLoginContainer, R.id.login_blurb).setVisibility(View.VISIBLE);
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tablet_checkout_expedia_logo, 0, 0, 0);
		}
		else if (ExpediaBookingApp.IS_EXPEDIA) {
			mLoginContainer.setBackgroundResource(isFlights
				? R.drawable.btn_login_flights
				: R.drawable.btn_login_hotels);
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(isFlights
				? R.drawable.ic_expedia_logo
				: Ui.obtainThemeResID(mContext, R.attr.hotelCheckoutLoginLogoDrawable), 0, 0, 0);
		}
		else {
			mLoginContainer.setBackgroundResource(R.drawable.btn_login_hotels);
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(
				Ui.obtainThemeResID(mContext, R.attr.hotelCheckoutLoginLogoDrawable), 0, 0, 0);
		}
		mLoginTextView.setTextColor(res.getColor(isTablet
			? R.color.login_text_tablet
			: isFlights
			? R.color.login_text_flight
			: R.color.login_text_hotels));
	}

	private void bindLogoutContainer(Traveler traveler, LineOfBusiness lob, boolean isElitePlusMember) {
		boolean isFlights = lob == LineOfBusiness.FLIGHTS;
		boolean isTablet = AndroidUtils.isTablet(getContext());
		boolean hasLoyaltyMembership = traveler.getLoyaltyMembershipNumber() != null;

		mLogoutContainer.setBackgroundResource(
			isTablet ? R.drawable.bg_checkout_information_single
				: isFlights ? R.drawable.bg_checkout_logged_in
				: R.drawable.bg_hotel_checkout_information);

		TextView top = Ui.findView(mLogoutContainer, R.id.account_top_textview);
		TextView bottom = Ui.findView(mLogoutContainer, R.id.account_bottom_textview);

		if (!isElitePlusMember) {
			mExpediaLogo.setImageResource(Ui.obtainThemeResID(mContext, R.attr.hotelCheckoutLogoutLogoDrawable));
		}

		// Tablet
		if (isTablet) {
			top.setText(traveler.getEmail());

			String points = "";
			if (isFlights) {
				FlightTrip flightTrip = Db.getFlightSearch().getSelectedFlightTrip();
				points = flightTrip == null ? "" : flightTrip.getRewardsPoints();
			}
			else {
				//TODO: do we know points for hotel stays?
			}

			bottom.setText(!TextUtils.isEmpty(points)
				? String.format(mContext.getString(R.string.x_points_for_this_trip_TEMPLATE), points)
				: isElitePlusMember
				? mContext.getString(R.string.youll_earn_bonus_points_for_this_booking)
				: mContext.getString(R.string.enrolled_in_expedia_rewards));
		}

		// Flights + Membership
		else if (isFlights && hasLoyaltyMembership) {
			mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_logged_in);
			top.setText(mContext.getString(R.string.logged_in_as));
			bottom.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
		}

		// Flights + NO Membership
		else if (isFlights) {
			top.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
			int resId = isElitePlusMember ? R.string.enrolled_in_expedia_elite_plus_rewards
				: R.string.enrolled_in_expedia_rewards;
			bottom.setText(mContext.getString(resId));

			FlightTrip flightTrip = Db.getFlightSearch().getSelectedFlightTrip();
			String points = flightTrip == null ? "" : flightTrip.getRewardsPoints();
			boolean USA = PointOfSale.getPointOfSale().getPointOfSaleId() == PointOfSaleId.UNITED_STATES;
			if (mRewardsContainer != null && flightTrip != null && !TextUtils.isEmpty(points) && USA) {
				String str = String.format(mContext.getString(R.string.youll_earn_points_TEMPLATE), points);
				TextView rewards = Ui.findView(mRewardsContainer, R.id.account_rewards_textview);
				rewards.setText(str);
				mRewardsContainer.setVisibility(View.VISIBLE);
				mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
				if (isElitePlusMember) {
					mRewardsContainer
						.setBackgroundResource(R.drawable.bg_checkout_information_elite_rewards_tab);
					mExpediaLogo.setImageResource(R.drawable.ic_expedia_logo_blue);
				}
			}
		}

		// Hotels + Membership
		else if (hasLoyaltyMembership) {
			top.setText(mContext.getString(R.string.logged_in_as));
			bottom.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
		}

		// Hotels + NO Membership
		else {
			top.setText(Html.fromHtml("<b>" + traveler.getEmail() + "</b>"));
			bottom.setText(mContext
				.getString(isElitePlusMember
					? R.string.enrolled_in_expedia_elite_plus_rewards
					: R.string.enrolled_in_expedia_rewards));
			if (isElitePlusMember) {
				String rewardsString = getResources().getString(
					R.string.youll_earn_bonus_points_for_this_booking);
				TextView rewards = Ui.findView(mRewardsContainer, R.id.account_rewards_textview);
				rewards.setText(rewardsString);
				mRewardsContainer.setVisibility(View.VISIBLE);
				mLogoutContainer.setBackgroundResource(
					R.drawable.bg_hotel_checkout_information_top_tab);
				mRewardsContainer.setBackgroundResource(
					R.drawable.bg_checkout_information_elite_rewards_hotel_tab);
				mExpediaLogo.setImageResource(R.drawable.ic_expedia_logo_blue);
			}
		}
	}

	public void bind(boolean isLoading, boolean isLoggedIn, User u) {
		bind(isLoading, isLoggedIn, u, LineOfBusiness.FLIGHTS);
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
