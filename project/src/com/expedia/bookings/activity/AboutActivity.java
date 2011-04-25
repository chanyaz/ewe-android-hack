package com.expedia.bookings.activity;

import java.util.HashMap;
import java.util.Locale;

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
import com.mobiata.android.SocialUtils;

public class AboutActivity extends com.mobiata.android.app.AboutActivity {

	private static final int DIALOG_CONTACT_EXPEDIA = 1;

	// Default is GB's number
	private static final String DEFAULT_SUPPORT_NUMBER = "0330-123-1235";

	// Which support number to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> SUPPORT_NUMBERS = new HashMap<String, String>() {
		{
			put("US", "1-877-829-0215");
			put("CA", "1-888-EXPEDIA");
			put("MX", "001-8003157301");
			put("AU", "13-38-10");
			put("NZ", "0800-998-799");
			put("JP", "0120-142-650");
			put("IN", "1800-419-1919");
			put("SG", "800-120-5806");
			put("MY", "1-800-815676");
			put("TH", "001-800-12-0667078");
			put("IT", "+39-02-91483685");
			put("DE", "01805-007146");
			put("NL", "0900-397-3342");
			put("ES", "901-01-01-14");
			put("AT", "0820-600630");
			put("GB", "0330-123-1235");
			put("FR", "0892-301-300");
			put("SE", "0200-810-341");
			put("DK", "80200088");
			put("NO", "800-36-401");
		}
	};

	// Default is the US website
	private static final String DEFAULT_WEBSITE = "http://www.expedia.com/?rfrr=app.android";

	// Which website to use for which country code (based on locale)
	@SuppressWarnings("serial")
	private static final HashMap<String, String> WEBSITE_URLS = new HashMap<String, String>() {
		{
			put("AU", "http://www.expedia.com.au/?rfrr=app.android");
			put("CA", "http://www.expedia.ca/?rfrr=app.android");
			put("GB", "http://www.expedia.co.uk/?rfrr=app.android");
			put("NZ", "http://www.expedia.co.nz/?rfrr=app.android");
			put("US", "http://www.expedia.com/?rfrr=app.android");
		}
	};

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
				SocialUtils.openSite(mContext, getWebsiteUrl());
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
				SocialUtils.openSite(mContext, getSupportUrl());
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
						SocialUtils.openSite(mContext, getSupportUrl());
						break;
					case 2:
						// Phone
						SocialUtils.call(mContext, getSupportNumber());
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

	public String getSupportUrl() {
		return "http://m.expedia.com/mt/support.expedia.com/app/home/p/532/?rfrr=app.android";
	}

	public String getSupportNumber() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		if (SUPPORT_NUMBERS.containsKey(countryCode)) {
			return SUPPORT_NUMBERS.get(countryCode);
		}
		else {
			return DEFAULT_SUPPORT_NUMBER;
		}
	}

	public String getWebsiteUrl() {
		String countryCode = Locale.getDefault().getCountry().toUpperCase();
		if (WEBSITE_URLS.containsKey(countryCode)) {
			return WEBSITE_URLS.get(countryCode);
		}
		else {
			return DEFAULT_WEBSITE;
		}
	}
}
