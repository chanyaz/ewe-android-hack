package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

/**
 * Created by sshanthakumar on 5/21/14.
 */
public class BookingUnavailableFragment extends LobableFragment {

	public static final String TAG = BookingUnavailableFragment.class.getName();

	private TextView mSoldOutText;
	private Button mRemoveItemButton;
	private Button mSelectNewItemButton;

	public static BookingUnavailableFragment newInstance() {
		return new BookingUnavailableFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_sold_out, container, false);
		mSoldOutText = Ui.findView(v, R.id.sold_out_text_view);
		mRemoveItemButton = Ui.findView(v, R.id.remove_sold_out_button);
		mSelectNewItemButton = Ui.findView(v, R.id.select_new_item_button);
		updateViews();
		return v;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
	}

	private void updateViews() {
		if (getLob() == LineOfBusiness.HOTELS) {
			mSoldOutText.setText(getString(R.string.tablet_sold_out_summary_text_hotel));
			mRemoveItemButton.setText(getString(R.string.tablet_sold_out_remove_hotel));
			mSelectNewItemButton.setText(getString(R.string.tablet_sold_out_select_hotel));
		}
		else {
			mSoldOutText.setText(getString(R.string.tablet_sold_out_summary_text_flight));
			mRemoveItemButton.setText(getString(R.string.tablet_sold_out_remove_flight));
			mSelectNewItemButton.setText(getString(R.string.tablet_sold_out_select_flight));
		}
	}
}
