package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.AvailabilityResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;

public class RoomsAndRatesFragment extends ListFragment {

	public static RoomsAndRatesFragment newInstance() {
		RoomsAndRatesFragment fragment = new RoomsAndRatesFragment();
		return fragment;
	}

	private RoomsAndRatesFragmentListener mListener;

	private RoomsAndRatesAdapter mAdapter;
	private TextView mMessageTextView;
	private TextView mFooterTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AvailabilityResponse response = Db.getSelectedAvailabilityResponse();
		if (response != null) {
			mAdapter = new RoomsAndRatesAdapter(getActivity(), response);
			mAdapter.setSelectedPosition(getPositionOfRate(Db.getSelectedRate()));
			setListAdapter(mAdapter);
		}
		else {
			mMessageTextView.setText(getString(R.string.room_rates_loading));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof RoomsAndRatesFragmentListener)) {
			throw new RuntimeException("RoomsAndRatesFragment Activity must implement RoomsAndRatesFragmentListener!");
		}

		mListener = (RoomsAndRatesFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_availability_list, container, false);
		mMessageTextView = (TextView) view.findViewById(android.R.id.empty);

		// Setup the ListView
		View footer = inflater.inflate(R.layout.footer_rooms_and_rates, null);
		mFooterTextView = (TextView) footer.findViewById(R.id.footer_text_view);
		((ListView) view.findViewById(android.R.id.list)).addFooterView(footer, null, false);

		AvailabilityResponse response = Db.getSelectedAvailabilityResponse();
		mFooterTextView.setVisibility(View.GONE);
		if (response != null) {
			CharSequence commonValueAdds = response.getCommonValueAddsString(getActivity());
			if (commonValueAdds != null) {
				mFooterTextView.setText(commonValueAdds);
				mFooterTextView.setVisibility(View.VISIBLE);
			}
		}

		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		mListener.onRateSelected((Rate) mAdapter.getItem(position));

		mAdapter.setSelectedPosition(position);
		mAdapter.notifyDataSetChanged();
	}

	private int getPositionOfRate(Rate rate) {
		if (rate != null) {
			int count = mAdapter.getCount();
			for (int position = 0; position < count; position++) {
				Object item = mAdapter.getItem(position);
				if (item != null && rate.equals(item)) {
					return position;
				}
			}
		}
		return -1;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface RoomsAndRatesFragmentListener {
		public void onRateSelected(Rate rate);
	}
}
