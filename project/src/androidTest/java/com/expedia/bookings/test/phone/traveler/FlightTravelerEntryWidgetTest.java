package com.expedia.bookings.test.phone.traveler;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.TravelerCheckoutStatus;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.pagemodels.common.TravelerModel.TravelerDetails;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightTravelerEntryWidget;
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel;
import rx.subjects.BehaviorSubject;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.espresso.CustomMatchers.withCompoundDrawable;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;

@RunWith(AndroidJUnit4.class)
public class FlightTravelerEntryWidgetTest {
	private FlightTravelerEntryWidget entryWidget;
	private FlightTravelerEntryWidgetViewModel testVM;

	protected final String testEmptyPassport = "Passport: Country";
	private Context context = InstrumentationRegistry.getTargetContext();

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_flight_entry_widget,
		R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		entryWidget = (FlightTravelerEntryWidget) activityTestRule.getRoot();
		Ui.getApplication(context).defaultTravelerComponent();
	}

	@Test
	public void testPassportCountryIsShowing() throws Throwable {
		Db.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(context, 0, showPassportCountryObservable,
			TravelerCheckoutStatus.CLEAN);
		setViewModel(testVM);

		TravelerDetails.clickAdvanced();
		EspressoUtils.assertViewIsDisplayed(R.id.redress_number);
		EspressoUtils.assertViewIsDisplayed(R.id.passport_country_spinner);
		onView(allOf(withSpinnerText(testEmptyPassport)));
		onView(allOf(withSpinnerText(testEmptyPassport), withCompoundDrawable(R.drawable.ic_error_blue))).check(doesNotExist());
	}

	@Test
	public void testFocusValidation() throws Throwable {
		Db.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(context, 0, showPassportCountryObservable,
			TravelerCheckoutStatus.CLEAN);
		setViewModel(testVM);

		onView(withId(R.id.first_name_input)).perform(click());
		onView(withId(R.id.last_name_input)).perform(click());
		onView(withId(R.id.first_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)));

		onView(withId(R.id.edit_email_address)).perform(click());
		onView(withId(R.id.last_name_input)).check(matches(withCompoundDrawable(R.drawable.invalid)));

		onView(withId(R.id.edit_phone_number)).perform(click());
		onView(withId(R.id.edit_email_address)).check(matches(withCompoundDrawable(R.drawable.invalid)));
	}

	@Test
	public void testPassportCountryIsNotShowing() throws Throwable {
		Db.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(false);

		testVM = new FlightTravelerEntryWidgetViewModel(context, 0, showPassportCountryObservable,
			TravelerCheckoutStatus.CLEAN);

		setViewModel(testVM);

		TravelerDetails.clickAdvanced();
		EspressoUtils.assertViewIsDisplayed(R.id.redress_number);
		EspressoUtils.assertViewIsNotDisplayed(R.id.passport_country_spinner);
	}

	@Test
	public void testPointOfSaleCountryAtTopOfPassportListBelowPlaceholder() throws Throwable {
		String pointOfSaleCountry = context.getString(PointOfSale.getPointOfSale().getCountryNameResId());
		String testPointOfSalePassport = "Passport: " + pointOfSaleCountry;

		Db.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(context, 0, showPassportCountryObservable,
			TravelerCheckoutStatus.CLEAN);

		setViewModel(testVM);

		onView(withId(R.id.passport_country_spinner)).check(matches(allOf(hasDescendant(withText(testEmptyPassport)), isDisplayed())));
		onView(withId(R.id.passport_country_spinner)).perform(click());
		onData(allOf(is(instanceOf(String.class)), is(pointOfSaleCountry))).atPosition(1).check(matches(isDisplayed()));
		onData(anything()).atPosition(1).perform(click());
		onView(withId(R.id.passport_country_spinner)).check(matches(hasDescendant(withText(testPointOfSalePassport))));
	}

	private void setViewModel(final FlightTravelerEntryWidgetViewModel viewModel) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				entryWidget.setViewModel(viewModel);
			}
		});
	}
}
