package com.expedia.bookings.test.robolectric;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.presenter.flight.FlightSearchPresenter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CalendarWidgetV2;
import com.expedia.bookings.widget.HotelTravelerPickerView;
import com.expedia.bookings.widget.TravelerWidgetV2;
import com.expedia.bookings.widget.shared.SearchInputTextView;
import com.expedia.vm.FlightSearchViewModel;
import com.expedia.vm.TravelerPickerViewModel;

import static com.expedia.bookings.R.id.viewpager;
import static org.junit.Assert.assertEquals;

/**
 * Created by vsuriyal on 12/30/16.
 */
@RunWith(RobolectricRunner.class)
public class FlightSearchPresenterTest {
	private FlightSearchPresenter widget;
	private Activity activity;

	@Before
	public void before() {
		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_Packages);
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
	public void testTravelerDialog() {
		TravelerWidgetV2 travelerCard = (TravelerWidgetV2) widget.findViewById(R.id.traveler_card);
		TravelerPickerViewModel vm = new TravelerPickerViewModel(activity);
		travelerCard.performClick();
		View view = travelerCard.getTravelerDialogView();
		HotelTravelerPickerView hotelTravelerPicker = (HotelTravelerPickerView) view
			.findViewById(R.id.traveler_view);

		hotelTravelerPicker.getAdultPlus().performClick();
		hotelTravelerPicker.getChildPlus().performClick();
		hotelTravelerPicker.getChildPlus().performClick();

		vm.setShowSeatingPreference(true);
		hotelTravelerPicker.getChild1().setSelection(0);
		hotelTravelerPicker.getChild2().setSelection(0);
		hotelTravelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.GONE, hotelTravelerPicker.getInfantError().getVisibility());

		hotelTravelerPicker.getChildPlus().performClick();

		hotelTravelerPicker.getChild3().setSelection(0);
		hotelTravelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.VISIBLE, hotelTravelerPicker.getInfantError().getVisibility());

		hotelTravelerPicker.getChild3().setSelection(2);
		hotelTravelerPicker.getInfantPreferenceSeatingSpinner().setSelection(0);
		assertEquals(View.GONE, hotelTravelerPicker.getInfantError().getVisibility());

		hotelTravelerPicker.getChild3().setSelection(0);
		hotelTravelerPicker.getInfantPreferenceSeatingSpinner().setSelection(1);
		assertEquals(View.GONE, hotelTravelerPicker.getInfantError().getVisibility());

	}

}
