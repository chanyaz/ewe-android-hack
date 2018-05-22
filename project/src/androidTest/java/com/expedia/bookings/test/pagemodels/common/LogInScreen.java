package com.expedia.bookings.test.pagemodels.common;

import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.Until;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.mobiata.android.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;
import static com.expedia.bookings.test.espresso.Common.device;
import static com.expedia.bookings.test.espresso.Common.waitForOneOfViewsToDisplay;
import static org.hamcrest.Matchers.allOf;

public class LogInScreen {
	final static UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
	// Object access

	public static ViewInteraction emailAddressEditText() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.email_address_sign_in))));
	}

	public static ViewInteraction passwordEditText() {
		return onView(allOf(withId(R.id.input_text), withParent(withId(R.id.password))));
	}

	public static ViewInteraction signInWithExpediaButton() {
		return onView(withId(R.id.account_sign_in));
	}

	public static ViewInteraction signInButton() {
		return onView(withId(R.id.sign_in_button));
	}

	public static ViewInteraction signInWithFacebookButton() {
		return onView(withId(R.id.sign_in_with_facebook_button));
	}
	// Object interaction

	public static void clickOnLoginButton() {
		Common.closeSoftKeyboard(LogInScreen.passwordEditText());
		Common.delay(1);
		signInButton().perform(click());
		Common.delay(2);
	}

	public static void typeTextEmailEditText(String text) {
		emailAddressEditText().perform(typeText(text));
	}

	public static void typeTextPasswordEditText(String text) {
		passwordEditText().perform(typeText(text));
	}

	public static void signIn(String username, String password) {
		typeTextEmailEditText(username);
		typeTextPasswordEditText(password);
		clickOnLoginButton();
	}

	public static class FacebookWebSignIn {
		public static void waitForViewToLoad() {
			mDevice.wait(Until.findObject(By.res("login_form")), 30000);
			mDevice.waitForIdle(3000); //Needed, because the view loads before the progress bar.
		}

		public static void typeInEmail(String text) {
			UiObject2 emailOrPhone = mDevice.findObject(By.clazz(EditText.class).descContains("Email or Phone"));
			UiObject2 mLoginEmail = mDevice.findObject(By.res("m_login_email"));

			if (emailOrPhone != null) {
				emailOrPhone.setText(text);
				Log.d("Used emailOrPhone");
			}
			else if (mLoginEmail != null) {
				mLoginEmail.setText(text);
				Log.d("Used mLoginEmail");
			}
			else {
				onWebView().withElement(findElement(Locator.ID, "m_login_email")).perform(webClick());
				Log.d("Used webView workaround");
			}
		}

		public static void typeInPassword(String text) {
			onWebView().withElement(findElement(Locator.ID, "m_login_password")).perform(webKeys(text));
		}

		public static void clickLogIn() {
			onWebView()
					.withElement(findElement(Locator.ID, "u_0_5"))
					.perform(webClick());
		}
	}

	public static class FacebookWebConfirmLogin {
		public static void waitForViewToLoad() {
			ArrayList<BySelector> views = new ArrayList<>();
			//Facebook has a set of parameters, that keep on rotating at random.
			//This will make sure that at least one of them is a match.
			views.add(By.res("m-future-page-header-title").desc("Confirm Login"));
			views.add(By.res("m-future-page-header-title").text("Confirm Login"));
			views.add(By.clazz("android.widget.Button").descContains("Continue"));
			waitForOneOfViewsToDisplay(views, 30, TimeUnit.SECONDS);

			mDevice.waitForIdle(3000); //Needed, because the view loads before the progress bar.
		}

		public static void clickContinue() {
			ArrayList<BySelector> buttons = new ArrayList<>();
			buttons.add(By.clazz("android.widget.Button").descContains("Continue"));
			buttons.add(By.clazz("android.widget.Button").text("Continue"));
			waitForOneOfViewsToDisplay(buttons, 10, TimeUnit.SECONDS);

			if (device.findObject(buttons.get(0)) != null) {
				device.findObject(buttons.get(0)).clickAndWait(Until.newWindow(), 10000);
			}
			else if (device.findObject(buttons.get(1)) != null) {
				device.findObject(buttons.get(1)).clickAndWait(Until.newWindow(), 10000);
			}
			else {
				throw new Resources.NotFoundException("Neither button was found, using provided params");
			}
		}

		public static void clickCancel() {
			mDevice.findObject(By.clazz(Button.class).desc("Cancel")).click();
		}
	}
}
