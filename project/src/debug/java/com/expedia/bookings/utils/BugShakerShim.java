package com.expedia.bookings.utils;

import java.util.HashSet;
import java.util.Set;

import android.app.Application;
import android.content.Context;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.github.stkent.bugshaker.BugShaker;
import com.mobiata.android.util.SettingUtils;

public class BugShakerShim {

	public static boolean isBugShakerEnabled(Context context) {
		return SettingUtils.get(context, context.getString(R.string.preference_enable_bugshaker), false);
	}

	public static void startNewBugShaker(Application application) {
		Set<String> emailList = new HashSet<>();
		emailList.add("EBAndroidDogfood@expedia.com");
		BugShaker.get(application)
			.setEmailAddressesAndSubjectLine(application.getString(R.string.bugshaker_report_headline), emailList)
			.setAlertDialogType()
			.setLoggingEnabled(BuildConfig.DEBUG)
			.assemble()
			.start();
	}

	public static void turnOff() {
		BugShaker.turnOff();
	}
}
