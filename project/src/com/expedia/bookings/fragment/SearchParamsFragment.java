package com.expedia.bookings.fragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLayoutChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.expedia.bookings.R;
import com.expedia.bookings.content.AutocompleteProvider;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchParams.SearchType;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.model.Search;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.CalendarUtils;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.NumberPicker;
import com.expedia.bookings.widget.NumberPicker.OnValueChangeListener;
import com.mobiata.android.Log;
import com.mobiata.android.util.NetUtils;
import com.mobiata.android.widget.CalendarDatePicker;

@TargetApi(11)
public class SearchParamsFragment extends Fragment implements LoaderCallbacks<Cursor> {

	private static final int NUM_SUGGESTIONS = 5;

	private static final String KEY_CHILD_AGES_POPUP_VISIBLE = "KEY_CHILD_AGES_POPUP_VISIBLE";

	private static final String INSTANCE_HAS_FOCUSED_SEARCH_FIELD = "INSTANCE_HAS_FOCUSED_SEARCH_FIELD";

	public static SearchParamsFragment newInstance() {
		return new SearchParamsFragment();
	}

	private EditText mLocationEditText;
	private List<SuggestionRow> mSuggestionRows;
	private CalendarDatePicker mCalendarDatePicker;
	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;
	private TextView mSuggestionErrorTextView;
	private TextView mSelectChildAgeTextView;
	private View mChildAgesLayout;
	private TextView mChildAgesButton;

	private SearchParamsFragmentListener mListener;

	// #11237: When you register a connectivity receiver, it necessarily fires a
	// response once.  We only want to listen when connectivity changes *after*
	// the initial registration.
	private boolean mDetectedInitialConnectivity;

	// Whether or not the search field has ever had focus.  This determines
	// how the autocomplete suggestions get displayed.
	private boolean mHasFocusedSearchField = false;

	// This is a copy of the last search params we used to update the Views.
	// It is only used so that we don't needlessly keep updating Views; it
	// should not be used for anything else.
	private HotelSearchParams mLastSearchParams = null;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mHasFocusedSearchField = savedInstanceState.getBoolean(INSTANCE_HAS_FOCUSED_SEARCH_FIELD, false);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, SearchParamsFragmentListener.class);
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
		mSelectChildAgeTextView = (TextView) view.findViewById(R.id.label_select_each_childs_age);
		mChildAgesLayout = view.findViewById(R.id.child_ages_layout);
		mChildAgesButton = (TextView) view.findViewById(R.id.child_ages_button);
		mChildAgesButton.addOnLayoutChangeListener(mChildAgesButtonLayoutChangeListener);

		// Need to set temporary max values for number pickers, or updateViews() won't work (since a picker value
		// must be within its valid range to be set)
		mAdultsNumberPicker.setMaxValue(100);
		mChildrenNumberPicker.setMaxValue(100);

		updateViews();

		if (Db.getHotelSearch().getSearchParams().getNumChildren() == 0) {
			hideChildAgesButton(false);
		}
		else {
			showChildAgesButton(false);
		}

		if (savedInstanceState != null && savedInstanceState.getBoolean(KEY_CHILD_AGES_POPUP_VISIBLE)) {
			showChildAgesPopup(false);
		}
		else {
			hideChildAgesPopup(false);
		}

		// We need to modify the OS touch event handling. Without this, the Location EditText
		// grabs focus right away, and also keeps focus even when the user has shifted his focus to the
		// date picker or the guest count spinners.
		final View touchInterceptor = view.findViewById(R.id.touch_interceptor);
		touchInterceptor.requestFocus();
		touchInterceptor.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mLocationEditText.isFocused()) {
						Rect outRect = new Rect();
						mLocationEditText.getGlobalVisibleRect(outRect);
						if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
							touchInterceptor.requestFocus();
						}
					}
				}
				return false;
			}
		});

		// Configure the location EditText
		mLocationEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					mHasFocusedSearchField = true;

					String text = mLocationEditText.getText().toString();
					if (text.equals(getString(R.string.current_location))
							|| text.equals(getString(R.string.enter_search_location))) {
						mLocationEditText.setText("");
					}
				}
				else {
					Ui.hideKeyboard(mLocationEditText);
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

		// Autosuggestions
		initSuggestionViews(view, inflater);
		startAutocomplete(Db.getHotelSearch().getSearchParams().getQuery());

		// Configure the calendar
		CalendarUtils.configureCalendarDatePicker(mCalendarDatePicker, CalendarDatePicker.SelectionMode.RANGE,
				LineOfBusiness.HOTELS);

		// Configure the number pickers
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		mAdultsNumberPicker.setOnValueChangedListener(mAdultCountChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(mChildCountChangeListener);

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
				showChildAgesPopup(true);
			}
		};
		mChildAgesButton.setOnClickListener(showChildAgesListener);

		// Configure the "x" and "done" buttons
		OnClickListener hideChildAgesListener = new OnClickListener() {
			public void onClick(View v) {
				hideChildAgesPopup(true);
			}
		};
		mChildAgesLayout.findViewById(R.id.done_button).setOnClickListener(hideChildAgesListener);
		mChildAgesLayout.findViewById(R.id.button_x).setOnClickListener(hideChildAgesListener);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(KEY_CHILD_AGES_POPUP_VISIBLE, isChildAgesPopupVisible());
		outState.putBoolean(INSTANCE_HAS_FOCUSED_SEARCH_FIELD, mHasFocusedSearchField);
	}

	@Override
	public void onStart() {
		super.onStart();

		// If the search params changed since last time we updated the views, update the views
		if (!Db.getHotelSearch().getSearchParams().equals(mLastSearchParams)) {
			Log.d("SearchParamsFragment: Detected change in HotelSearchParams, updating views.");
			mHasFocusedSearchField = true;

			updateViews();
			startAutocomplete(Db.getHotelSearch().getSearchParams().getQuery());
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mLocationEditText.addTextChangedListener(mLocationTextWatcher);

		mDetectedInitialConnectivity = false;

		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(mConnectivityReceiver, filter);
	}

	@Override
	public void onPause() {
		super.onPause();

		mLocationEditText.removeTextChangedListener(mLocationTextWatcher);

		getActivity().unregisterReceiver(mConnectivityReceiver);
	}

	public void startSearch() {
		mListener.onSearch();

		// Do this so that when the user clicks back, they aren't focused on the location edit text immediately
		mLocationEditText.clearFocus();
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	public void updateViews() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();

		// Before we update views, make sure we have a valid checkin/checkout dates
		params.ensureValidCheckInDate();

		if (mHasFocusedSearchField) {
			mLocationEditText.setText(params.getQuery());
		}
		else {
			mLocationEditText.setText(R.string.enter_search_location);
		}

		// Temporarily remove the OnDateChangedListener so that it is not fired
		// while we manually update the start/end dates
		mCalendarDatePicker.setOnDateChangedListener(null);

		CalendarUtils.updateCalendarPickerStartDate(mCalendarDatePicker, params.getCheckInDate());
		CalendarUtils.updateCalendarPickerEndDate(mCalendarDatePicker, params.getCheckOutDate());

		mCalendarDatePicker.setOnDateChangedListener(mDatesDateChangedListener);

		mAdultsNumberPicker.setValue(params.getNumAdults());
		mChildrenNumberPicker.setValue(params.getNumChildren());
		GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(), params.getChildren(), mChildAgesLayout,
				mChildAgeSelectedListener);

		if (params.getNumChildren() == 0) {
			hideChildAgesButton(false);
		}
		else {
			showChildAgesButton(false);
		}

		String labelSelectEachChildsAge = getResources().getQuantityString(R.plurals.select_each_childs_age,
				params.getNumChildren());
		mSelectChildAgeTextView.setText(labelSelectEachChildsAge);
		mChildAgesButton.setText(labelSelectEachChildsAge);

		mLastSearchParams = params.copy();
	}

	private void setHtmlTextView(View container, int textViewId, int strId) {
		TextView tv = (TextView) container.findViewById(textViewId);
		tv.setText(Html.fromHtml(getString(strId)));
	}

	private final CalendarDatePicker.OnDateChangedListener mDatesDateChangedListener = new CalendarDatePicker.OnDateChangedListener() {
		public void onDateChanged(CalendarDatePicker view, int year, int yearMonth, int monthDay) {
			CalendarUtils.syncParamsFromDatePickerRange(Db.getHotelSearch().getSearchParams(), mCalendarDatePicker);
		}
	};

	// Configure number pickers to dynamically change the layout on value changes
	private final OnValueChangeListener mAdultCountChangeListener = new OnValueChangeListener() {

		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			if (!isHidden()) {
				HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
				searchParams.setNumAdults(mAdultsNumberPicker.getValue());
			}

			GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			onGuestsChanged();
		}
	};

	private final OnValueChangeListener mChildCountChangeListener = new OnValueChangeListener() {

		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			if (!isHidden()) {
				HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
				List<ChildTraveler> children = searchParams.getChildren();
				Activity activity = getActivity();
				GuestsPickerUtils.resizeChildrenList(activity, children, mChildrenNumberPicker.getValue());

				String labelSelectEachChildsAge = getResources().getQuantityString(R.plurals.select_each_childs_age,
						searchParams.getNumChildren());
				mSelectChildAgeTextView.setText(labelSelectEachChildsAge);
				mChildAgesButton.setText(labelSelectEachChildsAge);
				mChildAgesButton.requestLayout();

				GuestsPickerUtils.showOrHideChildAgeSpinners(activity, children, mChildAgesLayout,
						mChildAgeSelectedListener);

				if (children.size() != 0) {
					if (mChildAgesButton.getAlpha() == 0) {
						showChildAgesButton(true);
					}
					else {
						showChildAgesPopup(true);
					}
				}
				else if (children.size() == 0 && mChildAgesButton.getAlpha() > 0) {
					hideChildAgesButton(true);
				}
			}

			GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			onGuestsChanged();
		}
	};

	private OnLayoutChangeListener mChildAgesButtonLayoutChangeListener = new OnLayoutChangeListener() {
		@Override
		public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
				int oldRight, int oldBottom) {
			if (mChildAgesLayout.getVisibility() == View.GONE) {
				return;
			}
			showChildAgesPopup(false);
		}
	};

	private static class HeightEvaluator implements TypeEvaluator<Integer> {

		public static ValueAnimator getAnimator(View v, int to) {
			int from = v.getHeight();
			ValueAnimator anim = ValueAnimator.ofInt(from, to);
			anim.setEvaluator(new HeightEvaluator(v));
			return anim;
		}

		private View v;

		public HeightEvaluator(View view) {
			this.v = view;
		}

		@Override
		public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
			int startInt = startValue;
			int num = (int) (startInt + fraction * (endValue - startInt));
			ViewGroup.LayoutParams layout = v.getLayoutParams();
			layout.height = num;
			v.setLayoutParams(layout);
			return num;
		}
	}

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Context context = getActivity();
			List<ChildTraveler> children = Db.getHotelSearch().getSearchParams().getChildren();
			GuestsPickerUtils.setChildrenFromSpinners(context, mChildAgesLayout, children);
			GuestsPickerUtils.updateDefaultChildAges(context, children);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}

	};

	private void showChildAgesButton(final boolean animated) {
		int height = getResources().getDimensionPixelSize(R.dimen.child_ages_button_height_restored);
		if (animated) {
			ObjectAnimator animAlpha = ObjectAnimator.ofFloat(mChildAgesButton, "alpha", 1f);
			ValueAnimator animHeight = HeightEvaluator.getAnimator(mChildAgesButton, height);
			AnimatorSet animSet = new AnimatorSet();
			animSet.playTogether(animAlpha, animHeight);
			animSet.addListener(new AnimatorListener() {

				@Override
				public void onAnimationCancel(Animator animation) {
					// Intentionally blank
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					showChildAgesPopup(animated);
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
					// Intentionally blank
				}

				@Override
				public void onAnimationStart(Animator animation) {
					// Intentionally blank
				}
			});
			animSet.start();
		}
		else {
			ViewGroup.LayoutParams layout = mChildAgesButton.getLayoutParams();
			layout.height = height;
			mChildAgesButton.setLayoutParams(layout);
			mChildAgesButton.setAlpha(1f);
		}
	}

	private void hideChildAgesButton(boolean animated) {
		int height = getResources().getDimensionPixelSize(R.dimen.child_ages_button_height_minimized);
		if (animated) {
			ObjectAnimator animAlpha = ObjectAnimator.ofFloat(mChildAgesButton, "alpha", 0f);
			ValueAnimator animHeight = HeightEvaluator.getAnimator(mChildAgesButton, height);
			AnimatorSet animSet = new AnimatorSet();
			animSet.playTogether(animAlpha, animHeight);
			animSet.start();
		}
		else {
			ViewGroup.LayoutParams layout = mChildAgesButton.getLayoutParams();
			layout.height = height;
			mChildAgesButton.setLayoutParams(layout);
			mChildAgesButton.setAlpha(0f);
		}
		if (mChildAgesLayout.getVisibility() == View.VISIBLE) {
			hideChildAgesPopup(animated);
		}
	}

	private boolean isChildAgesPopupVisible() {
		return mChildAgesLayout.getVisibility() == View.VISIBLE;
	}

	private void showChildAgesPopup(boolean animated) {
		if (!isChildAgesPopupVisible()) {
			if (animated) {
				Animation fadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
				mChildAgesLayout.startAnimation(fadeIn);
			}
			mChildAgesLayout.setVisibility(View.VISIBLE);
		}

		GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(),
				Db.getHotelSearch().getSearchParams().getChildren(),
				mChildAgesLayout, mChildAgeSelectedListener);

		// This needs to be run after mChildAgesPopup layout (because of getHeight()).
		// It also requires TargetApi(11) because [gs]etTranslationY is API 11. But
		// that's ok because this activity only targets tablet.
		mChildAgesLayout.post(new Runnable() {
			@TargetApi(11)
			@Override
			public void run() {
				int[] button = new int[2];
				mChildAgesButton.getLocationInWindow(button);
				int buttonTop = button[1];

				int[] popup = new int[2];
				mChildAgesLayout.getLocationInWindow(popup);
				int popupTop = popup[1] - (int) mChildAgesLayout.getTranslationY();

				int max = getView().getHeight() - mChildAgesLayout.getHeight();
				int padding = (int) (12 * getResources().getDisplayMetrics().density);

				int translation = Math.min(max, buttonTop - popupTop - padding);

				mChildAgesLayout.setTranslationY(translation);
			}
		});

	}

	private void hideChildAgesPopup(boolean animated) {
		if (animated) {
			Animation fadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
			mChildAgesLayout.startAnimation(fadeOut);
		}
		mChildAgesLayout.setVisibility(View.GONE);
	}

	//////////////////////////////////////////////////////////////////////////
	// Suggestion/autocomplete stuff

	private class SuggestionRow {
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

		/**
		 * Expects that sometimes search might == null.
		 * @param query either a Search object or a String
		 */
		public void configure(Object query) {
			Search search = null;
			String location;
			if (query instanceof Search) {
				search = (Search) query;
				location = search.getQuery();
			}
			else {
				location = query.toString();
			}

			setVisibility(View.VISIBLE);

			mRow.setOnClickListener(createRowOnClickListener(location, search));

			mLocation.setText(location);

			if (location.equals(getString(R.string.current_location))) {
				mLocation.setTypeface(Typeface.DEFAULT_BOLD);
				mIcon.setImageResource(R.drawable.ic_suggestion_current_location);
			}
			else {
				mLocation.setTypeface(Typeface.DEFAULT);
				mIcon.setImageResource(R.drawable.ic_suggestion_place);
			}

			if (search != null && search.getSearchType().equals(SearchType.HOTEL.toString())) {
				mIcon.setImageResource(R.drawable.ic_suggestion_hotel);
			}
		}

		/**
		 * Expects that sometimes search might == null.
		 * @param search
		 * @param suggestion
		 * @return
		 */
		private OnClickListener createRowOnClickListener(final String suggestion, final Search search) {
			return new OnClickListener() {
				public void onClick(View v) {
					Db.getHotelSearch().getSearchParams().setQuery(suggestion);
					if (search != null) {
						Db.getHotelSearch().getSearchParams().fillFromSearch(search);
					}
					mLocationEditText.setText(suggestion);
					mLocationEditText.clearFocus();

					// Hide the IME
					Ui.hideKeyboard(mLocationEditText);
				}
			};
		}

	}

	private void initSuggestionViews(View parent, LayoutInflater inflater) {
		// Configure suggestions
		ViewGroup suggestionsContainer = (ViewGroup) parent.findViewById(R.id.suggestions_layout);
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

		configureSuggestions(null);
	}

	private void startAutocomplete(String query) {
		Log.d("Querying autocomplete for: " + query);

		Bundle args = new Bundle();
		args.putString("QUERY", query);
		getLoaderManager().restartLoader(0, args, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// We only have one Loader, so we don't care about the ID.
		String query = args.getString("QUERY");
		if (query == null) {
			query = "";
		}

		Uri suggestUri = AutocompleteProvider.generateSearchUri(getActivity(), query, 5);

		return new CursorLoader(getActivity(), suggestUri, AutocompleteProvider.COLUMNS, null, null, "");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		configureSuggestions(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		configureSuggestions(null);
	}

	private void configureSuggestions(Cursor data) {
		if (!NetUtils.isOnline(getActivity())) {
			// Hide all of the current suggestion rows
			for (SuggestionRow row : mSuggestionRows) {
				row.setVisibility(View.INVISIBLE);
			}

			mSuggestionErrorTextView.setVisibility(View.VISIBLE);
			mSuggestionErrorTextView.setText(R.string.error_no_internet);
			return;
		}
		else if (data == null) {
			mSuggestionErrorTextView.setVisibility(View.VISIBLE);
			mSuggestionErrorTextView.setText(R.string.loading_suggestions);
			return;
		}
		else {
			mSuggestionErrorTextView.setVisibility(View.GONE);
		}

		data.moveToFirst();
		int a = 0;
		while (a < mSuggestionRows.size() && !data.isAfterLast()) {
			Object o = AutocompleteProvider.extractSearchOrString(data);
			if (o != null) {
				mSuggestionRows.get(a).configure(o);
				a++;
			}
			data.moveToNext();
		}
	}

	private TextWatcher mLocationTextWatcher = new TextWatcher() {

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mHasFocusedSearchField = true;

			String location = s.toString().trim();
			HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams();
			if (location.length() == 0 || location.equals(getString(R.string.current_location))) {
				searchParams.setSearchType(SearchType.MY_LOCATION);
				startAutocomplete("");
			}
			else if (!location.equals(searchParams.getQuery())) {
				searchParams.setQuery(location);
				searchParams.setSearchType(SearchType.FREEFORM);

				// If we have a query string, kick off a suggestions request
				// Do it on a delay though - we don't want to update suggestions every time user is edits a single
				// char on the text.
				if (mHasFocusedSearchField) {
					mHandler.removeMessages(WHAT_AUTOCOMPLETE);
					Message msg = new Message();
					msg.obj = location;
					msg.what = WHAT_AUTOCOMPLETE;

					// It's actually ok to do it immediately if the length is short, we know
					// that the provider won't do a network request in this case.
					int delay = location.length() < 3 ? 0 : 1000;

					mHandler.sendMessageDelayed(msg, delay);
				}
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
				startAutocomplete(Db.getHotelSearch().getSearchParams().getQuery());
			}
			else {
				mDetectedInitialConnectivity = true;
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Handler implementation

	private static final int WHAT_AUTOCOMPLETE = 1;

	private static final class LeakSafeHandler extends Handler {
		private WeakReference<SearchParamsFragment> mTarget;

		public LeakSafeHandler(SearchParamsFragment target) {
			mTarget = new WeakReference<SearchParamsFragment>(target);
		}

		@Override
		public void handleMessage(Message msg) {
			SearchParamsFragment target = mTarget.get();
			if (target == null) {
				return;
			}

			if (msg.what == WHAT_AUTOCOMPLETE) {
				final String query = (String) msg.obj;
				target.startAutocomplete(query);
			}
			super.handleMessage(msg);
		}
	}

	private final Handler mHandler = new LeakSafeHandler(this);

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void onResetParams() {
		startAutocomplete(Db.getHotelSearch().getSearchParams().getQuery());
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface SearchParamsFragmentListener {
		public void onSearch();
	}

	//////////////////////////////////////////////////////////////////////////
	// Ominture tracking

	private void onGuestsChanged() {
		Log.d("Tracking \"App.Hotels.Search.Refine.NumberTravelers\" change");

		final String pageName = "App.Hotels.Search.Refine.NumberTravelers."
				+ (mAdultsNumberPicker.getValue() + mChildrenNumberPicker.getValue());

		OmnitureTracking.trackSimpleEvent(getActivity(), pageName, null, null);
	}
}
