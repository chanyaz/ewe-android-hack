package com.expedia.bookings.activity;

import java.text.NumberFormat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ClearPrivateDataDialog;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.fragment.LoginConfirmLogoutDialogFragment;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.fragment.AboutSectionFragment.AboutSectionFragmentListener;
import com.mobiata.android.fragment.CopyrightFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.phrase.Phrase;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountSettingsActivity extends AppCompatActivity implements AboutSectionFragmentListener,
		AboutUtils.CountrySelectDialogListener, LoginConfirmLogoutDialogFragment.DoLogoutListener {
	private static final String TAG_SUPPORT = "TAG_SUPPORT";
	private static final String TAG_ALSO_BY_US = "TAG_ALSO_BY_US";
	private static final String TAG_LEGAL = "TAG_LEGAL";
	private static final String TAG_COPYRIGHT = "TAG_COPYRIGHT";
	private static final String TAG_COMMUNICATE = "TAG_COMMUNICATE";
	private static final String TAG_APP_SETTINGS = "TAG_APP_SETTINGS";

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

	private AboutUtils aboutUtils;

	private GestureDetectorCompat gestureDetector;

	private int secretCount = 0;

	private boolean wasStopped;

	private AboutSectionFragment appSettingsFragment;
	private AboutSectionFragment legalFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!ExpediaBookingApp.useTabletInterface(this)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		if (shouldBail()) {
			return;
		}

		aboutUtils = new AboutUtils(this);

		gestureDetector = new GestureDetectorCompat(this, mOnGestureListener);

		setContentView(R.layout.activity_account_settings);

		ButterKnife.inject(this);

		Toolbar toolbar = Ui.findView(this, R.id.toolbar);
		setSupportActionBar(toolbar);
		Ui.findView(this, android.R.id.home).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		AboutSectionFragment.Builder builder;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		adjustLoggedInViews();

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
		AboutSectionFragment supportFragment = Ui.findSupportFragment(this, TAG_SUPPORT);
		if (supportFragment == null) {
			builder = new AboutSectionFragment.Builder(this);

			builder.setTitle(R.string.about_section_support);

			builder.addRow(Phrase.from(this, R.string.website_TEMPLATE).put("brand",
							ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(this)).format()
							.toString(),
					ROW_EXPEDIA_WEBSITE);

			builder.addRow(R.string.booking_support, ROW_BOOKING_SUPPORT);
			builder.addRow(R.string.app_support, ROW_APP_SUPPORT);

			supportFragment = builder.build();
			ft.add(R.id.section_contact_us, supportFragment, TAG_SUPPORT);
		}

		// Communicate
		AboutSectionFragment communicateFragment = Ui.findSupportFragment(this, TAG_COMMUNICATE);
		if (communicateFragment == null) {
			builder = new AboutSectionFragment.Builder(this);

			builder.setTitle(R.string.about_section_communicate);

			builder.addRow(R.string.rate_our_app, ROW_RATE_APP);
			builder.addRow(R.string.WereHiring, ROW_WERE_HIRING);

			communicateFragment = builder.build();
			ft.add(R.id.section_communicate, communicateFragment, TAG_COMMUNICATE);
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

		// Apps also by us
		AboutSectionFragment alsoByFragment = Ui.findSupportFragment(this, TAG_ALSO_BY_US);
		if (alsoByFragment == null) {
			alsoByFragment = ProductFlavorFeatureConfiguration.getInstance().getAboutSectionFragment(this);
			if 	(alsoByFragment != null) {
				ft.add(R.id.section_also_by, alsoByFragment, TAG_ALSO_BY_US);
			}
		}


		// Copyright
		CopyrightFragment copyrightFragment = Ui.findSupportFragment(this, TAG_COPYRIGHT);
		if (copyrightFragment == null) {
			CopyrightFragment.Builder copyBuilder = new CopyrightFragment.Builder();
			copyBuilder.setAppName(Ui.obtainThemeResID(this, R.attr.skin_aboutAppNameString));
			copyBuilder.setCopyright(getCopyrightString());
			copyBuilder.setLogo(Ui.obtainThemeResID(this, R.attr.skin_aboutAppLogoDrawable));
			copyBuilder.setLogoUrl(ProductFlavorFeatureConfiguration.getInstance().getCopyrightLogoUrl(this));

			copyrightFragment = copyBuilder.build();
			ft.add(R.id.section_copyright, copyrightFragment, TAG_COPYRIGHT);
		}

		// All done
		ft.commit();

		TextView openSourceCredits = Ui.findView(this, R.id.open_source_credits_textview);
		openSourceCredits.setText(
				getString(R.string.this_app_makes_use_of_the_following) + " " + getString(R.string.open_source_names)
						+ "\n\n" + getString(R.string.stack_blur_credit));

		// Tracking
		if (savedInstanceState == null) {
			aboutUtils.trackAboutActivityPageLoad();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		legalFragment.setRowVisibility(ROW_ATOL_INFO, PointOfSale.getPointOfSale().showAtolInfo() ? View.VISIBLE : View.GONE);
	}

	private String getCopyrightString() {
		return Phrase.from(this, R.string.copyright_TEMPLATE).put("brand", BuildConfig.brand)
			.put("year", AndroidUtils.getAppBuildYear(this)).format().toString();
	}

	@Override
	protected void onStop() {
		super.onStop();
		wasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (wasStopped) {
			aboutUtils.trackAboutActivityPageLoad();
			wasStopped = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == AboutUtils.REQUEST_CODE_FEEDBACK && resultCode == RESULT_OK) {
			aboutUtils.trackFeedbackSubmitted();
		}
	}

	private boolean shouldBail() {
		return !ExpediaBookingApp.useTabletInterface(this) && !getResources().getBoolean(R.bool.portrait);
	}

	@Override
	public boolean onAboutRowClicked(int id) {
		switch (id) {
		case ROW_COUNTRY: {
			DialogFragment selectCountryDialog = aboutUtils.createCountrySelectDialog();
			selectCountryDialog.show(getSupportFragmentManager(), "selectCountryDialog");
			return true;
		}
		case ROW_BOOKING_SUPPORT: {
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
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(this);

			String license = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(this);
			String htmlEscapedData = "<pre>" + HtmlUtils.escape(license) + "</pre>";
			String html = HtmlUtils.wrapInHeadAndBody(htmlEscapedData);
			builder.setHtmlData(html);

			startActivity(builder.getIntent());

			return true;
		}

		case AboutSectionFragment.ROW_FLIGHT_TRACK: {
			aboutUtils.trackFlightTrackLink();
			return false;
		}
		case AboutSectionFragment.ROW_FLIGHT_BOARD: {
			aboutUtils.trackFlightBoardLink();
			return false;
		}

		case ROW_VSC_VOYAGES: {
			SocialUtils.openSite(this, AndroidUtils.getMarketAppLink(this, PKG_VSC_VOYAGES));
			return true;
		}
		case ROW_CLEAR_PRIVATE_DATA: {
			ClearPrivateDataDialog dialog = new ClearPrivateDataDialog();
			dialog.show(getSupportFragmentManager(), "clearPrivateDataDialog");
			return true;
		}
		}

		return false;
	}

	@Override
	public void onAboutRowRebind(int id, TextView titleTextView, TextView descriptionTextView) {
		if (id == ROW_COUNTRY) {
			if (descriptionTextView != null) {
				descriptionTextView.setText(getCountryDescription());
			}
		}
	}

	private String getCountryDescription() {
		PointOfSale info = PointOfSale.getPointOfSale();
		final String country = getString(info.getCountryNameResId());
		final String url = info.getUrl();
		return country + " - " + url;
	}

	private void adjustLoggedInViews() {
		if (User.isLoggedIn(this)) {
			Ui.findView(this, R.id.toolbar_signed_in).setVisibility(View.VISIBLE);
			Ui.findView(this, R.id.toolbar_not_signed_in).setVisibility(View.GONE);
			Ui.findView(this, R.id.sign_out_button).setVisibility(View.VISIBLE);

			ViewGroup loyaltySection = Ui.findView(this, R.id.section_loyalty_info);
			TextView memberNameView = Ui.findView(this, R.id.toolbar_name);
			TextView memberEmailView = Ui.findView(this, R.id.toolbar_email);
			TextView memberTierView = Ui.findView(this, R.id.toolbar_loyalty_tier_text);

			Traveler member = Db.getUser().getPrimaryTraveler();

			memberNameView.setText(member.getFullName());
			memberEmailView.setText(member.getEmail());

			if (member.getIsLoyaltyMembershipActive()) {
				loyaltySection.setVisibility(View.VISIBLE);
				memberTierView.setVisibility(View.VISIBLE);

				switch (member.getLoyaltyMembershipTier()) {
				case BLUE:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_base_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.expedia_plus_blue_text));
					memberTierView.setText(R.string.plus_blue);
					break;
				case SILVER:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_middle_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.expedia_plus_silver_text));
					memberTierView.setText(R.string.plus_silver);
					break;
				case GOLD:
					memberTierView.setBackgroundResource(R.drawable.bg_loyalty_badge_top_tier);
					memberTierView.setTextColor(ContextCompat.getColor(this, R.color.expedia_plus_gold_text));
					memberTierView.setText(R.string.plus_gold);
					break;
				}

				TextView availablePointsTextView = Ui.findView(this, R.id.available_points);
				TextView pendingPointsTextView = Ui.findView(this, R.id.pending_points);

				NumberFormat numberFormatter = NumberFormat.getInstance();
				availablePointsTextView.setText(numberFormatter.format(member.getLoyaltyPointsAvailable()));
				pendingPointsTextView.setText(getString(R.string.loyalty_points_pending,
						numberFormatter.format(member.getLoyaltyPointsPending())));

				TextView countryTextView = Ui.findView(this, R.id.country);
				PointOfSale pos = PointOfSale.getPointOfSale();
				countryTextView.setText(pos.getThreeLetterCountryCode());
				LayerDrawable flag = new LayerDrawable(new Drawable[] {
						ContextCompat.getDrawable(this, pos.getCountryFlagResId()),
						ContextCompat.getDrawable(this, R.drawable.fg_flag_circle)
				});
				countryTextView.setCompoundDrawablesWithIntrinsicBounds(flag, null, null, null);
			}
			else {
				loyaltySection.setVisibility(View.GONE);
				memberTierView.setVisibility(View.GONE);
			}
		}
		else {
			Ui.findView(this, R.id.toolbar_signed_in).setVisibility(View.GONE);
			Ui.findView(this, R.id.toolbar_not_signed_in).setVisibility(View.VISIBLE);
			Ui.findView(this, R.id.section_loyalty_info).setVisibility(View.GONE);
			Ui.findView(this, R.id.sign_out_button).setVisibility(View.GONE);
		}
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

		setResult(ExpediaBookingPreferenceActivity.RESULT_CHANGED_PREFS);

		adjustLoggedInViews();
		appSettingsFragment.notifyOnRowDataChanged(ROW_COUNTRY);
		legalFragment.setRowVisibility(ROW_ATOL_INFO, PointOfSale.getPointOfSale().showAtolInfo() ? View.VISIBLE : View.GONE);
		Toast.makeText(this, R.string.toast_private_data_cleared, Toast.LENGTH_LONG).show();
	}


	//////////////////////////
	// Sign Out Button

	@OnClick(R.id.sign_out_button)
	public void onSignOutButtonClick() {
		new LoginConfirmLogoutDialogFragment().show(getSupportFragmentManager(), LoginConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		User.signOut(this);
		adjustLoggedInViews();
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
}
