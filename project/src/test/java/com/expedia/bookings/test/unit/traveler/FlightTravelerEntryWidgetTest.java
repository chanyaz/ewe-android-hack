package com.expedia.bookings.test.unit.traveler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowAlertDialog;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.TravelerCheckoutStatus;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.widget.FlightTravelerEntryWidget;
import com.expedia.testutils.RobolectricPlaygroundRule;
import com.expedia.vm.traveler.FlightTravelerEntryWidgetViewModel;

import io.reactivex.subjects.BehaviorSubject;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(RobolectricRunner.class)
public class FlightTravelerEntryWidgetTest {
	private FlightTravelerEntryWidget entryWidget;
	private FlightTravelerEntryWidgetViewModel testVM;

	@Rule
	public RobolectricPlaygroundRule activityTestRule = new RobolectricPlaygroundRule(R.layout.test_flight_entry_widget,
			R.style.V2_Theme_Packages);

	@Before
	public void setUp() {
		entryWidget = activityTestRule.findRoot();
	}

	@Test
	public void testPassportCountryIsShowing() throws Throwable {
		Db.sharedInstance.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(activityTestRule.activity,
				0,
				showPassportCountryObservable,
				TravelerCheckoutStatus.CLEAN);
		setViewModel(testVM);

		entryWidget.findViewById(R.id.traveler_advanced_options_button).callOnClick();
		assertEquals(View.VISIBLE, entryWidget.findViewById(R.id.redress_number).getVisibility());
		assertEquals(View.VISIBLE, entryWidget.findViewById(R.id.passport_country_btn).getVisibility());
	}

	@Test
	public void testFocusValidation() throws Throwable {
		Db.sharedInstance.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(activityTestRule.activity,
				0,
				showPassportCountryObservable,
				TravelerCheckoutStatus.CLEAN);
		setViewModel(testVM);

		View firstNameInput = entryWidget.findViewById(R.id.first_name_input);
		View lastNameInput = entryWidget.findViewById(R.id.last_name_input);

		firstNameInput.getOnFocusChangeListener().onFocusChange(firstNameInput, true);
		firstNameInput.getOnFocusChangeListener().onFocusChange(firstNameInput, false);
		lastNameInput.getOnFocusChangeListener().onFocusChange(lastNameInput, true);
		assertErrorViewHasText(R.id.first_name_layout_input, R.string.first_name_validation_error_message);

		View editEmailAddressInput = entryWidget.findViewById(R.id.edit_email_address);
		lastNameInput.getOnFocusChangeListener().onFocusChange(lastNameInput, false);
		editEmailAddressInput.getOnFocusChangeListener().onFocusChange(editEmailAddressInput, true);
		assertErrorViewHasText(R.id.last_name_layout_input, R.string.last_name_validation_error_message);

		editEmailAddressInput.getOnFocusChangeListener().onFocusChange(editEmailAddressInput, false);
		assertErrorViewHasText(R.id.edit_email_layout_address, R.string.email_validation_error_message);
	}

	@Test
	public void testPassportCountryIsNotShowing() throws Throwable {
		Db.sharedInstance.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(false);

		testVM = new FlightTravelerEntryWidgetViewModel(activityTestRule.activity, 0, showPassportCountryObservable,
				TravelerCheckoutStatus.CLEAN);

		setViewModel(testVM);

		entryWidget.findViewById(R.id.traveler_advanced_options_button).callOnClick();
		assertEquals(View.VISIBLE, entryWidget.findViewById(R.id.redress_number).getVisibility());
		assertEquals(View.GONE, entryWidget.findViewById(R.id.passport_country_spinner).getVisibility());
	}

	@Test
	public void testPointOfSaleCountryAtTopOfPassportListBelowPlaceholder() throws Throwable {
		String pointOfSaleCountry = activityTestRule.activity.getString(PointOfSale.getPointOfSale().getCountryNameResId());

		Db.sharedInstance.getTravelers().add(new Traveler());
		BehaviorSubject<Boolean> showPassportCountryObservable = BehaviorSubject.create();
		showPassportCountryObservable.onNext(true);

		testVM = new FlightTravelerEntryWidgetViewModel(activityTestRule.activity, 0, showPassportCountryObservable,
				TravelerCheckoutStatus.CLEAN);

		setViewModel(testVM);

		EditText passportCountryButton = entryWidget.findViewById(R.id.passport_country_btn);
		assertEquals("", passportCountryButton.getText().toString());
		passportCountryButton.callOnClick();
		ShadowAlertDialog alertDialog = Shadows.shadowOf(ShadowAlertDialog.getLatestAlertDialog());
		String firstString = (String) alertDialog.getAdapter().getItem(0);
		assertEquals(pointOfSaleCountry, firstString);
		alertDialog.clickOnItem(0);
		assertEquals(pointOfSaleCountry, passportCountryButton.getText().toString());
	}

	private void setViewModel(final FlightTravelerEntryWidgetViewModel viewModel) throws Throwable {
		entryWidget.setViewModel(viewModel);
	}

	@NonNull
	private String getString(int stringId) {
		return activityTestRule.activity.getResources().getString(stringId);
	}

	private void assertErrorViewHasText(int layoutId, int errorStringId) {
		AppCompatTextView errorView = entryWidget.findViewById(layoutId).findViewById(R.id.textinput_error);
		assertNotNull(errorView);
		Assert.assertEquals(getString(errorStringId), errorView.getText().toString());
	}
}
