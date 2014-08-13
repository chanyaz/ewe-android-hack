package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

/**
 * Created by sshanthakumar on 5/21/14.
 */
public class BookingUnavailableFragment extends LobableFragment {

	public static final String TAG = BookingUnavailableFragment.class.getName();

	private View mRootC;
	private TextView mSoldOutText;
	private Button mSelectNewItemButton;

	private BookingUnavailableFragmentListener mListener;


	public static BookingUnavailableFragment newInstance() {
		return new BookingUnavailableFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = inflater.inflate(R.layout.fragment_sold_out, container, false);
		mSoldOutText = Ui.findView(mRootC, R.id.sold_out_text_view);
		mSelectNewItemButton = Ui.findView(mRootC, R.id.select_new_item_button);
		mSelectNewItemButton.setOnClickListener(mClickListener);
		FontCache.setTypeface(mSelectNewItemButton, R.id.select_new_item_button, FontCache.Font.ROBOTO_LIGHT);
		updateViews();
		return mRootC;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, BookingUnavailableFragmentListener.class);
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (isAdded() && mRootC != null) {
			updateViews();
		}
	}

	private View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			LineOfBusiness lob = getLob();
			Db.getTripBucket().clear(lob);
			if (mListener != null) {
				mListener.onSelectNewTripItem(lob);
			}
		}
	};

	private void updateViews() {
		if (getLob() == LineOfBusiness.HOTELS) {
			if (Db.getTripBucket().getHotel().getState() == TripBucketItemState.EXPIRED) {
				mSoldOutText.setText(getString(R.string.tablet_expired_summary_text_hotel));
			}
			else {
				mSoldOutText.setText(getString(R.string.tablet_sold_out_summary_text_hotel));
			}
			mSelectNewItemButton.setText(getString(R.string.tablet_sold_out_select_hotel));
		}
		else {
			boolean isPlural = Db.getFlightSearch().getSearchParams().getQueryLegCount() != 1;
			if (Db.getTripBucket().getFlight().getState() == TripBucketItemState.EXPIRED) {
				if (isPlural) {
					mSoldOutText.setText(getString(R.string.error_flights_hold_expired));
				}
				else {
					mSoldOutText.setText(getString(R.string.error_flight_hold_expired));
				}
			}
			else {
				if (isPlural) {
					mSoldOutText.setText(getString(R.string.tablet_sold_out_summary_text_flight));
				}
				else {
					mSoldOutText.setText(getString(R.string.tablet_sold_out_summary_text_flights));
				}
			}

			mSelectNewItemButton.setText(getString(R.string.tablet_sold_out_select_flight));
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// BookingUnavailableFragmentListener

	public interface BookingUnavailableFragmentListener {
		public void onTripBucketItemRemoved(LineOfBusiness lob);

		public void onSelectNewTripItem(LineOfBusiness lob);
	}
}
