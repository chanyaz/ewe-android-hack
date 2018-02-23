package com.expedia.bookings.test.pagemodels.lx;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.phone.lx.models.TicketSummaryDataModel;
import com.expedia.bookings.widget.LXOffersListWidget;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

public class LXInfositeScreen {

	public static ViewInteraction detailsDateContainer() {
		return onView(
			allOf(withId(R.id.offer_dates_container), isDescendantOfA(
				withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction offersWidgetContainer() {
		return onView(allOf(withId(R.id.offers), isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction ticketAddButton(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			isDescendantOfA(hasSibling(withText(containsString(ticketName)))), isDescendantOfA(withId(R.id.activity_details_presenter)));
		return onView(Matchers.allOf(withId(R.id.ticket_add), hasSibling(rowMatcher)));
	}

	public static ViewInteraction ticketContainer(String ticketName) {
		return onView(Matchers.allOf(withId(R.id.ticket_selectors_container), hasSibling(withText(ticketName)),
			isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction selectOffer(String ticketName) {
		return onView(Matchers.allOf(withId(R.id.offer_row), hasDescendant(withText(ticketName)),
			isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction ticketRemoveButton(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			isDescendantOfA(hasSibling(withText(ticketName))));
		return onView(Matchers.allOf(withId(R.id.ticket_remove), hasSibling(rowMatcher),
			isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction ticketCount(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			isDescendantOfA(hasSibling(withText(ticketName))));
		return onView(Matchers.allOf(withId(R.id.ticket_count), hasSibling(rowMatcher),
			isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction ticketRow(String ticketName, String travellerType) {
		return onView(Matchers.allOf(withText(containsString(travellerType)), withId(R.id.ticket_details),
			isDescendantOfA(hasSibling(withText(ticketName))),
			isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction priceSummary(String ticketName) {
		return onView(
			Matchers.allOf(withId(R.id.selected_ticket_summary), isDescendantOfA(hasSibling(withText(ticketName))),
				isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	public static ViewInteraction bookNowButton(String ticketName) {
		return onView(
			Matchers.allOf(withId(R.id.lx_book_now), withParent(hasSibling(
				withText(containsString(ticketName)))), isDescendantOfA(withId(R.id.activity_details_presenter))));
	}

	/*
		This method is used for getting the data back from the individual Tickets that are been offered by the Activity Infosite page
		Passed Params : index = Since on a particular day we might have more than one offers we want to have an index passed so that we can
		get the details.

		offerData: The holder of the data.
	*/

	public static ViewAction loadTicketSummary(final int index,
		final TicketSummaryDataModel offerDataContainer) {
		return new ViewAction() {

			@Override
			public Matcher<View> getConstraints() {
				return Matchers.allOf(isAssignableFrom(LXOffersListWidget.class));
			}

			@Override
			public void perform(UiController uiController, View view) {
				LinearLayout offerRow = (LinearLayout) ((ViewGroup) view).getChildAt(index);
				TextView offerTitleView = (TextView) offerRow.findViewById(R.id.offer_title);
				TextView offerPriceView = (TextView) offerRow.findViewById(R.id.price_summary);
				offerDataContainer.ticketTitle = offerTitleView.getText().toString();
				offerDataContainer.priceSummary = offerPriceView.getText().toString();
			}

			@Override
			public String getDescription() {
				return "Get the ticket details from the Offered tickets";
			}
		};
	}

	public static Matcher<View> withRestrictionText() {
		return new BoundedMatcher<View, TextView>(TextView.class) {
			@Override
			public boolean matchesSafely(TextView view) {
				String rowText = view.getText().toString();
				int startIndex = rowText.indexOf("(");
				int endIndex = rowText.indexOf(")");
				String restrictionText = rowText.substring(startIndex + 1, endIndex);
				return !restrictionText.trim().isEmpty();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("The restriction text must be present");
			}
		};
	}
}
