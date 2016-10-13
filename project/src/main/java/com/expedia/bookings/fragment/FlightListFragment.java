package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.tracking.AdTracker;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FlightAdapter;

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
	private TextView mPriceLabelTextView;
	private FlightLegSummarySection mSectionFlightLeg;

	private int mLegPosition;

	private int mFlightListBlurHeight;

	private boolean mIsLandscape;

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

		mIsLandscape = getResources().getBoolean(R.bool.landscape);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mListener = Ui.findFragmentListener(this, FlightListFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// If the parent is finishing already, don't bother with displaying the data
		if (getActivity().isFinishing()) {
			return null;
		}

		final View v = inflater.inflate(R.layout.fragment_flight_list, container, false);

		// Configure the header
		mListView = Ui.findView(v, android.R.id.list);
		mListView.setDividerHeight(0);
		ViewGroup header = Ui.inflate(inflater, R.layout.snippet_flight_header, mListView, false);
		mNumFlightsTextView = Ui.findView(header, R.id.num_flights_text_view);
		mPriceLabelTextView = Ui.findView(header, R.id.flight_price_label_text_view);
		mSectionFlightLeg = Ui.findView(header, R.id.flight_leg);
		mSectionFlightLeg.setBackgroundResource(R.drawable.bg_flight_card_search_results_top);
		mListView.addHeaderView(header);
		mListView.setHeaderDividersEnabled(false);

		if (PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()) {
			TextView airlineFeeBar = Ui.findView(v, R.id.airline_fee_bar);
			airlineFeeBar.setVisibility(View.VISIBLE);
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
		mAdapter = new FlightAdapter();
		mAdapter.registerDataSetObserver(mDataSetObserver);
		setListAdapter(mAdapter);

		// Setup data
		mAdapter.setLegPosition(mLegPosition);

		if (mLegPosition > 0) {
			FlightTripQuery previousQuery = Db.getFlightSearch().queryTrips(mLegPosition - 1);
			mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition), previousQuery.getMinTime(),
				previousQuery.getMaxTime());
		}
		else {
			mAdapter.setFlightTripQuery(Db.getFlightSearch().queryTrips(mLegPosition));
		}

		if (mLegPosition == 0) {
			if (mIsLandscape) {
				// F1151: Don't make space for the header leg in landscape orientation,
				// so that it looks prettier.
				mSectionFlightLeg.setVisibility(View.GONE);
			}
			else {
				// F1295: Even though it's invisible, bind it some random FlightLeg.  Otherwise,
				// the FlightLegSummarySection won't take up as much space as it will when we
				// are actually going to show it later.
				mSectionFlightLeg.bind(Db.getFlightSearch().queryTrips(0).getTrips().get(0).getLeg(0));
				mSectionFlightLeg.setVisibility(View.INVISIBLE);
			}
		}
		else {
			mSectionFlightLeg.setVisibility(View.VISIBLE);
			FlightSearch search = Db.getFlightSearch();
			FlightTripQuery query = search.queryTrips(0);
			mSectionFlightLeg.bind(null, search.getSelectedLegs()[0].getFlightLeg(), query.getMinTime(),
				query.getMaxTime());
		}

		Ui.runOnNextLayout(v, new Runnable() {
			public void run() {
				mListener.onFlightListLayout(FlightListFragment.this);
			}
		});

		OmnitureTracking.trackPageLoadFlightSearchResults(mLegPosition);
		AdTracker.trackPageLoadFlightSearchResults(mLegPosition);

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

		if (mListView != null) {
			mListView.setOnScrollListener(null);
			mListView = null;
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

		OmnitureTracking.trackLinkFlightSearchSelect(position - numHeaderViews + 1, mLegPosition);
	}

	//////////////////////////////////////////////////////////////////////////
	// Animators

	private static final float MAX_TRANSLATE_Y_DP = 300;

	public Animator createLegClickAnimator(boolean enter, FlightLeg flightLeg) {
		View v = getView();
		List<Animator> set = new ArrayList<Animator>();
		final List<View> hwLayerViews = new ArrayList<View>();
		float[] values = new float[2];

		if (v != null && mAdapter != null && mListView != null) {
			PropertyValuesHolder pvhAlpha = AnimUtils.createFadePropertyValuesHolder(enter);

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

					PropertyValuesHolder pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);
					set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha, pvhTranslation));

					hwLayerViews.add(child);
				}
				else if (a == skipPosition) {
					set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha));

					hwLayerViews.add(child);
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

					PropertyValuesHolder pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);
					set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha, pvhTranslation));

					hwLayerViews.add(child);
				}
			}
		}

		AnimatorSet animSet = AnimUtils.playTogether(set);

		animSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			}
		});

		return animSet;
	}

	public Animator createLegSelectAnimator(boolean enter) {
		View v = getView();
		List<Animator> set = new ArrayList<Animator>();
		final List<View> hwLayerViews = new ArrayList<View>();
		float[] values;

		if (v != null && mAdapter != null && mListView != null) {
			// Animate the entire listview up (except header)
			int translate = mListView.getHeight() - mListView.getChildAt(0).getHeight();
			int childCount = mListView.getChildCount();
			values = enter
				? new float[] { translate, 0 }
				: new float[] { 0, translate };
			PropertyValuesHolder pvhAlpha = AnimUtils.createFadePropertyValuesHolder(enter);
			PropertyValuesHolder pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);
			for (int a = 1; a < childCount; a++) {
				View child = mListView.getChildAt(a);
				set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha, pvhTranslation));

				hwLayerViews.add(child);
			}

			// Alpha in the header
			View header = mListView.getChildAt(0);
			set.add(AnimUtils.ofPropertyValuesHolder(header, pvhAlpha));

			hwLayerViews.add(header);
		}

		// Create AnimatorSet and return
		AnimatorSet animSet = AnimUtils.playTogether(set);

		animSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			}
		});

		return animSet;
	}

	// Used to find out where to animate to/from a card in the list
	public Pair<Integer, Integer> getFlightCardTopAndBottom(FlightLeg flightLeg) {
		if (flightLeg != null && mAdapter != null && mListView != null) {

			FlightAdapter adapter = mAdapter;
			ListView listView = mListView;

			int position = adapter.getPosition(flightLeg) + listView.getHeaderViewsCount();
			int firstPosition = listView.getFirstVisiblePosition();
			int lastPosition = listView.getLastVisiblePosition();

			if (position >= firstPosition && position <= lastPosition) {
				View v = listView.getChildAt(position - firstPosition);
				// F1302: Need to account for top padding in listview when calculating top/bottom
				int paddingTop = listView.getPaddingTop();
				if (v != null) {
					return new Pair<>(v.getTop() - paddingTop, v.getBottom() - paddingTop);
				}
			}
			else {
				// Find the first visible card and use that as measurement
				int headerCount = listView.getHeaderViewsCount();
				int targetPosition = (firstPosition < headerCount) ? headerCount - firstPosition : 0;
				View v = listView.getChildAt(targetPosition);
				int cardHeight = v != null ? v.getHeight() : 0;

				// Return a set of top/bottom just above or just below the ListView
				if (position < firstPosition) {
					return new Pair<>(-cardHeight, 0);
				}
				else {
					int listViewHeight = listView.getHeight();
					return new Pair<>(listViewHeight, listViewHeight + cardHeight);
				}
			}
		}

		//By default we return 0,0 which wont look great, but is valid
		return new Pair<>(0, 0);

	}

	public Pair<Integer, Integer> getSelectedFlightCardTopAndBottom() {
		return new Pair<>(mSectionFlightLeg.getTop(), mSectionFlightLeg.getBottom());
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
				String code = StrUtils.getLocationCityOrCode(location).toUpperCase(Locale.getDefault());
				int templateResId = returnFlight
					? R.string.select_a_flight_back_to_TEMPLATE
					: R.string.select_a_flight_to_TEMPLATE;
				mNumFlightsTextView.setText(getResources().getString(templateResId, code));
			}
		}
	}

	private void displayPriceLabel() {
		if (mPriceLabelTextView != null) {
			int labelResId;
			if (PointOfSale.getPointOfSale().doAirlinesChargeAdditionalFeeBasedOnPaymentMethod()) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					labelResId = R.string.prices_roundtrip_minimum_label;
				}
				else {
					labelResId = R.string.prices_oneway_minimum_label;
				}
			}
			else {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					labelResId = R.string.prices_roundtrip_label;
				}
				else {
					labelResId = R.string.prices_oneway_label;
				}
			}
			mPriceLabelTextView.setText(getString(labelResId));
		}
	}
	//////////////////////////////////////////////////////////////////////////
	// Dataset observer

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			displayNumFlights();
			displayPriceLabel();
		}
	};

//////////////////////////////////////////////////////////////////////////
// FlightListFragmentListener

	public interface FlightListFragmentListener {
		void onFlightListLayout(FlightListFragment fragment);

		void onFlightLegClick(FlightTrip trip, FlightLeg leg, int legPosition);

		void onDisableFade();

		void onFadeRangeChange(int startY, int endY);
	}

	//////////////////////////////////////////////////////////////////////////
	// OnScrollListener

	// Cached for faster processing
	private int mHeaderBottom = 0;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (view.getChildCount() > 0) {
			if (firstVisibleItem == 0 && !mIsLandscape) {
				if (mHeaderBottom == 0) {
					mHeaderBottom = mPriceLabelTextView.getBottom();
				}

				int bottom = mHeaderBottom + view.getTop() + view.getChildAt(0).getTop();
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
