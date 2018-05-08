package com.expedia.bookings.test.espresso;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiSelector;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class Common {
	private static Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
	public static UiDevice device = UiDevice.getInstance(instrumentation);
	public static UiAutomation uiAutomation = instrumentation.getUiAutomation();

	public static UiDevice getUiDevice() {
		return device;
	}

	public static void closeSoftKeyboard(ViewInteraction v) {
		v.perform(ViewActions.closeSoftKeyboard());
		delay(1);
	}

	private static boolean isKeyboardDisplayed() {
		PermissionGranter.allowPermission("android.permission.DUMP");

		String checkKeyboardCommand = "dumpsys input_method | grep mInputShown";
		try {
			Process process = Runtime.getRuntime().exec(checkKeyboardCommand);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			process.waitFor();

			return output.toString().contains("mInputShown=true");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void pressBack() {
		try {
			Espresso.pressBack();
		}
		catch (Exception e) {
			Log.v("Pressed back and got an exception: ", e);
		}
	}

	public static void genericPressBack() {
		genericCloseKeyboard();
		device.pressBack();
	}

	public static void genericCloseKeyboard() {
		if (Common.isKeyboardDisplayed()) {
			device.pressBack();
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

	public static void forceStopProcess(String packageName) {
		uiAutomation.executeShellCommand("am force-stop " + packageName);
		Common.delay(1);
	}

	public static void clearProcessData(String packageName) {
		uiAutomation.executeShellCommand("pm clear " + packageName);
		Common.delay(1);
	}

	public static Boolean isPackageInstalled(String packageName) {
		PackageManager packageManager = InstrumentationRegistry.getTargetContext().getPackageManager();

		try {
			packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
		}
		catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}

	public static Boolean waitForOneOfViewsToDisplay(ArrayList listOfMatchers, long time, TimeUnit timeUnit) {
		long timeoutTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, timeUnit);

		while (System.currentTimeMillis() <= timeoutTime) {
			if (isOneOfUiObjectsPresent(listOfMatchers)) {
				return true;
			}

			try {
				//Wait half a second between checks. This will reduce executing system CPU load
				Thread.sleep(500);
			}
			catch (Exception ignored) { }
		}
		return false;
	}

	public static Boolean isOneOfUiObjectsPresent(ArrayList listOfMatchers) {
		for (int i = 0; i < listOfMatchers.size(); i++) {
			try {
				if (listOfMatchers.get(i) instanceof Matcher) {
					onView((Matcher<View>) listOfMatchers.get(i)).check(matches(isDisplayed()));
					return true; //If the check didn't error out, that means the view is displayed.
				}
				else if (listOfMatchers.get(i) instanceof BySelector) {
					if (device.findObject((BySelector) listOfMatchers.get(i)) != null) {
						return true;
					}
				}
				else if (listOfMatchers.get(i) instanceof UiSelector) {
					if (device.findObject((UiSelector) listOfMatchers.get(i)).exists()) {
						return true;
					}
				}
				else {
					throw new IllegalArgumentException("Unknown Object Type!");
				}
			}
			catch (IllegalArgumentException e) {
				throw e;
			}
			catch (Exception ignored) { }
		}
		return false;
	}
}
