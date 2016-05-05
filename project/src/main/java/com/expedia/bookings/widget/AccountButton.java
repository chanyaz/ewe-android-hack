package com.expedia.bookings.widget;

import java.text.NumberFormat;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.Html;
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
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.RewardsInfo;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.TripBucketItemHotelV2;
import com.expedia.bookings.data.TripBucketItemLX;
import com.expedia.bookings.data.TripBucketItemPackages;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
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
			mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tablet_checkout_login_logo, 0, 0, 0);
			mLoginTextView
				.setTextColor(ContextCompat.getColor(getContext(), R.color.tablet_checkout_account_button_text_color));
		}
		else {
			LayoutParams lp = (LayoutParams) mLoginContainer.getLayoutParams();
			lp.height = LayoutParams.WRAP_CONTENT;
			LayoutParams lpt = (LayoutParams) mLoginTextView.getLayoutParams();
			if (lob == LineOfBusiness.HOTELSV2 || lob == LineOfBusiness.PACKAGES
				|| lob == LineOfBusiness.CARS || lob == LineOfBusiness.LX) {
				lpt.width = LayoutParams.WRAP_CONTENT;
				lpt.gravity = Gravity.CENTER;
				mLoginContainer.setBackgroundResource(R.drawable.material_account_sign_in_button_ripple);
				mLoginTextView.setTextColor(
					ContextCompat.getColor(getContext(), R.color.material_checkout_account_button_text_color));
				mLoginTextView
					.setCompoundDrawablesWithIntrinsicBounds(R.drawable.material_checkout_account_logo, 0, 0, 0);
			}
			else {
				int bgResourceId = Ui.obtainThemeResID(getContext(), android.R.attr.selectableItemBackground);
				lpt.width = LayoutParams.MATCH_PARENT;
				lpt.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;

				mLoginContainer.setBackgroundResource(R.drawable.old_checkout_account_button_background);
				mLoginTextView
					.setTextColor(ContextCompat.getColor(getContext(), R.color.old_checkout_account_button_text_color));
				mLoginTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.old_checkout_account_logo, 0, 0, 0);
				mLoginTextView.setBackgroundResource(bgResourceId);
			}
			mLoginTextView.setGravity(Gravity.LEFT);
			int padding = getResources().getDimensionPixelSize(R.dimen.account_button_text_padding);
			mLoginTextView.setPadding(padding, padding, padding, padding);
			if (isSignInEarnMessagingEnabled(lob)) {
				mLoginTextView.setText(getSignInWithRewardsAmountText(lob));
			}
			else {
				mLoginTextView.setText(Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
					.put("brand", BuildConfig.brand)
					.format());
			}

		}
	}

	private boolean isSignInEarnMessagingEnabled(LineOfBusiness lob) {
		return ProductFlavorFeatureConfiguration.getInstance().isEarnMessageOnCheckoutSignInButtonEnabled() && (
			lob == LineOfBusiness.HOTELSV2 || lob == LineOfBusiness.FLIGHTS);
	}

	private void bindLogoutContainer(Traveler traveler, LineOfBusiness lob) {
		updateBrandLogo(traveler.isLoyaltyMember());

		// Traveler Email Text
		TextView travelerEmailTextView = Ui.findView(mLogoutContainer, R.id.account_top_textview);
		travelerEmailTextView.setText(traveler.getEmail());

		// Bottom text -- rewards
		@StringRes int rewardsCategoryTextResId = 0;
		@ColorRes int rewardsCategoryColorResId = 0;
		@ColorRes int rewardsCategoryTextColorResId = 0;
		switch (traveler.getLoyaltyMembershipTier()) {
		case BASE:
			rewardsCategoryTextResId = R.string.reward_base_tier_name_long;
			rewardsCategoryColorResId = R.color.reward_base_tier_color;
			rewardsCategoryTextColorResId = R.color.reward_base_tier_text_color;
			break;
		case MIDDLE:
			rewardsCategoryTextResId = R.string.reward_middle_tier_name_long;
			rewardsCategoryColorResId = R.color.reward_middle_tier_color;
			rewardsCategoryTextColorResId = R.color.reward_middle_tier_text_color;
			break;
		case TOP:
			rewardsCategoryTextResId = R.string.reward_top_tier_name_long;
			rewardsCategoryColorResId = R.color.reward_top_tier_color;
			rewardsCategoryTextColorResId = R.color.reward_top_tier_text_color;
			break;
		}

		TextView rewardsCategoryTextView = Ui.findView(mLogoutContainer, R.id.account_bottom_textview);

		// If we should show rewards
		final boolean isRewardsEnabled = PointOfSale.getPointOfSale().shouldShowRewards();
		if (isRewardsEnabled && traveler.getLoyaltyMembershipTier() != LoyaltyMembershipTier.NONE) {
			//Show Rewards Category Text View
			if (ProductFlavorFeatureConfiguration.getInstance().shouldShowMemberTier()) {
				rewardsCategoryTextView.setVisibility(View.VISIBLE);
				rewardsCategoryTextView.setText(rewardsCategoryTextResId);
				rewardsCategoryTextView.setTextColor(ContextCompat.getColor(getContext(), rewardsCategoryColorResId));

			}
			else {
				rewardsCategoryTextView.setVisibility(View.GONE);
			}
			//Show Reward Points Container
			mRewardsContainer.setVisibility(View.VISIBLE);
			FontCache.setTypeface(rewardsCategoryTextView, FontCache.Font.EXPEDIASANS_REGULAR);
			setRewardsContainerBackground(mRewardsContainer, traveler.getLoyaltyMembershipTier());

			//Show/Update Reward Points Text
			String rewardPointsText = getRewardPointsText(lob);
			if (updateRewardsTextViewVisibility(rewardPointsText, lob, traveler.isLoyaltyMember())) {
				updateRewardsText(lob);
				mRewardsTextView.setTextColor(ContextCompat.getColor(getContext(), rewardsCategoryTextColorResId));
			}

			//Update Logout Container
			mLogoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
		}
		else {
			rewardsCategoryTextView.setVisibility(View.GONE);
			mRewardsContainer.setVisibility(View.GONE);
			setLogoutContainerBackground(mLogoutContainer);
		}

		// Logo
		mExpediaLogo.setImageResource(R.drawable.checkout_logout_logo);
	}

	public void updateRewardsText(LineOfBusiness lob) {
		mRewardsTextView.setText(getRewardPointsText(lob));
	}

	private boolean updateRewardsTextViewVisibility(String rewardPointsText, LineOfBusiness lob,
		boolean isLoyaltyMember) {
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
			mExpediaLogo.setImageResource(R.drawable.checkout_logout_logo);
		}
	}

	private String getRewardPointsText(LineOfBusiness lob) {
		String rewardPoints = "";
		switch (lob) {
		case FLIGHTS:
			TripBucketItemFlight flight = Db.getTripBucket().getFlight();
			FlightTrip flightTrip = flight == null ? null : flight.getFlightTrip();
			rewardPoints = flightTrip == null ? "" : getRewardsString(flightTrip.getRewards());
			break;

		case HOTELS:
			TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
			CreateTripResponse hotelTrip = hotel == null ? null : hotel.getCreateTripResponse();
			rewardPoints = hotelTrip == null ? "" : hotelTrip.getRewardsPoints();
			break;
		case HOTELSV2:
			TripBucketItemHotelV2 hotelV2 = Db.getTripBucket().getHotelV2();
			HotelCreateTripResponse trip = hotelV2 == null ? null : hotelV2.mHotelTripResponse;
			rewardPoints = trip == null ? "" : getRewardsString(trip.getRewards());
			break;
		case LX:
			TripBucketItemLX lx = Db.getTripBucket().getLX();
			LXCreateTripResponse createTripResponse = lx == null ? null : lx.getCreateTripResponse();
			rewardPoints = createTripResponse == null ? "" : createTripResponse.getRewardsPoints();
			break;
		case PACKAGES:
			TripBucketItemPackages pkgItem = Db.getTripBucket().getPackage();
			PackageCreateTripResponse packageTrip = pkgItem == null ? null : pkgItem.mPackageTripResponse;
			rewardPoints = packageTrip == null ? "" : getRewardsString(packageTrip.getRewards());
			break;
		}

		CharSequence youllEarnRewardsPointsText = "";
		if (Strings.isEmpty(rewardPoints)) {
			//Do nothing
		}
		else if (Strings.equals("0", rewardPoints)) {
			youllEarnRewardsPointsText = mContext.getString(R.string.you_are_a_valued_member);

		}
		else {
			switch (lob) {
			case FLIGHTS:
				youllEarnRewardsPointsText = Html.fromHtml(
					Phrase.from(this, R.string.x_rewards_currency_for_this_trip_TEMPLATE)
						.put("reward_currency", rewardPoints).format()
						.toString());
				break;
			case HOTELSV2:
			case HOTELS:
			case PACKAGES:
				boolean isUserBucketedForTest = Db.getAbacusResponse()
					.isUserBucketedForTest(AbacusUtils.EBAndroidAppHotel3xMessaging);
				boolean isTablet = AndroidUtils.isTablet(getContext());
				youllEarnRewardsPointsText = Html.fromHtml(
					Phrase.from(this, R.string.youll_earn_points_TEMPLATE).put("reward_currency", rewardPoints).format()
						.toString());
				if (isUserBucketedForTest && !isTablet && lob == LineOfBusiness.HOTELSV2) {
					youllEarnRewardsPointsText = Html
						.fromHtml(mContext.getString(R.string.youll_earn_points_ab_test_3x_TEMPLATE, rewardPoints));
				}
				break;
			case LX:
				youllEarnRewardsPointsText = Html
					.fromHtml(mContext.getString(R.string.youll_earn_points_ab_test_3x_TEMPLATE, rewardPoints));
			}
		}

		return youllEarnRewardsPointsText.toString();
	}

	private String getRewardsString(RewardsInfo rewards) {
		if (rewards != null) {
			if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
				return NumberFormat.getInstance().format(rewards.getPointsToEarn());
			}
			else if (rewards.getAmountToEarn() != null) {
				return rewards.getAmountToEarn()
					.getFormattedMoney(Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL);
			}
		}
		return "0";
	}

	private CharSequence getSignInWithRewardsAmountText(LineOfBusiness lob) {
		RewardsInfo rewardsInfo = getRewardsForLOB(lob);

		if (rewardsInfo != null) {
			//noinspection ConstantConditions This can never be null from api.
			String rewardsToEarn = rewardsInfo.getTotalAmountToEarn()
				.getFormattedMoneyFromAmountAndCurrencyCode();
			return Phrase.from(this, R.string.Sign_in_to_earn_TEMPLATE)
				.put("reward", rewardsToEarn)
				.format();
		}
		else {
			return Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format();
		}

	}

	public RewardsInfo getRewardsForLOB(LineOfBusiness lob) {
		if (lob == LineOfBusiness.HOTELSV2) {
			TripBucketItemHotelV2 hotelV2 = Db.getTripBucket().getHotelV2();
			HotelCreateTripResponse trip = hotelV2 == null ? null : hotelV2.mHotelTripResponse;
			//TODO Remove trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null. Currently we need to initialize it till we start getting this in production.
			if (trip != null && trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null) {
				return trip.getRewards();
			}
		}
		else if (lob == LineOfBusiness.FLIGHTS) {
			FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
			if (trip != null && trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null) {
				return trip.getRewards();
			}
		}
		return null;
	}

	protected void setLogoutContainerBackground(View logoutContainer) {
		logoutContainer.setBackgroundResource(R.drawable.bg_checkout_information_single);
	}

	protected void setRewardsContainerBackground(View rewardsContainer, LoyaltyMembershipTier membershipTier) {
		int rewardsBgResId = 0;
		switch (membershipTier) {
		case BASE:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_base_tier;
			break;
		case MIDDLE:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_middle_tier;
			break;
		case TOP:
			rewardsBgResId = R.drawable.bg_checkout_info_bottom_top_tier;
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
