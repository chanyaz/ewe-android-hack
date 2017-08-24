package com.expedia.bookings.widget;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.RewardsInfo;
import com.expedia.bookings.data.TripBucketItemFlightV2;
import com.expedia.bookings.data.extensions.LobExtensionsKt;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.packages.PackageCreateTripResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.data.trips.TripBucketItemHotelV2;
import com.expedia.bookings.data.trips.TripBucketItemLX;
import com.expedia.bookings.data.trips.TripBucketItemPackages;
import com.expedia.bookings.data.trips.TripBucketItemTransport;
import com.expedia.bookings.data.user.User;
import com.expedia.bookings.data.user.UserLoyaltyMembershipInformation;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.expedia.util.LoyaltyUtil;
import com.squareup.phrase.Phrase;

public class AccountButton extends LinearLayout {
	private final Context mContext;
	private AccountButtonClickListener mListener;

	private View mAccountLoadingContainer;
	private View mLoginContainer;
	@VisibleForTesting
	protected TextView mLoginTextView;
	private View mLogoutContainer;
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
		mRewardsTextView = (TextView) findViewById(R.id.account_rewards_textview);
		mExpediaLogo = Ui.findView(this, R.id.card_icon);
		mLoadingTextView = Ui.findView(this, R.id.loading_textview);
		mExpediaLogo.setContentDescription(Phrase.from(getContext(), R.string.brand_account_cont_desc_TEMPLATE)
			.put("brand", BuildConfig.brand)
			.format());
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
					mListener.accountLogoutClicked();
					OmnitureTracking.trackLogOutAction(OmnitureTracking.LogOut.SELECT);
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
		// Loading container
		mAccountLoadingContainer.setVisibility(isLoading ? View.VISIBLE : View.GONE);

		// If logged in, show the logout container
		if (isLoggedIn) {
			mLoginContainer.setVisibility(View.GONE);
			mLogoutContainer.setVisibility(View.VISIBLE);
			mRewardsTextView.setVisibility(View.VISIBLE);
			bindLogoutContainer(u, lob);
		}
		// If not logged in, show the login container
		else {
			mLoginContainer.setVisibility(View.VISIBLE);
			mLogoutContainer.setVisibility(View.GONE);
			mRewardsTextView.setVisibility(View.GONE);
			bindLoginContainer(lob);
		}
	}

	// Do some runtime styling, based on whether this is tablet or a white-labelled app
	private void bindLoginContainer(LineOfBusiness lob) {
		LayoutParams lp = (LayoutParams) mLoginContainer.getLayoutParams();
		lp.height = LayoutParams.WRAP_CONTENT;
		FrameLayout.LayoutParams lpt = (FrameLayout.LayoutParams) mLoginTextView.getLayoutParams();

		if (LobExtensionsKt.isMaterialLineOfBusiness(lob)) {
			lpt.width = LayoutParams.WRAP_CONTENT;
			lpt.gravity = Gravity.CENTER;
			int textColor = R.color.material_checkout_account_button_text_color;
			int drawableSkinAttribute = R.attr.skin_material_checkout_account_logo;
			mLoginTextView.setTextColor(
				ContextCompat.getColor(getContext(), textColor));
			int[] attrs = { drawableSkinAttribute };
			TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
			mLoginTextView
				.setCompoundDrawablesWithIntrinsicBounds(ta.getDrawable(0), null, null, null);
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
		mLoginTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		int padding = getResources().getDimensionPixelSize(R.dimen.account_button_text_padding);
		mLoginTextView.setPadding(padding, padding, padding, padding);
		showLoginButtonText(lob);
	}

	@VisibleForTesting
	protected void showLoginButtonText(LineOfBusiness lob) {
		if (isSignInEarnMessagingEnabled(lob)) {
			RewardsInfo rewardsInfo = getRewardsForLOB(lob);
			setLoginTextAndContentDescription(lob, rewardsInfo);
		}
		else {
			mLoginTextView.setText(Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
				.putOptional("brand", BuildConfig.brand)
				.format());
			mLoginTextView.setContentDescription(Phrase.from(this, R.string.Sign_in_with_cont_desc_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format());
		}
	}

	@VisibleForTesting
	protected void setLoginTextAndContentDescription(LineOfBusiness lob, RewardsInfo rewardsInfo) {
		if (rewardsInfo != null && getRewardsForLOB(lob) != null && shouldShowEarnMessage(lob, rewardsInfo)) {
			if (rewardsInfo.hasAmountToEarn()) {
				String rewardsToEarn = rewardsInfo.getTotalAmountToEarn().getFormattedMoneyFromAmountAndCurrencyCode(
					Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL);
				mLoginTextView.setText(getSignInWithRewardsAmountText(rewardsToEarn));
				mLoginTextView.setContentDescription(getSignInWithRewardsContentDescriptionText(rewardsToEarn));
			}
			else if (rewardsInfo.hasPointsToEarn()) {
				DecimalFormat formatter = new DecimalFormat("#,###");
				String rewardsToEarn = formatter.format(Math.round(rewardsInfo.getTotalPointsToEarn()));
				mLoginTextView.setText(getSignInWithRewardsAmountText(rewardsToEarn));
				mLoginTextView.setContentDescription(getSignInWithRewardsContentDescriptionText(rewardsToEarn));
			}
			if (lob == LineOfBusiness.FLIGHTS) {
				mLoginContainer.setBackgroundResource(R.drawable.flight_cko_acct_btn_rewards_bg);
			}
		}
		else {
			mLoginTextView.setText(getSignInWithoutRewardsText());
			mLoginTextView.setContentDescription(Phrase.from(this, R.string.Sign_in_with_cont_desc_TEMPLATE)
				.put("brand", BuildConfig.brand)
				.format());
			mLoginContainer.setBackgroundResource(R.drawable.material_cko_acct_btn_bg);
		}
	}

	private boolean shouldShowEarnMessage(LineOfBusiness lob, RewardsInfo rewardsInfo) {
		return (rewardsInfo.hasAmountToEarn() || rewardsInfo.hasPointsToEarn()) && LoyaltyUtil.Companion
			.shouldShowEarnMessage(Float.toString(getRewardsForLOB(lob).getPointsToEarn()),
				lob == LineOfBusiness.PACKAGES);
	}

	private boolean isSignInEarnMessagingEnabled(LineOfBusiness lob) {
		PointOfSale pos = PointOfSale.getPointOfSale();
		return (lob == LineOfBusiness.HOTELS && pos.isEarnMessageEnabledForHotels()
			|| ((lob == LineOfBusiness.FLIGHTS || lob == LineOfBusiness.FLIGHTS_V2) && pos.isEarnMessageEnabledForFlights())
			|| lob == LineOfBusiness.PACKAGES && pos.isEarnMessageEnabledForPackages());
	}

	private void bindLogoutContainer(User user, LineOfBusiness lob) {
		updateBrandLogoVisibility();

		if (user == null || user.getPrimaryTraveler() == null) {
			return;
		}

		String email = user.getPrimaryTraveler().getEmail();

		// Traveler Email Text
		TextView travelerEmailTextView = Ui.findView(mLogoutContainer, R.id.account_top_textview);
		travelerEmailTextView.setText(email);
		travelerEmailTextView
			.setContentDescription(Phrase.from(getContext(), R.string.signed_in_account_cont_desc_TEMPLATE)
				.put("email", email)
				.format());

		UserLoyaltyMembershipInformation loyaltyInfo = user.getLoyaltyMembershipInformation();
		LoyaltyMembershipTier loyaltyTier = null;
		if (loyaltyInfo != null) {
			loyaltyTier = loyaltyInfo.getLoyaltyMembershipTier();
		}
		if (loyaltyTier == null) {
			loyaltyTier = LoyaltyMembershipTier.NONE;
		}

		// Bottom text -- rewards
		@StringRes int rewardsCategoryTextResId = 0;
		@ColorRes int rewardsCategoryColorResId = 0;
		@ColorRes int rewardsCategoryTextColorResId = 0;
		switch (loyaltyTier) {
		case BASE:
			rewardsCategoryTextResId = R.string.reward_base_tier_name_long;
			rewardsCategoryColorResId = R.color.account_reward_base_tier_color;
			rewardsCategoryTextColorResId = R.color.account_reward_base_tier_text_color;
			break;
		case MIDDLE:
			rewardsCategoryTextResId = R.string.reward_middle_tier_name_long;
			rewardsCategoryColorResId = R.color.account_reward_middle_tier_color;
			rewardsCategoryTextColorResId = R.color.account_reward_middle_tier_text_color;
			break;
		case TOP:
			rewardsCategoryTextResId = R.string.reward_top_tier_name_long;
			rewardsCategoryColorResId = R.color.account_reward_top_tier_color;
			rewardsCategoryTextColorResId = R.color.account_reward_top_tier_text_color;
			break;
		}

		TextView rewardsCategoryTextView = Ui.findView(mLogoutContainer, R.id.account_bottom_textview);

		// If we should show rewards
		final boolean isRewardsEnabled = PointOfSale.getPointOfSale().shouldShowRewards();
		if (isRewardsEnabled && loyaltyTier != LoyaltyMembershipTier.NONE) {
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
			mRewardsTextView.setVisibility(View.VISIBLE);
			FontCache.setTypeface(rewardsCategoryTextView, FontCache.Font.EXPEDIASANS_REGULAR);
			setRewardsContainerBackgroundColor(mRewardsTextView, loyaltyTier);

			//Show/Update Reward Points Text
			String rewardPointsText = getRewardPointsText(lob);
			if (updateRewardsTextViewVisibilityForUserInLoyaltyProgram(rewardPointsText)) {
				updateRewardsText(lob);
				mRewardsTextView.setTextColor(ContextCompat.getColor(getContext(), rewardsCategoryTextColorResId));
			}

			//Update Logout Container
		}
		else {
			rewardsCategoryTextView.setVisibility(View.GONE);
			mRewardsTextView.setVisibility(View.GONE);
		}
	}

	public void updateRewardsText(LineOfBusiness lob) {
		mRewardsTextView.setText(getRewardPointsText(lob));
	}

	private boolean updateRewardsTextViewVisibilityForUserInLoyaltyProgram(String rewardPointsText) {
		if (!Strings.isEmpty(rewardPointsText)) {
			mRewardsTextView.setVisibility(View.VISIBLE);
			return true;
		}
		else {
			mRewardsTextView.setVisibility(View.GONE);
			return false;
		}
	}

	private void updateBrandLogoVisibility() {
		boolean showBrandLogo = ProductFlavorFeatureConfiguration.getInstance().shouldShowBrandLogoOnAccountButton();
		if (!showBrandLogo) {
			mExpediaLogo.setVisibility(View.INVISIBLE);
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

		case FLIGHTS_V2:
			TripBucketItemFlightV2 flightV2 = Db.getTripBucket().getFlightV2();
			FlightCreateTripResponse flightTripV2 = flightV2 == null ? null : flightV2.flightCreateTripResponse;
			rewardPoints = flightTripV2 == null ? "" : getRewardsString(flightV2.flightCreateTripResponse.getRewards());
			break;

		case HOTELS:
			TripBucketItemHotelV2 hotelV2 = Db.getTripBucket().getHotelV2();
			HotelCreateTripResponse trip = hotelV2 == null ? null : hotelV2.mHotelTripResponse;
			rewardPoints = trip == null ? "" : getRewardsString(trip.getRewards());
			break;
		case LX:
			TripBucketItemLX lx = Db.getTripBucket().getLX();
			LXCreateTripResponse createLXTripResponse = lx == null ? null : lx.getCreateTripResponse();
			rewardPoints = createLXTripResponse == null ? "" : createLXTripResponse.getRewardsPoints();
			break;
		case TRANSPORT:
			TripBucketItemTransport transport = Db.getTripBucket().getTransport();
			LXCreateTripResponse createTransportTripResponse =
				transport == null ? null : transport.getCreateTripResponse();
			rewardPoints = createTransportTripResponse == null ? "" : createTransportTripResponse.getRewardsPoints();
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
				youllEarnRewardsPointsText = HtmlCompat.fromHtml(
					Phrase.from(this, R.string.x_rewards_currency_for_this_trip_TEMPLATE)
						.put("reward_currency", rewardPoints).format()
						.toString());
				break;
			case FLIGHTS_V2:
			case HOTELS:
			case PACKAGES:
			case LX:
				youllEarnRewardsPointsText = HtmlCompat.fromHtml(
					Phrase.from(this, R.string.youll_earn_points_TEMPLATE).put("reward_currency", rewardPoints).format()
						.toString());
				break;
			}
		}

		return youllEarnRewardsPointsText.toString();
	}

	private String getRewardsString(RewardsInfo rewards) {
		if (rewards != null) {
			if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
				return NumberFormat.getInstance().format(rewards.getPointsToEarn());
			}
			else if (rewards.getAmountToEarn() != null && !rewards.getAmountToEarn().isZero()) {
				return rewards.getAmountToEarn().getFormattedMoneyFromAmountAndCurrencyCode(
					Money.F_NO_DECIMAL_IF_INTEGER_ELSE_TWO_PLACES_AFTER_DECIMAL);
			}
		}
		return "0";
	}

	public CharSequence getSignInWithRewardsAmountText(String rewardsToEarn) {
		//noinspection ConstantConditions This can never be null from api.
		return Phrase.from(this, R.string.Sign_in_to_earn_TEMPLATE)
			.put("reward", rewardsToEarn)
			.format();
	}

	public CharSequence getSignInWithoutRewardsText() {
		return Phrase.from(this, R.string.Sign_in_with_TEMPLATE)
			.putOptional("brand", BuildConfig.brand)
			.format();
	}

	public RewardsInfo getRewardsForLOB(LineOfBusiness lob) {
		if (lob == LineOfBusiness.HOTELS) {
			TripBucketItemHotelV2 hotelV2 = Db.getTripBucket().getHotelV2();
			HotelCreateTripResponse trip = hotelV2 == null ? null : hotelV2.mHotelTripResponse;
			//TODO Remove trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null. Currently we need to initialize it till we start getting this in production.
			if (trip != null && trip.getRewards() != null && (trip.getRewards().getTotalPointsToEarn() != 0
				|| trip.getRewards().getTotalAmountToEarn() != null)) {
				return trip.getRewards();
			}
		}
		else if (lob == LineOfBusiness.FLIGHTS) {
			FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
			if (trip != null && trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null) {
				return trip.getRewards();
			}
		}
		else if (lob == LineOfBusiness.PACKAGES) {
			TripBucketItemPackages packages = Db.getTripBucket().getPackage();
			PackageCreateTripResponse trip = packages == null ? null : packages.mPackageTripResponse;
			if (trip != null && trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null) {
				return trip.getRewards();
			}
		}
		else if (lob == LineOfBusiness.FLIGHTS_V2) {
			TripBucketItemFlightV2 flightV2 = Db.getTripBucket().getFlightV2();
			FlightCreateTripResponse trip = flightV2 == null ? null : flightV2.flightCreateTripResponse;
			if (trip != null && trip.getRewards() != null && trip.getRewards().getTotalAmountToEarn() != null) {
				return trip.getRewards();
			}
		}
		return null;
	}

	protected void setRewardsContainerBackgroundColor(View rewardsContainer, LoyaltyMembershipTier membershipTier) {
		int rewardsBgColor = 0;
		switch (membershipTier) {
		case BASE:
			rewardsBgColor = R.color.sign_in_user_base_tier;
			break;
		case MIDDLE:
			rewardsBgColor = R.color.sign_in_user_middle_tier;
			break;
		case TOP:
			rewardsBgColor = R.color.sign_in_user_top_tier;
			break;
		}

		rewardsContainer.setBackgroundColor(ContextCompat.getColor(getContext(), rewardsBgColor));
	}

	private void clearTabletCheckoutData() {
		clearTabletHotelCheckoutData();
		clearTabletFlightCheckoutData();
	}

	private void clearTabletHotelCheckoutData() {
		TripBucketItemHotel hotel = Db.getTripBucket().getHotel();
		if (hotel != null) {
			hotel.clearCheckoutData();
		}
	}

	private void clearTabletFlightCheckoutData() {
		TripBucketItemFlight flight = Db.getTripBucket().getFlight();
		if (flight != null) {
			flight.clearCheckoutData();
		}
	}

	public interface AccountButtonClickListener {
		void accountLoginClicked();

		void accountLogoutClicked();
	}

	public CharSequence getSignInWithRewardsContentDescriptionText(String rewardsToEarn) {
		//noinspection ConstantConditions This can never be null from api.
		return Phrase.from(this, R.string.Sign_in_to_earn_cont_desc_TEMPLATE)
			.put("reward", rewardsToEarn)
			.format();
	}
}
