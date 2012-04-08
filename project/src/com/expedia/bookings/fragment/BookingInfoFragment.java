package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.ReceiptWidget;

public class BookingInfoFragment extends Fragment {

	private View mCompleteBookingInfoButton;

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
		View view = inflater.inflate(R.layout.fragment_booking, container, false);

		mCompleteBookingInfoButton = view.findViewById(R.id.complete_booking_info_button);
		mCompleteBookingInfoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mListener.onEnterBookingInfoClick();
			}
		});

		mReceiptWidget = new ReceiptWidget(getActivity(), view.findViewById(R.id.receipt), false);

		TextView contactView = (TextView) view.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		updateReceipt();
		updateRoomDescription(view);
		ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), view);

		final View receipt = view.findViewById(R.id.receipt);
		final View roomDetailsContainer = view.findViewById(R.id.room_details_container_right);

		// 10886: Bottom aligning the complete booking info button
		// with the fragment receipt as long as the fragment receipt is 
		// last than the max defined height. In that case, using the maximum
		// height definition.
		if (roomDetailsContainer != null) {
			receipt.addOnLayoutChangeListener(new OnLayoutChangeListener() {

				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
						int oldRight, int oldBottom) {
					if (left == 0 && top == 0 && right == 0 && bottom == 0) {
						return;
					}

					int maxHeightInPx = (int) Math.ceil(getResources().getDisplayMetrics().density
							* getResources().getDimension(R.dimen.max_height_room_details_container));
					if (receipt.getMeasuredHeight() > maxHeightInPx) {
						((RelativeLayout.LayoutParams) roomDetailsContainer.getLayoutParams()).height = LayoutParams.WRAP_CONTENT;
						((RelativeLayout.LayoutParams) roomDetailsContainer.getLayoutParams()).addRule(
								RelativeLayout.ALIGN_BOTTOM, 0);
					}
					else {
						((RelativeLayout.LayoutParams) roomDetailsContainer.getLayoutParams()).addRule(
								RelativeLayout.ALIGN_BOTTOM, R.id.receipt);
					}

				}
			});
		}
		return view;
	}

	private void updateRoomDescription(View view) {
		if (view == null) {
			return;
		}

		Rate rate = Db.getSelectedRate();
		TextView roomTypeDescriptionTitleTextView = (TextView) view.findViewById(R.id.room_type_description_title_view);
		roomTypeDescriptionTitleTextView.setText(rate.getRatePlanName());

		TextView roomTypeDescriptionTextView = (TextView) view.findViewById(R.id.room_type_description_text_view);
		LayoutUtils.layoutRoomLongDescription(getActivity(), rate, roomTypeDescriptionTextView);
	}

	private void updateReceipt() {
		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate());
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void notifyRateSelected() {
		updateReceipt();
		updateRoomDescription(getView());
		ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), getView());
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface BookingInfoFragmentListener {
		public void onEnterBookingInfoClick();
	}
}
