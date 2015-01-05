package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.RoomsAndRatesListActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.utils.Ui;

public class PayLaterInfoFragment extends Fragment {

	private TextView mSelectRoomButton;

	public static PayLaterInfoFragment newInstance() {
		return new PayLaterInfoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_pay_later_info_screen, container, false);

		mSelectRoomButton = Ui.findView(v, R.id.select_room_button);
		if (Db.getHotelSearch().getSelectedProperty().isAvailable()) {
			mSelectRoomButton.setVisibility(View.VISIBLE);
			mSelectRoomButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(RoomsAndRatesListActivity.createIntent(getActivity()));
				}
			});
		}
		else {
			mSelectRoomButton.setVisibility(View.GONE);
		}

		return v;
	}
}
