package com.expedia.bookings.tracking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;

public class OmnitureReceiver extends BroadcastReceiver {
	private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("com.android.vending.INSTALL_REFERRER")) {
			ADMS_Measurement appMeasurement = ADMS_Measurement.sharedInstance(context);

			String marketingDate = FORMATTER.format(new Date());
			String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

			SettingUtils.save(context, context.getString(R.string.preference_amobee_marketing_date), marketingDate);

			appMeasurement.setVisitorID(androidId);
			appMeasurement.setEvar(7, androidId);
			appMeasurement.setEvar(10, marketingDate);
			appMeasurement.setEvar(28, "App Install");

			appMeasurement.track();
		}
	}
}
