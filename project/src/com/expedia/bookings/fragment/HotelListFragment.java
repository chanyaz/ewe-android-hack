package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.activity.TabletActivity.EventHandler;
import com.expedia.bookings.data.Filter.OnFilterChangedListener;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.widget.HotelAdapter;

public class HotelListFragment extends ListFragment implements EventHandler, OnFilterChangedListener {

	private HotelAdapter mAdapter;

	private ViewGroup mHeaderLayout;
	private TextView mNumHotelsTexView;
	private TextView mMessageTextView;
	
	public static HotelListFragment newInstance() {
		return new HotelListFragment();
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new HotelAdapter(getActivity());
		setListAdapter(mAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_list, container, false);

		mHeaderLayout = (ViewGroup) view.findViewById(R.id.header_layout);
		mNumHotelsTexView = (TextView) view.findViewById(R.id.num_hotels_text_view);
		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// ListFragment overrides

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		((TabletActivity) getActivity()).propertySelected((Property) mAdapter.getItem(position));
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_SEARCH_STARTED:
			mAdapter.setSearchResponse(null);
			mMessageTextView.setText(R.string.progress_searching_hotels);
			mHeaderLayout.setVisibility(View.GONE);
			break;
		case TabletActivity.EVENT_SEARCH_PROGRESS:
			mMessageTextView.setText((String) data);
			break;
		case TabletActivity.EVENT_SEARCH_COMPLETE:
			SearchResponse searchResponse = (SearchResponse) data;
			mAdapter.setSearchResponse(searchResponse);
			searchResponse.getFilter().addOnFilterChangedListener(this);
			updateNumHotels();
			mHeaderLayout.setVisibility(View.VISIBLE);
			break;
		case TabletActivity.EVENT_SEARCH_ERROR:
			mMessageTextView.setText((String) data);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// OnFilterChangedListener implementation

	@Override
	public void onFilterChanged() {
		updateNumHotels();
	}

	private void updateNumHotels() {
		int count = mAdapter.getCount();
		mNumHotelsTexView.setText(Html.fromHtml(getResources().getQuantityString(R.plurals.number_of_results, count,
				count)));
	}

//	//////////////////////////////////////////////////////////////////////////
//	// List implementation
//
//	@Override
//	public void onListItemClick(ListView l, View v, int position, long id) {
//		Property property = (Property) mAdapter.getItem(position);
//		Bundle arguments = new Bundle();
//		arguments.putString(Codes.PROPERTY, property.toJson().toString());
//		mActivity.showHotelDetails(property);
//	}
//	
	
}
