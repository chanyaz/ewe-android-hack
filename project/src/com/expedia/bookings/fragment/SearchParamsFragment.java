package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchFragmentActivity;
import com.expedia.bookings.activity.SearchFragmentActivity.InstanceFragment;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchParams.SearchType;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.widget.NumberPicker;
import com.expedia.bookings.widget.NumberPicker.OnValueChangeListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.Suggestion;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.widget.CalendarDatePicker;

public class SearchParamsFragment extends Fragment implements EventHandler {

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
	private TextView mSuggestionErrorTextView;
	private View mChildAgesLayout;


	// #10978: Tracks when an autocomplete row was just clicked, so that we don't
	// automatically start a new autocomplete query.
	private boolean mAutocompleteClicked = false;

	// #11237: When you register a connectivity receiver, it necessarily fires a
	// response once.  We only want to listen when connectivity changes *after*
	// the initial registration.
	private boolean mDetectedInitialConnectivity;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((SearchFragmentActivity) activity).mEventManager.registerEventHandler(this);
	}

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
		mSuggestionErrorTextView = (TextView) view.findViewById(R.id.suggestion_error_text_view);
		mChildAgesLayout = view.findViewById(R.id.child_ages_layout);

		// Need to set temporary max values for number pickers, or updateViews() won't work (since a picker value
		// must be within its valid range to be set)
		mAdultsNumberPicker.setMaxValue(100);
		mChildrenNumberPicker.setMaxValue(100);

		updateViews();

		// Configure the location EditText
		mLocationEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					getInstance().mHasFocusedSearchField = true;

					String text = mLocationEditText.getText().toString();
					if (text.equals(getString(R.string.current_location))
							|| text.equals(getString(R.string.enter_search_location))) {
						mLocationEditText.setText("");
					}
				}
			}
		});

		mLocationEditText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					if (v.getText().length() > 0) {
						// #11066: Start the search when user clicks "done"
						startSearch();
					}
					else {
						// Don't let the user click "done" if they haven't entered anything
						return true;
					}
				}
				return false;
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

		// Get suggestions from res
		final List<String> tmpArray = Arrays.asList(getResources().getStringArray(R.array.suggestions));
		mSuggestions = new ArrayList<String>();
		mSuggestions.addAll(tmpArray);
		Collections.shuffle(mSuggestions); // Randomly shuffle them for each launch

		// Add history to top
		List<String> searchHistory = new ArrayList<String>();
		for (Search search : Search.getRecentSearches(getActivity(), 5)) {
			searchHistory.add(search.getFreeformLocation());
		}
		mSuggestions.addAll(0, searchHistory);

		configureSuggestions(null);

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker);
		mCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		// Configure the number pickers
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(), getInstance().mSearchParams.getChildren(),
				mChildAgesLayout, mChildAgeSelectedListener);
		mAdultsNumberPicker.setOnValueChangedListener(mPersonCountChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(mPersonCountChangeListener);

		// Block NumberPickers from being editable
		mAdultsNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mChildrenNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// Configure the search button
		Button button = (Button) view.findViewById(R.id.search_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startSearch();
			}
		});

		// Configure the "choose children's ages" button
		OnClickListener showChildAgesListener = new OnClickListener() {
			public void onClick(View v) {
				mChildAgesLayout.setVisibility(View.VISIBLE);
			}
		};
		view.findViewById(R.id.child_ages_button).setOnClickListener(showChildAgesListener);

		// Configure the "x" and "done" buttons
		OnClickListener hideChildAgesListener = new OnClickListener() {
			public void onClick(View v) {
				mChildAgesLayout.setVisibility(View.GONE);
			}
		};
		view.findViewById(R.id.done_button).setOnClickListener(hideChildAgesListener);
		view.findViewById(R.id.button_x).setOnClickListener(hideChildAgesListener);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_AUTOCOMPLETE_DOWNLOAD)) {
			bd.registerDownloadCallback(KEY_AUTOCOMPLETE_DOWNLOAD, mAutocompleteCallback);
		}

		mLocationEditText.addTextChangedListener(mLocationTextWatcher);

		mDetectedInitialConnectivity = false;

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mConnectivityReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_AUTOCOMPLETE_DOWNLOAD);

		mLocationEditText.removeTextChangedListener(mLocationTextWatcher);

		getActivity().unregisterReceiver(mConnectivityReceiver);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((SearchFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
	}

	public void startSearch() {
		((SearchFragmentActivity) getActivity()).startSearch();

		// Do this so that when the user clicks back, they aren't focused on the location edit text immediately
		mLocationEditText.clearFocus();
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	public void updateViews() {
		InstanceFragment instance = getInstance();
		SearchParams params = instance.mSearchParams;

		// #11468: Not sure how we get into this state, but let's just try to prevent a crash for now.
		if (params == null) {
			Log.w("Somehow, params are null.  Resetting them to default to avoid problems.");
			instance.mSearchParams = params = new SearchParams();
			instance.mHasFocusedSearchField = false;
		}

		if (instance.mHasFocusedSearchField) {
			mLocationEditText.setText(params.getSearchDisplayText(getActivity()));
		}
		else {
			mLocationEditText.setText(R.string.enter_search_location);
		}

		// Temporarily remove the OnDateChangedListener so that it is not fired
		// while we manually update the start/end dates
		mCalendarDatePicker.setOnDateChangedListener(null);

		Calendar start = params.getCheckInDate();
		Calendar end = params.getCheckOutDate();
		mCalendarDatePicker.updateStartDate(start.get(Calendar.YEAR), start.get(Calendar.MONTH),
				start.get(Calendar.DAY_OF_MONTH));
		mCalendarDatePicker.updateEndDate(end.get(Calendar.YEAR), end.get(Calendar.MONTH),
				end.get(Calendar.DAY_OF_MONTH));

		mCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mAdultsNumberPicker.setValue(params.getNumAdults());
		mChildrenNumberPicker.setValue(params.getNumChildren());
	}

	private void setHtmlTextView(View container, int textViewId, int strId) {
		TextView tv = (TextView) container.findViewById(textViewId);
		tv.setText(Html.fromHtml(getString(strId)));
	}

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
			Calendar checkIn = new GregorianCalendar(mCalendarDatePicker.getStartYear(),
					mCalendarDatePicker.getStartMonth(), mCalendarDatePicker.getStartDayOfMonth());
			Calendar checkOut = new GregorianCalendar(mCalendarDatePicker.getEndYear(),
					mCalendarDatePicker.getEndMonth(), mCalendarDatePicker.getEndDayOfMonth());

			SearchParams searchParams = getInstance().mSearchParams;
			searchParams.setCheckInDate(checkIn);
			searchParams.setCheckOutDate(checkOut);
		}
	};

	// Configure number pickers to dynamically change the layout on value changes
	private final OnValueChangeListener mPersonCountChangeListener = new OnValueChangeListener() {

		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			if (!isHidden()) {
				SearchParams searchParams = getInstance().mSearchParams;
				List<Integer> children = searchParams.getChildren();
				searchParams.setNumAdults(mAdultsNumberPicker.getValue());
				Activity activity = getActivity();
				GuestsPickerUtils.resizeChildrenList(activity, children, mChildrenNumberPicker.getValue());
				boolean wasPopupGone = mChildAgesLayout.getVisibility() == View.GONE;
				GuestsPickerUtils.showOrHideChildAgeSpinners(activity, children, mChildAgesLayout, mChildAgeSelectedListener);

				// showOrHideChildAgeSpinners may automatically show the popup. We don't want that in this case.
				if (wasPopupGone) {
					mChildAgesLayout.setVisibility(View.GONE);
				}

				activity.findViewById(R.id.child_ages_button).setVisibility(children.size() != 0 ? View.VISIBLE : View.GONE);
			}

			GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);

		}

	};

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Context context = getActivity();
			List<Integer> children = getInstance().mSearchParams.getChildren();
			GuestsPickerUtils.setChildrenFromSpinners(context, mChildAgesLayout, children);
			GuestsPickerUtils.updateDefaultChildAges(context, children);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}

	};

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

			// Don't allow the row to be clicked if it's being hidden
			if (visibility != View.VISIBLE) {
				mRow.setClickable(false);
			}

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

		if (!NetUtils.isOnline(getActivity())) {
			// Hide all of the current suggestion rows
			for (SuggestionRow row : mSuggestionRows) {
				row.setVisibility(View.INVISIBLE);
			}

			mSuggestionErrorTextView.setVisibility(View.VISIBLE);
			mSuggestionErrorTextView.setText(R.string.error_no_internet);
			return;
		}
		else {
			mSuggestionErrorTextView.setVisibility(View.GONE);
		}

		// Show default suggestions in case of null query
		if (query == null || query.length() == 0 || !getInstance().mHasFocusedSearchField) {
			// Configure "my location" separately
			SuggestionRow currentLocationRow = mSuggestionRows.get(0);
			currentLocationRow.setVisibility(View.VISIBLE);
			currentLocationRow.mRow.setOnClickListener(createRowOnClickListener(getString(R.string.current_location)));
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

	private void configureSuggestionRow(SuggestionRow row, String suggestion) {
		row.setVisibility(View.VISIBLE);

		row.mRow.setOnClickListener(createRowOnClickListener(suggestion));

		row.mLocation.setText(suggestion);
		row.mIcon.setImageResource(R.drawable.autocomplete_pin);
	}

	private OnClickListener createRowOnClickListener(final String suggestion) {
		return new OnClickListener() {
			public void onClick(View v) {
				mAutocompleteClicked = true;

				mLocationEditText.setText(suggestion);
				mLocationEditText.clearFocus();

				// Hide the IME
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(mLocationEditText.getWindowToken(), 0);
			}
		};
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

	private TextWatcher mLocationTextWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			getInstance().mHasFocusedSearchField = true;

			String location = s.toString().trim();
			SearchParams searchParams = getInstance().mSearchParams;
			if (location.length() == 0 || location.equals(getString(R.string.current_location))) {
				searchParams.setSearchType(SearchType.MY_LOCATION);
				location = null;
			}
			else if (!location.equals(searchParams.getFreeformLocation())) {
				searchParams.setFreeformLocation(location);
				searchParams.setSearchType(SearchType.FREEFORM);
			}

			if (!mAutocompleteClicked) {
				configureSuggestions(location);
			}
			else {
				mAutocompleteClicked = false;
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		public void afterTextChanged(Editable s) {
			// Do nothing
		}
	};

	private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mDetectedInitialConnectivity) {
				// Kick off a new suggestion query based on the current text.
				configureSuggestions(mLocationEditText.getText().toString());
			}
			else {
				mDetectedInitialConnectivity = true;
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
						BackgroundDownloader.getInstance().addDownloadListener(KEY_AUTOCOMPLETE_DOWNLOAD, services);
						return services.getSuggestions(query, "geocode");
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
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case SearchFragmentActivity.EVENT_RESET_PARAMS:
			updateViews();
			BackgroundDownloader.getInstance().cancelDownload(KEY_AUTOCOMPLETE_DOWNLOAD);
			configureSuggestions(null);
			break;
		case SearchFragmentActivity.EVENT_UPDATE_PARAMS:
			BackgroundDownloader.getInstance().cancelDownload(KEY_AUTOCOMPLETE_DOWNLOAD);
			updateViews();
			configureSuggestions(null);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public SearchFragmentActivity.InstanceFragment getInstance() {
		return ((SearchFragmentActivity) getActivity()).getInstance();
	}
}
