package com.expedia.bookings.activity;

import java.util.LinkedHashMap;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.mobiata.android.SocialUtils;

public class AboutActivity extends com.mobiata.android.app.AboutActivity {

	private static final int DIALOG_CONTACT_EXPEDIA = 1;

	// For tracking - tells you when a user paused the Activity but came back to it
	private boolean mWasStopped;

	private AboutUtils mUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new AboutUtils(this);

		// Section about Expedia
		ViewGroup expediaSection = addSection();
		addSimpleRow(expediaSection, getString(R.string.contact_expedia), new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_CONTACT_EXPEDIA);
			}
		});
		addSimpleRow(expediaSection, getString(R.string.expedia_website), new OnClickListener() {
			public void onClick(View v) {
				mUtils.openExpediaWebsite();
			}
		});

		// Section about this app
		ViewGroup appSection = addSection();
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

		addHiringPitch(appSection);

		// Section about some of our other apps
		ViewGroup otherAppsSection = addSection(getString(R.string.travel_smart));
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

		ViewGroup rulesRestrictionsSection = addSection();
		final LinkedHashMap<String, String> map = RulesRestrictionsUtils.getInfoData(this);
		for (final String label : map.keySet()) {
			addSimpleRow(rulesRestrictionsSection, label, new OnClickListener() {
				public void onClick(View v) {
					SocialUtils.openSite(mContext, map.get(label));
				}
			});
		}

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
