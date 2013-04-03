package com.expedia.bookings.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.HtmlUtils;
import com.expedia.bookings.utils.Ui;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.dialog.MailChimpDialogFragment;
import com.mobiata.android.dialog.MailChimpDialogFragment.OnSubscribeEmailClickedListener;
import com.mobiata.android.dialog.MailChimpFailureDialogFragment;
import com.mobiata.android.dialog.MailChimpSuccessDialogFragment;
import com.mobiata.android.fragment.CopyrightFragment;
import com.mobiata.android.fragment.AboutSectionFragment;
import com.mobiata.android.fragment.AboutSectionFragment.AboutSectionFragmentListener;
import com.mobiata.android.util.MailChimpUtils;
import com.mobiata.android.util.MailChimpUtils.MailChimpResult;

public class AboutActivity extends SherlockFragmentActivity implements AboutSectionFragmentListener, OnSubscribeEmailClickedListener {
	private static final String TAG_CONTACT_US = "TAG_CONTACT_US";
	private static final String TAG_ALSO_BY_US = "TAG_ALSO_BY_US";
	private static final String TAG_LEGAL = "TAG_LEGAL";
	private static final String TAG_COPYRIGHT = "TAG_COPYRIGHT";

	private static final String DOWNLOAD_MAILCHIMP = "DOWNLOAD_MAILCHIMP";

	private static final String TAG_MAILCHIMP_DIALOG = "TAG_MAILCHIMP_DIALOG";
	private static final String TAG_MAILCHIMP_SUCCESS_DIALOG = "TAG_MAILCHIMP_SUCCESS_DIALOG";
	private static final String TAG_MAILCHIMP_FAILURE_DIALOG = "TAG_MAILCHIMP_FAILURE_DIALOG";

	private static final int ROW_CONTACT_EXPEDIA = 1;
	private static final int ROW_APP_SUPPORT = 2;
	private static final int ROW_APP_FEEDBACK = 3;
	private static final int ROW_WERE_HIRING = 4;
	private static final int ROW_PRIVACY_POLICY = 5;
	private static final int ROW_TERMS_AND_CONDITIONS = 6;
	private static final int ROW_ATOL_INFO = 7;
	private static final int ROW_OPEN_SOURCE_LICENSES = 8;

	private AboutUtils mAboutUtils;

	private boolean mWasStopped;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAboutUtils = new AboutUtils(this);

		setContentView(R.layout.activity_about);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);

		AboutSectionFragment.Builder builder;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		// Contact Us
		AboutSectionFragment contactUsFragment = Ui.findSupportFragment(this, TAG_CONTACT_US);
		if (contactUsFragment == null) {
			builder = new AboutSectionFragment.Builder(this);
			builder.addRow(R.string.contact_expedia, ROW_CONTACT_EXPEDIA);
			builder.addRow(R.string.app_support, ROW_APP_SUPPORT);
			builder.addRow(R.string.app_feedback, ROW_APP_FEEDBACK);
			builder.addRow(com.mobiata.android.R.string.WereHiring, ROW_WERE_HIRING);
			contactUsFragment = builder.build();
			ft.add(R.id.section_contact_us, contactUsFragment, TAG_CONTACT_US);
		}

		// Apps also by us
		AboutSectionFragment alsoByFragment = Ui.findSupportFragment(this, TAG_ALSO_BY_US);
		if (alsoByFragment == null) {
			alsoByFragment = AboutSectionFragment.buildOtherAppsSection(this);
			ft.add(R.id.section_also_by, alsoByFragment, TAG_ALSO_BY_US);
		}

		// T&C, privacy, etc
		AboutSectionFragment legalFragment = Ui.findSupportFragment(this, TAG_LEGAL);
		if (legalFragment == null) {
			builder = new AboutSectionFragment.Builder(this);
			builder.setTitle(R.string.legal_information);
			builder.addRow(R.string.info_label_privacy_policy, ROW_PRIVACY_POLICY);
			builder.addRow(R.string.info_label_terms_conditions, ROW_TERMS_AND_CONDITIONS);
			if (PointOfSale.getPointOfSale().getPointOfSaleId().equals(PointOfSaleId.UNITED_KINGDOM)) {
				builder.addRow(R.string.lawyer_label_atol_information, ROW_ATOL_INFO);
			}
			builder.addRow(R.string.open_source_software_licenses, ROW_OPEN_SOURCE_LICENSES);
			legalFragment = builder.build();
			ft.add(R.id.section_legal, legalFragment, TAG_LEGAL);
		}

		// Copyright
		CopyrightFragment copyrightFragment = Ui.findSupportFragment(this, TAG_COPYRIGHT);
		if (copyrightFragment == null) {
			copyrightFragment = CopyrightFragment.newInstance(getString(R.string.app_name), getString(R.string.copyright));
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
		getSupportMenuInflater().inflate(R.menu.menu_about, menu);
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
		boolean handled = false;
		switch (id) {
		case ROW_CONTACT_EXPEDIA: {
			showDialog(DIALOG_CONTACT_EXPEDIA);
			handled =  true;
			break;
		}
		case ROW_APP_SUPPORT: {
			mAboutUtils.openAppSupport();
			handled = true;
			break;
		}
		case ROW_APP_FEEDBACK: {
			mAboutUtils.openAppFeedback();
			handled = true;
			break;
		}
		case ROW_WERE_HIRING: {
			SocialUtils.openSite(this, "http://www.mobiata.com/careers");
			mAboutUtils.trackHiringLink();
			handled = true;
			break;
		}

		// Legal section
		case ROW_TERMS_AND_CONDITIONS: {
			PointOfSale posInfo = PointOfSale.getPointOfSale();
			SocialUtils.openSite(this, posInfo.getTermsAndConditionsUrl());
			handled = true;
			break;
		}
		case ROW_PRIVACY_POLICY: {
			PointOfSale posInfo = PointOfSale.getPointOfSale();
			SocialUtils.openSite(this, posInfo.getPrivacyPolicyUrl());
			handled = true;
			break;
		}
		case ROW_ATOL_INFO: {
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(this);

			String message = getString(R.string.lawyer_label_atol_long_message);
			String html = HtmlUtils.wrapInHeadAndBody(message);
			builder.setHtmlData(html);

			startActivity(builder.getIntent());

			handled = true;
			break;
		}
		case ROW_OPEN_SOURCE_LICENSES: {
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(this);

			String license = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this);
			String htmlEscapedData = "<pre>" + HtmlUtils.escape(license) + "</pre>";
			String html = HtmlUtils.wrapInHeadAndBody(htmlEscapedData);
			builder.setHtmlData(html);

			startActivity(builder.getIntent());

			handled = true;
			break;
		}

		// Track app row clicks - return false, because we want the default behaviour
		case AboutSectionFragment.ROW_FLIGHT_TRACK_FREE: {
			mAboutUtils.trackFlightTrackFreeLink();
			break;
		}
		case AboutSectionFragment.ROW_FLIGHT_TRACK: {
			mAboutUtils.trackFlightTrackLink();
			break;
		}
		case AboutSectionFragment.ROW_FLIGHT_BOARD: {
			mAboutUtils.trackFlightBoardLink();
			break;
		}

		default:
			handled = false;
		}
		return handled;
	}

	private final OnDownloadComplete<MailChimpResult> mMailChimpCallback = new OnDownloadComplete<MailChimpResult>() {
		@Override
		public void onDownload(MailChimpResult result) {
			if (result == null || result.mSuccess == false) {
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
}

