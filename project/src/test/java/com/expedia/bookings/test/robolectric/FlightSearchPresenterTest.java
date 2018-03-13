package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.flights.FlightSearchParams;
import com.expedia.bookings.data.flights.FlightServiceClassType;
import com.expedia.bookings.data.flights.RecentSearch;
import com.expedia.bookings.data.flights.RecentSearchDAO;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.services.TestObserver;
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM;
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager;
import com.expedia.bookings.utils.AbacusTestUtils;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CalendarWidgetV2;
import com.expedia.bookings.widget.FlightAdvanceSearchWidget;
import com.expedia.bookings.widget.FlightCabinClassPickerView;
import com.expedia.bookings.widget.FlightCabinClassWidget;
import com.expedia.bookings.widget.FlightTravelerPickerView;
import com.expedia.bookings.widget.FlightTravelerWidgetV2;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TravelerPickerView;
import com.expedia.bookings.widget.TravelerWidgetV2;
import com.expedia.bookings.widget.flights.DateFormatterTextView;
import com.expedia.bookings.widget.flights.RecentSearchWidgetContainer;
import com.expedia.bookings.widget.shared.SearchInputTextView;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.vm.TravelerPickerViewModel;
import com.expedia.vm.flights.FlightAdvanceSearchViewModel;
import com.squareup.phrase.Phrase;

import io.reactivex.Flowable;
import kotlin.Unit;

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
		NestedScrollView scrollView = (NestedScrollView) widget.findViewById(R.id.scrollView);
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
		TestObserver tooManyInfantsInLapTestSubscriber = new TestObserver<>();
		TestObserver tooManyInfantsInSeatTestSubscriber = new TestObserver<>();
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
		int noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());


		travelerPicker.getChildPlus().performClick();

		travelerPicker.getChild3().setSelection(0);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), true);

		travelerPicker.getChildPlus().performClick();
		travelerPicker.getChild4().setSelection(15);
		travelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), false);
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
		TestObserver tooManyInfantsInLapTestSubscriber = new TestObserver<>();
		TestObserver tooManyInfantsInSeatTestSubscriber = new TestObserver<>();
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
		int noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());

		travelerPicker.getInfantCountSelector().getTravelerPlus().performClick();

		travelerPicker.getInfantInSeat().setChecked(true);
		assertEquals(View.VISIBLE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), true);

		travelerPicker.getYouthCountSelector().getTravelerPlus().performClick();
		travelerPicker.getInfantInSeat().setChecked(true);
		assertEquals(View.GONE, travelerPicker.getInfantError().getVisibility());
		noOfEvents = tooManyInfantsInLapTestSubscriber.values().size();
		assertEquals(tooManyInfantsInLapTestSubscriber.values().get(noOfEvents - 1), false);
		assertEquals(tooManyInfantsInSeatTestSubscriber.values().get(noOfEvents - 1), false);
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
		TestObserver<Boolean> isRoundTripSearchSubscriber = new TestObserver<>();
		widget.getSearchViewModel().isRoundTripSearchObservable().subscribe(isRoundTripSearchSubscriber);

		isRoundTripSearchSubscriber.assertValue(false);
	}

	@Test
	public void testRoundTripTabs() {
		initializeWidget();
		selectRoundTripTabAtIndex(0);
		TestObserver<Boolean> isRoundTripSearchSubscriber = new TestObserver<>();
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
	public void testSearchValidationConcurrent() {
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		TestObserver<Unit> errorNoDestinationTestSubscriber = new TestObserver();
		TestObserver<Unit> errorNoOriginObservableTestSubscriber = new TestObserver();
		TestObserver<Unit> errorNoDatesObservableTestSubscriber = new TestObserver();

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
	public void testFLightGreedyCallTriggeredForRoundTip() {
		TestObserver<FlightSearchParams> greedyParamsTestSubscriber = new TestObserver<>();
		setUpForFlightGreedySearch();
		initializeWidget();
		widget.getSearchViewModel().getGreedySearchParamsObservable().subscribe(greedyParamsTestSubscriber);

		widget.getSearchViewModel().getOriginLocationObserver().onNext(getSuggestion("SFO", "San Francisco"));
		widget.getSearchViewModel().getDestinationLocationObserver().onNext(getSuggestion("DEL", "Delhi"));
		LocalDate dateNow = LocalDate.now();
		widget.getSearchViewModel().datesUpdated(dateNow, dateNow.plusDays(3));
		widget.getSearchViewModel().getDateSetObservable().onNext(Unit.INSTANCE);
		greedyParamsTestSubscriber.assertValueCount(1);
	}

	@Test
	public void testFlightGreedyCallAbortOnTravelerChange() {
		TestObserver<Unit> cancelGreedyCallTestSubscriber = new TestObserver<>();
		setUpForFlightGreedySearch();
		initializeWidget();
		widget.getSearchViewModel().getCancelGreedyCallObservable().subscribe(cancelGreedyCallTestSubscriber);

		//Dont abort greedy call when no traveler is changed
		TravelerWidgetV2 travelerCard = widget.getTravelerWidgetV2();
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		travelerCard.getTravelerDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();
		cancelGreedyCallTestSubscriber.assertValueCount(0);

		//Abort greedy call when traveler is changed
		travelerCard.performClick();
		TravelerPickerView travelerPicker = view.findViewById(R.id.traveler_view);
		travelerPicker.getAdultPlus().performClick();
		travelerCard.getTravelerDialog().getButton(DialogInterface.BUTTON_POSITIVE).performClick();

		cancelGreedyCallTestSubscriber.assertValueCount(1);
	}

	@Test
	public void testCalendarTooltipContentDescriptionForRoundtrip() {
		TestObserver<String> toolTipContDescTestSubscriber = new TestObserver<>();
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		vm.getCalendarTooltipContDescObservable().subscribe(toolTipContDescTestSubscriber);

		//When dates are not selected
		vm.datesUpdated(null, null);
		assertEquals("Select dates", toolTipContDescTestSubscriber.values().get(0));

		//When start date is selected
		LocalDate dateNow = LocalDate.now();
		vm.datesUpdated(dateNow, null);
		assertEquals(getExpectedToolTipContDesc(dateNow, null),
			toolTipContDescTestSubscriber.values().get(1));

		//When start and end date is selected
		vm.datesUpdated(dateNow, dateNow.plusDays(3));
		assertEquals(getExpectedToolTipContDesc(dateNow, dateNow.plusDays(3)),
			toolTipContDescTestSubscriber.values().get(2));
	}

	@Test
	public void testCalendarTooltipContentDescriptionForOneWay() {
		TestObserver<String> toolTipContDescTestSubscriber = new TestObserver<>();
		initializeWidget();
		FlightSearchViewModel vm = widget.getSearchViewModel();
		vm.getCalendarTooltipContDescObservable().subscribe(toolTipContDescTestSubscriber);
		vm.isRoundTripSearchObservable().onNext(false);

		//When dates are not selected
		vm.datesUpdated(null, null);
		assertEquals("Select dates", toolTipContDescTestSubscriber.values().get(0));

		//When start date is selected
		LocalDate dateNow = LocalDate.now();
		vm.datesUpdated(dateNow, null);
		assertEquals(LocaleBasedDateFormatUtils.localDateToMMMd(dateNow) + ". Select dates again to modify",
			toolTipContDescTestSubscriber.values().get(1));
	}

	@Test
	public void testRoundTripTabContentDescription() {
		initializeWidget();
		TabLayout.Tab tab = widget.getTabs().getTabAt(0);
		tab.select();
		assertEquals("Roundtrip tab", tab.getContentDescription());
	}

	@Test
	public void testOnewayTabContentDescription() {
		initializeWidget();
		TabLayout.Tab tab = widget.getTabs().getTabAt(1);
		tab.select();
		assertEquals("One way tab", tab.getContentDescription());
	}

	@Test
	public void testRecentSearchWidget() {
		AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRecentSearch);
		AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRecentSearch, 1);
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		TextView recentSearchHeader = recentSearchWidgetContainer.findViewById(R.id.recent_search_widget_header);
		ImageView recentSearchIcon = (ImageView) recentSearchWidgetContainer.findViewById(R.id.recent_search_icon);
		ImageView recentSearchChevron = (ImageView) recentSearchWidgetContainer.findViewById(R.id.recent_search_header_chevron);

		assertEquals(View.VISIBLE , recentSearchWidgetContainer.getVisibility());
		assertEquals("Recent Searches", recentSearchHeader.getText().toString());
		assertEquals(View.VISIBLE ,recentSearchIcon.getVisibility());
		assertEquals(View.VISIBLE ,recentSearchChevron.getVisibility());

	}

	@Test
	public void testRecentSearchWidgetItem() {
		setupRecentSearch();
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();

		RecyclerView recentSearchWidgetItems = recentSearchWidgetContainer.getRecyclerView();

		ArrayList<RecentSearch> recentSearches = new ArrayList<RecentSearch>();
		RecentSearch recentSearch =  new RecentSearch("SFO", "LAS", "{\"coordinates\"}".getBytes(),
			"{\"coordinates\"}".getBytes(),"2018-05-10", "2018-05-31", "COACH",
			1520490226L, 668, "USD", 1, "10,12",
			false, true);

		recentSearches.add(recentSearch);

		recentSearchWidgetContainer.getViewModel().getRecentSearchesObservable().onNext(recentSearches);
		recentSearchWidgetItems.measure(0, 0);
		recentSearchWidgetItems.layout(0, 0, 100, 10000);

		View firstItem = recentSearchWidgetItems.getChildAt(0);

		TextView sourceLocation = firstItem.findViewById(R.id.recent_search_origin);
		TextView destinationLocation = firstItem.findViewById(R.id.recent_search_destination);
		TextView price = firstItem.findViewById(R.id.recent_search_price);
		DateFormatterTextView dateRange = firstItem.findViewById(R.id.recent_search_date);
		TextView priceSubtitle = firstItem.findViewById(R.id.recent_search_price_subtitle);
		TextView travelerCount = firstItem.findViewById(R.id.recent_search_traveler_count);
		TextView flightClass = firstItem.findViewById(R.id.recent_search_class);

		assertEquals("SFO", sourceLocation.getText().toString());
		assertEquals("LAS", destinationLocation.getText().toString());
		assertEquals("$668", price.getText().toString());
		assertEquals("May 10  -  May 31", dateRange.getText().toString());
		assertEquals("as of Jan 18", priceSubtitle.getText().toString());
		assertEquals("3", travelerCount.getText().toString());
		assertEquals("Economy", flightClass.getText().toString());
	}

	@Test
	public void testRecentWidgetVisibilityWhenZeroItems() {
		setupRecentSearch();
		ArrayList<RecentSearch> recentSearches = new ArrayList<RecentSearch>();
		assertEquals(View.VISIBLE ,widget.findViewById(R.id.flight_recent_searches_widget).getVisibility());

		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		LinearLayout recentSearchWidget = recentSearchWidgetContainer.findViewById(R.id.recent_search_widget);

		recentSearchWidgetContainer.getViewModel().getFetchRecentSearchesObservable().onNext(Unit.INSTANCE);
		assertEquals(View.GONE ,recentSearchWidget.getVisibility());
	}

	@Test
	public void testRecentWidgetWhenOneItem() {
		setupRecentSearch();
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		LinearLayout recentSearchWidget = recentSearchWidgetContainer.findViewById(R.id.recent_search_widget);
		recentSearchWidgetContainer.getViewModel().getRecentSearchVisibilityObservable().onNext(true);
		assertEquals(View.VISIBLE ,widget.findViewById(R.id.flight_recent_searches_widget).getVisibility());
		assertEquals(View.VISIBLE ,recentSearchWidget.getVisibility());
	}

	@Test
	public void testRecentSearchItemSelectionWithValidDates() {
		setupRecentSearch();
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		RecyclerView recentSearchWidgetItems = recentSearchWidgetContainer.getRecyclerView();
		ArrayList<RecentSearch> recentSearches = new ArrayList<RecentSearch>();
		LocalDate startDate = LocalDate.now().plusDays(30);
		LocalDate endDate = LocalDate.now().plusDays(40);

		RecentSearch recentSearch =  new RecentSearch("SFO", "LAS",
			createSuggestion("SFO","San Francisco").getBytes(),
			createSuggestion("LAS" ,"Las Vegas").getBytes(),
			startDate.toString(), endDate.toString(), "COACH",
			1520490226L, 668, "USD", 1, "10,12",
			false, true);
		recentSearches.add(recentSearch);
		recentSearchWidgetContainer.getViewModel().getRecentSearchesObservable().onNext(recentSearches);
		recentSearchWidgetItems.measure(0, 0);
		recentSearchWidgetItems.layout(0, 0, 100, 10000);
		//Assertions
		recentSearchWidgetItems.getChildAt(0).performClick();
		assertEquals("San Francisco", widget.getOriginCardView().getText());
		assertEquals("Las Vegas", widget.getDestinationCardView().getText());
		assertEquals(LocaleBasedDateFormatUtils.localDateToEEEMMMd(startDate) + "  -  " +
			LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate), widget.getCalendarWidgetV2().getText());
		assertEquals("3 travelers", widget.getTravelerWidgetV2().getText());
		assertEquals("Economy", widget.getFlightCabinClassWidget().getText());
	}

	@Test
	public void testRecentSearchWhenDepartureDateInvalid() {
		setupRecentSearch();
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		RecyclerView recentSearchWidgetItems = recentSearchWidgetContainer.getRecyclerView();
		ArrayList<RecentSearch> recentSearches = new ArrayList<RecentSearch>();
		LocalDate startDate = LocalDate.now().minusDays(20);
		LocalDate endDate = LocalDate.now().plusDays(30);

		RecentSearch recentSearch =  new RecentSearch("SFO", "LAS",
			createSuggestion("SFO","San Francisco").getBytes(),
			createSuggestion("LAS" ,"Las Vegas").getBytes(),
			startDate.toString(), endDate.toString(), "PREMIUM_COACH",
			1520490226L, 668, "USD", 2, "",
			false, true);
		recentSearches.add(recentSearch);
		recentSearchWidgetContainer.getViewModel().getRecentSearchesObservable().onNext(recentSearches);
		recentSearchWidgetItems.measure(0, 0);
		recentSearchWidgetItems.layout(0, 0, 1000, 10000);
		//Assertions
		recentSearchWidgetItems.getChildAt(0).performClick();
		assertEquals("San Francisco", widget.getOriginCardView().getText());
		assertEquals("Las Vegas", widget.getDestinationCardView().getText());
		assertEquals(LocaleBasedDateFormatUtils.localDateToEEEMMMd(LocalDate.now()) + "  -  " +
			LocaleBasedDateFormatUtils.localDateToEEEMMMd(endDate), widget.getCalendarWidgetV2().getText());
		assertEquals("2 travelers", widget.getTravelerWidgetV2().getText());
		assertEquals("Premium Economy", widget.getFlightCabinClassWidget().getText());
	}

	@Test
	public void testRecentSearchWhenBothDatesInvalid() {
		setupRecentSearch();
		RecentSearchWidgetContainer recentSearchWidgetContainer = widget.getRecentSearchWidgetContainer();
		RecyclerView recentSearchWidgetItems = recentSearchWidgetContainer.getRecyclerView();
		ArrayList<RecentSearch> recentSearches = new ArrayList<RecentSearch>();

		RecentSearch recentSearch =  new RecentSearch("SFO", "LAS",
			createSuggestion("SFO","San Francisco").getBytes(),
			createSuggestion("LAS" ,"Las Vegas").getBytes(),
			LocalDate.now().minusDays(30).toString(), LocalDate.now().minusDays(20).toString(), "PREMIUM_COACH",
			1520490226L, 668, "USD", 2, "",
			false, true);
		recentSearches.add(recentSearch);
		recentSearchWidgetContainer.getViewModel().getRecentSearchesObservable().onNext(recentSearches);
		recentSearchWidgetItems.measure(0, 0);
		recentSearchWidgetItems.layout(0, 0, 1000, 10000);
		//Assertions
		recentSearchWidgetItems.getChildAt(0).performClick();
		assertEquals("Select dates", widget.getCalendarWidgetV2().getText());
	}

	private void setupRecentSearch() {
		AbacusTestUtils.bucketTestsAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsRecentSearch);
		AbacusTestUtils.bucketTestWithVariant(AbacusUtils.EBAndroidAppFlightsRecentSearch, 1);
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);
		widget.setSearchViewModel(new FlightSearchViewModel(activity));
	}

	private String getExpectedToolTipContDesc(LocalDate startDate, LocalDate endDate) {
		return (endDate == null) ? LocaleBasedDateFormatUtils.localDateToMMMd(startDate) + ". Next: Select return date" :
			LocaleBasedDateFormatUtils.localDateToMMMd(startDate)
				+ " to " + LocaleBasedDateFormatUtils.localDateToMMMd(startDate.plusDays(3)) + ". Select dates again to modify";
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

	private void setUpForFlightGreedySearch() {
		AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppFlightsGreedySearchCall, 1);
		Ui.getApplication(activity).defaultFlightComponents();
		widget = (FlightSearchPresenter) LayoutInflater.from(activity).inflate(R.layout.test_flight_search_presenter,
			null);
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

	private String createSuggestion(String airportCode, String displayName) {
		return "{\"coordinates\":{\"lat\":37.61594,\"long\":-122.387996},\"gaiaId\":\"5195347\",\"hierarchyInfo\":"
			+ "{\"airport\":{\"airportCode\":\""
			+ airportCode
			+ "\",\"multicity\":\"178305\"},\"country\":{\"isoCode3\":\"USA\",\"name\""
			+ ":\"United States of America\"},\"isChild\":false},\"iconType\":\"SEARCH_TYPE_ICON\",\"isMinorAirport\":false,"
			+ "\"isSearchThisArea\":false,\"regionNames\":{\"displayName\":\""
			+ displayName
			+ "\",\"fullName\":\""
			+ displayName
			+ "\",\"lastSearchName\":\""
			+ displayName
			+ "\",\"shortName\":\""
			+ displayName
			+ "\"},\"type\":\"AIRPORT\"}";
	}

	private class TestRecentSearchDAO extends RecentSearchDAO {

		private RecentSearch recentSearchItem = new RecentSearch("SFO", "LAS", "{\"coordinates\"}".getBytes(),
			"{\"coordinates\"}".getBytes(),"2018-05-10", "2018-05-31", "COACH",
			1519277785754L, 668, "USD", 1, "10,12",
			false, true);

		@Override
		public void insert(@NotNull RecentSearch recentSearch) { }

		@NotNull
		@Override
		public RecentSearch getOldestRecentSearch() {
			return recentSearchItem;
		}

		@Override
		public void delete(@NotNull RecentSearch recentSearch) { }

		@Override
		public int checkIfExist(@NotNull String sourceAirportCode, @NotNull String destinationAirportCode,
			boolean isRoundTrip) {
			return 0;
		}

		@Override
		public int count() {
			return 1;
		}

		@NotNull
		@Override
		public Flowable<List<RecentSearch>> loadAll() {
			List<RecentSearch> recentSearches = new ArrayList<RecentSearch>();
			recentSearches.add(recentSearchItem);
			return Flowable.just(recentSearches);
		}

		@Override
		public void clear() { }
	}
}
