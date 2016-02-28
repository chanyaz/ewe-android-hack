package com.expedia.bookings.test.phone.traveler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.widget.traveler.NameEntryView;
import com.expedia.ui.HotelActivity;
import com.expedia.vm.traveler.NameEntryViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class NameEntryViewTest {
	private final String testFirstName = "Oscar";
	private final String testMiddleInitial = "T";
	private final String testLastName = "Grouch";
	private NameEntryView nameView;
	@Rule
	public ActivityTestRule<HotelActivity> activityTestRule = new ActivityTestRule<>(HotelActivity.class);

	@Before
	public void setUp() {
		nameView = (NameEntryView) LayoutInflater.from(activityTestRule.getActivity()).inflate(R.layout.name_entry_view,
			new NameEntryView(activityTestRule.getActivity(), null), true);
	}

	@Test
	public void testNameUpdates() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		nameView.getFirstName().setText(testFirstName);
		assertEquals(testFirstName, testViewModel.getTraveler().getFirstName());

		nameView.getMiddleInitial().setText(testMiddleInitial);
		assertEquals(testMiddleInitial, testViewModel.getTraveler().getMiddleName());

		nameView.getLastName().setText(testLastName);
		assertEquals(testLastName, testViewModel.getTraveler().getLastName());
	}

	@Test
	public void testTravelerPrePopulated() {
		Traveler traveler = new Traveler();
		traveler.setFirstName(testFirstName);
		traveler.setMiddleName(testMiddleInitial);
		traveler.setLastName(testLastName);
		nameView.setViewModel(new NameEntryViewModel(traveler));

		assertEquals(testFirstName, nameView.getFirstName().getText().toString());
		assertEquals(testMiddleInitial, nameView.getMiddleInitial().getText().toString());
		assertEquals(testLastName, nameView.getLastName().getText().toString());
	}

	@Test
	public void testTravelerUpdated() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		testViewModel.getFirstNameSubject().onNext(testFirstName);
		assertEquals(testFirstName, nameView.getFirstName().getText().toString());
	}

	@Test
	public void testErrorState() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		testViewModel.getFirstNameErrorSubject().onNext(0);
		testViewModel.getMiddleNameErrorSubject().onNext(0);
		testViewModel.getLastNameErrorSubject().onNext(0);
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());

		nameView.getFirstName().setText(testFirstName);
		assertEquals(nameView.getFirstName().getCompoundDrawables()[2], null);

		nameView.getMiddleInitial().setText(testMiddleInitial);
		assertEquals(nameView.getMiddleInitial().getCompoundDrawables()[2], null);

		nameView.getLastName().setText(testLastName);
		assertEquals(nameView.getLastName().getCompoundDrawables()[2], null);
	}

	@Test
	public void testMiddleNameOnlyOneLetter() {
		String invalidMiddleName = "The";
		String initial = "T";
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		nameView.getMiddleInitial().setText(invalidMiddleName);
		assertEquals("Only One Letter Allowed", initial, testViewModel.getTraveler().getMiddleName());

		assertEquals("Only One LetterAllowed", initial, nameView.getMiddleInitial().getText().toString());
	}
}
