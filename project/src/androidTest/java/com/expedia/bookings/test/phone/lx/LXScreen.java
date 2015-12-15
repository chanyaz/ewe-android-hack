package com.expedia.bookings.test.phone.lx;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Assert;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.SpoonScreenshotUtils;
import com.expedia.bookings.test.espresso.TabletViewActions;
import com.expedia.bookings.widget.LXCategoryListAdapter;
import com.expedia.bookings.widget.LXResultsListAdapter;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.ViewActions.waitFor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class LXScreen {
	public static ViewInteraction calendar() {
		return onView(withId(R.id.search_calendar));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.search_location));
	}

	public static void didNotGoToResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.search_params_container);
	}

	public static void selectLocation(String location) throws Throwable {
		Common.delay(1);
		onView(withText(location))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity().getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_dates));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction itinNumberOnConfirmationScreen() {
		return onView(withId(R.id.itin_number));
	}

	public static ViewInteraction searchResultsWidget() {
		return onView(withId(R.id.lx_search_results_widget));
	}

	public static ViewInteraction searchCategoryResultsWidget() {
		return onView(withId(R.id.lx_category_results_widget));
	}

	public static ViewInteraction searchFailed() {
		return onView(withId(R.id.lx_search_error_widget));
	}

	public static ViewInteraction searchList() {
		return onView(recyclerView(R.id.lx_search_results_list));
	}

	public static ViewInteraction categoryList() {
		return onView(recyclerView(R.id.lx_category_list));
	}

	public static void waitForSearchListDisplayed() {
		searchList().perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
		// Wait an extra bit just to be sure the list items have settled
		Common.delay(2);
	}

	public static void waitForCategoryListDisplayed() {
		categoryList().perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
		// Wait an extra bit just to be sure the list items have settled
		Common.delay(2);
	}

	public static void waitForSearchResultsWidgetDisplayed() {
		searchResultsWidget().perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
	}

	public static void waitForCategoryResultsWidgetDisplayed() {
		searchCategoryResultsWidget().perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
	}

	public static ViewInteraction sortAndFilterButton() {
		return onView(withId(R.id.sort_filter_button));
	}

	public static ViewInteraction sortAndFilterWidget() {
		return onView(withId(R.id.sort_filter_widget));
	}

	public static Matcher<View> recyclerView(int viewId) {
		return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
	}

	public static ViewInteraction progressDetails() {
		return onView(withId(R.id.overlay_title_container));
	}

	public static void waitForLoadingDetailsNotDisplayed() {
		progressDetails().perform(waitFor(not(isDisplayed()), 10, TimeUnit.SECONDS));
	}

	public static void waitForDetailsDisplayed() {
		Common.delay(1);
		onView(withId(R.id.offers)).perform(waitFor(isDisplayed(), 10, TimeUnit.SECONDS));
	}

	public static ViewInteraction detailsWidget() {
		return onView(withId(R.id.activity_details));
	}

	public static ViewInteraction withOfferText(String offerText) {
		return onView(allOf(withId(R.id.offer_row), withChild(withChild(withText(startsWith(offerText))))));
	}

	public static ViewInteraction selectTicketsButton(String offerText) {
		return onView(allOf(withId(R.id.select_tickets), hasSibling(withChild(withText(startsWith(offerText))))));
	}

	public static ViewInteraction ticketPicker(String offerText) {
		return onView(allOf(withId(R.id.offer_tickets_picker),
			hasSibling(withChild(withChild(withText(startsWith(offerText)))))));
	}

	public static ViewInteraction showMore() {
		return onView(withId(R.id.show_more_widget));
	}

	public static ViewInteraction srpErrorToolbar() {
		return onView(allOf(isDescendantOfA(withId(R.id.search_list_presenter)), withId(R.id.toolbar)));
	}

	public static ViewInteraction toolbar() {
		return onView(withId(R.id.toolbar));
	}

	public static ViewInteraction searchWidgetToolbarNavigation() {
		return onView(allOf(isDescendantOfA(withId(R.id.search_params_widget)), withParent(withId(R.id.toolbar)),
			isAssignableFrom(ImageButton.class)));
	}

	public static ViewInteraction resultsPresenterToolbarNavigation() {
		return onView(
			allOf(isDescendantOfA(withId(R.id.search_list_presenter)), withParent(withId(R.id.toolbar)),
				isAssignableFrom(ImageButton.class)));
	}

	public static ViewInteraction searchButton() {
		return onView(allOf(withId(R.id.search_btn), isDescendantOfA(hasSibling(withId(R.id.search_container)))));
	}

	public static ViewInteraction searchButtonInSRPToolbar() {
		return onView(allOf(isDescendantOfA(withId(R.id.search_list_presenter)), withId(R.id.menu_open_search)));
	}

	public static ViewInteraction detailsDate(String dateText) {
		return onView(allOf(withParent(withId(R.id.offer_dates_container)), withText(dateText)));
	}

	public static ViewAction setLXActivities(final List<LXActivity> activities) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return withId(R.id.lx_search_results_list);
			}

			@Override
			public String getDescription() {
				return "Placing the view holder in the recycler view";
			}

			@Override
			public void perform(UiController uiController, View view) {
				uiController.loopMainThreadUntilIdle();
				RecyclerView rv = (RecyclerView) view;
				((LXResultsListAdapter) rv.getAdapter()).setItems(activities);
			}
		};
	}

	public static ViewAction setLXCategories(final List<LXCategoryMetadata> categories) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return withId(R.id.lx_category_list);
			}

			@Override
			public String getDescription() {
				return "Placing the view holder in the recycler view";
			}

			@Override
			public void perform(UiController uiController, View view) {
				uiController.loopMainThreadUntilIdle();
				RecyclerView rv = (RecyclerView) view;
				((LXCategoryListAdapter) rv.getAdapter()).setItems(categories);
			}
		};
	}

	public static ViewAction performCategoryViewHolderComparison(final String title) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View viewHolder) {
				TextView categoryTitle = (TextView) viewHolder.findViewById(R.id.category_title);

				Assert.assertEquals(title, categoryTitle.getText());
			}
		};
	}

	public static ViewAction performViewHolderComparison(final String title, final String price,
		final String originalPrice, final String duration) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return null;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public void perform(UiController uiController, View viewHolder) {
				TextView titleText = (TextView) viewHolder.findViewById(R.id.activity_title);
				TextView priceText = (TextView) viewHolder.findViewById(R.id.activity_price);
				TextView originalPriceText = (TextView) viewHolder.findViewById(R.id.activity_original_price);
				TextView durationText = (TextView) viewHolder.findViewById(R.id.activity_duration);

				Assert.assertEquals(title, titleText.getText());
				Assert.assertEquals(price, priceText.getText());
				Assert.assertEquals(originalPrice, originalPriceText.getText().toString());
				Assert.assertEquals(duration, durationText.getText());
			}
		};
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

	public static ViewInteraction checkoutErrorScreen() {
		return onView(withId(R.id.lx_checkout_error_widget));
	}

	public static ViewInteraction checkoutErrorText() {
		return onView(allOf(isDescendantOfA(withId(R.id.lx_checkout_error_widget)), withId(R.id.error_text)));
	}

	public static ViewInteraction checkoutErrorButton() {
		return onView(allOf(isDescendantOfA(withId(R.id.lx_checkout_error_widget)),
			withId(R.id.error_action_button)));
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

	public static ViewInteraction searchErrorScreen() {
		return onView(withId(R.id.lx_search_error_widget));
	}

	public static ViewInteraction searchErrorText() {
		return onView(allOf(isDescendantOfA(withId(R.id.lx_search_error_widget)), withId(R.id.error_text)));
	}

	public static ViewInteraction searchErrorButton() {
		return onView(allOf(isDescendantOfA(withId(R.id.lx_search_error_widget)),
			withId(R.id.error_action_button)));
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

}
