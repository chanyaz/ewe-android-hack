package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearch.FlightTripQuery;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.FlightAdapter;
import com.mobiata.android.util.Ui;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

// IMPLEMENTATION NOTE: This implementation heavily leans towards the user only picking
// two legs of a flight (outbound and inbound).  If you want to adapt it for 3+ legs, you
// will need to rewrite a good portion of it.
public class FlightListFragment extends ListFragment implements OnScrollListener {

	public static final String TAG = FlightListFragment.class.getName();

	private static final String ARG_LEG_POSITION = "INSTANCE_LEG_POSITION";

	private FlightAdapter mAdapter;

	private FlightListFragmentListener mListener;

	private ListView mListView;
	private TextView mNumFlightsTextView;
	private FlightLegSummarySection mSectionFlightLeg;

	private int mLegPosition;

	private int mFlightListBlurHeight;

	public static FlightListFragment newInstance(int legPosition) {
		FlightListFragment fragment = new FlightListFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mFlightListBlurHeight = (int) getResources().getDimension(R.dimen.flight_list_blur_height);

		mLegPosition = getArguments().getInt(ARG_LEG_POSITION);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof FlightListFragmentListener)) {
			throw new RuntimeException("FlightListFragment activity must implement FlightListFragmentListener!");
		}

		mListener = (FlightListFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// If the parent is finishing already, don't bother with displaying the data
		if (getActivity().isFinishing()) {
			return null;
		}

		final View v = inflater.inflate(R.layout.fragment_flight_list, container, false);

		LayoutUtils.adjustPaddingForOverlayMode(getActivity(), v, false);
		boolean addBottomPadding = LayoutUtils.needsBottomPaddingForOverlay(getActivity(), true);

		// Configure the header
		mListView = Ui.findView(v, android.R.id.list);
		mListView.setDividerHeight(0);
		ViewGroup header = (ViewGroup) inflater.inflate(R.layout.snippet_flight_header, mListView, false);
		mNumFlightsTextView = Ui.findView(header, R.id.num_flights_text_view);
		mNumFlightsTextView.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));
		mSectionFlightLeg = Ui.findView(header, R.id.flight_leg);
		mSectionFlightLeg.setBackgroundResource(R.drawable.bg_flight_card_search_results_top);
		mListView.addHeaderView(header);
		mListView.setHeaderDividersEnabled(false);

		// Configure footer (if we need the extra padding for overlay mode)
		if (addBottomPadding) {
			View footer = inflater.inflate(R.layout.snippet_flight_footer, mListView, false);
			mListView.addFooterView(footer);
			mListView.setFooterDividersEnabled(false);
		}

		// Only dynamically blur background if there is no header
		// flight card being shown.
		if (mLegPosition == 0) {
			mListView.setOnScrollListener(this);
		}
		else {
			mListView.setOnScrollListener(null);
			mListener.onDisableFade();
		}

		// Add the adapter
		mAdapter = new FlightAdapter(getActivity(), savedInstanceState);
		mAdapter.registerDataSetObserver(mDataSetObserver);
		setListAdapter(mAdapter);

		// Setup data
		mAdapter.setLegPosition(mLegPosition);

		mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));

		if (mLegPosition == 0) {
			mSectionFlightLeg.setVisibility(View.INVISIBLE);
		}
		else {
			mSectionFlightLeg.setVisibility(View.VISIBLE);
			FlightSearch search = Db.getFlightSearch();
			FlightTripQuery query = search.queryTrips(0);
			mSectionFlightLeg.bind(null, search.getSelectedLegs()[0].getFlightLeg(), query.getMinTime(),
					query.getMaxTime());
		}

		v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				v.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				mListener.onFlightListLayout(FlightListFragment.this);
			}
		});

		OmnitureTracking.trackPageLoadFlightSearchResults(getActivity(), mLegPosition);

		return v;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		// null check before destroying it. tests seem to get the app in the state where mAdapter == null here sometimes
		if (mAdapter != null) {
			mAdapter.destroy();
			mAdapter = null;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		// Adjust clicks for the header view count
		int numHeaderViews = l.getHeaderViewsCount();
		if (position < numHeaderViews) {
			return;
		}

		// Notify that a flight leg was clicked
		FlightTrip trip = mAdapter.getItem(position - numHeaderViews);
		FlightLeg leg = trip.getLeg(mLegPosition);
		mListener.onFlightLegClick(trip, leg, mLegPosition);

		OmnitureTracking.trackLinkFlightSearchSelect(getActivity(), position - numHeaderViews + 1, mLegPosition);
	}

	//////////////////////////////////////////////////////////////////////////
	// Animators

	private static final float MAX_TRANSLATE_Y_DP = 300;

	public Animator createLegClickAnimator(boolean enter, FlightLeg flightLeg) {
		View v = getView();
		List<Animator> set = new ArrayList<Animator>();
		float[] values = new float[2];

		// Animate each element of the listview away, at relative speeds (the further from position, the faster)
		float maxTranslateY = getResources().getDisplayMetrics().density * MAX_TRANSLATE_Y_DP;
		int skipPosition = mAdapter.getPosition(flightLeg) + mListView.getHeaderViewsCount()
				- mListView.getFirstVisiblePosition();
		Pair<Integer, Integer> cardTopAndBottom = getFlightCardTopAndBottom(flightLeg);
		int targetTop = cardTopAndBottom.first;
		int spaceBelow = mListView.getHeight() - targetTop;
		int childCount = mListView.getChildCount();
		for (int a = 0; a < childCount; a++) {
			View child = mListView.getChildAt(a);
			int childTop = child.getTop();
			if (a < skipPosition) {
				float translation = ((float) (targetTop - childTop) / (float) targetTop) * maxTranslateY;
				if (enter) {
					values[0] = -translation;
					values[1] = 0;
				}
				else {
					values[0] = 0;
					values[1] = -translation;
				}
				set.add(ObjectAnimator.ofFloat(child, "translationY", values));
			}
			else if (a > skipPosition) {
				float translation = ((float) (childTop - targetTop) / (float) spaceBelow) * maxTranslateY;
				if (enter) {
					values[0] = translation;
					values[1] = 0;
				}
				else {
					values[0] = 0;
					values[1] = translation;
				}
				set.add(ObjectAnimator.ofFloat(child, "translationY", values));
			}
		}

		// Fade in/out the entire view
		set.add(AnimUtils.createFadeAnimator(v, enter));

		return AnimUtils.playTogether(set);
	}

	public Animator createLegSelectAnimator(boolean enter) {
		View v = getView();
		List<Animator> set = new ArrayList<Animator>();
		float[] values;

		// Animate the entire listview up (except header)
		int translate = mListView.getHeight() - mListView.getChildAt(0).getHeight();
		int childCount = mListView.getChildCount();
		values = (enter) ? new float[] { translate, 0 } : new float[] { 0, translate };
		for (int a = 1; a < childCount; a++) {
			set.add(ObjectAnimator.ofFloat(mListView.getChildAt(a), "translationY", values));
		}

		// Fade in/out the entire view
		set.add(AnimUtils.createFadeAnimator(v, enter));

		// Create AnimatorSet and return
		return AnimUtils.playTogether(set);
	}

	// Used to find out where to animate to/from a card in the list
	public Pair<Integer, Integer> getFlightCardTopAndBottom(FlightLeg flightLeg) {
		int position = mAdapter.getPosition(flightLeg) + mListView.getHeaderViewsCount();
		int firstPosition = mListView.getFirstVisiblePosition();
		int lastPosition = mListView.getLastVisiblePosition();

		if (position >= firstPosition && position <= lastPosition) {
			View v = mListView.getChildAt(position - firstPosition);
			return new Pair<Integer, Integer>(v.getTop(), v.getBottom());
		}
		else {
			// Find the first visible card and use that as measurement
			int headerCount = mListView.getHeaderViewsCount();
			int targetPosition = (firstPosition < headerCount) ? headerCount - firstPosition : 0;
			View v = mListView.getChildAt(targetPosition);
			int cardHeight = v.getHeight();

			// Return a set of top/bottom just above or just below the ListView
			if (position < firstPosition) {
				return new Pair<Integer, Integer>(-cardHeight, 0);
			}
			else {
				int listViewHeight = mListView.getHeight();
				return new Pair<Integer, Integer>(listViewHeight, listViewHeight + cardHeight);
			}
		}
	}

	public Pair<Integer, Integer> getSelectedFlightCardTopAndBottom() {
		return new Pair<Integer, Integer>(mSectionFlightLeg.getTop(), mSectionFlightLeg.getBottom());
	}

	//////////////////////////////////////////////////////////////////////////
	// Header control

	private void displayNumFlights() {
		if (mNumFlightsTextView != null) {
			int count = mAdapter.getCount();
			if (count == 0) {
				mNumFlightsTextView.setText(null);
			}
			else {
				FlightSearchParams params = Db.getFlightSearch().getSearchParams();
				Location location = (mLegPosition == 0) ? params.getArrivalLocation() : params.getDepartureLocation();
				boolean returnFlight = mLegPosition > 0;
				if (returnFlight) {
					mNumFlightsTextView.setText((getResources().getString(R.string.select_a_flight_back_to_TEMPLATE,
							StrUtils.getLocationCityOrCode(location))).toUpperCase());
				}
				else {
					mNumFlightsTextView.setText((getResources().getString(R.string.select_a_flight_to_TEMPLATE,
							StrUtils.getLocationCityOrCode(location))).toUpperCase());
				}
			}
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Dataset observer

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			displayNumFlights();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// FlightListFragmentListener

	public interface FlightListFragmentListener {
		public void onFlightListLayout(FlightListFragment fragment);

		public void onFlightLegClick(FlightTrip trip, FlightLeg leg, int legPosition);

		public void onDisableFade();

		public void onFadeRangeChange(int startY, int endY);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnScrollListener

	// Cached for faster processing
	private int mNumFlightsTextViewBottom = 0;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view.getChildCount() > 0) {
			if (firstVisibleItem == 0) {
				if (mNumFlightsTextViewBottom == 0) {
					mNumFlightsTextViewBottom = mNumFlightsTextView.getBottom();
				}

				int bottom = mNumFlightsTextViewBottom + view.getTop() + view.getChildAt(0).getTop();
				mListener.onFadeRangeChange(bottom - mFlightListBlurHeight, bottom);
			}
			else {
				mListener.onDisableFade();
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// Do nothing
	}
}
