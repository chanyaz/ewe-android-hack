package com.expedia.bookings.activity;

import java.util.LinkedHashMap;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.ContactExpediaDialogFragment;
import com.expedia.bookings.utils.AboutUtils;
import com.expedia.bookings.utils.RulesRestrictionsUtils;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.app.HoneycombAboutActivity;

public class TabletAboutActivity extends HoneycombAboutActivity {

	private AboutUtils mUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUtils = new AboutUtils(this);

		setAboutAppsTitle(getString(R.string.travel_smart));

		ViewGroup standardSection = getStandardSection();

		// Section about Expedia
		addSimpleRow(standardSection, getString(R.string.contact_expedia),
				getString(R.string.contact_expedia_description), new OnClickListener() {
					public void onClick(View v) {
						DialogFragment newFragment = ContactExpediaDialogFragment.newInstance();
						newFragment.show(getFragmentManager(), "ContactExpediaDialog");
					}
				});
		addSimpleRow(standardSection, getString(R.string.expedia_website),
				getString(R.string.expedia_website_description), new OnClickListener() {
					public void onClick(View v) {
						mUtils.openExpediaWebsite();
					}
				});

		// Section about this app
		addSimpleRow(standardSection, getString(R.string.app_feedback), getString(R.string.app_feedback_description),
				new OnClickListener() {
					public void onClick(View v) {
						mUtils.openAppFeedback();
					}
				});
		addSimpleRow(standardSection, getString(R.string.app_support), getString(R.string.app_support_description),
				new OnClickListener() {
					public void onClick(View v) {
						mUtils.openAppSupport();
					}
				});
		addSimpleRow(standardSection, getString(R.string.TellAFriend), getString(R.string.tell_a_friend_description),
				new OnClickListener() {
					public void onClick(View v) {
						mUtils.tellAFriend();
					}
				});

		// Add rules & restrictions
		final LinkedHashMap<String, String> map = RulesRestrictionsUtils.getInfoData(this);
		for (final String label : map.keySet()) {
			addSimpleRow(standardSection, label, new OnClickListener() {
				public void onClick(View v) {
					SocialUtils.openSite(mContext, map.get(label));
				}
			});
		}

		addMobiataLogo(standardSection);

		// Section about some of our other apps
		ViewGroup otherAppsSection = determineSectionForAboutApps();
		addAppAbout(otherAppsSection, APP_FLIGHTTRACK, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightTrackLink();
			}
		});
		addAppAbout(otherAppsSection, APP_FLIGHTTRACKPRO, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightTrackLink();
			}
		});
		addAppAbout(otherAppsSection, APP_FLIGHTBOARD, 0, new OnClickListener() {
			public void onClick(View v) {
				mUtils.trackFlightBoardLink();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == AboutUtils.REQUEST_CODE_FEEDBACK && resultCode == RESULT_OK) {
			mUtils.trackFeedbackSubmitted();
		}
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
