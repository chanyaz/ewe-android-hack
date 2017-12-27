package com.expedia.bookings.test.espresso;

import java.util.Locale;

import android.content.Context;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Common {
	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
		delay(1);
	}

	public static void pressBack() {
		try {
			Espresso.pressBack();
		}
		catch (Exception e) {
			Log.v("Pressed back and got an exception: ", e);
		}
	}

	public static void setPOS(PointOfSaleId pos) {
		Context context = InstrumentationRegistry.getTargetContext();
		SettingUtils.save(context, R.string.PointOfSaleKey, String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(context);
	}

	public static void setFeatureFlag(String key, boolean enable) {
		Context context = InstrumentationRegistry.getTargetContext();
		SettingUtils.save(context, key, enable);
	}

	public static void setLocale(Locale loc) {
		ExpediaBookingApp app = getApplication();
		Configuration conf = app.getResources().getConfiguration();
		app.handleConfigurationChanged(conf, loc);
	}

	public static ExpediaBookingApp getApplication() {
		return Ui.getApplication(InstrumentationRegistry.getTargetContext());
	}

	public static void clickOkString() {
		onView(withText(R.string.ok)).perform(click());
	}

	public static void delay(int seconds) {
		while (seconds-- > 0) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {
				//ignore
			}
		}
	}
}
