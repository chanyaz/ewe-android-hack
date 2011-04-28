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
import com.expedia.bookings.tracking.TrackingUtils;
import com.expedia.bookings.utils.SupportUtils;
import com.mobiata.android.Log;
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
				onTellAFriend();

				SocialUtils.email(mContext, getString(R.string.tell_a_friend_subject),
						getString(R.string.tell_a_friend_body));
			}
		});

		// Section about some of our other apps
		ViewGroup otherAppsSection = addSection(getString(R.string.travel_smart));
		addAppAbout(otherAppsSection, APP_FLIGHTTRACK, 0, new OnClickListener() {
			public void onClick(View v) {
				onFlightTrackLink();
			}
		});
		addAppAbout(otherAppsSection, APP_FLIGHTBOARD, 0, new OnClickListener() {
			public void onClick(View v) {
				onFlightBoardLink();
			}
		});

		// Tracking
		if (savedInstanceState == null) {
			onPageLoad();
		}
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
						onEmailSupport();
						SocialUtils.email(mContext, "support@expedia.com",
								getString(R.string.contact_expedia_email_subject), null);
						break;
					case 1:
						// Website
						SocialUtils.openSite(mContext, SupportUtils.getSupportUrl());
						break;
					case 2:
						// Phone
						onCallSupport();
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

	//////////////////////////////////////////////////////////////////////////////////////////
	// Omniture tracking

	public void onPageLoad() {
		Log.d("Tracking \"App.Hotel.Support\" pageLoad");
		TrackingUtils.trackSimpleEvent(this, "App.Hotel.Support", null, "Shopper", null);
	}

	public void onCallSupport() {
		Log.d("Tracking \"call support\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, "event35", null, "App.Info.CallSupport");
	}

	public void onEmailSupport() {
		Log.d("Tracking \"email support\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, "event36", null, "App.Info.EmailSupport");
	}

	public void onTellAFriend() {
		Log.d("Tracking \"tell a friend\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Info.TellAFriend");
	}

	public void onFlightTrackLink() {
		Log.d("Tracking \"flighttrack\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Link.FlightTrack");
	}

	public void onFlightBoardLink() {
		Log.d("Tracking \"flightboard\" onClick");
		TrackingUtils.trackSimpleEvent(this, null, null, null, "App.Link.FlightBoard");
	}
}
