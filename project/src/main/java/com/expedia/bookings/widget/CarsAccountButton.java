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
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;

public class CarsAccountButton extends LinearLayout {
	private Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	private TextView mLoginTextView;
	private View mLogoutContainer;
	private View mErrorContainer;
	private View mRewardsContainer;
	private TextView mRewardsTextView;
	private View mLogoutButton;
	private View mLoadingLogoutButton;
	private ImageView mExpediaLogo;

	public CarsAccountButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public CarsAccountButton(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	protected void onFinishInflate() {
		mAccountLoadingContainer = findViewById(R.id.account_loading_container);
		mLoginContainer = findViewById(R.id.account_login_container);
		mLoginTextView = Ui.findView(mLoginContainer, R.id.login_text_view);
		mLogoutContainer = findViewById(R.id.account_logout_container);
		mErrorContainer = findViewById(R.id.error_container);
		mRewardsContainer = findViewById(R.id.account_rewards_container);
		mRewardsTextView = Ui.findView(mRewardsContainer, R.id.account_rewards_textview);
		mExpediaLogo = Ui.findView(this, R.id.card_icon);

		mLoginContainer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					mListener.accountLoginClicked();
				}
			}
		});

		mLogoutButton = mLogoutContainer.findViewById(R.id.account_logout_logout_button);
		mLoadingLogoutButton = mAccountLoadingContainer.findViewById(R.id.account_loading_logout_button);

		OnClickListener logoutListener = new OnClickListener() {
			public void onClick(View v) {
				if (mListener != null) {
					clearCheckoutData();
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

		// Errors container
		if (mErrorContainer != null) {
			mErrorContainer.setVisibility(View.GONE);
		}

		// Rewards container
		if (mRewardsContainer != null) {
			mRewardsContainer.setVisibility(View.GONE);
		}

		// Loading container
		mAccountLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		// If logged in, show the logout container
		if (isLoggedIn) {
			mLoginContainer.setVisibility(View.GONE);
			mLogoutContainer.setVisibility(View.VISIBLE);
			bindLogoutContainer(traveler, lob);
		}
		// If not logged in, show the login container
		else {
			mLoginContainer.setVisibility(View.VISIBLE);
			mLogoutContainer.setVisibility(View.GONE);
			bindLoginContainer(lob);
		}
	}

	// Do some runtime styling, based on whether this is tablet or a white-labelled app
	private void bindLoginContainer(LineOfBusiness lob) {
		boolean isTablet = AndroidUtils.isTablet(getContext());

		if (isTablet) {
			mLoginContainer.setBackgroundResource(R.drawable.bg_checkout_information_single);
			Ui.findView(mLoginContainer, R.id.login_blurb).setVisibility(View.INVISIBLE);
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(
				Ui.obtainThemeResID(mContext, R.attr.skin_tabletCheckoutLoginLogoDrawable), 0, 0, 0);
			mLoginTextView.setTextColor(
				Ui.obtainThemeColor(mContext, R.attr.skin_tabletCheckoutLoginButtonTextColor));
		}
		else {
			int bgResourceId = ProductFlavorFeatureConfiguration.getInstance().getLoginContainerBackgroundResId(mContext);
			mLoginContainer.setBackgroundResource(bgResourceId);

			boolean doesLoginTextViewHaveCompoundDrawables = ProductFlavorFeatureConfiguration.getInstance()
				.doesLoginTextViewHaveCompoundDrawables();
			if (doesLoginTextViewHaveCompoundDrawables) {
				mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(
					Ui.obtainThemeResID(mContext, R.attr.skin_phoneCheckoutLoginLogoDrawable), 0, 0, 0);
			}

			mLoginTextView.setTextColor(
				Ui.obtainThemeColor(mContext, R.attr.skin_phoneCheckoutLoginButtonTextColor));
		}
	}

	private void bindLogoutContainer(Traveler traveler, LineOfBusiness lob) {
		final boolean isFlights = lob == LineOfBusiness.FLIGHTS;
		final boolean isRewardsEnabled = PointOfSale.getPointOfSale().shouldShowRewards();

		TextView top = Ui.findView(mLogoutContainer, R.id.account_top_textview);
		TextView bottom = Ui.findView(mLogoutContainer, R.id.account_bottom_textview);

		boolean showBrandLogo = ProductFlavorFeatureConfiguration.getInstance().shouldShowBrandLogoOnAccountButton();
		if (!showBrandLogo) {
			mExpediaLogo.setVisibility(View.INVISIBLE);
		}
		else if (!traveler.isLoyaltyMember()) {
			mExpediaLogo.setImageResource(Ui.obtainThemeResID(mContext, R.attr.skin_hotelCheckoutLogoutLogoDrawable));
		}

		// Top text
		String topText = traveler.getEmail();
		top.setText(topText);

		// Bottom text -- rewards
		int bottomTextResId = 0;
		int colorResId = 0;
		int textColorResId = 0;
		int rewardsBgResId = 0;
		switch (traveler.getLoyaltyMembershipTier()) {
		case BLUE:
			bottomTextResId = R.string.Expedia_plus_blue;
			colorResId = R.color.expedia_plus_blue;
			textColorResId = R.color.expedia_plus_blue_text;
			rewardsBgResId = R.color.expedia_plus_blue;
			break;
		case SILVER:
			bottomTextResId = R.string.Expedia_plus_silver;
			colorResId = R.color.expedia_plus_silver;
			textColorResId = R.color.expedia_plus_silver_text;
			rewardsBgResId = R.color.expedia_plus_silver;
			break;
		case GOLD:
			bottomTextResId = R.string.Expedia_plus_gold;
			colorResId = R.color.expedia_plus_gold;
			textColorResId = R.color.expedia_plus_gold_text;
			rewardsBgResId = R.color.expedia_plus_gold;
			break;
		}

		// Rewards text
		String points = "";
		if (isFlights) {
			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			FlightTrip flightTrip = flight == null ? null : flight.getFlightTrip();
			points = flightTrip == null ? "" : flightTrip.getRewardsPoints();
		}
		else {
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			CreateTripResponse hotelTrip = hotel == null ? null : hotel.getCreateTripResponse();
			points = hotelTrip == null ? "" : hotelTrip.getRewardsPoints();
		}

		CharSequence pointsText = "";
		if (!TextUtils.isEmpty(points)) {
			mRewardsTextView.setVisibility(View.VISIBLE);
			if (isFlights) {
				pointsText = Html.fromHtml(mContext.getString(R.string.x_points_for_this_trip_TEMPLATE, points));
			}
			else {
				pointsText = Html.fromHtml(mContext.getString(R.string.youll_earn_points_TEMPLATE, points));
			}
		}
		else if (traveler.isLoyaltyMember()) {
			mRewardsTextView.setVisibility(View.GONE);
		}

		// If we should show rewards
		if (bottomTextResId != 0 && isRewardsEnabled) {
			bottom.setText(bottomTextResId);
			bottom.setVisibility(View.VISIBLE);
			bottom.setTextColor(getResources().getColor(colorResId));

			FontCache.setTypeface(bottom, FontCache.Font.EXPEDIASANS_REGULAR);
			mRewardsContainer.setVisibility(View.VISIBLE);
			mRewardsContainer.setBackgroundColor(getResources().getColor(rewardsBgResId));
			mRewardsTextView.setText(pointsText);
			mRewardsTextView.setTextColor(getResources().getColor(textColorResId));
		}
		else {
			bottom.setVisibility(View.GONE);
			mRewardsContainer.setVisibility(View.GONE);
		}

		// Logo
		mExpediaLogo.setImageResource(Ui.obtainThemeResID(mContext, R.attr.skin_hotelCheckoutLogoutLogoDrawable));
	}

	private void clearCheckoutData() {
		clearHotelCheckoutData();
		clearFlightCheckoutData();
	}

	private void clearHotelCheckoutData() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			hotel.clearCheckoutData();
		}
	}

	private void clearFlightCheckoutData() {
		TripBucketItemFlight flight = Db.getTripBucket().getFlight();
		if (flight != null) {
			flight.clearCheckoutData();
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
