package com.expedia.bookings.test.component.lx;

import java.util.List;

import org.hamcrest.Matcher;
import org.joda.time.LocalDate;
import org.junit.Assert;

import android.app.Instrumentation;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.test.ui.espresso.TabletViewActions;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.SpoonScreenshotUtils;
import com.expedia.bookings.utils.LXUtils;
import com.expedia.bookings.widget.LXResultsListAdapter;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

public class LXViewModel {
	public static ViewInteraction calendar() {
		return onView(withId(R.id.search_calendar));
	}

	public static ViewInteraction location() {
		return onView(withId(R.id.search_location));
	}

	public static void didNotGoToResults() {
		EspressoUtils.assertViewIsDisplayed(R.id.search_params_container);
	}

	public static void selectLocation(Instrumentation instrumentation, String location) throws Throwable {
		ScreenActions.delay(1);
		onView(withText(location))
			.inRoot(withDecorView(
				not(is(SpoonScreenshotUtils.getCurrentActivity(instrumentation).getWindow().getDecorView()))))
			.perform(click());
	}

	public static ViewInteraction selectDateButton() {
		return onView(withId(R.id.select_dates));
	}

	public static void selectDates(LocalDate start, LocalDate end) {
		calendar().perform(TabletViewActions.clickDates(start, end));
	}

	public static ViewInteraction itinNumberOnConfirmationScreen() {
		return onView(withId(R.id.itinerary_text_view));
	}

	public static ViewInteraction searchResultsWidget() {
		return onView(withId(R.id.lx_search_results_widget));
	}

	public static ViewInteraction searchFailed() {
		return onView(withId(R.id.lx_search_failure));
	}

	public static ViewInteraction searchList() {
		return onView(recyclerView(R.id.lx_search_results_list));
	}

	public static Matcher<View> recyclerView(int viewId) {
		return allOf(isAssignableFrom(RecyclerView.class), withId(viewId));
	}

	public static ViewInteraction recyclerItemView(Matcher<View> identifyingMatcher, int recyclerViewId) {
		Matcher<View> itemView = allOf(withParent(recyclerView(recyclerViewId)),
			withChild(identifyingMatcher));
		return Espresso.onView(itemView);
	}

	public static ViewInteraction progressDetails() {
		return onView(withId(R.id.loading_details));
	}

	public static ViewInteraction detailsWidget() {
		return onView(withId(R.id.activity_details));
	}

	public static Matcher<View> recyclerGalleryMatcher() {
		return withId(R.id.activity_gallery);
	}

	public static ViewInteraction infoContainer() {
		return onView(withId(R.id.activity_info_container));
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

	public static ViewInteraction toolbar() {
		return onView(withId(R.id.toolbar));
	}

	public static ViewInteraction searchButton() {
		return onView(withId(R.id.menu_search));
	}

	public static ViewInteraction detailsDate(String dateText) {
		return onView(allOf(withParent(withId(R.id.offer_dates_container)), withText(endsWith(dateText))));
	}

	public static ViewInteraction detailsDateContainer() {
		return onView(withId(R.id.offer_dates_container));
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
				((LXResultsListAdapter) rv.getAdapter()).setActivities(activities);
			}
		};
	}

	public static ViewAction performViewHolderComparison(final String title, final String price, final List<String> categoriesList) {
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
				TextView categoryText = (TextView) viewHolder.findViewById(R.id.activity_category);

				Assert.assertEquals(title, titleText.getText());
				Assert.assertEquals(price, priceText.getText());
				Assert.assertEquals(LXUtils.bestApplicableCategory(categoriesList), categoryText.getText());
			}
		};
	}

	public static ViewInteraction getTicketAddButtonViewFromTicketName(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = allOf(withText(containsString(travellerType)),
			withParent(withParent(hasSibling(withText(containsString(ticketName))))));
		return onView(allOf(withId(R.id.ticket_add), hasSibling(rowMatcher)));
	}

	public static ViewInteraction getBookNowButtonFromTicketName(String ticketName) {
		return onView(allOf(withId(R.id.lx_book_now), withParent(hasSibling(withText(containsString(ticketName))))));
	}


	//Checkout
	public static ViewInteraction checkoutWidget() {
		return onView(withId(R.id.checkout));
	}
	public static ViewInteraction checkoutOfferTitle() {
		return onView(withId(R.id.lx_offer_title_text));
	}
	public static ViewInteraction checkoutGroupText() {
		return onView(withId(R.id.lx_group_text));
	}
	public static ViewInteraction checkoutOfferDate() {
		return onView(withId(R.id.lx_offer_date));
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
}
