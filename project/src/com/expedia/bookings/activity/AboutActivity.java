package com.expedia.bookings.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.SupportUtils;
import com.mobiata.android.SocialUtils;

public class AboutActivity extends com.mobiata.android.app.AboutActivity {

	private static final int DIALOG_CONTACT_EXPEDIA = 1;

	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;

		// Section about Expedia
		ViewGroup expediaSection = addSection();
		addSimpleRow(expediaSection, getString(R.string.contact_expedia), new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_CONTACT_EXPEDIA);
			}
		});
		addSimpleRow(expediaSection, getString(R.string.expedia_website), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.openSite(mContext, SupportUtils.getWebsiteUrl());
			}
		});

		// Section about this app
		ViewGroup appSection = addSection();
		addSimpleRow(appSection, getString(R.string.app_feedback), new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		addSimpleRow(appSection, getString(R.string.app_support), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.openSite(mContext, SupportUtils.getSupportUrl());
			}
		});
		addSimpleRow(appSection, getString(R.string.TellAFriend), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.email(mContext, getString(R.string.tell_a_friend_subject),
						getString(R.string.tell_a_friend_body));
			}
		});

		// Section about some of our other apps
		ViewGroup otherAppsSection = addSection(getString(R.string.travel_smart));
		addAppAbout(otherAppsSection, APP_FLIGHTTRACK, 0);
		addAppAbout(otherAppsSection, APP_FLIGHTBOARD, 0);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_CONTACT_EXPEDIA: {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.contact_expedia_via);
			builder.setItems(R.array.contact_expedia_items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CONTACT_EXPEDIA);

					switch (which) {
					case 0:
						// Email
						SocialUtils.email(mContext, "support@expedia.com",
								getString(R.string.contact_expedia_email_subject), null);
						break;
					case 1:
						// Website
						SocialUtils.openSite(mContext, SupportUtils.getSupportUrl());
						break;
					case 2:
						// Phone
						SocialUtils.call(mContext, SupportUtils.getSupportNumber());
						break;
					}
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_CONTACT_EXPEDIA);
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_CONTACT_EXPEDIA);
				}
			});
			return builder.create();
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
