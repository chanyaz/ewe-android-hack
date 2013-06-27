package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.RoomsAndRatesFragmentActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.dialog.HotelSoldOutDialog;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.RoomsAndRatesAdapter;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

public class RoomsAndRatesFragment extends ListFragment {

	public static RoomsAndRatesFragment newInstance() {
		RoomsAndRatesFragment fragment = new RoomsAndRatesFragment();
		return fragment;
	}

	private RoomsAndRatesFragmentListener mListener;

	private RoomsAndRatesAdapter mAdapter;

	private ProgressBar mProgressBar;
	private TextView mEmptyTextView;
	private TextView mFooterTextView;

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
		mProgressBar = Ui.findView(view, R.id.progress_bar);
		mEmptyTextView = Ui.findView(view, R.id.empty_text_view);

		// Setup the ListView
		View footer = inflater.inflate(R.layout.footer_rooms_and_rates, null);
		mFooterTextView = (TextView) footer.findViewById(R.id.footer_text_view);
		((ListView) view.findViewById(android.R.id.list)).addFooterView(footer, null, false);
		mFooterTextView.setVisibility(View.GONE);

		// Hide the header if this is not the tablet
		if (!AndroidUtils.isHoneycombTablet(getActivity())) {
			Ui.findView(view, R.id.header_layout).setVisibility(View.GONE);
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
	// Control

	public void showProgress() {
		mProgressBar.setVisibility(View.VISIBLE);
		mEmptyTextView.setText(R.string.room_rates_loading);
	}

	public void notifyAvailabilityLoaded() {
		String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		HotelOffersResponse response = Db.getHotelSearch().getHotelOffersResponse(selectedId);

		mProgressBar.setVisibility(View.GONE);

		if (response == null) {
			OmnitureTracking.trackErrorPage(getActivity(), "RatesListRequestFailed");
			mEmptyTextView.setText(R.string.error_no_response_room_rates);
			return;
		}
		else if (response.getRateCount() == 0) {
			Db.getHotelSearch().removeProperty(selectedId);

			HotelSoldOutDialog dialog = HotelSoldOutDialog.newInstance();
			dialog.show(getFragmentManager(), "soldOutDialog");

			mEmptyTextView.setText(R.string.error_no_hotel_rooms_available);
			mAdapter = null;
			setListAdapter(null);
			mListener.noRatesAvailable();
			return;
		}

		loadResponse(response);
	}

	private void loadResponse(HotelOffersResponse response) {
		if (response.hasErrors()) {
			StringBuilder sb = new StringBuilder();
			for (ServerError error : response.getErrors()) {
				sb.append(error.getPresentableMessage(getActivity()));
				sb.append("\n");
			}
			mEmptyTextView.setText(sb.toString().trim());
			OmnitureTracking.trackErrorPage(getActivity(), "RatesListRequestFailed");
			return;
		}

		mAdapter = new RoomsAndRatesAdapter(getActivity(), response);

		final String selectedId = Db.getHotelSearch().getSelectedProperty().getPropertyId();
		final Rate selectedRate = Db.getHotelSearch().getAvailability(selectedId).getSelectedRate();
		if (selectedRate == null || getPositionOfRate(selectedRate) == -1) {
			mAdapter.setSelectedPosition(0);
			if (mListener instanceof RoomsAndRatesFragmentActivity) {
				if (mAdapter.getCount() > 0) {
					mListener.onRateSelected((Rate) mAdapter.getItem(0));
				}
				else {
					mListener.noRatesAvailable();
				}
			}
		}
		else {
			mAdapter.setSelectedPosition(getPositionOfRate(selectedRate));
		}
		setListAdapter(mAdapter);

		// Disable highlighting if we're on phone UI
		mAdapter.highlightSelectedPosition(AndroidUtils.isHoneycombTablet(getActivity()));

		CharSequence commonValueAdds = response.getCommonValueAddsString(getActivity());
		if (commonValueAdds != null) {
			mFooterTextView.setText(commonValueAdds);
			mFooterTextView.setVisibility(View.VISIBLE);
		}

		if (mAdapter.getCount() == 0) {
			OmnitureTracking.trackErrorPage(getActivity(), "HotelHasNoRoomsAvailable");
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface RoomsAndRatesFragmentListener {
		public void onRateSelected(Rate rate);
		public void noRatesAvailable();
	}
}
