package com.expedia.bookings.tracking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;
import com.omniture.AppMeasurement;

public class OmnitureReceiver extends BroadcastReceiver {
	private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			AppMeasurement appMeasurement = new AppMeasurement((Application) context.getApplicationContext());

			String marketingDate = FORMATTER.format(new Date());
			String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

			SettingUtils.save(context, context.getString(R.string.preference_amobee_marketing_date), marketingDate);

			appMeasurement.visitorID = androidId;
			appMeasurement.eVar7 = androidId;
			appMeasurement.eVar10 = marketingDate;
			appMeasurement.eVar28 = "App Install";

			appMeasurement.track();
		}
	}
}