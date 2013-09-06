package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.ReceiptWidget;

@TargetApi(11)
public class BookingInfoFragment extends Fragment {

	private Button mCompleteBookingInfoButton;

	private ReceiptWidget mReceiptWidget;

	private BookingInfoFragmentListener mListener;

	public static BookingInfoFragment newInstance() {
		BookingInfoFragment fragment = new BookingInfoFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof BookingInfoFragmentListener)) {
			throw new RuntimeException("BookingInfoFragment Activity must implement BookingInfoFragmentListener!");
		}

		mListener = (BookingInfoFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_booking, container, false);

		mCompleteBookingInfoButton = (Button) view.findViewById(R.id.complete_booking_info_button);
		mCompleteBookingInfoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mListener.onEnterBookingInfoClick();
			}
		});

		mReceiptWidget = new ReceiptWidget(getActivity(), view.findViewById(R.id.receipt), false);

		Rate selectedRate = Db.getHotelSearch().getSelectedRate();
		if (selectedRate != null) {
			updateReceipt();
			updateRoomDescription(view);
		}

		return view;
	}

	private void updateRoomDescription(View view) {
		if (view == null) {
			return;
		}

		Rate rate = Db.getHotelSearch().getSelectedRate();
		TextView roomTypeDescriptionTitleTextView = (TextView) view.findViewById(R.id.room_type_description_title_view);
		roomTypeDescriptionTitleTextView.setText(rate.getRatePlanName());

		TextView roomTypeDescriptionTextView = (TextView) view.findViewById(R.id.room_type_description_text_view);
		LayoutUtils.layoutRoomLongDescription(getActivity(), rate, roomTypeDescriptionTextView);
	}

	private void updateReceipt() {
		Rate selectedRate = Db.getHotelSearch().getSelectedRate();
		mReceiptWidget.updateData(Db.getHotelSearch().getSelectedProperty(), Db.getHotelSearch().getSearchParams(), selectedRate);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void notifyRateSelected() {
		mCompleteBookingInfoButton.setEnabled(true);
		updateReceipt();
		updateRoomDescription(getView());
	}

	public void notifyNoRates() {
		mCompleteBookingInfoButton.setEnabled(false);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface BookingInfoFragmentListener {
		public void onEnterBookingInfoClick();
	}
}
