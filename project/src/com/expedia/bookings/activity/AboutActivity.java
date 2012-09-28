package com.expedia.bookings.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;

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

		// Section about Expedia
		ViewGroup expediaSection = addSection(getString(R.string.EXPEDIA));
		addSimpleRow(expediaSection, getString(R.string.contact_expedia), new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_CONTACT_EXPEDIA);
			}
		});
		addSimpleRow(expediaSection, getString(R.string.expedia_website), new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_EXPEDIA_WEBSITE);
			}
		});

		// Section about this app
		ViewGroup appSection = addSection(getString(R.string.INTERACT));
		addSimpleRow(appSection, getString(R.string.app_feedback), new OnClickListener() {
			public void onClick(View v) {
				mUtils.openAppFeedback();
			}
		});
		addSimpleRow(appSection, getString(R.string.app_support), new OnClickListener() {
			public void onClick(View v) {
				mUtils.openAppSupport();
			}
		});
		addSimpleRow(appSection, getString(R.string.TellAFriend), new OnClickListener() {
			public void onClick(View v) {
				mUtils.tellAFriend();
			}
		});

		addHiringPitch(appSection, new OnClickListener() {
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

		ViewGroup rulesRestrictionsSection = addSection(getString(R.string.RULES_AND_RESTRICTIONS));
		addSimpleRow(rulesRestrictionsSection, getString(R.string.info_label_terms_conditions), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.openSite(mContext, RulesRestrictionsUtils.getTermsAndConditionsUrl(mContext));
			}
		});
		addSimpleRow(rulesRestrictionsSection, getString(R.string.info_label_privacy_policy), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.openSite(mContext, RulesRestrictionsUtils.getPrivacyPolicyUrl(mContext));
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

                ViewGroup twitterButton = Ui.findView(this, R.id.follow_twitter_button);
                twitterButton.setOnClickListener(getTwitterButtonClickedListener());

                ViewGroup facebookButton = Ui.findView(this, R.id.follow_facebook_button);
                facebookButton.setOnClickListener(getFacebookButtonClickedListener());

		// Tracking
		if (savedInstanceState == null) {
			mUtils.trackAboutActivityPageLoad();
		}
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
