package com.expedia.account.test.ui;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.account.sample.SignInActivity;
import com.expedia.account.sample.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by doug on 6/18/15.
 */
public class PhoneHappyPath extends ActivityInstrumentationTestCase2<SignInActivity> {
	public PhoneHappyPath() {
		super(SignInActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		getActivity();
		super.runTest();
	}

	// Note to self: to find these ID's easily, use uiautomatorviewer

	// Another note to self: run tests using
	// ./scripts/build_and_run_happy_path.bash

	public void testSignIn() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_sign_in))))
			.perform(typeText("success@test.com"));
		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.password))))
			.perform(typeText("goodpass"));
		onView(withId(R.id.sign_in_button))
			.perform(click());

		// Wait for login to happen
		Thread.sleep(5000);

		onView(allOf(withId(R.id.sign_in_status_title), withText(R.string.Success)))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testSignInFailedBadPassword() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_sign_in))))
			.perform(typeText("badpass@test.com"));
		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.password))))
			.perform(typeText("baddpass"));
		onView(withId(R.id.sign_in_button))
			.perform(click());

		// Wait for dialog box to show up
		Thread.sleep(3000);

		onView(withText(R.string.acct__Sign_in_failed_TITLE))
			.check(matches(isDisplayed()));
		onView(withText("OK"))
			.perform(click());
		onView(withId(R.id.sign_in_button))
			.check(matches(isCompletelyDisplayed()));
	}

	public void testCreateAccount() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.create_account))
			.perform(click());

		// Wait for email lookup transition
		Thread.sleep(1000);

		onView(withId(R.id.action_next))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_create_account))))
			.perform(typeText("newuser@test.com"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_name))))
			.perform(typeText("Test"));

		onView(withId(R.id.action_next))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.last_name))))
			.perform(typeText("Testerson"));

		onView(withId(R.id.action_next))
			.check(matches(isEnabled()));

		onView(withId(R.id.action_next))
			.perform(click());

		Thread.sleep(1000);

		onView(withId(R.id.action_next))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_password))))
			.perform(typeText("3xp3d1acc"));

		onView(withId(R.id.action_next))
			.check(matches(not(isEnabled())));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.second_password))))
			.perform(typeText("3xp3d1acc"));

		onView(withId(R.id.action_next))
			.check(matches(isEnabled()));

		onView(withId(R.id.action_next))
			.perform(click());

		Thread.sleep(1000);

		onView(withId(R.id.terms_of_use_checkbox))
			.check(matches(isNotChecked()));

		onView(withId(R.id.button_create_account))
			.check(matches(not(isEnabled())));

		onView(withId(R.id.terms_of_use_checkbox))
			.perform(click());

		onView(withId(R.id.button_create_account))
			.check(matches(isEnabled()));

		onView(withId(R.id.button_create_account))
			.perform(click());
	}
}
