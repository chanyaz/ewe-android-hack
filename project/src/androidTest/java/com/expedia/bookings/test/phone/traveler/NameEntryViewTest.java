package com.expedia.bookings.test.phone.traveler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.design.widget.TextInputLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.LayoutInflater;

import com.expedia.bookings.R;
import com.expedia.bookings.data.TravelerName;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.traveler.NameEntryView;
import com.expedia.vm.traveler.TravelerNameViewModel;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class NameEntryViewTest {
	private final String testFirstName = "Oscar";
	private final String testMiddleInitial = "T";
	private final String testLastName = "Grouch";
	private NameEntryView nameView;

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_name_entry_view,
		R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal());
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, false);

		nameView = (NameEntryView) activityTestRule.getRoot();
		Ui.getApplication(InstrumentationRegistry.getTargetContext()).defaultTravelerComponent();
	}

	@Test
	public void testMaterialForm() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms);
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, true);

		NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activityTestRule.getActivity())
			.inflate(R.layout.test_name_entry_view, null);

		assertTrue(nameEntryView.getMaterialFormTestEnabled());
		TextInputLayout textInputLayout = (TextInputLayout) nameEntryView.findViewById(R.id.first_name_layout_input);
		assertNotNull(textInputLayout);
	}

	@Test
	public void testNameUpdates() throws Throwable {
		TravelerNameViewModel testViewModel = new TravelerNameViewModel(InstrumentationRegistry.getTargetContext());
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		onView(withId(R.id.first_name_input)).perform(typeText(testFirstName));
		onView(withId(R.id.middle_name_input)).perform(typeText(testMiddleInitial));
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
		TravelerNameViewModel testViewModel = new TravelerNameViewModel(InstrumentationRegistry.getTargetContext());
		testViewModel.updateTravelerName(name);
		setViewModel(testViewModel);

		assertEquals(testFirstName, name.getFirstName());
		assertEquals(testMiddleInitial, name.getMiddleName());
		assertEquals(testLastName, name.getLastName());
	}

	@Test
	public void testErrorState() throws Throwable {
		final TravelerNameViewModel testViewModel = new TravelerNameViewModel(InstrumentationRegistry.getTargetContext());
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				testViewModel.getFirstNameViewModel().getErrorSubject().onNext(true);
				testViewModel.getMiddleNameViewModel().getErrorSubject().onNext(true);
				testViewModel.getLastNameViewModel().getErrorSubject().onNext(true);
			}
		});

		//test for accessibility content description
		assertEquals(nameView.getFirstName().getErrorContDesc(),
			"Error");
		assertEquals(nameView.getMiddleName().getErrorContDesc(),
			"Error");
		assertEquals(nameView.getLastName().getErrorContDesc(),
			"Error");

		assertEquals(nameView.getFirstName().getCompoundDrawables()[2],
			nameView.getFirstName().getErrorIcon());
		assertEquals(nameView.getMiddleName().getCompoundDrawables()[2],
			nameView.getMiddleName().getErrorIcon());
		assertEquals(nameView.getLastName().getCompoundDrawables()[2],
			nameView.getLastName().getErrorIcon());

		onView(withId(R.id.first_name_input)).perform(typeText(testFirstName));
		onView(withId(R.id.middle_name_input)).perform(typeText(testMiddleInitial));
		onView(withId(R.id.last_name_input)).perform(typeText(testLastName));

		assertEquals(nameView.getFirstName().getCompoundDrawables()[2], null);
		assertEquals(nameView.getMiddleName().getCompoundDrawables()[2], null);
		assertEquals(nameView.getLastName().getCompoundDrawables()[2], null);
	}

	@Test
	public void testMiddleNameError() throws Throwable {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms);
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, true);

		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				NameEntryView nameEntryView = (NameEntryView) LayoutInflater.from(activityTestRule.getActivity())
					.inflate(R.layout.test_name_entry_view, null);

				TravelerNameViewModel testViewModel = new TravelerNameViewModel(InstrumentationRegistry.getTargetContext());
				TravelerName traveler = new TravelerName();
				testViewModel.updateTravelerName(traveler);

				nameEntryView.setViewModel(testViewModel);
				testViewModel.getMiddleNameViewModel().getErrorSubject().onNext(true);

				TextInputLayout textInputLayout = (TextInputLayout) nameEntryView.findViewById(R.id.middle_name_layout_input);
				assertEquals("Enter middle name using letters only", textInputLayout.getError());
			}
		});

		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppUniversalCheckoutMaterialForms, AbacusUtils.DefaultVariant.CONTROL.ordinal());
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.preference_universal_checkout_material_forms, false);
	}

	@Test
	public void testMiddleNameAcceptsOneOrMoreLetter() throws Throwable {
		final String validMiddleName = "The";
		String initial = "The";
		TravelerNameViewModel testViewModel = new TravelerNameViewModel(InstrumentationRegistry.getTargetContext());
		TravelerName traveler = new TravelerName();
		testViewModel.updateTravelerName(traveler);
		setViewModel(testViewModel);

		onView(withId(R.id.middle_name_input)).perform(typeText(validMiddleName));
		assertEquals("More than one letter allowed", initial, traveler.getMiddleName());
		assertEquals("More than one letter allowed", initial, nameView.getMiddleName().getText().toString());
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
