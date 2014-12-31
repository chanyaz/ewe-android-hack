package com.expedia.bookings.activity;

import java.util.Calendar;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextUtils;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.dialog.ClearPrivateDataDialog;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.dialog.MailChimpDialogFragment;
import com.mobiata.android.dialog.MailChimpDialogFragment.OnSubscribeEmailClickedListener;
import com.mobiata.android.dialog.MailChimpFailureDialogFragment;
import com.mobiata.android.dialog.MailChimpSuccessDialogFragment;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.fragment.AboutSectionFragment.AboutSectionFragmentListener;
import com.mobiata.android.fragment.AboutSectionFragment.Builder;
import com.mobiata.android.fragment.AboutSectionFragment.RowDescriptor;
import com.mobiata.android.fragment.CopyrightFragment;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.MailChimpUtils;
import com.mobiata.android.util.MailChimpUtils.MailChimpResult;

public class AboutActivity extends FragmentActivity implements AboutSectionFragmentListener,
		OnSubscribeEmailClickedListener {
	private static final String TAG_CONTACT_US = "TAG_CONTACT_US";
	private static final String TAG_ALSO_BY_US = "TAG_ALSO_BY_US";
	private static final String TAG_LEGAL = "TAG_LEGAL";
	private static final String TAG_COPYRIGHT = "TAG_COPYRIGHT";

	private static final String DOWNLOAD_MAILCHIMP = "DOWNLOAD_MAILCHIMP";

	private static final String TAG_MAILCHIMP_DIALOG = "TAG_MAILCHIMP_DIALOG";
	private static final String TAG_MAILCHIMP_SUCCESS_DIALOG = "TAG_MAILCHIMP_SUCCESS_DIALOG";
	private static final String TAG_MAILCHIMP_FAILURE_DIALOG = "TAG_MAILCHIMP_FAILURE_DIALOG";

	private static final int ROW_BOOKING_SUPPORT = 1;
	private static final int ROW_EXPEDIA_WEBSITE = 2;
	private static final int ROW_APP_SUPPORT = 3;
	private static final int ROW_WERE_HIRING = 4;
	private static final int ROW_PRIVACY_POLICY = 5;
	private static final int ROW_TERMS_AND_CONDITIONS = 6;
	private static final int ROW_ATOL_INFO = 7;
	private static final int ROW_OPEN_SOURCE_LICENSES = 8;

	private static final int ROW_VSC_VOYAGES = 9;
	private final static String PKG_VSC_VOYAGES = "com.vsct.vsc.mobile.horaireetresa.android";

	private static final int ROW_VSC_PRIVATE_DATA = 10;

	private AboutUtils mAboutUtils;

	private GestureDetectorCompat mGestureDetector;

	private int mSecretCount = 0;

	private boolean mWasStopped;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAboutUtils = new AboutUtils(this);

		mGestureDetector = new GestureDetectorCompat(this, mOnGestureListener);

		setContentView(R.layout.activity_about);

		ActionBar ab = getActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);

		AboutSectionFragment.Builder builder;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// Contact Us
		AboutSectionFragment contactUsFragment = Ui.findSupportFragment(this, TAG_CONTACT_US);
		if (contactUsFragment == null) {
			builder = new AboutSectionFragment.Builder(this);

			builder.addRow(R.string.booking_support, ROW_BOOKING_SUPPORT);

			builder.addRow(Ui.obtainThemeResID(this, R.attr.skin_aboutAppSupportString), ROW_APP_SUPPORT);

			builder.addRow(Ui.obtainThemeResID(this, R.attr.skin_aboutWebsiteString), ROW_EXPEDIA_WEBSITE);

			if (ExpediaBookingApp.IS_EXPEDIA) {
				builder.addRow(com.mobiata.android.R.string.WereHiring, ROW_WERE_HIRING);
			}

			// 1170. VSC Add clear private data in info/about screen
			else if (ExpediaBookingApp.IS_VSC) {
				builder.addRow(R.string.clear_private_data, ROW_VSC_PRIVATE_DATA);
			}

			contactUsFragment = builder.build();
			ft.add(R.id.section_contact_us, contactUsFragment, TAG_CONTACT_US);
		}

		// Apps also by us
		AboutSectionFragment alsoByFragment = Ui.findSupportFragment(this, TAG_ALSO_BY_US);
		if (alsoByFragment == null && (ExpediaBookingApp.IS_EXPEDIA || ExpediaBookingApp.IS_VSC)) {
			if (ExpediaBookingApp.IS_VSC) {
				alsoByFragment = buildVSCOtherAppsSection(this);
			}
			else {
				alsoByFragment = AboutSectionFragment.buildOtherAppsSection(this);
			}
			ft.add(R.id.section_also_by, alsoByFragment, TAG_ALSO_BY_US);
		}

		// T&C, privacy, etc
		AboutSectionFragment legalFragment = Ui.findSupportFragment(this, TAG_LEGAL);
		if (legalFragment == null) {
			builder = new AboutSectionFragment.Builder(this);
			builder.setTitle(R.string.legal_information);
			builder.addRow(R.string.info_label_privacy_policy, ROW_PRIVACY_POLICY);
			builder.addRow(R.string.info_label_terms_conditions, ROW_TERMS_AND_CONDITIONS);
			if (PointOfSale.getPointOfSale().showAtolInfo()) {
				builder.addRow(R.string.lawyer_label_atol_information, ROW_ATOL_INFO);
			}
			builder.addRow(R.string.open_source_software_licenses, ROW_OPEN_SOURCE_LICENSES);
			legalFragment = builder.build();
			ft.add(R.id.section_legal, legalFragment, TAG_LEGAL);
		}

		// Copyright
		CopyrightFragment copyrightFragment = Ui.findSupportFragment(this, TAG_COPYRIGHT);
		if (copyrightFragment == null) {
			CopyrightFragment.Builder copyBuilder = new CopyrightFragment.Builder();
			copyBuilder.setAppName(Ui.obtainThemeResID(this, R.attr.skin_aboutAppNameString));
			copyBuilder.setCopyright(getCopyrightString());
			copyBuilder.setLogo(Ui.obtainThemeResID(this, R.attr.skin_aboutAppLogoDrawable));
			if (ExpediaBookingApp.IS_EXPEDIA || ExpediaBookingApp.IS_VSC) {
				copyBuilder.setLogoUrl(this.getString(Ui.obtainThemeResID(this, R.attr.skin_aboutInfoUrlString)));
			}
			else {
				copyBuilder.setLogoUrl(PointOfSale.getPointOfSale().getWebsiteUrl());
			}

			copyrightFragment = copyBuilder.build();
			ft.add(R.id.section_copyright, copyrightFragment, TAG_COPYRIGHT);
		}

		// All done
		ft.commit();

		StringBuilder sb = new StringBuilder();
		sb.append(getString(R.string.this_app_makes_use_of_the_following));
		sb.append(" ");
		sb.append(getString(R.string.open_source_names));
		sb.append("\n\n");
		sb.append(getString(R.string.stack_blur_credit));
		TextView openSourceCredits = Ui.findView(this, R.id.open_source_credits_textview);
		openSourceCredits.setText(sb.toString());

		// Tracking
		if (savedInstanceState == null) {
			mAboutUtils.trackAboutActivityPageLoad();
		}
	}

	private String getCopyrightString() {
		int templateId = Ui.obtainThemeResID(this, R.attr.skin_aboutCopyrightString);
		return getString(templateId, AndroidUtils.getAppBuildDate(this).get(Calendar.YEAR));
	}

	private AboutSectionFragment buildVSCOtherAppsSection(Context context) {
		Builder builder = new Builder(context);
		builder.setTitle(R.string.AlsoByVSC);

		RowDescriptor app = new RowDescriptor();
		app.title = context.getString(R.string.VSC_Voyages_SNF);
		app.description = context.getString(R.string.VSC_Voyages_SNF_description);
		app.clickId = ROW_VSC_VOYAGES;
		app.drawableId = R.drawable.ic_vsc_train_app;
		builder.addRow(app);

		return builder.build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mWasStopped = true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mWasStopped) {
			mAboutUtils.trackAboutActivityPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == AboutUtils.REQUEST_CODE_FEEDBACK && resultCode == RESULT_OK) {
			mAboutUtils.trackFeedbackSubmitted();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// #1242. For VSC and travelocity don't add social media menu items.
		if (ExpediaBookingApp.IS_EXPEDIA) {
			getMenuInflater().inflate(R.menu.menu_about, menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		case R.id.about_subscribe:
			MailChimpDialogFragment dialogFrag = new MailChimpDialogFragment();
			dialogFrag.show(getSupportFragmentManager(), MailChimpDialogFragment.class.toString());
			return true;
		case R.id.about_follow:
			SocialUtils.openSite(this, "https://twitter.com/intent/user?screen_name=mobiata");
			return true;
		case R.id.about_like:
			SocialUtils.openSite(this, "http://www.facebook.com/pages/Mobiata/95307070557");
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private static final int DIALOG_CONTACT_EXPEDIA = 100;

	@Override
	public Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CONTACT_EXPEDIA: {
			return mAboutUtils.createContactExpediaDialog(new Runnable() {
				@Override
				public void run() {
					removeDialog(DIALOG_CONTACT_EXPEDIA);
				}
			});
		}
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	public boolean onAboutRowClicked(int id) {
		switch (id) {
		case ROW_BOOKING_SUPPORT: {
			showDialog(DIALOG_CONTACT_EXPEDIA);
			return true;
		}
		case ROW_EXPEDIA_WEBSITE: {
			mAboutUtils.openExpediaWebsite();
			return true;
		}
		case ROW_APP_SUPPORT: {
			mAboutUtils.openAppSupport();
			return true;
		}
		case ROW_WERE_HIRING: {
			mAboutUtils.openCareers();
			return true;
		}

		// Legal section
		case ROW_TERMS_AND_CONDITIONS: {
			mAboutUtils.openTermsAndConditions();
			return true;
		}
		case ROW_PRIVACY_POLICY: {
			mAboutUtils.openPrivacyPolicy();
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

			String license = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
			String htmlEscapedData = "<pre>" + HtmlUtils.escape(license) + "</pre>";
			String html = HtmlUtils.wrapInHeadAndBody(htmlEscapedData);
			builder.setHtmlData(html);

			startActivity(builder.getIntent());

			return true;
		}

		case AboutSectionFragment.ROW_FLIGHT_TRACK: {
			mAboutUtils.trackFlightTrackLink();
			return false;
		}
		case AboutSectionFragment.ROW_FLIGHT_BOARD: {
			mAboutUtils.trackFlightBoardLink();
			return false;
		}

		case ROW_VSC_VOYAGES: {
			SocialUtils.openSite(this, AndroidUtils.getGooglePlayAppLink(PKG_VSC_VOYAGES));
			return true;
		}
		// 1170. VSC Add clear private data in info/about screen
		case ROW_VSC_PRIVATE_DATA: {
			ClearPrivateDataDialog dialog = new ClearPrivateDataDialog();
			dialog.show(getSupportFragmentManager(), "clearPrivateDataDialog");
			return true;
		}
		}

		return false;
	}

	private final OnDownloadComplete<MailChimpResult> mMailChimpCallback = new OnDownloadComplete<MailChimpResult>() {
		@Override
		public void onDownload(MailChimpResult result) {
			if (result != null && result.mSuccess) {
				MailChimpFailureDialogFragment dialogFragment = new MailChimpFailureDialogFragment();
				Bundle args = new Bundle();
				if (!TextUtils.isEmpty(result.mErrorMessage)) {
					args.putString("message", result.mErrorMessage);
				}
				else {
					args.putString("message", getString(com.mobiata.android.R.string.MailChimpFailure));
				}
				dialogFragment.setArguments(args);
				dialogFragment.show(getSupportFragmentManager(), MailChimpFailureDialogFragment.class.toString());
			}
			else {
				MailChimpSuccessDialogFragment dialogFragment = new MailChimpSuccessDialogFragment();
				dialogFragment.show(getSupportFragmentManager(), MailChimpSuccessDialogFragment.class.toString());
			}
			return;
		}
	};

	@Override
	public void onSubscribeEmail(String email) {
		MailChimpUtils.subscribeEmail(this, DOWNLOAD_MAILCHIMP, email, mMailChimpCallback);
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
		mGestureDetector.onTouchEvent(ev);

		return super.dispatchTouchEvent(ev);
	}

	private OnGestureListener mOnGestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Point screenSize = AndroidUtils.getScreenSize(AboutActivity.this);
			Rect hitRect;
			int fourthWidth = screenSize.x / 4;
			int fourthHeight = screenSize.y / 4;

			if (mSecretCount % 2 == 0) {
				// Bottom left
				hitRect = new Rect(0, 3 * fourthHeight, fourthWidth, screenSize.y);
			}
			else {
				// Bottom right
				hitRect = new Rect(3 * fourthWidth, 3 * fourthHeight, screenSize.x, screenSize.y);
			}

			if (hitRect.contains((int) e.getX(), (int) e.getY())) {
				if (mSecretCount == 7) {
					activateSecret();
					mSecretCount = 0;
				}
				else {
					mSecretCount++;
				}
			}
			else {
				mSecretCount = 0;
			}

			return false;
		}
	};

	private void activateSecret() {
		// Normally we wouldn't access the Fragment's logo directly, but this is a special case.
		ImageView logo = Ui.findView(this, com.mobiata.android.R.id.logo);
		if (logo != null) {
			logo.setImageResource(R.drawable.ic_secret);
		}
	}
}
