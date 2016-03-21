package com.expedia.bookings.test.phone.traveler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.widget.traveler.NameEntryView;
import com.expedia.vm.traveler.TravelerNameViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NameEntryViewTest {
	private final String testFirstName = "Oscar";
	private final String testMiddleInitial = "T";
	private final String testLastName = "Grouch";
	private NameEntryView nameView;

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_name_entry_view, R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		nameView = (NameEntryView) activityTestRule.getRoot();
	}

	@Test
	public void testNameUpdates() throws Throwable {
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		onView(withId(R.id.first_name_input)).perform(typeText(testFirstName));
		onView(withId(R.id.middle_initial_input)).perform(typeText(testMiddleInitial));
		onView(withId(R.id.last_name_input)).perform(typeText(testLastName));

		assertEquals(testFirstName, traveler.getFirstName());
		assertEquals(testMiddleInitial, traveler.getMiddleName());
		assertEquals(testLastName, traveler.getLastName());
	}

	@Test
	public void testTravelerPrePopulated() throws Throwable {
		TravelerName name = new TravelerName();
		name.setFirstName(testFirstName);
		name.setMiddleName(testMiddleInitial);
		name.setLastName(testLastName);
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		testViewModel.updateTravelerName(name);
		setViewModel(testViewModel);

		assertEquals(testFirstName, name.getFirstName());
		assertEquals(testMiddleInitial, name.getMiddleName());
		assertEquals(testLastName, name.getLastName());
	}

	@Test
	public void testErrorState() throws Throwable {
		final TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testViewModel.getFirstNameErrorSubject().onNext(true);
				testViewModel.getMiddleNameErrorSubject().onNext(true);
				testViewModel.getLastNameErrorSubject().onNext(true);
			}
		});
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());

		onView(withId(R.id.first_name_input)).perform(typeText(testFirstName));
		onView(withId(R.id.middle_initial_input)).perform(typeText(testMiddleInitial));
		onView(withId(R.id.last_name_input)).perform(typeText(testLastName));

		assertEquals(nameView.getFirstName().getCompoundDrawables()[2], null);
		assertEquals(nameView.getMiddleInitial().getCompoundDrawables()[2], null);
		assertEquals(nameView.getLastName().getCompoundDrawables()[2], null);
	}

	@Test
	public void testMiddleNameOnlyOneLetter() throws Throwable {
		final String invalidMiddleName = "The";
		String initial = "T";
		TravelerNameViewModel testViewModel = new TravelerNameViewModel();
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		onView(withId(R.id.middle_initial_input)).perform(typeText(invalidMiddleName));
		assertEquals("Only One Letter Allowed", initial, traveler.getMiddleName());
		assertEquals("Only One LetterAllowed", initial, nameView.getMiddleInitial().getText().toString());
	}

	private void setViewModel(final TravelerNameViewModel viewModel) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				nameView.setViewModel(viewModel);
			}
		});
	}
}
