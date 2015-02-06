package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutWebViewActivity;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;

// Methods that tie together TabletAboutActivity and AboutActivity
public class AboutUtils {

	public static final int REQUEST_CODE_FEEDBACK = 1;

	private Activity mActivity;

	public AboutUtils(Activity activity) {
		mActivity = activity;
	}

	//////////////////////////////////////////////////////////////////////////
	// Handling clicks on different items

	@SuppressLint("NewApi")
	public Dialog createContactExpediaDialog(final Runnable onDismiss) {
		AlertDialog.Builder builder = new Builder(mActivity, R.style.LightDialog);

		builder.setTitle(Ui.obtainThemeResID(mActivity, R.attr.skin_infoContactUsString));

		// Figure out which items to display to the user
		List<String> items = new ArrayList<String>();
		final List<Runnable> actions = new ArrayList<Runnable>();

		// Let's always show the phone option and have the OS take care of how to handle onClick for tablets without telephony.
		// In which case it pops up a dialog to show the number and give 2 options i.e. "Close" & "Add to Contacts"
		items.add(mActivity.getString(R.string.contact_expedia_phone));
		actions.add(new Runnable() {
			public void run() {
				contactViaPhone();
			}
		});

		// Always show website option
		items.add(mActivity.getString(R.string.contact_expedia_website));
		actions.add(new Runnable() {
			public void run() {
				contactViaWeb();
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
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

	@SuppressLint("NewApi")
	public Dialog createExpediaWebsiteDialog(final Runnable onDismiss) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.LightDialog);

		builder.setMessage(Ui.obtainThemeResID(mActivity, R.attr.skin_dialogMessageLaunchMobileSite));
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (onDismiss != null) {
					onDismiss.run();
				}
			}
		});
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
		SocialUtils.call(mActivity, PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
	}

	public void contactViaWeb() {
		openWebsite(mActivity, PointOfSale.getPointOfSale().getAppSupportUrl(), true);
	}

	public void contactViaEmail() {
		trackEmailSupport();
		SocialUtils.email(mActivity, PointOfSale.getPointOfSale().getSupportEmail(),
			mActivity.getString(Ui.obtainThemeResID(mActivity, R.attr.skin_infoContactEmailSubjectString)), null);
	}

	public void openExpediaWebsite() {
		openWebsite(mActivity, PointOfSale.getPointOfSale().getWebsiteUrl(), true);
	}

	public void openContactUsVSC() {
		openWebsite(mActivity, "http://voyages-sncf.mobi/aide-appli-2/aide-appli-hotel/pagecontactandroid.html", false, false);
	}

	public void openAppSupport() {
		if (ExpediaBookingApp.IS_VSC) {
			openWebsite(mActivity, mActivity.getString(R.string.app_support_url_vsc), false, true);
		}
		else if (ExpediaBookingApp.IS_TRAVELOCITY) {
			openWebsite(mActivity, mActivity.getString(R.string.app_support_url_tvly), false, true);
		}
		else if (ExpediaBookingApp.IS_AAG) {
			openWebsite(mActivity, mActivity.getString(R.string.app_support_url_aag), false, true);
		}
		else if (ExpediaBookingApp.IS_EXPEDIA) {
			openWebsite(mActivity, mActivity.getString(R.string.app_support_url), false, true);
		}
		else {
			throw new RuntimeException("Did not handle app support url for current build flavor");
		}
	}

	public void tellAFriend() {
		trackTellAFriend();

		SocialUtils.email(mActivity, mActivity.getString(Ui.obtainThemeResID(mActivity, R.attr.skin_tellAFriendSubject)),
			mActivity.getString(Ui.obtainThemeResID(mActivity, R.attr.skin_tellAFriendBody)));
	}

	public void openCareers() {
		openWebsite(mActivity, "http://www.mobiata.com/careers", false);
		trackHiringLink();
	}

	public void openTermsAndConditions() {
		PointOfSale posInfo = PointOfSale.getPointOfSale();
		openWebsite(mActivity, posInfo.getTermsAndConditionsUrl(), false);
	}

	public void openPrivacyPolicy() {
		PointOfSale posInfo = PointOfSale.getPointOfSale();
		openWebsite(mActivity, posInfo.getPrivacyPolicyUrl(), false);
	}

	private void openWebsite(Context context, String url, boolean useExternalBrowser) {
		openWebsite(context, url, useExternalBrowser, false);
	}

	private void openWebsite(Context context, String url, boolean useExternalBrowser, boolean showEmailButton) {
		if (useExternalBrowser) {
			SocialUtils.openSite(context, url);
		}
		else {
			AboutWebViewActivity.IntentBuilder builder = new AboutWebViewActivity.IntentBuilder(context);
			builder.setUrl(url);
			builder.setShowEmailButton(showEmailButton);
			context.startActivity(builder.getIntent());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Omniture tracking events

	public void trackAboutActivityPageLoad() {
		Log.d("Tracking \"App.Hotel.Support\" pageLoad");
		OmnitureTracking.trackSimpleEvent(mActivity, "App.Hotel.Support", null, null);
	}

	public void trackCallSupport() {
		Log.d("Tracking \"call support\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, "event35", "App.Info.CallSupport");
	}

	public void trackEmailSupport() {
		Log.d("Tracking \"email support\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, "event36", "App.Info.EmailSupport");
	}

	public void trackTellAFriend() {
		Log.d("Tracking \"tell a friend\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, null, "App.Info.TellAFriend");
	}

	public void trackFlightTrackFreeLink() {
		Log.d("Tracking \"flighttrackfree\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, null, "App.Link.FlightTrackFree");
	}

	public void trackFlightTrackLink() {
		Log.d("Tracking \"flighttrack\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, null, "App.Link.FlightTrack");
	}

	public void trackFlightBoardLink() {
		Log.d("Tracking \"flightboard\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, null, "App.Link.FlightBoard");
	}

	public void trackHiringLink() {
		Log.d("Tracking \"hiring\" onClick");
		OmnitureTracking.trackSimpleEvent(mActivity, null, null, "App.Link.Mobiata.Jobs");
	}

	public void trackFeedbackPageLoad() {
		Log.d("Tracking \"App.Feedback\" pageLoad");
		OmnitureTracking.trackSimpleEvent(mActivity, "App.Feedback", null, null);
	}

	public void trackFeedbackSubmitted() {
		Log.d("Tracking \"app feedback\" onClick");

		// TODO: referrerId should display the # of stars the user gave us, however we cannot get
		// that information from OpinionLab yet.
		OmnitureTracking.trackSimpleEvent(mActivity, null, "event37", null);
	}
}
