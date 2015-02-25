package com.expedia.bookings.test.component.lx;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.ViewInteraction;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.test.rules.ExpediaMockWebServerRule;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.test.ui.phone.pagemodels.common.ScreenActions;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class LXDetailsPresenterTests {
	@Rule
	public final PlaygroundRule playground = new PlaygroundRule(R.layout.lx_details_presenter);

	@Rule
	public final ExpediaMockWebServerRule server = new ExpediaMockWebServerRule();

	@Test
	public void testActivityDetails() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		LXViewModel.progressDetails().check(matches(isDisplayed()));
		ScreenActions.delay(2);

		LXViewModel.detailsWidget().check(matches(isDisplayed()));
		LXViewModel.recyclerGallery().check(matches(isDisplayed()));
		LXViewModel.infoContainer().check(matches(isDisplayed()));
		LXViewModel.descriptionContent().check(matches(isDisplayed()));
		LXViewModel.highlightsContent().check(matches(isDisplayed()));
		LXViewModel.locationContent().check(matches(isDisplayed()));
	}

	@Test
	public void testActivityInfo() {
		Events.post(new Events.LXActivitySelected(new LXActivity()));
		ScreenActions.delay(2);

		ViewInteraction info = LXViewModel.infoContainer();
		info.check(matches(hasDescendant(allOf(withId(R.id.title), withText(startsWith("New York Pass"))))));
		info.check(matches(hasDescendant(allOf(withId(R.id.price),withText("$130")))));
		info.check(matches(hasDescendant(allOf(withId(R.id.duration), withText("2d")))));
		info.check(matches(hasDescendant(withId(R.id.free_cancellation))));

	}
}
