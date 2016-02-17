package com.expedia.bookings.test.phone.traveler;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.widget.traveler.NameEntryView;
import com.expedia.ui.HotelActivity;
import com.expedia.vm.traveler.NameEntryViewModel;

import static org.junit.Assert.assertEquals;



@RunWith(AndroidJUnit4.class)
public class NameEntryViewTest {
	private final String testFirstName = "Oscar";
	private final String testMiddleName = "The";
	private final String testLastName = "Grouch";
	private NameEntryView nameView;
	@Rule
	public ActivityTestRule<HotelActivity> activityTestRule = new ActivityTestRule<>(HotelActivity.class);

	@Before
	public void setUp() {
		nameView = (NameEntryView) LayoutInflater.from(activityTestRule.getActivity()).inflate(R.layout.name_entry_widget,
			new NameEntryView(activityTestRule.getActivity(), null), true);

		// Espresso will barf otherwise.
		nameView.getFirstName().setHintAnimationEnabled(false);
		nameView.getMiddleInitial().setHintAnimationEnabled(false);
		nameView.getLastName().setHintAnimationEnabled(false);
	}

	@Test
	public void testNameUpdates() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		nameView.getFirstName().getEditText().setText(testFirstName);
		assertEquals(testFirstName, testViewModel.getTraveler().getFirstName());

		nameView.getMiddleInitial().getEditText().setText(testMiddleName);
		assertEquals(testMiddleName, testViewModel.getTraveler().getMiddleName());

		nameView.getLastName().getEditText().setText(testLastName);
		assertEquals(testLastName, testViewModel.getTraveler().getLastName());
	}

	@Test
	public void testTravelerPrePopulated() {
		Traveler traveler = new Traveler();
		traveler.setFirstName(testFirstName);
		traveler.setMiddleName(testMiddleName);
		traveler.setLastName(testLastName);
		nameView.setViewModel(new NameEntryViewModel(traveler));

		assertEquals(testFirstName, nameView.getFirstName().getEditText().getText().toString());
		assertEquals(testMiddleName, nameView.getMiddleInitial().getEditText().getText().toString());
		assertEquals(testLastName, nameView.getLastName().getEditText().getText().toString());
	}

	@Test
	public void testTravelerUpdated() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		testViewModel.getFirstNameSubject().onNext(testFirstName);
		assertEquals(testFirstName, nameView.getFirstName().getEditText().getText().toString());
	}

	@Test
	public void testErrorState() {
		NameEntryViewModel testViewModel = new NameEntryViewModel(new Traveler());
		nameView.setViewModel(testViewModel);

		testViewModel.getFirstNameErrorSubject().onNext(0);
		testViewModel.getMiddleNameErrorSubject().onNext(0);
		testViewModel.getLastNameErrorSubject().onNext(0);
		assertEquals(nameView.getFirstName().getEditText().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getEditText().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getFirstName().getEditText().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());

		nameView.getFirstName().getEditText().setText(testFirstName);
		assertEquals(nameView.getFirstName().getEditText().getCompoundDrawables()[2], null);

		nameView.getMiddleInitial().getEditText().setText(testMiddleName);
		assertEquals(nameView.getMiddleInitial().getEditText().getCompoundDrawables()[2], null);

		nameView.getLastName().getEditText().setText(testLastName);
		assertEquals(nameView.getLastName().getEditText().getCompoundDrawables()[2], null);
	}
}
