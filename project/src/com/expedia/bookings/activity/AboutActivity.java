package com.expedia.bookings.activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.android.util.ViewUtils;

public class AboutActivity extends com.mobiata.android.app.AboutActivity {

	private static final int DIALOG_CONTACT_EXPEDIA = 1;
	private static final int DIALOG_EXPEDIA_WEBSITE = 2;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private AboutUtils mUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new AboutUtils(this);

		ViewGroup firstSection = addSection();
		firstSection.setBackgroundDrawable((Drawable) null);
		addSimpleRow(firstSection, getString(R.string.contact_expedia), new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_CONTACT_EXPEDIA);
			}
		});
		addSimpleRow(firstSection, getString(R.string.app_support), new OnClickListener() {
			public void onClick(View v) {
				mUtils.openAppSupport();
			}
		});
		addSimpleRow(firstSection, getString(R.string.app_feedback), new OnClickListener() {
			public void onClick(View v) {
				mUtils.openAppFeedback();
			}
		});
		addHiringPitch(firstSection, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackHiringLink();
			}
		});

		// Section about some of our other apps
		ViewGroup otherAppsSection = addSection(getString(R.string.ALSO_BY_MOBIATA));
		addAppAbout(otherAppsSection, APP_FLIGHTTRACKFREE, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightTrackFreeLink();
			}
		});
		addAppAbout(otherAppsSection, APP_FLIGHTTRACK, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightTrackLink();
			}
		});
		addAppAbout(otherAppsSection, APP_FLIGHTBOARD, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightBoardLink();
			}
		});

		TextView title = Ui.findView(this, R.id.action_bar_title);
		if (title != null) {
			title.setText(getString(R.string.about_title_template, getString(R.string.app_name)));
		}

		View upButton = Ui.findView(this, R.id.action_bar_up_button);
		if (upButton != null) {
			upButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
		}

		ViewGroup emailButton = Ui.findView(this, R.id.follow_email_button);
		emailButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_MAILCHIMP);
			}
		});
		ViewUtils.setAllCaps(emailButton);

		ViewGroup twitterButton = Ui.findView(this, R.id.follow_twitter_button);
		twitterButton.setOnClickListener(getTwitterButtonClickedListener());
		ViewUtils.setAllCaps(twitterButton);

		ViewGroup facebookButton = Ui.findView(this, R.id.follow_facebook_button);
		facebookButton.setOnClickListener(getFacebookButtonClickedListener());
		ViewUtils.setAllCaps(facebookButton);

		// Tracking
		if (savedInstanceState == null) {
			mUtils.trackAboutActivityPageLoad();
		}

		TextView tac_link = Ui.findView(this, R.id.terms_and_conditions_link);
		tac_link.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>",
					RulesRestrictionsUtils.getTermsAndConditionsUrl(mContext), mContext.getString(R.string.info_label_terms_conditions))));
		tac_link.setMovementMethod(LinkMovementMethod.getInstance());

		TextView privacy_policy_link = Ui.findView(this, R.id.privacy_policy_link);
		privacy_policy_link.setText(Html.fromHtml(String.format("<a href=\"%s\">%s</a>",
					RulesRestrictionsUtils.getPrivacyPolicyUrl(mContext), mContext.getString(R.string.info_label_privacy_policy))));
		privacy_policy_link.setMovementMethod(LinkMovementMethod.getInstance());
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
			mUtils.trackAboutActivityPageLoad();
			mWasStopped = false;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == AboutUtils.REQUEST_CODE_FEEDBACK && resultCode == RESULT_OK) {
			mUtils.trackFeedbackSubmitted();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CONTACT_EXPEDIA: {
			return mUtils.createContactExpediaDialog(new Runnable() {
				public void run() {
					removeDialog(DIALOG_CONTACT_EXPEDIA);
				}
			});
		}
		case DIALOG_EXPEDIA_WEBSITE: {
			return mUtils.createExpediaWebsiteDialog(new Runnable() {
				@Override
				public void run() {
					removeDialog(DIALOG_EXPEDIA_WEBSITE);
				}
			});
		}
		}

		return super.onCreateDialog(id);
	}

	@Override
	public String getAboutHtml() {
		return getString(R.string.copyright);
	}

	@Override
	public boolean useDefaultBehavior() {
		return false;
	}
}
