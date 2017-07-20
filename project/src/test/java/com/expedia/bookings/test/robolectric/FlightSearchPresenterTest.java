package com.expedia.bookings.test.robolectric;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.RoboLayoutInflater;

import android.content.Context;
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
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.flights.FlightServiceClassType;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.utils.AbacusTestUtils;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CalendarWidgetV2;
import com.expedia.bookings.widget.FlightAdvanceSearchWidget;
import com.expedia.bookings.widget.FlightCabinClassPickerView;
import com.expedia.bookings.widget.FlightCabinClassWidget;
import com.expedia.bookings.widget.FlightTravelerPickerView;
import com.expedia.bookings.widget.FlightTravelerWidgetV2;
import com.expedia.bookings.widget.TravelerPickerView;
import com.expedia.bookings.widget.TravelerWidgetV2;
import com.expedia.bookings.widget.shared.SearchInputTextView;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.vm.TravelerPickerViewModel;
import com.expedia.vm.flights.FlightAdvanceSearchViewModel;
import com.squareup.phrase.Phrase;

import kotlin.Unit;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricRunner.class)
@Config(shadows = {ShadowGCM.class, ShadowUserManager.class})
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
		AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightAdvanceSearch);
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
		AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightAdvanceSearch);
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

	@Test
	public void testRevampFlightTravelerDialogForInfantErrorInLap() {
		setUpFlightTravelerRevamp(true);
		FlightTravelerWidgetV2 travelerCard = (FlightTravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		FlightTravelerPickerView travelerPicker = (FlightTravelerPickerView) view
			.findViewById(R.id.flight_traveler_view);

		travelerPicker.getAdultCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();

		travelerPicker.getViewModel().setShowSeatingPreference(true);
		travelerPicker.getInfantInLap().setChecked(true);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantInLap().setChecked(true);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getInfantCountSelector().getTravelerMinus().performClick();
		travelerPicker.getChildCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantInLap().setChecked(true);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getChildCountSelector().getTravelerMinus().performClick();
		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantInSeat().setChecked(true);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());
		setUpFlightTravelerRevamp(false);
	}

	public void testRevampFlightTravelerDialogForInfantErrorInSeat() {

		setUpFlightTravelerRevamp(true);
		TestSubscriber tooManyInfantsInLapTestSubscriber = new TestSubscriber<>();
		TestSubscriber tooManyInfantsInSeatTestSubscriber = new TestSubscriber<>();
		FlightTravelerWidgetV2 travelerCard = (FlightTravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		FlightTravelerPickerView travelerPicker = (FlightTravelerPickerView) view
			.findViewById(R.id.flight_traveler_view);
		travelerPicker.getViewmodel().getTooManyInfantsInLap().subscribe(tooManyInfantsInLapTestSubscriber);
		travelerPicker.getViewmodel().getTooManyInfantsInSeat().subscribe(tooManyInfantsInSeatTestSubscriber);

		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();

		travelerPicker.getViewmodel().setShowSeatingPreference(true);
		travelerPicker.getInfantInSeat().setChecked(true);
		int noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();

		travelerPicker.getInfantInSeat().setChecked(true);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), true);

		travelerPicker.getYouthCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantInSeat().setChecked(true);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.getOnNextEvents().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.getOnNextEvents().get(noOfEvents - 1), false);
		setUpFlightTravelerRevamp(false);
	}

	@Test
	public void testRevampFlightTravelDialogForChildAgeAfterDismiss() {
		setUpFlightTravelerRevamp(true);

		FlightTravelerWidgetV2 travelerCard = (FlightTravelerWidgetV2) widget.getTravelerWidgetV2();
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		FlightTravelerPickerView travelerPicker = (FlightTravelerPickerView) view
			.findViewById(R.id.flight_traveler_view);
		travelerPicker.getChildCountSelector().getTravelerPlus().performClick();
		travelerPicker.getChildCountSelector().getTravelerPlus().performClick();

		assertEquals("[10, 10]" , travelerPicker.getViewmodel().getTravelerParamsObservable().getValue().getChildrenAges().toString());

		travelerCard.getTravelerDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
		travelerCard.performClick();

		assertEquals("[10, 10]" , travelerPicker.getViewmodel().getTravelerParamsObservable().getValue().getChildrenAges().toString());
		travelerPicker.getChildCountSelector().getTravelerPlus().performClick();
		travelerCard.getTravelerDialog().dismiss();

		travelerCard.performClick();
		assertEquals("[10, 10]" , travelerPicker.getViewmodel().getTravelerParamsObservable().getValue().getChildrenAges().toString());
		setUpFlightTravelerRevamp(false);
	}


	private void testFlightCabinClassWidgetVisibility() {
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter, null);

		ViewStub flightCabinClassStub = (ViewStub) widget.findViewById(R.id.flight_cabin_class_stub);
		FlightCabinClassWidget flightCabinClassWidget = (FlightCabinClassWidget) flightCabinClassStub.inflate();
		assertEquals(flightCabinClassWidget.getVisibility(), View.VISIBLE);
	}

	@Test
	public void testFlightCabinClassValue() {
		String cabinClassCoachName = activity.getResources().getString(FlightServiceClassType.CabinCode.COACH.getResId());
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

	@Test
	public void testFlightAdvanceSearchWidget() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightAdvanceSearch);
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);
		FlightAdvanceSearchWidget flightAdvanceSearchWidget = widget.getFlightAdvanceSearchWidget();
		FlightAdvanceSearchViewModel vm = new FlightAdvanceSearchViewModel();
		flightAdvanceSearchWidget.setViewModel(vm);

		assertEquals(View.GONE, flightAdvanceSearchWidget.getExpandedAdvanceSearchView().getVisibility());
		assertEquals(View.VISIBLE, flightAdvanceSearchWidget.getCollapsedAdvanceSearchView().getVisibility());

		flightAdvanceSearchWidget.getCollapsedAdvanceSearchView().performClick();
		assertEquals(View.VISIBLE, flightAdvanceSearchWidget.getExpandedAdvanceSearchView().getVisibility());
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

	@Test
	public void testSearchButtonState() {
		AccessibilityManager mockAccessibilityManager = Mockito.mock(AccessibilityManager.class);
		Context spyContext = Mockito.spy(activity);
		Mockito.when(spyContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).thenReturn(mockAccessibilityManager);
		Mockito.when(mockAccessibilityManager.isEnabled()).thenReturn(true);
		Mockito.when(mockAccessibilityManager.isTouchExplorationEnabled()).thenReturn(true);
		assertTrue(AccessibilityUtil.isTalkBackEnabled(spyContext));

		widget = (FlightSearchPresenter) new RoboLayoutInflater(spyContext).inflate(R.layout.test_flight_search_presenter,
			null);
		Button searchBtn = (Button) widget.findViewById(R.id.search_btn);
		assertFalse(searchBtn.isEnabled());
	}

	@Test
	public void testGetSearchViewModel() {
		Ui.getApplication(activity).defaultTravelerComponent();
		FlightSearchViewModel flightSearchViewModel = new FlightSearchViewModel(activity);
		widget.setSearchViewModel(flightSearchViewModel);

		assertNotNull(widget.getSearchViewModel());
	}

	@Test
	public void testTabsOneWayTripTab() {
		initializeWidget();
		selectRoundTripTabAtIndex(1);
		TestSubscriber<Boolean> isRoundTripSearchSubscriber = new TestSubscriber<>();
		widget.getSearchViewModel().isRoundTripSearchObservable().subscribe(isRoundTripSearchSubscriber);

		isRoundTripSearchSubscriber.assertValue(false);
	}

	@Test
	public void testRoundTripTabs() {
		initializeWidget();
		selectRoundTripTabAtIndex(0);
		TestSubscriber<Boolean> isRoundTripSearchSubscriber = new TestSubscriber<>();
		widget.getSearchViewModel().isRoundTripSearchObservable().subscribe(isRoundTripSearchSubscriber);

		isRoundTripSearchSubscriber.assertValue(true);
	}

	@Test
	public void testSwapToFromButtonWhenDisabled() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightSwitchFields);
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter, null);
		ImageView swapBtn = (ImageView) widget.findViewById(R.id.swapFlightsLocationsButton);

		assertEquals(swapBtn.getVisibility(), View.VISIBLE);
		assertFalse(swapBtn.isEnabled());
		initializeWidget();

		SuggestionV4 origin = getSuggestion("SFO", "San Francisco");
		SuggestionV4 destination = getSuggestion("DEL", "Delhi");
		widget.getSearchViewModel().getOriginLocationObserver().onNext(origin);
		assertFalse(swapBtn.isEnabled());

		widget.getSearchViewModel().getDestinationLocationObserver().onNext(destination);
		assertTrue(swapBtn.isEnabled());
	}

	@Test
	public void testSwapToFromButton() {
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightSwitchFields);
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter, null);
		ImageView swapBtn = (ImageView) widget.findViewById(R.id.swapFlightsLocationsButton);
		SearchInputTextView originFlight = widget.getOriginCardView();
		SearchInputTextView destinationFlight = widget.getDestinationCardView();
		initializeWidget();

		SuggestionV4 origin = getSuggestion("SFO", "San Francisco");
		SuggestionV4 destination = getSuggestion("DEL", "Delhi");

		widget.getSearchViewModel().getOriginLocationObserver().onNext(origin);
		widget.getSearchViewModel().getDestinationLocationObserver().onNext(destination);

		swapBtn.performClick();
		assertEquals(originFlight.getText(), destination.regionNames.displayName);
		assertEquals(destinationFlight.getText(), origin.regionNames.displayName);

		swapBtn.performClick();
		assertEquals(destinationFlight.getText(), destination.regionNames.displayName);
		assertEquals(originFlight.getText(), origin.regionNames.displayName);
	}

	private SuggestionV4 getSuggestion(String airportCode, String displayName) {
		SuggestionV4 suggestion = new SuggestionV4();
		SuggestionV4.Airport suggestionAirport = new SuggestionV4.Airport();
		suggestionAirport.airportCode = airportCode;
		suggestion.hierarchyInfo = new SuggestionV4.HierarchyInfo();
		suggestion.hierarchyInfo.airport = suggestionAirport;
		suggestion.regionNames = new SuggestionV4.RegionNames();
		suggestion.regionNames.displayName = displayName;
		return suggestion;
	}

	@Test
	public void testSearchValidationStepByStep() {
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		TestSubscriber<Unit> errorNoDestinationTestSubscriber = new TestSubscriber();
		TestSubscriber<Unit> errorNoOriginObservableTestSubscriber = new TestSubscriber();
		TestSubscriber<Unit> errorNoDatesObservableTestSubscriber = new TestSubscriber();

		vm.getErrorNoDestinationObservable().subscribe(errorNoDestinationTestSubscriber);
		vm.getErrorNoOriginObservable().subscribe(errorNoOriginObservableTestSubscriber);
		vm.getErrorNoDatesObservable().subscribe(errorNoDatesObservableTestSubscriber);

		vm.getPerformSearchObserver().onNext(Unit.INSTANCE);
		errorNoOriginObservableTestSubscriber.assertValueCount(1);
		errorNoDestinationTestSubscriber.assertValueCount(0);
		errorNoDatesObservableTestSubscriber.assertValueCount(0);

		assertNull(widget.getDestinationCardView().getCompoundDrawables()[2]);
		assertNull(widget.getOriginCardView().getCompoundDrawables()[2]);
		assertNull(widget.getCalendarWidgetV2().getCompoundDrawables()[2]);
	}

	@Test
	public void testSearchValidationConcurrent() {
		RoboTestHelper.INSTANCE.bucketTests(AbacusUtils.EBAndroidAppFlightSearchFormValidation);
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		TestSubscriber<Unit> errorNoDestinationTestSubscriber = new TestSubscriber();
		TestSubscriber<Unit> errorNoOriginObservableTestSubscriber = new TestSubscriber();
		TestSubscriber<Unit> errorNoDatesObservableTestSubscriber = new TestSubscriber();

		vm.getErrorNoDestinationObservable().subscribe(errorNoDestinationTestSubscriber);
		vm.getErrorNoOriginObservable().subscribe(errorNoOriginObservableTestSubscriber);
		vm.getErrorNoDatesObservable().subscribe(errorNoDatesObservableTestSubscriber);

		vm.getPerformSearchObserver().onNext(Unit.INSTANCE);
		errorNoOriginObservableTestSubscriber.assertValueCount(1);
		errorNoDestinationTestSubscriber.assertValueCount(1);
		errorNoDatesObservableTestSubscriber.assertValueCount(1);

		assertNotNull(widget.getDestinationCardView().getCompoundDrawablesRelative()[2]);
		assertNotNull(widget.getOriginCardView().getCompoundDrawablesRelative()[2]);
		assertNotNull(widget.getCalendarWidgetV2().getCompoundDrawablesRelative()[2]);
	}

	@Test
	public void testOriginDestinationPlaceholders() {
		assertEquals(activity.getString(R.string.fly_from_hint), widget.getOriginSearchBoxPlaceholderText());
		assertEquals(activity.getString(R.string.fly_to_hint), widget.getDestinationSearchBoxPlaceholderText());
	}

	@Test
	public void testCalendarTooltipContentDescriptionForRoundtrip() {
		TestSubscriber<String> toolTipContDescTestSubscriber = new TestSubscriber<>();
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		vm.getCalendarTooltipContDescObservable().subscribe(toolTipContDescTestSubscriber);

		//When dates are not selected
		vm.datesUpdated(null, null);
		assertEquals("Select dates", toolTipContDescTestSubscriber.getOnNextEvents().get(0));

		//When start date is selected
		LocalDate dateNow = LocalDate.now();
		vm.datesUpdated(dateNow, null);
		assertEquals(getExpectedToolTipContDesc(dateNow, null),
			toolTipContDescTestSubscriber.getOnNextEvents().get(1));

		//When start and end date is selected
		vm.datesUpdated(dateNow, dateNow.plusDays(3));
		assertEquals(getExpectedToolTipContDesc(dateNow, dateNow.plusDays(3)),
			toolTipContDescTestSubscriber.getOnNextEvents().get(2));
	}

	@Test
	public void testCalendarTooltipContentDescriptionForOneWay() {
		TestSubscriber<String> toolTipContDescTestSubscriber = new TestSubscriber<>();
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		vm.getCalendarTooltipContDescObservable().subscribe(toolTipContDescTestSubscriber);
		vm.isRoundTripSearchObservable().onNext(false);

		//When dates are not selected
		vm.datesUpdated(null, null);
		assertEquals("Select dates", toolTipContDescTestSubscriber.getOnNextEvents().get(0));

		//When start date is selected
		LocalDate dateNow = LocalDate.now();
		vm.datesUpdated(dateNow, null);
		assertEquals(DateUtils.localDateToMMMd(dateNow) + ". Select dates again to modify",
			toolTipContDescTestSubscriber.getOnNextEvents().get(1));
	}

	private String getExpectedToolTipContDesc(LocalDate startDate, LocalDate endDate) {
		return (endDate == null) ? DateUtils.localDateToMMMd(startDate) + ". Next: Select return date" :
			DateUtils.localDateToMMMd(startDate)
				+ " to " + DateUtils.localDateToMMMd(startDate.plusDays(3)) + ". Select dates again to modify";
	}

	private void initializeWidget() {
		Ui.getApplication(activity).defaultTravelerComponent();
		FlightSearchViewModel flightSearchViewModel = new FlightSearchViewModel(activity);
		widget.setSearchViewModel(flightSearchViewModel);
	}

	private void selectRoundTripTabAtIndex(int index) {
		TabLayout.Tab tab = widget.getTabs().getTabAt(index);
		tab.select();
	}

	private void setUpFlightTravelerRevamp(boolean isUserBucketed) {
		if (isUserBucketed) {
			AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp);
		}
		else {
			AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp);
		}
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);
	}
}
