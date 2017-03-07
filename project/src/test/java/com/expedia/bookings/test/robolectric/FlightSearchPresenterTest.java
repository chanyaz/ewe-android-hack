package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.flights.FlightServiceClassType;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.utils.AbacusTestUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CalendarWidgetV2;
import com.expedia.bookings.widget.FlightCabinClassPickerView;
import com.expedia.bookings.widget.FlightCabinClassWidget;
import com.expedia.bookings.widget.TravelerPickerView;
import com.expedia.bookings.widget.TravelerWidgetV2;
import com.expedia.bookings.widget.shared.SearchInputTextView;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.vm.TravelerPickerViewModel;
import com.squareup.phrase.Phrase;

import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by vsuriyal on 12/30/16.
 */
@RunWith(RobolectricRunner.class)
@Config(shadows = {ShadowGCM.class,ShadowUserManager.class})
public class FlightSearchPresenterTest {
	private FlightSearchPresenter widget;
	private FragmentActivity activity;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(FragmentActivity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);

	}

	@Test
	public void checkViewDefaultState() {
		Toolbar toolbar = (Toolbar) widget.findViewById(R.id.search_toolbar);
		assertEquals(toolbar.getVisibility(), View.VISIBLE);
		TabLayout tab = (TabLayout) widget.findViewById(R.id.tabs);
		assertEquals(tab.getVisibility(), View.VISIBLE);
		ScrollView scrollView = (ScrollView) widget.findViewById(R.id.scrollView);
		assertEquals(scrollView.getVisibility(), View.VISIBLE);
		ViewPager viewpager = (ViewPager) widget.findViewById(R.id.viewpager);
		assertEquals(viewpager.getVisibility(), View.VISIBLE);
		SearchInputTextView originCard = (SearchInputTextView) widget.findViewById(R.id.origin_card);
		assertEquals(originCard.getVisibility(), View.VISIBLE);
		SearchInputTextView destinationCard = (SearchInputTextView) widget.findViewById(R.id.destination_card);
		assertEquals(destinationCard.getVisibility(), View.VISIBLE);
		CalendarWidgetV2 calendarCard = (CalendarWidgetV2) widget.findViewById(R.id.calendar_card);
		assertEquals(calendarCard.getVisibility(), View.VISIBLE);
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		assertEquals(travelerCard.getVisibility(), View.VISIBLE);
		Button searchBtn = (Button) widget.findViewById(R.id.search_btn);
		assertEquals(searchBtn.getVisibility(), View.VISIBLE);
		RecyclerView suggestionList = (RecyclerView) widget.findViewById(R.id.suggestion_list);
		assertEquals(suggestionList.getVisibility(), View.GONE);
		testFlightCabinClassWidgetVisibility();
	}

	@Test
	public void testWidgetsDefaultText() {
		ViewPager viewpager = (ViewPager) widget.findViewById(R.id.viewpager);
		String tabText = viewpager.getAdapter().getPageTitle(0).toString();
		assertEquals(tabText, activity.getResources().getString(R.string.flights_round_trip_label));
		tabText = viewpager.getAdapter().getPageTitle(1).toString();
		assertEquals(tabText, activity.getResources().getString(R.string.flights_one_way_label));
		SearchInputTextView originCard = (SearchInputTextView) widget.findViewById(R.id.origin_card);
		assertEquals(originCard.getText().toString(), activity.getResources().getString(R.string.fly_from_hint));
		SearchInputTextView destinationCard = (SearchInputTextView) widget.findViewById(R.id.destination_card);
		assertEquals(destinationCard.getText().toString(), activity.getResources().getString(R.string.fly_to_hint));
		CalendarWidgetV2 calendarCard = (CalendarWidgetV2) widget.findViewById(R.id.calendar_card);
		assertEquals(calendarCard.getText().toString(), activity.getResources().getString(R.string.select_dates));
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		assertEquals(travelerCard.getText().toString(),
			activity.getResources().getString(R.string.package_search_traveler_default_text));
		Button searchBtn = (Button) widget.findViewById(R.id.search_btn);
		assertEquals(searchBtn.getText().toString(), activity.getResources().getString(R.string.search));

		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass);

		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);

		ViewStub flightCabinClassStub = (ViewStub) widget.findViewById(R.id.flight_cabin_class_stub);
		FlightCabinClassWidget flightCabinClassWidget = (FlightCabinClassWidget) flightCabinClassStub.inflate();
		assertEquals(activity.getResources().getString(FlightServiceClassType.CabinCode.COACH.getResId()), flightCabinClassWidget.getText());
	}


	@Test
	public void testRoundTrip() {
		CalendarWidgetV2 calendarCard = (CalendarWidgetV2) widget.findViewById(R.id.calendar_card);
		Ui.getApplication(activity).defaultTravelerComponent();
		FlightSearchViewModel flightSearchViewModel = new FlightSearchViewModel(activity);
		calendarCard.setViewModel(flightSearchViewModel);

		flightSearchViewModel.isRoundTripSearchObservable().onNext(true);
		assertEquals(activity.getResources().getString(R.string.select_dates), calendarCard.getText().toString());

		flightSearchViewModel.isRoundTripSearchObservable().onNext(false);
		assertEquals(activity.getResources().getString(R.string.select_departure_date),
			calendarCard.getText().toString());
	}

	@Test
	public void testTravelerDialogForInfantErrorInLap() {
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		TravelerPickerViewModel vm = new TravelerPickerViewModel(activity);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		TravelerPickerView travelerPicker = (TravelerPickerView) view
			.findViewById(R.id.traveler_view);

		travelerPicker.getAdultPlus().performClick();
		travelerPicker.getChildPlus().performClick();
		travelerPicker.getChildPlus().performClick();

		vm.setShowSeatingPreference(true);
		travelerPicker.getChild1().setSelection(0);
		travelerPicker.getChild2().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getChildPlus().performClick();

		travelerPicker.getChild3().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getChild3().setSelection(2);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getChild3().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

	}

	@Test
	public void testTravelerDialogForInfantErrorInSeat() {
		TestSubscriber tooManyInfantsInLapTestSubscriber = new TestSubscriber<>();
		TestSubscriber tooManyInfantsInSeatTestSubscriber = new TestSubscriber<>();
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		TravelerPickerViewModel vm = new TravelerPickerViewModel(activity);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		TravelerPickerView travelerPicker = (TravelerPickerView) view
			.findViewById(R.id.traveler_view);
		travelerPicker.getViewmodel().getTooManyInfantsInLap().subscribe(tooManyInfantsInLapTestSubscriber);
		travelerPicker.getViewmodel().getTooManyInfantsInSeat().subscribe(tooManyInfantsInSeatTestSubscriber);

		travelerPicker.getChildPlus().performClick();
		travelerPicker.getChildPlus().performClick();

		vm.setShowSeatingPreference(true);
		travelerPicker.getChild1().setSelection(0);
		travelerPicker.getChild2().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		int noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());


		travelerPicker.getChildPlus().performClick();

		travelerPicker.getChild3().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), true);

		travelerPicker.getChildPlus().performClick();
		travelerPicker.getChild4().setSelection(15);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
	}

	@Test
	public void testTravelDialogForChildAgeAfterDismiss() {
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		TravelerPickerView travelerPicker = (TravelerPickerView) view
			.findViewById(R.id.traveler_view);

		travelerPicker.getChildPlus().performClick();

		travelerPicker.getChild1().setSelection(2);
		travelerCard.getTravelerDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
		travelerCard.performClick();
		assertEquals(2, travelerPicker.getChild1().getSelectedItemPosition());

		travelerPicker.getChild1().setSelection(3);
		travelerCard.getTravelerDialog().dismiss();

		travelerCard.performClick();
		assertEquals(2, travelerPicker.getChild1().getSelectedItemPosition());
	}

	private void testFlightCabinClassWidgetVisibility() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass);

		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter, null);

		ViewStub flightCabinClassStub = (ViewStub) widget.findViewById(R.id.flight_cabin_class_stub);
		FlightCabinClassWidget flightCabinClassWidget = (FlightCabinClassWidget) flightCabinClassStub.inflate();
		assertEquals(flightCabinClassWidget.getVisibility(), View.VISIBLE);

		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppFlightPremiumClass, AbacusUtils.DefaultVariant.CONTROL.ordinal());
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);

		flightCabinClassStub = (ViewStub) widget.findViewById(R.id.flight_cabin_class_stub);
		assertNotEquals(flightCabinClassStub.getVisibility(), View.VISIBLE);
	}

	@Test
	public void testFlightCabinClassValue() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightPremiumClass);
		String cabinClassCoachName =  activity.getResources().getString(FlightServiceClassType.CabinCode.COACH.getResId());
		String cabinClassBusinessName = activity.getResources().getString(FlightServiceClassType.CabinCode.BUSINESS.getResId());

		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);

		FlightCabinClassWidget flightCabinClassWidget = widget.getFlightCabinClassWidget();
		assertEquals(cabinClassCoachName, flightCabinClassWidget.getText());

		flightCabinClassWidget.performClick();
		View view = flightCabinClassWidget.getFlightCabinClassDialogView();

		FlightCabinClassPickerView flightCabinClassPickerView = (FlightCabinClassPickerView) view.findViewById(R.id.flight_class_view);

		assertEquals(flightCabinClassPickerView.getEconomyClassRadioButton().getId(), flightCabinClassPickerView.getRadioGroup().getCheckedRadioButtonId());

		flightCabinClassPickerView.getBusinessClassRadioButton().performClick();

		flightCabinClassWidget.getDialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick();
		assertEquals(cabinClassBusinessName, flightCabinClassWidget.getText());
		assertEquals(getCabinClassContentDescription(cabinClassBusinessName), flightCabinClassWidget.getContentDescription());

		flightCabinClassWidget.performClick();
		assertEquals(flightCabinClassPickerView.getBusinessClassRadioButton().getId(), flightCabinClassPickerView.getRadioGroup().getCheckedRadioButtonId());

		flightCabinClassPickerView.getEconomyClassRadioButton().performClick();
		flightCabinClassWidget.getDialog().dismiss();
		assertEquals(cabinClassBusinessName, flightCabinClassWidget.getText());
		assertEquals(getCabinClassContentDescription(cabinClassBusinessName), flightCabinClassWidget.getContentDescription());

		flightCabinClassWidget.performClick();
		assertEquals(flightCabinClassPickerView.getBusinessClassRadioButton().getId(), flightCabinClassPickerView.getRadioGroup().getCheckedRadioButtonId());
	}

	private String getCabinClassContentDescription(String cabinClassName) {
		return Phrase.from(activity.getResources().getString(R.string.select_preferred_flight_class_cont_desc_TEMPLATE)).
			put("seatingclass", cabinClassName).format().toString();
	}

	@Test
	public void testOriginFlightContentDescription() {
		// When flight is not selected
		SearchInputTextView originFlight = widget.getOriginCardView();
		assertEquals(originFlight.getContentDescription(), "Flying from. Button");

		// When flight is selected
		Ui.getApplication(activity).defaultTravelerComponent();
		widget.setSearchViewModel(new FlightSearchViewModel(activity));
		widget.getSearchViewModel().getFormattedOriginObservable().onNext("San Francisco");
		assertEquals(originFlight.getContentDescription(), "Flying from. Button. San Francisco");
	}

	@Test
	public void testDestinationFlightContentDescription() {
		// When flight is not selected
		SearchInputTextView destinationFlight = widget.getDestinationCardView();
		assertEquals(destinationFlight.getContentDescription(), "Flying to. Button");

		// When flight is selected
		Ui.getApplication(activity).defaultTravelerComponent();
		// TODO calendarWidgetV2.showCalendarDialog() in FlightSearchPresenter is failing in robolectric.
		// Adding this workaround to not show dialog in this test till we figure out the reason for failure.
		FlightSearchViewModel flightSearchViewModel = new FlightSearchViewModel(activity);
		flightSearchViewModel.datesUpdated(LocalDate.now(), LocalDate.now());
		widget.setSearchViewModel(flightSearchViewModel);
		widget.getSearchViewModel().getFormattedDestinationObservable().onNext("San Francisco");
		assertEquals(destinationFlight.getContentDescription(), "Flying to. Button. San Francisco");
	}
}
