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
import com.expedia.bookings.widget.traveler.PhoneEntryView;
import com.expedia.ui.HotelActivity;
import com.expedia.vm.traveler.PhoneEntryViewModel;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class PhoneEntryViewTest {
	private final int testCode = 355;
	private final String testCodeString = "355";
	private final String testCountryName = "Albania";
	private final String testNumber = "773 202 5862";
	private PhoneEntryView phoneEntryView;

	@Rule
	public ActivityTestRule<HotelActivity> activityTestRule = new ActivityTestRule<>(HotelActivity.class);

	@Before
	public void setUp() {
		phoneEntryView = (PhoneEntryView) LayoutInflater.from(activityTestRule.getActivity()).inflate(R.layout.phone_entry_widget,
			new PhoneEntryView(activityTestRule.getActivity(), null), true);

		// Espresso will barf otherwise.
		phoneEntryView.getPhoneNumber().setHintAnimationEnabled(false);
	}

	@Test
	public void updatePhone() {
		PhoneEntryViewModel phoneVM = new PhoneEntryViewModel(new Traveler());
		phoneEntryView.setViewModel(phoneVM);

		phoneEntryView.getPhoneNumber().getEditText().setText(testNumber);
		assertEquals(testNumber, phoneVM.getTraveler().getPhoneNumber());
	}

	@Test
	public void phonePrePopulated() {
		Traveler traveler = new Traveler();
		traveler.setPhoneNumber(testNumber);
		traveler.setPhoneCountryCode(testCodeString);
		traveler.setPhoneCountryName(testCountryName);

		phoneEntryView.setViewModel(new PhoneEntryViewModel(traveler));

		assertEquals(testNumber, phoneEntryView.getPhoneNumber().getEditText().getText().toString());
		assertEquals(testCode, phoneEntryView.getPhoneSpinner().getSelectedTelephoneCountryCode());
		assertEquals(testCountryName, phoneEntryView.getPhoneSpinner().getSelectedTelephoneCountry());
	}

	@Test
	public void phoneErrorState() {
		PhoneEntryViewModel phoneVM = new PhoneEntryViewModel(new Traveler());
		phoneEntryView.setViewModel(phoneVM);

		phoneVM.getPhoneErrorSubject().onNext(0);

		assertEquals(phoneEntryView.getPhoneNumber().getEditText().getCompoundDrawables()[2],
			phoneEntryView.getPhoneNumber().getErrorIcon());
		phoneEntryView.getPhoneNumber().getEditText().setText(testNumber);
		assertEquals(phoneEntryView.getPhoneNumber().getEditText().getCompoundDrawables()[2], null);
	}
}
