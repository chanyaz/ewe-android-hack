package com.expedia.bookings.widget;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.TripBucketItemLX;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.phrase.Phrase;

public class AccountButton extends LinearLayout {
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
	private TextView mLoadingTextView;

	public AccountButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		inflate(context, R.layout.account_v2_button, this);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mAccountLoadingContainer = findViewById(R.id.account_loading_container);
		mLoginContainer = findViewById(R.id.account_login_container);
		mLoginTextView = Ui.findView(mLoginContainer, R.id.login_text_view);
		mLogoutContainer = findViewById(R.id.account_logout_container);
		mErrorContainer = findViewById(R.id.error_container);
		mRewardsContainer = findViewById(R.id.account_rewards_container);
		mRewardsTextView = Ui.findView(mRewardsContainer, R.id.account_rewards_textview);
		mExpediaLogo = Ui.findView(this, R.id.card_icon);
		mLoadingTextView = Ui.findView(this, R.id.loading_textview);

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

		mLoadingTextView.setText(Phrase.from(this, R.string.loading_brand_account_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.format());

		mLoginTextView.setText(Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.format());
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

		// Loading container
		mAccountLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		// If logged in, show the logout container
		if (isLoggedIn) {
			mLoginContainer.setVisibility(View.GONE);
			mLogoutContainer.setVisibility(View.VISIBLE);
			mRewardsContainer.setVisibility(View.VISIBLE);
			bindLogoutContainer(traveler, lob);
		}
		// If not logged in, show the login container
		else {
			mLoginContainer.setVisibility(View.VISIBLE);
			mLogoutContainer.setVisibility(View.GONE);
			mRewardsContainer.setVisibility(View.GONE);
			bindLoginContainer(lob);
		}
	}

	// Do some runtime styling, based on whether this is tablet or a white-labelled app
	private void bindLoginContainer(LineOfBusiness lob) {
		boolean isTablet = AndroidUtils.isTablet(getContext());

		if (isTablet) {
			LayoutParams lp = (LayoutParams) mLoginContainer.getLayoutParams();
			lp.height = getResources().getDimensionPixelSize(R.dimen.account_button_height);
			LayoutParams lpt = (LayoutParams) mLoginTextView.getLayoutParams();
			lpt.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
			mLoginContainer.setBackgroundResource(R.drawable.bg_checkout_information_single);
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(
				Ui.obtainThemeResID(mContext, R.attr.skin_tabletCheckoutLoginLogoDrawable), 0, 0, 0);
			mLoginTextView.setTextColor(
				Ui.obtainThemeColor(mContext, R.attr.skin_tabletCheckoutLoginButtonTextColor));
		}
		else {
			if (lob == LineOfBusiness.HOTELS || lob == LineOfBusiness.FLIGHTS) {
				LayoutParams lp = (LayoutParams) mLoginContainer.getLayoutParams();
				lp.height = getResources().getDimensionPixelSize(R.dimen.account_button_height);
				LayoutParams lpt = (LayoutParams) mLoginTextView.getLayoutParams();
				lpt.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
				mLoginTextView
					.setCompoundDrawablePadding(getResources().getDimensionPixelSize(R.dimen.card_icon_padding));
			}
			else {
				LayoutParams lp = (LayoutParams) mLoginContainer.getLayoutParams();
				lp.height = LayoutParams.WRAP_CONTENT;
				LayoutParams lpt = (LayoutParams) mLoginTextView.getLayoutParams();
				lpt.width = LayoutParams.MATCH_PARENT;
				lpt.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
				int bgResourceId = Ui.obtainThemeResID(getContext(), android.R.attr.selectableItemBackground);
				mLoginTextView.setBackgroundResource(bgResourceId);
				mLoginTextView.setGravity(Gravity.LEFT);

				int padding = getResources().getDimensionPixelSize(R.dimen.account_button_text_padding);
				mLoginTextView.setPadding(padding, padding, padding, padding);
			}
			mLoginTextView.setTextColor(Ui.obtainThemeColor(mContext, R.attr.skin_phoneCheckoutLoginButtonTextColor));
			mLoginContainer.setBackgroundResource(
				Ui.obtainThemeResID(getContext(), R.attr.skin_phoneCheckoutLoginButtonDrawable));
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(
				Ui.obtainThemeResID(getContext(), R.attr.skin_phoneCheckoutLoginLogoDrawable), 0, 0, 0);
		}
	}

	private void bindLogoutContainer(Traveler traveler, LineOfBusiness lob) {
		updateBrandLogo(traveler.isLoyaltyMember());

		// Traveler Email Text
		TextView travelerEmailTextView = Ui.findView(mLogoutContainer, R.id.account_top_textview);
		travelerEmailTextView.setText(traveler.getEmail());

		// Bottom text -- rewards
		int expediaPlusRewardsCategoryTextResId = 0;
		int expediaPlusRewardsCategoryColorResId = 0;
		int expediaPlusRewardsCategoryTextColorResId = 0;
		switch (traveler.getLoyaltyMembershipTier()) {
		case BLUE:
			expediaPlusRewardsCategoryTextResId = R.string.Expedia_plus_blue;
			expediaPlusRewardsCategoryColorResId = R.color.expedia_plus_blue;
			expediaPlusRewardsCategoryTextColorResId = R.color.expedia_plus_blue_text;
			break;
		case SILVER:
			expediaPlusRewardsCategoryTextResId = R.string.Expedia_plus_silver;
			expediaPlusRewardsCategoryColorResId = R.color.expedia_plus_silver;
			expediaPlusRewardsCategoryTextColorResId = R.color.expedia_plus_silver_text;
			break;
		case GOLD:
			expediaPlusRewardsCategoryTextResId = R.string.Expedia_plus_gold;
			expediaPlusRewardsCategoryColorResId = R.color.expedia_plus_gold;
			expediaPlusRewardsCategoryTextColorResId = R.color.expedia_plus_gold_text;
			break;
		}

		TextView expediaPlusRewardsCategoryTextView = Ui.findView(mLogoutContainer, R.id.account_bottom_textview);

		// If we should show rewards
		final boolean isRewardsEnabled = PointOfSale.getPointOfSale().shouldShowRewards();
		if (isRewardsEnabled && traveler.getLoyaltyMembershipTier() != Traveler.LoyaltyMembershipTier.NONE) {
			//Show Rewards Category Text View
			expediaPlusRewardsCategoryTextView.setVisibility(View.VISIBLE);
			expediaPlusRewardsCategoryTextView.setText(expediaPlusRewardsCategoryTextResId);
			expediaPlusRewardsCategoryTextView.setTextColor(getResources().getColor(expediaPlusRewardsCategoryColorResId));
			//Show Reward Points Container
			mRewardsContainer.setVisibility(View.VISIBLE);
			FontCache.setTypeface(expediaPlusRewardsCategoryTextView, FontCache.Font.EXPEDIASANS_REGULAR);
			setRewardsContainerBackground(mRewardsContainer, traveler.getLoyaltyMembershipTier());

			//Show/Update Reward Points Text
			String rewardPointsText = getRewardPointsText(lob);
			if (updateRewardsTextViewVisibility(rewardPointsText, lob, traveler.isLoyaltyMember())) {
				mRewardsTextView.setText(rewardPointsText);
				mRewardsTextView.setTextColor(getResources().getColor(expediaPlusRewardsCategoryTextColorResId));
			}

			//Update Logout Container
			mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
		}
		else {
			expediaPlusRewardsCategoryTextView.setVisibility(View.GONE);
			mRewardsContainer.setVisibility(View.GONE);
			setLogoutContainerBackground(mLogoutContainer);
		}

		// Logo
		mExpediaLogo.setImageResource(Ui.obtainThemeResID(mContext, R.attr.skin_hotelCheckoutLogoutLogoDrawable));
	}

	private boolean updateRewardsTextViewVisibility(String rewardPointsText, LineOfBusiness lob, boolean isLoyaltyMember) {
		if (!Strings.isEmpty(rewardPointsText)) {
			mRewardsTextView.setVisibility(View.VISIBLE);
			return true;
		}
		else if (isLoyaltyMember) {
			mRewardsTextView.setVisibility(View.GONE);
			return false;
		}
		return false;
	}

	private void updateBrandLogo(boolean isLoyaltyMember) {
		boolean showBrandLogo = ProductFlavorFeatureConfiguration.getInstance().shouldShowBrandLogoOnAccountButton();
		if (!showBrandLogo) {
			mExpediaLogo.setVisibility(View.INVISIBLE);
		}
		else if (!isLoyaltyMember) {
			mExpediaLogo.setImageResource(Ui.obtainThemeResID(mContext, R.attr.skin_hotelCheckoutLogoutLogoDrawable));
		}
	}

	private String getRewardPointsText(LineOfBusiness lob) {
		String rewardPoints = "";
		switch (lob) {
		case FLIGHTS:
			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			FlightTrip flightTrip = flight == null ? null : flight.getFlightTrip();
			rewardPoints = flightTrip == null ? "" : flightTrip.getRewardsPoints();
			break;

		case HOTELS:
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			CreateTripResponse hotelTrip = hotel == null ? null : hotel.getCreateTripResponse();
			rewardPoints = hotelTrip == null ? "" : hotelTrip.getRewardsPoints();
			break;

		case LX:
			TripBucketItemLX lx = Db.getTripBucket().getLX();
			LXCreateTripResponse createTripResponse = lx == null ? null : lx.getCreateTripResponse();
			rewardPoints = createTripResponse == null ? "" : createTripResponse.getRewardsPoints();
			break;
		}

		CharSequence youllEarnRewardsPointsText = "";
		if (!TextUtils.isEmpty(rewardPoints)) {
			switch (lob) {
			case FLIGHTS:
				youllEarnRewardsPointsText = Html.fromHtml(mContext.getString(R.string.x_points_for_this_trip_TEMPLATE, rewardPoints));
				break;
			case HOTELS:
				boolean isUserBucketedForTest = Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotel3xMessaging);
				boolean isTablet = AndroidUtils.isTablet(getContext());
				if (isUserBucketedForTest && !isTablet) {
					youllEarnRewardsPointsText = Html.fromHtml(mContext.getString(R.string.youll_earn_points_ab_test_3x_TEMPLATE, rewardPoints));
				}
				else {
					youllEarnRewardsPointsText = Html.fromHtml(mContext.getString(R.string.youll_earn_points_TEMPLATE, rewardPoints));
				}
				break;
			case LX:
				youllEarnRewardsPointsText = Html.fromHtml(mContext.getString(R.string.youll_earn_points_TEMPLATE, rewardPoints));
			}
		}

		return youllEarnRewardsPointsText.toString();
	}

	protected void setLogoutContainerBackground(View logoutContainer) {
		logoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_single);
	}

	protected void setRewardsContainerBackground(View rewardsContainer, Traveler.LoyaltyMembershipTier membershipTier) {
		int rewardsBgResId = 0;
		switch (membershipTier) {
		case BLUE:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_blue;
			break;
		case SILVER:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_silver;
			break;
		case GOLD:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_gold;
			break;
		}

		rewardsContainer.setBackgroundResource(rewardsBgResId);
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
		void accountLoginClicked();
		void accountLogoutClicked();
	}
}
