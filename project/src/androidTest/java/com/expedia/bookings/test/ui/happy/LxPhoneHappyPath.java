package com.expedia.bookings.test.ui.happy;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hamcrest.Matchers;
import org.joda.time.LocalDate;

import android.support.test.espresso.contrib.RecyclerViewActions;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.LXBaseActivity;
import com.expedia.bookings.test.component.lx.LXViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;
import com.expedia.bookings.test.ui.utils.EspressoUtils;
import com.expedia.bookings.test.ui.utils.PhoneTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.hasAtLeastOneChild;
import static com.expedia.bookings.test.ui.espresso.CustomMatchers.hasOptionsCount;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getDataFromTheTileView;
import static com.expedia.bookings.test.ui.espresso.ViewActions.getString;
import static com.expedia.bookings.test.ui.espresso.ViewActions.isEnabled;
import static com.expedia.bookings.test.ui.espresso.ViewActions.validateDateButtonAtIndex;
import static com.expedia.bookings.test.ui.espresso.ViewActions.waitFor;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;

public class LxPhoneHappyPath extends PhoneTestCase {

	private static final String TAG = LxPhoneHappyPath.class.getSimpleName();
	final static String ACTIVITY_TITLE = "Activity Title";
	final static String ACTIVITY_CATEGORIES = "Activity Categories";
	final static String ACTIVITY_PRICE = "Activity Price";
	final static String ACTIVITY_PRICE_TICKET_TYPE = "Activity Price Ticket Type";

	public LxPhoneHappyPath() {
		super(LXBaseActivity.class);
	}

	/*
		This is a detailed test flow that will verify all the manual checks across the LX path
	 */

	public void testLxPhoneHappyPath() throws Throwable {
		//Start the basic search
		performSearch();
		assertTrue("Atleast one result must appear", (EspressoUtils.getListCount(LXViewModel.searchList()) >= 1));
		//capture the data from the first Activity
		HashMap<String, String> srpTileCapturedData = getDataFromSRPTile(0);
		//perform click on the first tile
		onView(withId(R.id.lx_search_results_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
		onView(withId(R.id.loading_details)).perform(waitFor(10L, TimeUnit.SECONDS, ProgressBar.class));
		validateHeroImageInActivityDetails(srpTileCapturedData);
		ScreenActions.delay(1);
		validateHighLightSectionPresence();
		validateDateSelectionContainerHas15Offers();
		validateAtleastOneOfferIsClickable();
		validateTicketsGetDisplayedOnClickingOffer();
	}


	/*
		This method is used to perform a basic search on LX path
	 */

	private void performSearch() throws Throwable {
		String expectedLocationDisplayName = "San Francisco, CA";
		screenshot("LX_search");
		LXViewModel.location().perform(typeText("San"));
		LXViewModel.selectLocation(getInstrumentation(), expectedLocationDisplayName);
		LXViewModel.selectDateButton().perform(click());
		LXViewModel.selectDates(LocalDate.now(), null);
		screenshot("LX_Search_Params_Entered");
		LXViewModel.searchButton().perform(click());
		//Wait for the progress bar to disappear
		onView(withId(R.id.loading_results)).perform(waitFor(10L, TimeUnit.SECONDS, ProgressBar.class));
		screenshot("On_Search_Screen");
	}

	/*
		This method is used to capture the data from a given tile of the search result page
	 */

	private HashMap<String, String> getDataFromSRPTile(int index) throws Throwable {
		// create a data container
		HashMap<String, String> dataContainer = new HashMap<>();
		dataContainer.put(ACTIVITY_TITLE, null);
		dataContainer.put(ACTIVITY_CATEGORIES, null);
		dataContainer.put(ACTIVITY_PRICE, null);
		dataContainer.put(ACTIVITY_PRICE_TICKET_TYPE, null);
		AtomicReference<HashMap<String, String>> atomicHolderForDataContainer = new AtomicReference<HashMap<String, String>>();
		//Pass the container to the view for loading.
		atomicHolderForDataContainer.set(dataContainer);
		onView(withId(R.id.lx_search_results_list)).perform(
			RecyclerViewActions
				.actionOnItemAtPosition(index, getDataFromTheTileView(atomicHolderForDataContainer)));
		//validate that we have received all the results from the activity tile at the supplied index.
		for (String key : atomicHolderForDataContainer.get().keySet()) {
			assertNotNull("The " + key + " is not present in the search result at index " + index,
				atomicHolderForDataContainer.get().get(key));
		}
		return atomicHolderForDataContainer.get();
	}

	/*
		This is to validate if we are showing the correct data in the Hero Image container
		Expected : The data that was passed from the search Page must be present in the Infosite page
	 */
	private void validateHeroImageInActivityDetails(HashMap<String, String> benchmarkDataFromSRPTile) throws Throwable {
		onView(allOf(withId(R.id.title), withParent(withId(R.id.activity_info_container))))
			.check(matches(withText(benchmarkDataFromSRPTile.get(ACTIVITY_TITLE))));
		onView(allOf(withId(R.id.price), withParent(withId(R.id.activity_info_container)))).check(
			matches(withText(benchmarkDataFromSRPTile.get(ACTIVITY_PRICE))));
		// get the price ticket type and then assert on it.
		AtomicReference<String> priceTicketType = new AtomicReference<>();
		onView(allOf(withId(R.id.per_ticket_type), withParent(withId(R.id.activity_info_container))))
			.perform(getString(priceTicketType));
		assertTrue("The price type must contain " + priceTicketType.get(),
			benchmarkDataFromSRPTile.get(ACTIVITY_PRICE_TICKET_TYPE).toLowerCase()
				.contains(priceTicketType.get().toLowerCase()));
	}

	/*
		This is to validate theta the Highlight section must be present and has some data
	 */
	private void validateHighLightSectionPresence() throws Throwable {
		onView(allOf(withId(R.id.section_title), withText(getInstrumentation().getTargetContext().getString(
			R.string.highlights_activity_details)))).check(matches(
			isDisplayed()));
		AtomicReference<String> highlightCaptionContent = new AtomicReference<>();
		// get the content of the view that is the sibling of the view that has title as [Highlights] and has id as section_title
		onView(allOf(withId(R.id.section_content), hasSibling(allOf(withId(R.id.section_title),
			withText(getInstrumentation().getTargetContext().getString(R.string.highlights_activity_details))))))
			.perform(getString(highlightCaptionContent));
		assertTrue("The Highlight section must contain some text ", !highlightCaptionContent.get().isEmpty());
	}

	/*
		Expected:This is to validate that we have exactly 15 offers to show.
	 */

	private void validateDateSelectionContainerHas15Offers() throws Throwable {
		//validate that the number of options available is 15
		LXViewModel.detailsDateContainer().check(matches(hasOptionsCount(15)));
		// check all the dates in the 15 radio buttons.
		for (int dayOffset = 0; dayOffset < 15; dayOffset++) {
			LocalDate dayToValidate = LocalDate.now().plusDays(dayOffset);
			String currentDayOfTheWeek = dayToValidate.dayOfWeek().getAsShortText();
			String currentDayOfTheMonth = dayToValidate.dayOfMonth().getAsText();
			LXViewModel.detailsDateContainer()
				.perform(validateDateButtonAtIndex(dayOffset, currentDayOfTheWeek, currentDayOfTheMonth));
		}
	}

	/*
		This is to validate among all the 15 days that we have on the infosite page we must have atleast one clickable date.
		Expected : We must have atleast one offer in the search window
	 */

	private void validateAtleastOneOfferIsClickable() throws Throwable {
		OfferRadioButtonFields firstEnabled = getFirstEnabled();
		assertTrue("We must have atleast one offer that can be clickable ",
			firstEnabled != null && firstEnabled.enabled);
	}


	/*
		This is to validate if upon click on a particular date we get the offers or not.
		Expected : If the offer is valid( enabled) then we must have atleast one ticket to show
	 */

	private void validateTicketsGetDisplayedOnClickingOffer() throws Throwable {
		// check if we have atleast one selectable offer
		OfferRadioButtonFields firstEnabled = getFirstEnabled();
		assertTrue("We must have atleast one offer that can be clickable ",
			firstEnabled != null && firstEnabled.enabled);
		onView(Matchers.allOf(withParent(withId(R.id.offer_dates_container)), withText(endsWith(firstEnabled.dayOfTheMonth)),
			withText(startsWith(
				firstEnabled.weekDay)))).perform(click());
		onView(withId(R.id.offers)).check(matches(hasAtLeastOneChild()));
	}

	private class OfferRadioButtonFields {
		public String weekDay;
		public String dayOfTheMonth;
		public boolean enabled;
	}


	private OfferRadioButtonFields getFirstEnabled() {
		String currentDayOfTheWeek = null, currentDayOfTheMonth = null;
		boolean gotAnEnabledButton = false;
		for (int dayOffset = 0; dayOffset < 15; dayOffset++) {
			LocalDate dayToValidate = LocalDate.now().plusDays(dayOffset);
			currentDayOfTheWeek = dayToValidate.dayOfWeek().getAsShortText();
			currentDayOfTheMonth = dayToValidate.dayOfMonth().getAsText();
			AtomicReference<Boolean> enabledContainer = new AtomicReference<Boolean>();
			onView(Matchers
				.allOf(withParent(withId(R.id.offer_dates_container)), withText(endsWith(currentDayOfTheMonth)),
					withText(startsWith(
						currentDayOfTheWeek)))).perform(scrollTo()).perform(isEnabled(enabledContainer));
			if (enabledContainer.get()) {
				gotAnEnabledButton = true;
				break;
			}
		}
		if (!gotAnEnabledButton) {
			return null;
		}

		//Return the fields of the enabled button back
		OfferRadioButtonFields radioButton = new OfferRadioButtonFields();
		radioButton.weekDay = currentDayOfTheWeek;
		radioButton.dayOfTheMonth = currentDayOfTheMonth;
		radioButton.enabled = true;
		return radioButton;
	}

}
