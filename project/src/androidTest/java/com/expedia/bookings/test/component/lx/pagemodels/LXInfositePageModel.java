package com.expedia.bookings.test.component.lx.pagemodels;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.lx.models.TicketSummaryDataModel;
import com.expedia.bookings.widget.LXOffersListWidget;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.containsString;


public class LXInfositePageModel {
	public static ViewInteraction title() {
		return onView(allOf(withId(R.id.title), withParent(withId(R.id.activity_info_container))));
	}

	public static ViewInteraction priceOnHeroImage() {
		return onView(allOf(withId(R.id.price), withParent(withId(R.id.activity_info_container))));
	}

	public static ViewInteraction perTravellerType() {
		return onView(allOf(withId(R.id.per_ticket_type), withParent(withId(R.id.activity_info_container))));
	}

	public static ViewInteraction category() {
		return onView(allOf(withId(R.id.category), withParent(withId(R.id.activity_info_container))));
	}

	public static ViewInteraction detailsDateContainer() {
		return onView(withId(R.id.offer_dates_container));
	}

	public static ViewInteraction offersWidgetContainer() {
		return onView(Matchers.allOf(withId(R.id.offers)));
	}

	public static ViewInteraction ticketAddButton(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			withParent(withParent(hasSibling(withText(containsString(ticketName))))));
		return onView(Matchers.allOf(withId(R.id.ticket_add), hasSibling(rowMatcher)));
	}

	public static ViewInteraction ticketContainer(String ticketName) {
		return onView(Matchers.allOf(withId(R.id.ticket_selectors_container), hasSibling(withText(ticketName))));
	}

	public static ViewInteraction ticketRemoveButton(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			withParent(withParent(hasSibling(withText(ticketName)))));
		return onView(Matchers.allOf(withId(R.id.ticket_remove), hasSibling(rowMatcher)));
	}

	public static ViewInteraction ticketCount(String ticketName, String travellerType) {
		Matcher<View> rowMatcher = Matchers.allOf(withText(containsString(travellerType)),
			withParent(withParent(hasSibling(withText(ticketName)))));
		return onView(Matchers.allOf(withId(R.id.ticket_count), hasSibling(rowMatcher)));
	}

	public static ViewInteraction ticketRow(String ticketName, String travellerType) {
		return onView(Matchers.allOf(withText(containsString(travellerType)),
			withParent(withParent(hasSibling(withText(ticketName))))));
	}

	public static ViewInteraction priceSummary(String ticketName) {
		return onView(
			Matchers.allOf(withId(R.id.selected_ticket_summary), withParent(hasSibling(withText(ticketName)))));
	}

	public static ViewInteraction bookNowButton(String ticketName) {
		return onView(
			Matchers.allOf(withId(R.id.lx_book_now), withParent(hasSibling(withText(containsString(ticketName))))));
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

}
