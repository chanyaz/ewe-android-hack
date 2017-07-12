package com.expedia.bookings.test.pagemodels.common;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;

import com.expedia.bookings.R;
import com.expedia.bookings.widget.DomainAdapter;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

public class ProfileScreen {
	public static void clickClearPrivateData() {
		onView(withText(R.string.clear_private_data)).perform(scrollTo(), click());
	}

	public static void clickCancel() {
		onView(withText(R.string.cancel)).perform(click());
	}

	public static void clickOK() {
		onView(withText(R.string.ok)).perform(click());
	}

	public static void clickCountry() {
		onView(allOf(withId(R.id.row_title), withText(R.string.preference_point_of_sale_title))).perform(scrollTo(), click());
	}

	public static void clickCountryInList(String country) {
		onData(withCountryName(country)).perform(click());
	}

	private static Matcher<Object> withCountryName(final String countryName) {
		return new BoundedMatcher<Object, DomainAdapter.DomainTuple>(DomainAdapter.DomainTuple.class) {
			@Override
			public void describeTo(Description description) {
				description.appendText("with country name: " + countryName);
			}

			@Override
			protected boolean matchesSafely(DomainAdapter.DomainTuple item) {
				return countryName != null && countryName.equals(item.mName);
			}
		};
	}

	public static ViewInteraction atolInformation() {
		return onView(withText(R.string.lawyer_label_atol_information));
	}

	public static void clickSignInButton() {
		onView(withId(R.id.sign_in_button)).perform(click());
	}

	public static void clickFacebookSignInButton() {
		onView(withId(R.id.sign_in_with_facebook_button)).perform(click());
	}

	public static void clickCreateAccountButton() {
		onView(withId(R.id.create_account_button)).perform(click());
	}
}
