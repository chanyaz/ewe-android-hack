package com.expedia.bookings.test.pagemodels.lx;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.IdlingResources;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static com.expedia.bookings.test.espresso.ViewActions.waitForViewToDisplay;
import static org.hamcrest.Matchers.allOf;

public class LXScreen {
	public static ViewInteraction calendar() {
		return onView(withId(R.id.calendar));
	}

	public static ViewInteraction locationCardView() {
		return onView(withId(R.id.destination_card));
	}
	public static ViewInteraction location() {
		return onView(withId(R.id.search_src_text)).perform(waitForViewToDisplay());
	}

	public static void didNotGoToResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.scrollView);
	}

	public static void didOpenResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.lx_search_results_list);
	}

	public static void selectLocation(String location) throws Throwable {
		Common.delay(1);
		SearchScreen.selectLocation(location);
	}

	public static ViewInteraction selectDateButton() {
		return SearchScreen.selectDateButton();
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		SearchScreen.selectDates(start, end);
	}

	public static ViewInteraction itinNumberOnConfirmationScreen() {
		return onView(withId(R.id.itin_number));
	}

	public static ViewInteraction searchFailed() {
		return onView(withId(R.id.lx_search_error_widget));
	}

	public static ViewInteraction searchList() {
		return onView(recyclerView(R.id.lx_search_results_list));
	}

	public static void waitForSearchListDisplayed() {
		searchList().perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
		// Wait an extra bit just to be sure the list items have settled
		Common.delay(2);
	}

	public static Matcher<View> recyclerView(int viewId) {
		return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
	}

	public static ViewInteraction toolbar() {
		return onView(withId(R.id.toolbar));
	}

	public static ViewInteraction searchButton() {
		return SearchScreen.searchButton();
	}

	public static ViewInteraction searchButtonOnDetailsToolbar() {
		return onView(allOf(isDescendantOfA(withId(R.id.activity_details_presenter)), withId(R.id.menu_open_search)));
	}

	//Checkout
	public static ViewInteraction checkoutWidget() {
		return onView(withId(R.id.checkout));
	}

	public static ViewInteraction checkoutOfferTitle() {
		return onView(withId(R.id.lx_offer_title_text));
	}

	public static ViewInteraction checkoutActivityTitle() {
		return onView(withId(R.id.lx_activity_title_text));
	}

	public static ViewInteraction checkoutGroupText() {
		return onView(withId(R.id.lx_group_text));
	}

	public static ViewInteraction checkoutOfferLocation() {
		return onView(withId(R.id.lx_offer_location));
	}

	public static ViewInteraction checkoutGrandTotalText() {
		return onView(withId(R.id.grand_total_text));
	}

	public static ViewInteraction checkoutPriceText() {
		return onView(withId(R.id.price_text));
	}

	public static ViewInteraction checkoutFreeCancellationText() {
		return onView(withId(R.id.free_cancellation_text));
	}

	public static ViewInteraction checkoutSignInCard() {
		return onView(withId(R.id.login_widget));
	}

	public static ViewInteraction checkoutPaymentInfoCard() {
		return onView(withId(R.id.payment_info_card_view));
	}

	public static ViewInteraction checkoutContactInfoCard() {
		return onView(withId(R.id.main_contact_info_card_view));
	}

	public static ViewInteraction checkoutSlideToPurchase() {
		return onView(withId(R.id.slide_to_purchase_widget));
	}

	// LX Rules widget view models

	public static ViewInteraction rulesWidget() {
		return onView(withId(R.id.rules));
	}

	public static ViewInteraction rulesWidgetCancellationPolicyHeader() {
		return onView(allOf(withId(R.id.cancellation_policy_header_text_view), withText(R.string.cancellation_policy)));
	}

	public static ViewInteraction rulesWidgetCancellationPolicyContent(String cancellationPolicyContent) {
		return onView(allOf(withId(R.id.cancellation_policy_text_view), withText(cancellationPolicyContent)));
	}

	public static ViewInteraction rulesWidgetRulesRestrictions() {
		return onView(allOf(withId(R.id.rules_and_restrictions), withText(R.string.rules_and_restrictions)));
	}

	public static ViewInteraction rulesWidgetTermsConditions() {
		return onView(allOf(withId(R.id.terms_and_conditions), withText(R.string.terms_and_conditions)));
	}

	public static ViewInteraction rulesWidgetPrivacyPolicy() {
		return onView(allOf(withId(R.id.privacy_policy), withText(R.string.privacy_policy)));
	}

	public static ViewInteraction rulesWidgetToolbar() {
		return onView(withId(R.id.lx_rules_toolbar));
	}

	public static ViewInteraction resultList() {
		return onView(withId(R.id.lx_search_results_list));
	}

	public static void clickOnResultAtIndex(int index) {
		resultList().perform(
			RecyclerViewActions
				.actionOnItemAtPosition(index, click()));
	}

	public static ViewInteraction getTile(String activityTitle, int listId) {
		return listItemView(withChild(withChild(withText(activityTitle))), listId);
	}

	public static Matcher<View> withResults(final int expectedResultsCount) {
		return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
			@Override
			public boolean matchesSafely(RecyclerView view) {
				return view.getAdapter().getItemCount() == expectedResultsCount;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The total number of results must match");
			}
		};
	}

	public static ViewInteraction listItemView(Matcher<View> identifyingMatcher, int listId) {
		Matcher<View> itemView = allOf(withParent(recyclerView(listId)),
			withChild(identifyingMatcher));
		return onView(itemView);
	}

	public static void goToSearchResults(IdlingResources.LxIdlingResource lxIdlingResource) throws Throwable {
		if (!lxIdlingResource.isSearchResultsAvailable()) {
			SearchScreen.doGenericLXSearch();
		}
	}
}
