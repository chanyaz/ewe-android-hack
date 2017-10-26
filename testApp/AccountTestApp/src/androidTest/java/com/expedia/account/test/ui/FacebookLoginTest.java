package com.expedia.account.test.ui;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.account.sample.R;
import com.expedia.account.sample.SignInActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by doug on 6/18/15.
 */
public class FacebookLoginTest extends ActivityInstrumentationTestCase2<SignInActivity> {
	public FacebookLoginTest() {
		super(SignInActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		getActivity();
		super.runTest();
	}

	public void testFacebookCancel() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("cancel"))
			.perform(click());

		Thread.sleep(5000);

		onView(withId(R.id.sign_in_button))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookDeniedEmailPermission() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("Denied email permission"))
			.perform(click());

		Thread.sleep(5000);

		onView(withText(R.string.acct__fb_user_denied_email_heading))
			.check(matches(isDisplayed()));
		onView(withText("OK"))
			.perform(click());
		onView(withId(R.id.sign_in_button))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookNotLinked_Back() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("notLinked"))
			.perform(click());

		Thread.sleep(5000);

		onView(withText(R.string.acct__fb_notLinked_title))
			.check(matches(isDisplayed()));
		pressBack();

		Thread.sleep(5000);

		onView(withId(R.id.sign_in_button))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookNotLinked_New() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("notLinked"))
			.perform(click());

		Thread.sleep(5000);

		onView(withText(R.string.acct__fb_notLinked_title))
			.check(matches(isDisplayed()));
		onView(withText(R.string.acct__fb_notLinked_new_button))
			.perform(click());

		Thread.sleep(8000);

		onView(allOf(withId(R.id.sign_in_status_title), withText(R.string.Success)))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookNotLinked_Existing_Success() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("notLinked"))
			.perform(click());

		Thread.sleep(5000);

		onView(withText(R.string.acct__fb_notLinked_title))
			.check(matches(isDisplayed()));
		onView(withText(R.string.acct__fb_notLinked_existing_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withId(R.id.action_link))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_facebook))))
			.perform(typeText("success@test.com"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.password_facebook))))
			.perform(typeText("abcdefg"));

		onView(withId(R.id.action_link))
			.check(matches(isEnabled()));

		onView(withId(R.id.action_link))
			.perform(click());

		Thread.sleep(8000);

		onView(allOf(withId(R.id.sign_in_status_title), withText(R.string.Success)))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookExisting() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("existing"))
			.perform(click());

		Thread.sleep(5000);

		onView(withId(R.id.action_link))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.password_facebook))))
			.perform(typeText("abcdefg"));

		onView(withId(R.id.action_link))
			.check(matches(isEnabled()));

		onView(withId(R.id.action_link))
			.perform(click());

		Thread.sleep(8000);

		onView(allOf(withId(R.id.sign_in_status_title), withText(R.string.Success)))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookSuccess() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("success"))
			.perform(click());

		Thread.sleep(5000);

		onView(allOf(withId(R.id.sign_in_status_title), withText(R.string.Success)))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testFacebookGenericError() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.sign_in_with_facebook_button))
			.perform(click());

		Thread.sleep(5000);

		onView(withText("generic Facebook error"))
			.perform(click());

		Thread.sleep(5000);

		onView(withText(R.string.acct__fb_unable_to_sign_into_facebook))
			.check(matches(isDisplayed()));
		onView(withText("OK"))
			.perform(click());
		onView(withId(R.id.sign_in_button))
			.check(matches(isCompletelyDisplayed()));
	}
}
