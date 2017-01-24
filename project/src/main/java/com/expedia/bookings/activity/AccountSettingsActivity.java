package com.expedia.bookings.activity;

import java.text.NumberFormat;
import java.util.Calendar;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.OnClick;
import com.expedia.account.Config;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.UserLoyaltyMembershipInformation;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ClearPrivateDataDialog;
import com.expedia.bookings.dialog.TextViewDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.Constants;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.UserAccountRefresher;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.fragment.AboutSectionFragment.AboutSectionFragmentListener;
import com.mobiata.android.fragment.CopyrightFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.phrase.Phrase;

public class AccountSettingsActivity extends AppCompatActivity implements AboutSectionFragmentListener,
	AboutUtils.CountrySelectDialogListener, LoginConfirmLogoutDialogFragment.DoLogoutListener,
	UserAccountRefresher.IUserAccountRefreshListener, ClearPrivateDataDialog.ClearPrivateDataDialogListener,
	GoogleApiClient.ConnectionCallbacks, CopyrightFragment.CopyrightFragmentListener {
	private static final String TAG_SUPPORT = "TAG_SUPPORT";
	private static final String TAG_LEGAL = "TAG_LEGAL";
	private static final String TAG_COPYRIGHT = "TAG_COPYRIGHT";
	private static final String TAG_COMMUNICATE = "TAG_COMMUNICATE";
	private static final String TAG_APP_SETTINGS = "TAG_APP_SETTINGS";
	private static final String GOOGLE_SIGN_IN_SUPPORT = "GOOGLE_SIGN_IN_SUPPORT";

	private static final int ROW_BOOKING_SUPPORT = 1;
	private static final int ROW_EXPEDIA_WEBSITE = 2;
	private static final int ROW_APP_SUPPORT = 3;
	private static final int ROW_WERE_HIRING = 4;
	private static final int ROW_PRIVACY_POLICY = 5;
	private static final int ROW_TERMS_AND_CONDITIONS = 6;
	private static final int ROW_ATOL_INFO = 7;
	private static final int ROW_OPEN_SOURCE_LICENSES = 8;

	public static final int ROW_VSC_VOYAGES = 9;
	private final static String PKG_VSC_VOYAGES = "com.vsct.vsc.mobile.horaireetresa.android";

	private static final int ROW_CLEAR_PRIVATE_DATA = 10;
	private static final int ROW_RATE_APP = 11;
	private static final int ROW_COUNTRY = 12;
	private static final int ROW_REWARDS_VISA_CARD = 13;

	private AboutUtils aboutUtils;
	private UserAccountRefresher userAccountRefresher;

	private GestureDetectorCompat gestureDetector;

	private int secretCount = 0;

	private AboutSectionFragment appSettingsFragment;
	private AboutSectionFragment supportFragment;
	private AboutSectionFragment legalFragment;
	private CopyrightFragment copyrightFragment;
	private ScrollView scrollContainer;
	private boolean notPortraitOrientation;

	private GoogleApiClient mGoogleApiClient;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		notPortraitOrientation = !getResources().getBoolean(R.bool.portrait);
		if (shouldBail()) {
			return;
		}

		aboutUtils = new AboutUtils(this);
		userAccountRefresher = new UserAccountRefresher(this, LineOfBusiness.PROFILE, null);
		userAccountRefresher.ensureAccountIsRefreshed();

		gestureDetector = new GestureDetectorCompat(this, mOnGestureListener);

		setContentView(R.layout.activity_account_settings);

		ButterKnife.inject(this);

		Toolbar toolbar = Ui.findView(this, R.id.toolbar);
		setSupportActionBar(toolbar);

		TextView googleAccountChange = Ui.findView(this, R.id.google_account_change);
		setGoogleAccountChangeVisiblity(googleAccountChange);
		googleAccountChange.setOnClickListener(new GoogleAccountChangeListener());

		Ui.findView(this, android.R.id.home).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		final View toolbarShadow = Ui.findView(this, R.id.toolbar_dropshadow);
		scrollContainer = Ui.findView(this, R.id.scroll_container);
		final float fortyEightDips = TypedValue
			.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
		toolbarShadow.setAlpha(0);
		scrollContainer.getViewTreeObserver().addOnScrollChangedListener(
			new ViewTreeObserver.OnScrollChangedListener() {
				@Override
				public void onScrollChanged() {
					float value = scrollContainer.getScrollY() / fortyEightDips;
					toolbarShadow.setAlpha(Math.min(1, Math.max(0, value)));
				}
			});

		AboutSectionFragment.Builder builder;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// App Settings
		appSettingsFragment = Ui.findSupportFragment(this, TAG_APP_SETTINGS);
		if (appSettingsFragment == null) {
			builder = new AboutSectionFragment.Builder(this);

			builder.setTitle(R.string.about_section_app_settings);

			AboutSectionFragment.RowDescriptor rowDescriptor = new AboutSectionFragment.RowDescriptor();
			rowDescriptor.clickTag = ROW_COUNTRY;
			rowDescriptor.title = getString(R.string.preference_point_of_sale_title);
			rowDescriptor.description = getCountryDescription();
			builder.addRow(rowDescriptor);

			appSettingsFragment = builder.build();
			ft.add(R.id.section_app_settings, appSettingsFragment, TAG_APP_SETTINGS);
		}

		// Support
		supportFragment = Ui.findSupportFragment(this, TAG_SUPPORT);
		if (supportFragment == null) {
			builder = new AboutSectionFragment.Builder(this);

			builder.setTitle(R.string.about_section_support);

			builder.addRow(getPOSSpecificWebsiteSupportString(),
					ROW_EXPEDIA_WEBSITE);

			builder.addRow(R.string.booking_support, ROW_BOOKING_SUPPORT);
			builder.addRow(R.string.app_support, ROW_APP_SUPPORT);

			if (ProductFlavorFeatureConfiguration.getInstance().isRewardsCardEnabled()) {
				builder.addRow(Phrase.from(this, R.string.rewards_visa_card_TEMPLATE).put("brand_reward_name",
					 getString(R.string.brand_reward_name)).format()
					.toString(), ROW_REWARDS_VISA_CARD);
			}

			supportFragment = builder.build();
			ft.add(R.id.section_contact_us, supportFragment, TAG_SUPPORT);
		}

		// Communicate
		AboutSectionFragment communicateFragment = Ui.findSupportFragment(this, TAG_COMMUNICATE);
		if (communicateFragment == null) {
			if (ProductFlavorFeatureConfiguration.getInstance().isCommunicateSectionEnabled()) {
				builder = new AboutSectionFragment.Builder(this);
				builder.setTitle(R.string.about_section_communicate);
				if (ProductFlavorFeatureConfiguration.getInstance().isRateOurAppEnabled()) {
					builder.addRow(R.string.rate_our_app, ROW_RATE_APP);
				}
				if (ProductFlavorFeatureConfiguration.getInstance().isWeReHiringEnabled()) {
					builder.addRow(R.string.WereHiring, ROW_WERE_HIRING);
				}
				communicateFragment = builder.build();
				ft.add(R.id.section_communicate, communicateFragment, TAG_COMMUNICATE);
			}
		}

		// T&C, privacy, etc
		legalFragment = Ui.findSupportFragment(this, TAG_LEGAL);
		if (legalFragment == null) {
			builder = new AboutSectionFragment.Builder(this);
			builder.setTitle(R.string.legal_information);
			builder.addRow(R.string.clear_private_data, ROW_CLEAR_PRIVATE_DATA);
			builder.addRow(R.string.info_label_terms_conditions, ROW_TERMS_AND_CONDITIONS);
			builder.addRow(R.string.info_label_privacy_policy, ROW_PRIVACY_POLICY);
			builder.addRow(R.string.lawyer_label_atol_information, ROW_ATOL_INFO);
			builder.addRow(R.string.open_source_software_licenses, ROW_OPEN_SOURCE_LICENSES);
			legalFragment = builder.build();
			ft.add(R.id.section_legal, legalFragment, TAG_LEGAL);
		}


		// Copyright
		copyrightFragment = Ui.findSupportFragment(this, TAG_COPYRIGHT);
		if (copyrightFragment == null) {
			CopyrightFragment.Builder copyBuilder = new CopyrightFragment.Builder();
			copyBuilder.setAppName(R.string.app_copyright_name);
			copyBuilder.setCopyright(getCopyrightString());
			copyBuilder.setLogo(R.drawable.app_copyright_logo);

			copyrightFragment = copyBuilder.build();
			ft.add(R.id.section_copyright, copyrightFragment, TAG_COPYRIGHT);
		}

		// All done
		ft.commit();

		TextView openSourceCredits = Ui.findView(this, R.id.open_source_credits_textview);
		openSourceCredits.setText(
			getString(R.string.this_app_makes_use_of_the_following) + " " + getString(R.string.open_source_names)
				+ "\n\n" + getString(R.string.stack_blur_credit));

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		if (shouldBail()) {
			return;
		}
		legalFragment
			.setRowVisibility(ROW_ATOL_INFO, PointOfSale.getPointOfSale().showAtolInfo() ? View.VISIBLE : View.GONE);
	}

	private String getCopyrightString() {
		return Phrase.from(this, R.string.copyright_TEMPLATE).put("brand", BuildConfig.brand)
			.put("year", Calendar.getInstance().get(Calendar.YEAR)).format().toString();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (shouldBail()) {
			return;
		}
		userAccountRefresher.setUserAccountRefreshListener(null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (shouldBail()) {
			return;
		}
		OmnitureTracking.trackAccountPageLoad();
		userAccountRefresher.setUserAccountRefreshListener(this);
		adjustLoggedInViews();
	}

	private boolean shouldBail() {
		boolean isPhone = !ExpediaBookingApp.useTabletInterface(this);
		return isPhone && notPortraitOrientation;
	}

	@Override
	public boolean onAboutRowClicked(int id) {
		switch (id) {
		case ROW_COUNTRY: {
			if (PointOfSale.getAllPointsOfSale(this).size() > 1) {
				OmnitureTracking.trackClickCountrySetting();
				DialogFragment selectCountryDialog = aboutUtils.createCountrySelectDialog();
				selectCountryDialog.show(getSupportFragmentManager(), "selectCountryDialog");
			}
			return true;
		}
		case ROW_BOOKING_SUPPORT: {
			OmnitureTracking.trackClickSupportBooking();
			DialogFragment contactExpediaDialog = aboutUtils.createContactExpediaDialog();
			contactExpediaDialog.show(getSupportFragmentManager(), "contactExpediaDialog");
			return true;
		}
		case ROW_EXPEDIA_WEBSITE: {
			aboutUtils.openExpediaWebsite();
			return true;
		}
		case ROW_APP_SUPPORT: {
			aboutUtils.openAppSupport();
			return true;
		}
		case ROW_RATE_APP: {
			aboutUtils.rateApp();
			return true;
		}
		case ROW_WERE_HIRING: {
			aboutUtils.openCareers();
			return true;
		}
		case ROW_REWARDS_VISA_CARD: {
			aboutUtils.openRewardsCard();
			return true;
		}

		// Legal section
		case ROW_TERMS_AND_CONDITIONS: {
			aboutUtils.openTermsAndConditions();
			return true;
		}
		case ROW_PRIVACY_POLICY: {
			aboutUtils.openPrivacyPolicy();
			return true;
		}
		case ROW_ATOL_INFO: {
			OmnitureTracking.trackClickAtolInformation();

			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(this);

			String message = getString(R.string.lawyer_label_atol_long_message);
			String html;
			if (ExpediaBookingApp.useTabletInterface(this)) {
				html = HtmlUtils.wrapInHeadAndBodyWithStandardTabletMargins(message);
			}
			else {
				html = HtmlUtils.wrapInHeadAndBody(message);
			}
			builder.setHtmlData(html);

			startActivity(builder.getIntent());

			return true;
		}
		case ROW_OPEN_SOURCE_LICENSES: {
			aboutUtils.openOpenSourceLicenses();
			return true;
		}

		case AboutSectionFragment.ROW_FLIGHT_TRACK: {
			OmnitureTracking.trackClickDownloadAppLink("FlightTrack");
			return false;
		}
		case AboutSectionFragment.ROW_FLIGHT_BOARD: {
			OmnitureTracking.trackClickDownloadAppLink("FlightBoard");
			return false;
		}

		case ROW_VSC_VOYAGES: {
			SocialUtils.openSite(this, AndroidUtils.getMarketAppLink(this, PKG_VSC_VOYAGES));
			return true;
		}
		case ROW_CLEAR_PRIVATE_DATA: {
			OmnitureTracking.trackClickClearPrivateData();
			ClearPrivateDataDialog dialog = new ClearPrivateDataDialog();
			dialog.show(getSupportFragmentManager(), "clearPrivateDataDialog");
			return true;
		}
		}

		return false;
	}

	@Override
	public void onAboutRowRebind(int id, TextView titleTextView, TextView descriptionTextView) {

		switch (id) {
			case ROW_COUNTRY :
				if (descriptionTextView != null) {
					descriptionTextView.setText(getCountryDescription());
				}
				break;
			case ROW_EXPEDIA_WEBSITE :
				if (titleTextView != null) {
					titleTextView.setText(getPOSSpecificWebsiteSupportString());
				}
				break;
			default:
				break;
		}
	}

	private String getPOSSpecificWebsiteSupportString() {
		return Phrase.from(this, R.string.website_TEMPLATE).put("brand",
				ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(this)).format()
				.toString();
	}

	private String getCountryDescription() {
		PointOfSale info = PointOfSale.getPointOfSale();
		final String country = getString(info.getCountryNameResId());
		final String url = info.getUrl();
		return country + " - " + url;
	}

	private void adjustLoggedInViews() {
		View signOutButton = Ui.findView(this, R.id.sign_out_button);
		View toolbarNotSignedIn = Ui.findView(this, R.id.toolbar_not_signed_in);
		View toolbarSignedIn = Ui.findView(this, R.id.toolbar_signed_in);
		ViewGroup loyaltySection = Ui.findView(this, R.id.section_loyalty_info);
		View sectionSignIn = Ui.findView(this, R.id.section_sign_in);

		if (User.isLoggedIn(this)) {
			sectionSignIn.setVisibility(View.GONE);
			toolbarSignedIn.setVisibility(View.VISIBLE);
			toolbarNotSignedIn.setVisibility(View.GONE);
			signOutButton.setVisibility(View.VISIBLE);

			TextView memberNameView = Ui.findView(this, R.id.toolbar_name);
			TextView memberEmailView = Ui.findView(this, R.id.toolbar_email);
			TextView memberTierView = Ui.findView(this, R.id.toolbar_loyalty_tier_text);

			User user = Db.getUser();
			Traveler member = user.getPrimaryTraveler();

			memberNameView.setText(member.getFullName());
			memberEmailView.setText(member.getEmail());

			UserLoyaltyMembershipInformation userLoyaltyInfo = user.getLoyaltyMembershipInformation();

			if (userLoyaltyInfo != null && userLoyaltyInfo.isLoyaltyMembershipActive()) {

				switch (userLoyaltyInfo.getLoyaltyMembershipTier()) {
				case BASE:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_base_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.reward_base_tier_text_color));
					memberTierView.setText(R.string.reward_base_tier_name_short);
					break;
				case MIDDLE:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_middle_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.reward_middle_tier_text_color));
					memberTierView.setText(R.string.reward_middle_tier_name_short);
					break;
				case TOP:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_top_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.reward_top_tier_text_color));
					memberTierView.setText(R.string.reward_top_tier_name_short);
					break;
				}

				TextView availablePointsTextView = Ui.findView(this, R.id.available_points);
				TextView pendingPointsTextView = Ui.findView(this, R.id.pending_points);

				NumberFormat numberFormatter = NumberFormat.getInstance();
				if (ProductFlavorFeatureConfiguration.getInstance().isRewardProgramPointsType()) {
					availablePointsTextView
						.setText(numberFormatter.format(userLoyaltyInfo.getLoyaltyPointsAvailable()));
				}
				else {
					availablePointsTextView.setText(
						userLoyaltyInfo.getLoyaltyMonetaryValue().getFormattedMoneyFromAmountAndCurrencyCode());
				}

				if (member.getLoyaltyPointsPending() > 0) {
					pendingPointsTextView.setVisibility(View.VISIBLE);
					pendingPointsTextView.setText(getString(R.string.loyalty_points_pending,
						numberFormatter.format(userLoyaltyInfo.getLoyaltyPointsPending())));
				}
				else {
					pendingPointsTextView.setVisibility(View.GONE);
				}

				TextView currencyTextView = Ui.findView(this, R.id.currency);
				TextView pointsMonetaryValueLabel = Ui.findView(this, R.id.points_monetary_value_label);
				TextView pointsMonetaryValueTextView = Ui.findView(this, R.id.points_monetary_value);
				View secondRowContainer = Ui.findView(this, R.id.second_row_container);
				View rowDivider = Ui.findView(this, R.id.row_divider);
				View firstRowCountry = Ui.findView(this, R.id.first_row_country);

				if (userLoyaltyInfo.isAllowedToShopWithPoints() && ProductFlavorFeatureConfiguration.getInstance()
					.isRewardProgramPointsType()) {
					Money loyaltyMonetaryValue = userLoyaltyInfo.getLoyaltyMonetaryValue();
					currencyTextView.setText(loyaltyMonetaryValue.getCurrency());
					pointsMonetaryValueTextView.setText(loyaltyMonetaryValue.getFormattedMoney());
					setupCountryView((TextView) secondRowContainer.findViewById(R.id.country));
					pointsMonetaryValueTextView.setVisibility(View.VISIBLE);
					pointsMonetaryValueLabel.setVisibility(View.VISIBLE);
					secondRowContainer.setVisibility(View.VISIBLE);
					rowDivider.setVisibility(View.VISIBLE);
					firstRowCountry.setVisibility(View.GONE);
				}
				else {
					setupCountryView((TextView) firstRowCountry.findViewById(R.id.country));
					secondRowContainer.setVisibility(View.GONE);
					pointsMonetaryValueTextView.setVisibility(View.GONE);
					pointsMonetaryValueLabel.setVisibility(View.GONE);
					firstRowCountry.setVisibility(View.VISIBLE);
					rowDivider.setVisibility(View.GONE);
				}
				loyaltySection.setVisibility(View.VISIBLE);
				if (ProductFlavorFeatureConfiguration.getInstance().shouldShowMemberTier()) {
					memberTierView.setVisibility(View.VISIBLE);
				}
				else {
					memberTierView.setVisibility(View.GONE);
				}
			}
			else {
				loyaltySection.setVisibility(View.GONE);
				memberTierView.setVisibility(View.GONE);
			}
		}
		else {
			View facebookButton = Ui.findView(this, R.id.sign_in_with_facebook_button);
			if (ProductFlavorFeatureConfiguration.getInstance().isFacebookLoginIntegrationEnabled()) {
				facebookButton.setVisibility(View.VISIBLE);
			}
			else {
				facebookButton.setVisibility(View.GONE);
			}

			Button createAccountButton = Ui.findView(this, R.id.create_account_button);
			createAccountButton.setText(
				Phrase.from(this, R.string.acct__Create_a_new_brand_account)
					.put("brand", BuildConfig.brand)
					.format());

			sectionSignIn.setVisibility(View.VISIBLE);
			toolbarSignedIn.setVisibility(View.GONE);
			toolbarNotSignedIn.setVisibility(View.VISIBLE);
			loyaltySection.setVisibility(View.GONE);
			signOutButton.setVisibility(View.GONE);
		}
	}

	////////////////////////////////
	// UserAccountRefreshListener

	@Override
	public void onUserAccountRefreshed() {
		adjustLoggedInViews();
	}

	/////////////////////////////////
	// CountrySelectDialogListener

	@Override
	public void showDialogFragment(DialogFragment dialog) {
		dialog.show(getSupportFragmentManager(), "dialog_from_about_utils");
	}

	@Override
	public void onNewCountrySelected(int pointOfSaleId) {
		SettingUtils.save(this, R.string.PointOfSaleKey, Integer.toString(pointOfSaleId));

		ClearPrivateDataUtil.clear(this);
		PointOfSale.onPointOfSaleChanged(this);
		AdTracker.updatePOS();

		setResult(Constants.RESULT_CHANGED_PREFS);

		adjustLoggedInViews();
		appSettingsFragment.notifyOnRowDataChanged(ROW_COUNTRY);
		supportFragment.notifyOnRowDataChanged(ROW_EXPEDIA_WEBSITE);
		legalFragment
			.setRowVisibility(ROW_ATOL_INFO, PointOfSale.getPointOfSale().showAtolInfo() ? View.VISIBLE : View.GONE);
		Toast.makeText(this, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
	}

	////////////////////////////////////
	// ClearPrivateDataDialogListener

	@Override
	public void onPrivateDataCleared() {
		adjustLoggedInViews();
		Toast.makeText(this, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
	}

	//////////////////////
	// Sign In Buttons

	@OnClick(R.id.sign_in_button)
	public void onSignInButtonClick() {
		Bundle args = AccountLibActivity
			.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.SignIn, null);
		User.signIn(this, args);
	}

	@OnClick(R.id.sign_in_with_facebook_button)
	public void onSignInFacebookButtonClick() {
		Bundle args = AccountLibActivity
			.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.FacebookSignIn, null);
		User.signIn(this, args);
	}

	@OnClick(R.id.create_account_button)
	public void onCreateAccountButtonClick() {
		Bundle args = AccountLibActivity
			.createArgumentsBundle(LineOfBusiness.PROFILE, Config.InitialState.CreateAccount, null);
		User.signIn(this, args);
	}


	//////////////////////////
	// Sign Out Button

	@OnClick(R.id.sign_out_button)
	public void onSignOutButtonClick() {
		OmnitureTracking.trackClickSignOut();
		new LoginConfirmLogoutDialogFragment().show(getSupportFragmentManager(), LoginConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		disableAutoSignIn();
		User.signOut(this);
		scrollContainer.smoothScrollTo(0, 0);
		adjustLoggedInViews();
	}

	private void disableAutoSignIn() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addConnectionCallbacks(this)
			.addApi(Auth.CREDENTIALS_API)
			.build();
		mGoogleApiClient.connect();
	}

	//////////////////////////////////////////////////////////////////////////
	// Secret Access
	//
	// For things like diagnostics panels.  Activates when you press the
	// bottom left/right corners of the Activity repeatedly.

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// We're using the gesture detector to detect taps, regardless of if anyone
		// else cared about it and used it.
		gestureDetector.onTouchEvent(ev);

		return super.dispatchTouchEvent(ev);
	}

	private OnGestureListener mOnGestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Point screenSize = AndroidUtils.getScreenSize(AccountSettingsActivity.this);
			Rect hitRect;
			int fourthWidth = screenSize.x / 4;
			int fourthHeight = screenSize.y / 4;

			if (secretCount % 2 == 0) {
				// Bottom left
				hitRect = new Rect(0, 3 * fourthHeight, fourthWidth, screenSize.y);
			}
			else {
				// Bottom right
				hitRect = new Rect(3 * fourthWidth, 3 * fourthHeight, screenSize.x, screenSize.y);
			}

			if (hitRect.contains((int) e.getX(), (int) e.getY())) {
				if (secretCount == 7) {
					activateSecret();
					secretCount = 0;
				}
				else {
					secretCount++;
				}
			}
			else {
				secretCount = 0;
			}

			return false;
		}
	};

	private void activateSecret() {
		// Normally we wouldn't access the Fragment's logo directly, but this is a special case.
		ImageView logo = Ui.findView(this, com.mobiata.android.R.id.logo);
		if (logo != null) {
			logo.setImageResource(R.drawable.ic_secret);
			if (BuildConfig.DEBUG) {
				Db.setMemoryTestActive(true);
			}
		}
	}

	private void setupCountryView(TextView countryTextView) {
		PointOfSale pos = PointOfSale.getPointOfSale();
		countryTextView.setText(pos.getThreeLetterCountryCode());
		LayerDrawable flag = new LayerDrawable(new Drawable[] {
			ContextCompat.getDrawable(this, pos.getCountryFlagResId()),
			ContextCompat.getDrawable(this, R.drawable.fg_flag_circle)
		});
		countryTextView.setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null);
	}

	private void setGoogleAccountChangeVisiblity(View view) {
		view.setVisibility(ProductFlavorFeatureConfiguration.getInstance().isGoogleAccountChangeEnabled() ? View.VISIBLE
			: View.GONE);
	}

	@Override
	public void onConnected(@Nullable Bundle bundle) {
		Auth.CredentialsApi.disableAutoSignIn(mGoogleApiClient);
		mGoogleApiClient.disconnect();
	}

	@Override
	public void onConnectionSuspended(int i) {
		// Connection is never suspended for disabling auto sign in.
	}

	 @Override
	 public boolean onLogoLongClick() {
		 return false;
	 }

	 @Override
	public void onLogoClick() {
		SocialUtils.openSite(this, ProductFlavorFeatureConfiguration.getInstance().getCopyrightLogoUrl(this));
	}

	private class GoogleAccountChangeListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			FragmentManager fm = getSupportFragmentManager();
			TextViewDialog mDialog = (TextViewDialog) fm.findFragmentByTag(GOOGLE_SIGN_IN_SUPPORT);
			if (mDialog == null) {
				//Create the dialog
				mDialog = new TextViewDialog();
				mDialog.setCancelable(false);
				mDialog.setCanceledOnTouchOutside(false);
				mDialog.setMessage(
					Phrase.from(AccountSettingsActivity.this, R.string.google_account_change_message_TEMPLATE)
						.put("brand", BuildConfig.brand)
						.format());
			}
			mDialog.show(fm, GOOGLE_SIGN_IN_SUPPORT);
		}
	}

}
