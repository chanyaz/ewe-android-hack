package com.expedia.bookings.activity;

import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.mobiata.android.SocialUtils;

public class AboutActivity extends com.mobiata.android.app.AboutActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Context context = this;

		// Section about Expedia
		ViewGroup expediaSection = addSection();
		addSimpleRow(expediaSection, getString(R.string.contact_expedia), new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
			}
		});
		addSimpleRow(expediaSection, getString(R.string.expedia_website), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.openSite(context, getWebsiteUrl());
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
				SocialUtils.openSite(context, getSupportUrl());
			}
		});
		addSimpleRow(appSection, getString(R.string.TellAFriend), new OnClickListener() {
			public void onClick(View v) {
				SocialUtils.email(context, getString(R.string.tell_a_friend_subject),
						getString(R.string.tell_a_friend_body));
			}
		});

		// Section about some of our other apps
		ViewGroup otherAppsSection = addSection(getString(R.string.travel_smart));
		addAppAbout(otherAppsSection, APP_FLIGHTTRACK, 0);
		addAppAbout(otherAppsSection, APP_FLIGHTBOARD, 0);
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

	public String getWebsiteUrl() {
		Locale locale = Locale.getDefault();
		String country = locale.getCountry();
		if (country.equals("CA")) {
			return "http://www.expedia.ca/?rfrr=app.android";
		}
		else if (country.equals("GB")) {
			return "http://www.expedia.co.uk/?rfrr=app.android";
		}
		else if (country.equals("AU")) {
			return "http://www.expedia.com.au/?rfrr=app.android";
		}
		else if (country.equals("NZ")) {
			return "http://www.expedia.co.nz/?rfrr=app.android";
		}
		else {
			// Default to US site
			return "http://www.expedia.com/?rfrr=app.android";
		}
	}
}
