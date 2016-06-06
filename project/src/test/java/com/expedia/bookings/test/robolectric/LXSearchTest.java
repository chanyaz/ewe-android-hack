package com.expedia.bookings.test.robolectric;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.expedia.bookings.R;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.presenter.lx.LXSearchParamsPresenter;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AlwaysFilterAutoCompleteTextView;
import com.expedia.bookings.widget.LxSuggestionAdapter;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.time.widget.CalendarPicker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(RobolectricRunner.class)
public class LXSearchTest {

	private Context context;
	private Activity activity;
	private LayoutInflater inflater;
	private LXSearchParamsPresenter searchwidget;
	private LXSearchParams searchParams;
	private AlwaysFilterAutoCompleteTextView autoCompleteTextView;

	@Before
	public void setup() {

		activity = Robolectric.buildActivity(Activity.class).create().get();
		activity.setTheme(R.style.V2_Theme_LX);
		context = activity.getBaseContext();
		Ui.getApplication(activity).defaultLXComponents();
		inflater = activity.getLayoutInflater();
		searchwidget = (LXSearchParamsPresenter) inflater.inflate(R.layout.test_lx_search_presenter, null);
		searchParams = searchwidget.getCurrentParams();
		autoCompleteTextView = (AlwaysFilterAutoCompleteTextView) searchwidget.findViewById(R.id.search_location);
	}

	@Test
	public void testSearchFormComponentVisibility() {

		View calendarContainer = searchwidget.findViewById(R.id.calendar_container);
		ToggleButton selectDate = (ToggleButton) searchwidget.findViewById(R.id.select_dates);
		Button searchButton = (Button) searchwidget.findViewById(R.id.search_btn);
		ImageView locationIcon = (ImageView) ((ViewGroup) searchwidget.findViewById(R.id.location_container)).getChildAt(0);
		TextView toolbarSearchText = (TextView)  searchwidget.findViewById(R.id.toolbar_search_text);
		assertNotNull(calendarContainer);
		assertEquals("Search Activities", toolbarSearchText.getText().toString());
		assertEquals(View.VISIBLE, searchButton.getVisibility());
		assertEquals(View.VISIBLE, locationIcon.getVisibility());
		assertEquals(View.VISIBLE, autoCompleteTextView.getVisibility());
		assertEquals(View.VISIBLE, selectDate.getVisibility());
		assertEquals(false, selectDate.isChecked());
		assertEquals(View.INVISIBLE, calendarContainer.getVisibility());
	}

	@Test
	public void testSearchLocationDropdownList() {

		LxSuggestionAdapter suggestionAdapter = (LxSuggestionAdapter) autoCompleteTextView.getAdapter();
		List<SuggestionV4> suggestionV4s = new ArrayList<>();
		SuggestionV4 s1 = getDummySuggestion("", "Las Vegas, NV", "and vicnity", "Las Vegas, NV and vicnity");
		SuggestionV4 s2 = getDummySuggestion("", "San Francisco, NY", "and nearby" , "San Francisco, NY and nearby");
		suggestionV4s.add(s1);
		suggestionV4s.add(s2);
		suggestionAdapter.updateRecentHistory(suggestionV4s);

		View viewHolderAtPositionOne = suggestionAdapter.getView(1, null, searchwidget);
		View viewHolderAtPositionTwo = suggestionAdapter.getView(2, null, searchwidget);
		TextView displayNameOne = (TextView) viewHolderAtPositionOne.findViewById(R.id.title_textview);
		TextView displayNameTwo = (TextView) viewHolderAtPositionTwo.findViewById(R.id.title_textview);
		TextView cityNameOne = (TextView) viewHolderAtPositionOne.findViewById(R.id.city_name_textView);
		TextView cityNameTwo = (TextView) viewHolderAtPositionTwo.findViewById(R.id.city_name_textView);

		assertEquals("Las Vegas, NV", displayNameOne.getText().toString());
		assertEquals("and vicnity", cityNameOne.getText().toString());
		assertEquals("San Francisco, NY", displayNameTwo.getText().toString());
		assertEquals("and nearby", cityNameTwo.getText().toString());

	}

	@Test
	public void testSelectLocationfromSuggestions() {
		LxSuggestionAdapter suggestionAdapter =  (LxSuggestionAdapter) autoCompleteTextView.getAdapter();
		ToggleButton selectDate = (ToggleButton) searchwidget.findViewById(R.id.select_dates);
		CalendarPicker calendarPicker = (CalendarPicker) searchwidget.findViewById(R.id.search_calendar);
		ImageView nextMonth = (ImageView) searchwidget.findViewById(R.id.next_month);

		List<SuggestionV4> suggestionV4s = new ArrayList<>();
		SuggestionV4 s1 = getDummySuggestion("", "Las Vegas, NV","and vicnity", "Las Vegas, NV and vicnity");
		SuggestionV4 s2 = getDummySuggestion("", "San Francisco, NY","and nearby" , "San Francisco, NY and nearby");
		suggestionV4s.add(s1);
		suggestionV4s.add(s2);
		suggestionAdapter.addNearbyAndRecents(suggestionV4s,context);
		autoCompleteTextView.getOnItemClickListener().onItemClick(null, null, 2, 0);
		assertEquals("Las Vegas, NV and vicnity", searchParams.location);
		assertEquals(true, selectDate.isChecked());
		assertEquals(View.VISIBLE, calendarPicker.getVisibility());
		assertEquals(View.VISIBLE, nextMonth.getVisibility());
		assertEquals(true, nextMonth.isClickable());

	}

	private SuggestionV4 getDummySuggestion(String gaiaid, String displayName, String shortName, String fullname) {

		SuggestionV4 dummySuggestion = new SuggestionV4();
		SuggestionV4.RegionNames regionNames = new SuggestionV4.RegionNames();
		SuggestionV4.LatLng coordinates = new SuggestionV4.LatLng();

		dummySuggestion.gaiaId = gaiaid;
		regionNames.displayName = displayName;
		regionNames.shortName = shortName;
		regionNames.fullName = fullname;
		coordinates.lat = 0;
		coordinates.lng = 0;
		dummySuggestion.regionNames = regionNames;
		dummySuggestion.coordinates = coordinates;
		dummySuggestion.iconType = SuggestionV4.IconType.SEARCH_TYPE_ICON;
		return dummySuggestion;
	}
}
