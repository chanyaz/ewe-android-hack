package com.expedia.bookings.utils;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutWebViewActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.DomainAdapter;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.SettingUtils;
import com.squareup.phrase.Phrase;

// Methods that tie together TabletAboutActivity and AboutActivity
public class AboutUtils {

	private Activity mActivity;

	public AboutUtils(Activity activity) {
		mActivity = activity;
	}

	//////////////////////////////////////////////////////////////////////////
	// Handling clicks on different items

	public DialogFragment createContactExpediaDialog() {
		return new ContactExpediaDialog();
	}

	public interface CountrySelectDialogListener {
		void showDialogFragment(DialogFragment dialog);

		void onNewCountrySelected(int pointOfSaleId);
	}

	public DialogFragment createCountrySelectDialog() {
		if (!(mActivity instanceof CountrySelectDialogListener)) {
			throw new IllegalStateException("Activity must implement CountrySelectDialogListener");
		}

		return new CountrySelectDialog();
	}

	public void openExpediaWebsite() {
		OmnitureTracking.trackClickSupportWebsite();
		openWebsite(mActivity, PointOfSale.getPointOfSale().getWebsiteUrl(), true);
	}

	public void openAppSupport() {
		OmnitureTracking.trackClickSupportApp();
		openWebsite(mActivity, ProductFlavorFeatureConfiguration.getInstance().getAppSupportUrl(mActivity), false,
			true);
	}

	public void openCareers() {
		openWebsite(mActivity, "http://www.mobiata.com/careers", false);
		OmnitureTracking.trackClickWereHiring();
	}

	public void openTermsAndConditions() {
		OmnitureTracking.trackClickTermsAndConditions();
		PointOfSale posInfo = PointOfSale.getPointOfSale();
		openWebsite(mActivity, posInfo.getTermsAndConditionsUrl(), false);
	}

	public void openPrivacyPolicy() {
		OmnitureTracking.trackClickPrivacyPolicy();
		PointOfSale posInfo = PointOfSale.getPointOfSale();
		openWebsite(mActivity, posInfo.getPrivacyPolicyUrl(), false);
	}

	public void openOpenSourceLicenses() {
		OmnitureTracking.trackClickOpenSourceLicenses();

		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(mActivity);

		String license = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(mActivity);
		String gps = "";
		if (license != null) {
			gps = "<h3>Google Play Services</h3>\n<pre>\n" + HtmlUtils.escape(license) + "</pre>\n";
		}
		String html;
		try {
			html = IoUtils.convertStreamToString(mActivity.getAssets().open("open_source_licenses.html"))
					.replace("{gps}", gps);
		}
		catch (IOException e) {
			html = HtmlUtils.wrapInHeadAndBody(gps);
		}

		builder.setHtmlData(html);

		mActivity.startActivity(builder.getIntent());
	}

	public void rateApp() {
		OmnitureTracking.trackClickRateApp();

		Uri uri = Uri.parse("market://details?id=" + mActivity.getPackageName());
		Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

		// To count with Play market backstack, After pressing back button,
		// to taken back to our application, we need to add following flags to intent.
		//noinspection deprecation
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
			Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
			Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

		try {
			mActivity.startActivity(goToMarket);
		}
		catch (ActivityNotFoundException e) {
			mActivity.startActivity(new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://play.google.com/store/apps/details?id=" + mActivity.getPackageName())));
		}
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


	public static class ContactExpediaDialog extends DialogFragment {
		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new Builder(getActivity(), R.style.LightDialog);

			builder.setTitle(Phrase.from(getActivity(), R.string.contact_via_TEMPLATE)
				.put("brand", BuildConfig.brand).format());

			// Figure out which items to display to the user
			String[] items = new String[2];
			final Runnable[] actions = new Runnable[2];

			// Let's always show the phone option and have the OS take care of how to handle onClick for tablets without telephony.
			// In which case it pops up a dialog to show the number and give 2 options i.e. "Close" & "Add to Contacts"
			items[0] = getActivity().getString(R.string.contact_expedia_phone);
			actions[0] = new Runnable() {
				public void run() {
					contactViaPhone();
				}
			};

			// Always show website option
			items[1] = getActivity().getString(R.string.contact_expedia_website);
			actions[1] = new Runnable() {
				public void run() {
					ProductFlavorFeatureConfiguration.getInstance().contactUsViaWeb(getActivity());
				}
			};

			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					actions[which].run();
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// nothing to do since user has cancelled
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					// nothing to do since user has cancelled
				}
			});

			return builder.create();
		}

		public void contactViaPhone() {
			trackCallSupport();
			SocialUtils
				.call(getActivity(), PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser()));
		}

		public void trackCallSupport() {
			Log.d("Tracking \"call support\" onClick");
			OmnitureTracking.trackSimpleEvent(null, "event35", "App.Info.CallSupport");
		}

	}

	public static class CountrySelectDialog extends DialogFragment {

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			List<PointOfSale> poses = PointOfSale.getAllPointsOfSale(getActivity());
			int len = poses.size();
			CharSequence[] entries = new CharSequence[len];
			CharSequence[] entrySubText = new CharSequence[len];
			final int[] entryValues = new int[len];
			for (int a = 0; a < len; a++) {
				PointOfSale info = poses.get(a);
				entries[a] = getActivity().getString(info.getCountryNameResId());
				entrySubText[a] = info.getUrl();
				entryValues[a] = info.getPointOfSaleId().getId();
			}

			final int startingIndex = getIndexOfValue(entryValues,
				SettingUtils.get(getActivity(), R.string.PointOfSaleKey, -1));
			DomainAdapter domainAdapter = new DomainAdapter(getActivity());
			domainAdapter.setDomains(entries, entrySubText);
			domainAdapter.setSelected(startingIndex);
			builder.setAdapter(domainAdapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, final int newIndex) {
					if (newIndex != startingIndex) {
						((CountrySelectDialogListener) getActivity()).showDialogFragment(new DialogFragment() {
							@NonNull
							@Override
							public Dialog onCreateDialog(Bundle savedInstanceState) {
								Builder builder = new AlertDialog.Builder(getActivity());
								builder.setTitle(R.string.dialog_clear_private_data_title);
								if (User.isLoggedIn(getActivity())) {
									builder.setMessage(R.string.dialog_sign_out_and_clear_private_data_msg);
								}
								else {
									builder.setMessage(R.string.dialog_clear_private_data_msg);
								}
								builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										((CountrySelectDialogListener) getActivity())
											.onNewCountrySelected(entryValues[newIndex]);
									}
								});
								builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// ignore, just let the dialog go away
									}
								});
								return builder.create();
							}
						});
					}
				}
			});
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// user did not change setting, nothing to do
				}
			});
			builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// user did not change setting, nothing to do
				}
			});

			return builder.create();
		}

		private int getIndexOfValue(int[] values, int value) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] == value) {
					return i;
				}
			}

			return -1;
		}
	}
}
