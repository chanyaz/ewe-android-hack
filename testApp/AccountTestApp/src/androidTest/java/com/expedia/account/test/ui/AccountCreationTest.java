package com.expedia.account.test.ui;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.Toolbar;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.expedia.account.sample.R;
import com.expedia.account.sample.MockAccountLibActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class AccountCreationTest extends ActivityInstrumentationTestCase2<MockAccountLibActivity> {
	public AccountCreationTest() {
		super(MockAccountLibActivity.class);
	}

	@Override
	public void runTest() throws Throwable {
		getActivity();
		super.runTest();
	}

	public void testAccountCreationFormFields() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();
		onView(withId(R.id.toolbar)).check(matches(withNavigationContentDescription("Close")));

		onView(withId(R.id.create_account))
			.perform(click());

		// Wait for email/name transition
		Thread.sleep(1000);

		onView(withId(R.id.logo_text))
			.check(matches(withContentDescription("Expialidocious")));

		onView(withId(R.id.email_address_create_account))
			.check(matches(isDisplayed()));

		onView(withId(R.id.first_name))
			.check(matches(isDisplayed()));

		onView(withId(R.id.last_name))
			.check(matches(isDisplayed()));
	}


	public void testEnterValidEmail() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.create_account))
			.perform(click());

		// Wait for email/name transition
		Thread.sleep(1000);

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_create_account))))
			.perform(typeText("newuser@test.com"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_name))))
			.perform(typeText("Test"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.last_name))))
			.perform(typeText("Testerson"));

		onView(withId(R.id.action_next))
			.perform(click());

		onView(withId(R.id.user_image))
			.check(matches(withContentDescription("TT")));

		onView(withId(R.id.user_greeting))
			.check(matches(withContentDescription("Hello Test")));

		onView(withId(R.id.user_email))
			.check(matches(withContentDescription("newuser@test.com")));

		onView(withId(R.id.first_password))
			.check(matches(isDisplayed()));

		onView(withId(R.id.second_password))
			.check(matches(isDisplayed()));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_password)))).check(matches(withContentDescription("Create Password")));
		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.second_password)))).check(matches(withContentDescription("Confirm Password")));
		onView(withId(R.id.toolbar)).check(matches(withNavigationContentDescription("Back")));

		onView(withId(R.id.action_next)).perform(click());

		onView(withId(R.id.user_image))
			.check(matches(withContentDescription("TT")));

		onView(withId(R.id.user_greeting))
			.check(matches(withContentDescription("Hello Test")));

		onView(withId(R.id.user_email))
			.check(matches(withContentDescription("newuser@test.com")));

		onView(withId(R.id.terms_of_use_layout)).check(matches(withContentDescription(
			"I have read and agree to the Terms of Use and the Privacy Policy. Checkbox. Unchecked.")));
		onView(withId(R.id.agree_to_spam_layout)).check(matches(withContentDescription(
			"Send me email marketing and deals. Checkbox. Checked.")));

	}

	public void testCheckboxStatus() throws InterruptedException {
		onView(withText("Mock Mode"))
			.perform(click());
		pressBack();

		onView(withId(R.id.create_account))
			.perform(click());

		// Wait for email/name transition
		Thread.sleep(1000);

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.email_address_create_account))))
			.perform(typeText("newuser@test.com"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_name))))
			.perform(typeText("Test"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.last_name))))
			.perform(typeText("Testerson"));

		onView(withId(R.id.action_next))
			.perform(click());

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.first_password))))
			.perform(typeText("123456"));

		onView(allOf(withId(R.id.input_text), isDescendantOfA(withId(R.id.second_password))))
			.perform(typeText("123456"));

		onView(withId(R.id.action_next)).perform(click());


		onView(withId(R.id.terms_of_use_checkbox)).check(matches(isNotChecked()));

		onView(withId(R.id.terms_of_use_layout)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.terms_of_use_checkbox)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.terms_of_use_text)).check(matches(ViewMatchers.isEnabled()));


		onView(withId(R.id.enroll_in_loyalty_checkbox)).check(matches(isNotChecked()));

		onView(withId(R.id.enroll_in_loyalty_layout)).check(matches(not(ViewMatchers.isEnabled())));

		onView(withId(R.id.enroll_in_loyalty_checkbox)).check(matches(not(ViewMatchers.isEnabled())));

		onView(withId(R.id.enroll_in_loyalty_text)).check(matches(not(ViewMatchers.isEnabled())));


		onView(withId(R.id.agree_to_spam_checkbox)).check(matches(isChecked()));

		onView(withId(R.id.agree_to_spam_layout)).check(matches(not(ViewMatchers.isEnabled())));

		onView(withId(R.id.agree_to_spam_checkbox)).check(matches(not(ViewMatchers.isEnabled())));

		onView(withId(R.id.agree_to_spam_text)).check(matches(not(ViewMatchers.isEnabled())));

		onView(withId(R.id.button_create_account)).check(matches(not(ViewMatchers.isEnabled())));


		onView(withId(R.id.terms_of_use_checkbox)).perform(click());


		onView(withId(R.id.enroll_in_loyalty_checkbox)).check(matches(isNotChecked()));

		onView(withId(R.id.enroll_in_loyalty_layout)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.enroll_in_loyalty_checkbox)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.enroll_in_loyalty_text)).check(matches(ViewMatchers.isEnabled()));


		onView(withId(R.id.agree_to_spam_checkbox)).check(matches(isChecked()));

		onView(withId(R.id.agree_to_spam_layout)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.agree_to_spam_checkbox)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.agree_to_spam_text)).check(matches(ViewMatchers.isEnabled()));

		onView(withId(R.id.button_create_account)).check(matches(ViewMatchers.isEnabled()));


	}

	public static Matcher<View> withNavigationContentDescription(final String contentDescription) {
		return new BoundedMatcher<View, Toolbar>(Toolbar.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("Toolbar navigation has this content description -> " + contentDescription);
			}

			@Override
			public boolean matchesSafely(Toolbar toolbar) {
				if (toolbar.getNavigationContentDescription().toString().equals(contentDescription)) {
					return true;
				}
				return false;
			}
		};
	}
}
