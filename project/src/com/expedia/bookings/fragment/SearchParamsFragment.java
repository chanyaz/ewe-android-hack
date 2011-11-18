package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.Suggestion;
import com.mobiata.android.widget.CalendarDatePicker;
import com.mobiata.android.widget.CalendarDatePicker.OnDateChangedListener;

public class SearchParamsFragment extends Fragment {

	private static final int NUM_SUGGESTIONS = 5;

	private static final String KEY_AUTOCOMPLETE_DOWNLOAD = "KEY_AUTOCOMPLETE_DOWNLOAD";

	public static SearchParamsFragment newInstance() {
		return new SearchParamsFragment();
	}

	private EditText mLocationEditText;
	private List<SuggestionRow> mSuggestionRows;
	private CalendarDatePicker mCalendarDatePicker;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_search_params, container, false);

		// Manually put HTML-ified code into instruction textviews
		// TODO: On a future translation, get rid of this step by using "<b>" in the strings instead of "&lt;b&gt;"
		setHtmlTextView(view, R.id.where_text_view, R.string.where_are_you_going);
		setHtmlTextView(view, R.id.when_text_view, R.string.drag_to_extend_your_stay);
		setHtmlTextView(view, R.id.who_text_view, R.string.who_is_going);

		mLocationEditText = (EditText) view.findViewById(R.id.location_edit_text);
		mCalendarDatePicker = (CalendarDatePicker) view.findViewById(R.id.dates_date_picker);
		mAdultsNumberPicker = (NumberPicker) view.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) view.findViewById(R.id.children_number_picker);

		// Need to set temporary max values for number pickers, or updateViews() won't work (since a picker value
		// must be within its valid range to be set)
		mAdultsNumberPicker.setMaxValue(100);
		mChildrenNumberPicker.setMaxValue(100);

		updateViews();

		// Configure the location EditText
		mLocationEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (!isHidden() && isAdded()) {
					String location = s.toString().trim();
					SearchParams searchParams = getInstance().mSearchParams;
					if (location.length() == 0 || location.equals(getString(R.string.current_location))) {
						searchParams.setSearchType(SearchType.MY_LOCATION);
						configureSuggestions(null);
					}
					else if (!location.equals(searchParams.getFreeformLocation())) {
						searchParams.setFreeformLocation(location);
						searchParams.setSearchType(SearchType.FREEFORM);
						configureSuggestions(location);
					}
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// Do nothing
			}

			public void afterTextChanged(Editable s) {
				// Do nothing
			}
		});
		mLocationEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && mLocationEditText.getText().toString().equals(getString(R.string.current_location))) {
					mLocationEditText.setText("");
				}
			}
		});

		// Configure suggestions
		ViewGroup suggestionsContainer = (ViewGroup) view.findViewById(R.id.suggestions_layout);
		mSuggestionRows = new ArrayList<SuggestionRow>();
		for (int a = 0; a < NUM_SUGGESTIONS; a++) {
			ViewGroup suggestionRow = (ViewGroup) inflater.inflate(R.layout.snippet_suggestion, suggestionsContainer,
					false);
			SuggestionRow row = new SuggestionRow();
			row.mRow = suggestionRow;
			row.mIcon = (ImageView) suggestionRow.findViewById(R.id.icon);
			row.mLocation = (TextView) suggestionRow.findViewById(R.id.location);
			mSuggestionRows.add(row);
			suggestionsContainer.addView(suggestionRow);

			if (a + 1 < NUM_SUGGESTIONS) {
				row.mDivider = inflater.inflate(R.layout.snippet_autocomplete_divider, suggestionsContainer, false);
				suggestionsContainer.addView(row.mDivider);
			}
		}

		mSuggestions = Arrays.asList(getResources().getStringArray(R.array.suggestions));
		Collections.shuffle(mSuggestions); // Randomly shuffle them for each launch
		configureSuggestions(null);

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);
		mCalendarDatePicker.setOnDateChangedListener(new OnDateChangedListener() {
			public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
				if (!isHidden()) {
					Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(), mCalendarDatePicker
							.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
					Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(), mCalendarDatePicker
							.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

					SearchParams searchParams = getInstance().mSearchParams;
					searchParams.setCheckInDate(checkIn);
					searchParams.setCheckOutDate(checkOut);
				}
			}
		});

		// Configure the number pickers
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		OnValueChangeListener valueChangeListener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (!isHidden()) {
					SearchParams searchParams = getInstance().mSearchParams;
					searchParams.setNumAdults(mAdultsNumberPicker.getValue());
					searchParams.setNumChildren(mChildrenNumberPicker.getValue());
				}

				GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(valueChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(valueChangeListener);

		// Block NumberPickers from being editable
		mAdultsNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mChildrenNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// Configure the search button
		Button button = (Button) view.findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SearchResultsFragmentActivity.class);
				intent.putExtra(Codes.SEARCH_PARAMS, getInstance().mSearchParams.toJson().toString());
				startActivity(intent);

				// Do this so that when the user clicks back, they aren't focused on the location edit text immediately
				mLocationEditText.clearFocus();
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_AUTOCOMPLETE_DOWNLOAD)) {
			bd.registerDownloadCallback(KEY_AUTOCOMPLETE_DOWNLOAD, mAutocompleteCallback);
		}

		updateViews();
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_AUTOCOMPLETE_DOWNLOAD);
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	public void updateViews() {
		SearchParams params = getInstance().mSearchParams;

		mLocationEditText.setText(params.getSearchDisplayText(getActivity()));

		Calendar start = params.getCheckInDate();
		mCalendarDatePicker.updateStartDate(start.get(Calendar.YEAR), start.get(Calendar.MONTH),
				start.get(Calendar.DAY_OF_MONTH));
		Calendar end = params.getCheckOutDate();
		mCalendarDatePicker.updateEndDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH),
				end.get(Calendar.DAY_OF_MONTH));

		mAdultsNumberPicker.setValue(params.getNumAdults());
		mChildrenNumberPicker.setValue(params.getNumChildren());
	}

	private void setHtmlTextView(View container, int textViewId, int strId) {
		TextView tv = (TextView) container.findViewById(textViewId);
		tv.setText(Html.fromHtml(getString(strId)));
	}

	//////////////////////////////////////////////////////////////////////////
	// Suggestion/autocomplete stuff

	private List<String> mSuggestions;

	private static class SuggestionRow {
		public ViewGroup mRow;
		public View mDivider;
		public ImageView mIcon;
		public TextView mLocation;

		public void setVisibility(int visibility) {
			mIcon.setVisibility(visibility);
			mLocation.setVisibility(visibility);

			if (mDivider != null) {
				if (visibility == View.VISIBLE) {
					mDivider.setBackgroundResource(R.drawable.autocomplete_seperator);
				}
				else {
					mDivider.setBackgroundResource(R.drawable.bg_suggestion);
				}
			}
		}
	}

	private void configureSuggestions(final String query) {
		mHandler.removeMessages(WHAT_AUTOCOMPLETE);

		// Show default suggestions in case of null query
		if (query == null || query.length() == 0) {
			// Configure "my location" separately
			SuggestionRow currentLocationRow = mSuggestionRows.get(0);
			currentLocationRow.setVisibility(View.VISIBLE);
			currentLocationRow.mRow.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mLocationEditText.setText(R.string.current_location);

				}
			});
			currentLocationRow.mLocation.setText(R.string.current_location);
			currentLocationRow.mLocation.setTypeface(Typeface.DEFAULT_BOLD);
			currentLocationRow.mIcon.setImageResource(R.drawable.autocomplete_location);

			for (int a = 1; a < mSuggestionRows.size(); a++) {
				configureSuggestionRow(mSuggestionRows.get(a), mSuggestions.get(a));
			}
		}
		else {
			// If we have a query string, kick off a suggestions request
			// Do it on a delay though - we don't want to update suggestions every time user is edits a single
			// char on the text
			Message msg = new Message();
			msg.obj = query;
			msg.what = WHAT_AUTOCOMPLETE;
			mHandler.sendMessageDelayed(msg, 1000);
		}
	}

	private void configureSuggestionRow(SuggestionRow row, final String suggestion) {
		row.setVisibility(View.VISIBLE);

		row.mRow.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mLocationEditText.setText(suggestion);
				mLocationEditText.clearFocus();
			}
		});

		row.mLocation.setText(suggestion);
		row.mIcon.setImageResource(R.drawable.autocomplete_pin);
	}

	@SuppressWarnings("unchecked")
	private OnDownloadComplete mAutocompleteCallback = new OnDownloadComplete() {
		public void onDownload(Object results) {
			List<Suggestion> suggestions = (List<Suggestion>) results;
			if (suggestions == null) {
				suggestions = new ArrayList<Suggestion>();
			}

			int numSuggestions = suggestions.size();
			for (int a = 0; a < mSuggestionRows.size(); a++) {
				SuggestionRow row = mSuggestionRows.get(a);
				if (a < numSuggestions) {
					configureSuggestionRow(row, suggestions.get(a).mSuggestion);
				}
				else {
					row.setVisibility(View.INVISIBLE);
					row.mRow.setClickable(false);
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Handler implementation

	private static final int WHAT_AUTOCOMPLETE = 1;

	private Handler mHandler = new SearchParamsHandler();

	private class SearchParamsHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == WHAT_AUTOCOMPLETE) {
				final String query = (String) msg.obj;

				Log.d("Querying autocomplete for: " + query);

				final Download download = new Download() {
					public Object doDownload() {
						GoogleServices services = new GoogleServices(getActivity());
						BackgroundDownloader.getInstance().addDownloadListener(KEY_AUTOCOMPLETE_DOWNLOAD,
								services);
						return services.getSuggestions(query);
					}
				};

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				bd.cancelDownload(KEY_AUTOCOMPLETE_DOWNLOAD);
				bd.startDownload(KEY_AUTOCOMPLETE_DOWNLOAD, download, mAutocompleteCallback);
			}
			super.handleMessage(msg);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchFragmentActivity.InstanceFragment getInstance() {
		return ((SearchFragmentActivity) getActivity()).getInstance();
	}
}
