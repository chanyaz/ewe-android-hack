package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AppFeedbackActivity;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.opinionlab.oo.sdk.android.CommentCardActivity;

// Methods that tie together TabletAboutActivity and AboutActivity
public class AboutUtils {

	public static final int REQUEST_CODE_FEEDBACK = 1;

	private Activity mActivity;

	public AboutUtils(Activity activity) {
		mActivity = activity;
	}

	//////////////////////////////////////////////////////////////////////////
	// Handling clicks on different items

	public Dialog createContactExpediaDialog(final Runnable onDismiss) {
		AlertDialog.Builder builder;
		if (AndroidUtils.getSdkVersion() < 11) {
			builder = new Builder(mActivity);
		}
		else {
			builder = new Builder(mActivity, R.style.LightDialog);
		}

		builder.setTitle(R.string.contact_expedia_via);

		// Figure out which items to display to the user
		List<String> items = new ArrayList<String>();
		final List<Runnable> actions = new ArrayList<Runnable>();

		// Only add phone option if device can make calls
		if (AndroidUtils.hasTelephonyFeature(mActivity)) {
			items.add(mActivity.getString(R.string.contact_expedia_phone));
			actions.add(new Runnable() {
				public void run() {
					contactViaPhone();
				}
			});
		}

		// Always show website option
		items.add(mActivity.getString(R.string.contact_expedia_website));
		actions.add(new Runnable() {
			public void run() {
				contactViaWeb();
			}
		});

		// Always show email option
		items.add(mActivity.getString(R.string.contact_expedia_email));
		actions.add(new Runnable() {
			public void run() {
				contactViaEmail();
			}
		});

		builder.setItems(items.toArray(new String[0]), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (onDismiss != null) {
					onDismiss.run();
				}

				actions.get(which).run();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (onDismiss != null) {
					onDismiss.run();
				}
			}
		});
		if (onDismiss != null) {
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					if (onDismiss != null) {
						onDismiss.run();
					}
				}
			});
		}

		return builder.create();
	}

	public Dialog createExpediaWebsiteDialog(final Runnable onDismiss) {
		AlertDialog.Builder builder;
		if (AndroidUtils.getSdkVersion() < 11) {
			builder = new AlertDialog.Builder(mActivity);
		}
		else {
			builder = new AlertDialog.Builder(mActivity, R.style.LightDialog);
		}
		builder.setMessage(R.string.dialog_message_launch_expedia_mobile_site);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (onDismiss != null) {
					onDismiss.run();
				}
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				openExpediaWebsite();

				if (onDismiss != null) {
					onDismiss.run();
				}
			}
		});
		if (onDismiss != null) {
			builder.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					onDismiss.run();
				}
			});
		}

		return builder.create();
	}

	public void contactViaPhone() {
		trackCallSupport();
		SocialUtils.call(mActivity, SupportUtils.getInfoSupportNumber(mActivity));
	}

	public void contactViaWeb() {
		SocialUtils.openSite(mActivity, SupportUtils.getContactExpediaUrl(mActivity));
	}

	public void contactViaEmail() {
		trackEmailSupport();
		SocialUtils.email(mActivity, "support@expedia.com",
				mActivity.getString(R.string.contact_expedia_email_subject), null);
	}

	public void openExpediaWebsite() {
		SocialUtils.openSite(mActivity, SupportUtils.getWebsiteUrl());
	}

	public void openAppFeedback() {
		Intent intent = new Intent(mActivity, AppFeedbackActivity.class);
		intent.putExtra(CommentCardActivity.EXTRA_REFERRAL_URL, "http://expediahotelandroid.expedia.com/"
				+ AndroidUtils.getAppVersion(mActivity));
		mActivity.startActivityForResult(intent, REQUEST_CODE_FEEDBACK);

		trackFeedbackPageLoad();
	}

	public void openAppSupport() {
		SocialUtils.openSite(mActivity, SupportUtils.getAppSupportUrl(mActivity));
	}

	public void tellAFriend() {
		trackTellAFriend();

		SocialUtils.email(mActivity, mActivity.getString(R.string.tell_a_friend_subject),
				mActivity.getString(R.string.tell_a_friend_body));
	}

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking events

	public void trackAboutActivityPageLoad() {
		Log.d("Tracking \"App.Hotel.Support\" pageLoad");
		TrackingUtils.trackSimpleEvent(mActivity, "App.Hotel.Support", null, "Shopper", null);
	}

	public void trackCallSupport() {
		Log.d("Tracking \"call support\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, "event35", null, "App.Info.CallSupport");
	}

	public void trackEmailSupport() {
		Log.d("Tracking \"email support\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, "event36", null, "App.Info.EmailSupport");
	}

	public void trackTellAFriend() {
		Log.d("Tracking \"tell a friend\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, null, null, "App.Info.TellAFriend");
	}

	public void trackFlightTrackFreeLink() {
		Log.d("Tracking \"flighttrackfree\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, null, null, "App.Link.FlightTrackFree");
	}

	public void trackFlightTrackLink() {
		Log.d("Tracking \"flighttrack\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, null, null, "App.Link.FlightTrack");
	}

	public void trackFlightBoardLink() {
		Log.d("Tracking \"flightboard\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, null, null, "App.Link.FlightBoard");
	}

	public void trackHiringLink() {
		Log.d("Tracking \"hiring\" onClick");
		TrackingUtils.trackSimpleEvent(mActivity, null, null, null, "App.Link.Mobiata.Jobs");
	}

	public void trackFeedbackPageLoad() {
		Log.d("Tracking \"App.Feedback\" pageLoad");
		TrackingUtils.trackSimpleEvent(mActivity, "App.Feedback", null, null, null);
	}

	public void trackFeedbackSubmitted() {
		Log.d("Tracking \"app feedback\" onClick");

		// TODO: referrerId should display the # of stars the user gave us, however we cannot get
		// that information from OpinionLab yet.
		TrackingUtils.trackSimpleEvent(mActivity, null, "event37", null, null);
	}
}
