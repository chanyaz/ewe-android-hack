package com.expedia.account.test.ui;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.account.sample.SignInActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

public class SmartLockHappyPath extends ActivityInstrumentationTestCase2<SignInActivity> {
	public SmartLockHappyPath() {
		super(SignInActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		getActivity();
		super.runTest();
	}

	public void testSignIn() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());

		onView(withText("success@test.com"))
			.perform(click());

		// Wait for login to happen
		Thread.sleep(3000);

		onView(withText("Done"))
			.perform(click());
	}

	public void testSignInFail() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());

		onView(withText("failure@failure.com"))
			.perform(click());
		onView(withText("OK"))
			.perform(click());
	}
}
