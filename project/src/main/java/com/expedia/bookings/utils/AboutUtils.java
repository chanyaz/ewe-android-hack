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

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutWebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.squareup.phrase.Phrase;

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

		builder.setTitle(Phrase.from(mActivity, R.string.contact_via_TEMPLATE).put("brand", BuildConfig.brand).format());

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
				ProductFlavorFeatureConfiguration.getInstance().contactUsViaWeb(mActivity);
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

	public void contactViaPhone() {
		trackCallSupport();
		SocialUtils.call(mActivity, PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
	}

	public void openExpediaWebsite() {
		openWebsite(mActivity, PointOfSale.getPointOfSale().getWebsiteUrl(), true);
	}

	public void openAppSupport() {
		openWebsite(mActivity, ProductFlavorFeatureConfiguration.getInstance().getAppSupportUrl(mActivity), false, true);
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

	public static void openWebsite(Context context, String url, boolean useExternalBrowser) {
		openWebsite(context, url, useExternalBrowser, false);
	}

	public static void openWebsite(Context context, String url, boolean useExternalBrowser, boolean showEmailButton) {
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
		OmnitureTracking.trackSimpleEvent("App.Hotel.Support", null, null);
	}

	public void trackCallSupport() {
		Log.d("Tracking \"call support\" onClick");
		OmnitureTracking.trackSimpleEvent(null, "event35", "App.Info.CallSupport");
	}

	public void trackFlightTrackLink() {
		Log.d("Tracking \"flighttrack\" onClick");
		OmnitureTracking.trackSimpleEvent(null, null, "App.Link.FlightTrack");
	}

	public void trackFlightBoardLink() {
		Log.d("Tracking \"flightboard\" onClick");
		OmnitureTracking.trackSimpleEvent(null, null, "App.Link.FlightBoard");
	}

	public void trackHiringLink() {
		Log.d("Tracking \"hiring\" onClick");
		OmnitureTracking.trackSimpleEvent(null, null, "App.Link.Mobiata.Jobs");
	}

	public void trackFeedbackSubmitted() {
		Log.d("Tracking \"app feedback\" onClick");

		// TODO: referrerId should display the # of stars the user gave us, however we cannot get
		// that information from OpinionLab yet.
		OmnitureTracking.trackSimpleEvent(null, "event37", null);
	}
}
