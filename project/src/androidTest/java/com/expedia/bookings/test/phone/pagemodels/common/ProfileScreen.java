package com.expedia.bookings.test.phone.pagemodels.common;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import android.content.Context;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;

import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.widget.DomainAdapter;
import com.squareup.phrase.Phrase;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;

public class ProfileScreen {
	public static void clickBookingSupport() {
		onView(withText(R.string.booking_support)).perform(click());
	}

	public static void clickContactPhone() {
		onView(withText(R.string.contact_expedia_phone)).perform(click());
	}

	public static void clickExpediaWebsite(Context context) {
		onView(withText(Phrase.from(context, R.string.website_TEMPLATE).put("brand",
				ProductFlavorFeatureConfiguration.getInstance().getPOSSpecificBrandName(context)).format()
				.toString())).perform(click());
	}

	public static void clickRateApp() {
		onView(withText(R.string.rate_our_app)).perform(scrollTo(), click());
	}

	public static void clickAppSupport() {
		onView(withText(R.string.app_support)).perform(scrollTo(), click());
	}

	public static void clickWereHiring() {
		onView(withText(R.string.WereHiring)).perform(scrollTo(), click());
	}

	public static void clickTerms() {
		onView(withText(R.string.info_label_terms_conditions)).perform(scrollTo(), click());
	}

	public static void clickPrivacyPolicy() {
		onView(withText(R.string.info_label_privacy_policy)).perform(scrollTo(), click());
	}

	public static void clickOpenSource() {
		onView(withText(R.string.open_source_software_licenses)).perform(scrollTo(), click());
	}

	public static void clickMobiataLogo() {
		onView(withId(R.id.logo)).perform(scrollTo(), click());
	}

	public static void clickClearPrivateData() {
		onView(withText(R.string.clear_private_data)).perform(scrollTo(), click());
	}

	public static void clickCancel() {
		onView(withText(R.string.cancel)).perform(click());
	}

	public static void clickOK() {
		onView(withText(R.string.ok)).perform(click());
	}

	public static void scrollToCopyright() {
		onView(withId(R.id.open_source_credits_textview)).perform(scrollTo());
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

	public static ViewInteraction tierBadge() {
		return onView(withId(R.id.toolbar_loyalty_tier_text));
	}

	public static void clickSignInButton() {
		onView(withId(R.id.sign_in_button)).perform(click());
	}

	public static void clickSignOutButton() {
		onView(withId(R.id.sign_out_button)).perform(click());
	}

	public static void clickFacebookSignInButton() {
		onView(withId(R.id.sign_in_with_facebook_button)).perform(click());
	}

	public static void clickCreateAccountButton() {
		onView(withId(R.id.create_account_button)).perform(click());
	}

	public static void waitForAccountViewDisplay() {
		onView(withId(R.id.account_view)).perform(waitForViewToDisplay());
	}

	public static void waitForAccountPagerDisplay() {
		onView(withId(R.id.scroll_container)).perform(waitForViewToDisplay());
	}
}
