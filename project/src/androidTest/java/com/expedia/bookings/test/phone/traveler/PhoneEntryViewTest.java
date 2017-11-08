package com.expedia.bookings.test.phone.traveler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Phone;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.CustomMatchers;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.traveler.PhoneEntryView;
import com.expedia.vm.traveler.TravelerPhoneViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class PhoneEntryViewTest {
	private final String testCodeString = "355";
	private final String testCountryName = "Albania";
	private final String testNumber = "773 202 5862";
	private PhoneEntryView phoneEntryView;

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_phone_entry_view,
		R.style.V2_Theme_Packages);

	@Before
	public void setup() {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal());

		Context context = InstrumentationRegistry.getTargetContext();
		Ui.getApplication(context).defaultTravelerComponent();
	}

	@Test
	public void testMaterialForm() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				PhoneEntryView phoneEntryView = (PhoneEntryView) LayoutInflater.from(activityTestRule.getActivity())
					.inflate(R.layout.test_phone_entry_view, null);
				TextInputLayout textInputLayout = (TextInputLayout) phoneEntryView
					.findViewById(R.id.edit_phone_layout_number);
				assertNotNull(textInputLayout);
			}
		});

	}

	@Test
	public void updatePhone() throws Throwable {
		phoneEntryView = (PhoneEntryView) activityTestRule.getRoot();
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(InstrumentationRegistry.getTargetContext());
		Phone phone = new Phone();
		phoneVM.updatePhone(phone);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				phoneEntryView.setViewModel(phoneVM);
			}
		});

		onView(withId(R.id.edit_phone_number)).perform(typeText(testNumber));
		assertEquals(testNumber, phone.getNumber());
	}

	@Test
	public void phonePrePopulated() throws Throwable {
		phoneEntryView = (PhoneEntryView) activityTestRule.getRoot();
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(InstrumentationRegistry.getTargetContext());
		Phone phone = new Phone();
		phone.setNumber(testNumber);
		phone.setCountryCode(testCodeString);
		phone.setCountryName(testCountryName);
		phoneVM.updatePhone(phone);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				phoneEntryView.setViewModel(phoneVM);
			}
		});

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.edit_phone_number, testNumber);
		EspressoUtils.assertViewWithTextIsDisplayed(android.R.id.text1, "+" + testCodeString);
	}

	@Test
	public void phoneErrorState() throws Throwable {
		phoneEntryView = (PhoneEntryView) activityTestRule.getRoot();
		final TravelerPhoneViewModel phoneVM = new TravelerPhoneViewModel(InstrumentationRegistry.getTargetContext());
		phoneVM.updatePhone(new Phone());

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				phoneEntryView.setViewModel(phoneVM);
				phoneVM.getPhoneViewModel().getErrorSubject().onNext(true);
			}
		});

		//test for accessibility content description
		assertEquals("Enter a valid phone number", phoneEntryView.getPhoneNumber().getErrorContDesc());

		onView(CustomMatchers.withCompoundDrawable(R.drawable.invalid)).check(matches(isDisplayed()));

		onView(withId(R.id.edit_phone_number)).perform(typeText(testNumber));
		onView(CustomMatchers.withCompoundDrawable(R.drawable.invalid)).check(doesNotExist());
	}
}
