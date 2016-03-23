package com.expedia.bookings.test.phone.traveler;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.test.espresso.EspressoUser;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.rules.PlaygroundRule;
import com.expedia.bookings.widget.FlightTravelerEntryWidget;
import com.expedia.vm.traveler.TravelerViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FlightTravelerEntryWidgetTest {
	private FlightTravelerEntryWidget entryWidget;
	private TravelerViewModel testVM;

	@Rule
	public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

	@Rule
	public PlaygroundRule activityTestRule = new PlaygroundRule(R.layout.test_flight_entry_widget,
		R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		entryWidget = (FlightTravelerEntryWidget) activityTestRule.getRoot();
	}

	@Test
	public void testPassportCountryIsShowing() throws Throwable {
		Db.getTravelers().add(new Traveler());
		testVM = new TravelerViewModel(InstrumentationRegistry.getTargetContext(), 0);
		testVM.getShowPassportCountryObservable().onNext(true);
		setViewModel(testVM);

		EspressoUser.clickOnView(R.id.advanced_options_button);
		EspressoUtils.assertViewIsDisplayed(R.id.redress_number);
		EspressoUtils.assertViewIsDisplayed(R.id.passport_country_spinner);
	}

	@Test
	public void testPassportCountryIsNotShowing() throws Throwable {
		Db.getTravelers().add(new Traveler());
		testVM = new TravelerViewModel(InstrumentationRegistry.getTargetContext(), 0);
		testVM.getShowPassportCountryObservable().onNext(false);
		setViewModel(testVM);

		EspressoUser.clickOnView(R.id.advanced_options_button);
		EspressoUtils.assertViewIsDisplayed(R.id.redress_number);
		EspressoUtils.assertViewIsNotDisplayed(R.id.passport_country_spinner);
	}

	private void setViewModel(final TravelerViewModel viewModel) throws Throwable {
		uiThreadTestRule.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				entryWidget.setViewModel(viewModel);
			}
		});
	}
}
