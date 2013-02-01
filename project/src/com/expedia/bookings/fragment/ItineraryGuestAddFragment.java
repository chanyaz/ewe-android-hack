package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

public class ItineraryGuestAddFragment extends Fragment {

	public static final String TAG = "ItineraryGuestAddDialogFragment";

	private Button mFindItinBtn;
	private TextView mStatusMessageTv;
	private EditText mEmailEdit;
	private EditText mItinNumEdit;
	private AddGuestItineraryDialogListener mListener;

	public static ItineraryGuestAddFragment newInstance() {
		return new ItineraryGuestAddFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_add_guest_itin, container, false);

		mStatusMessageTv = Ui.findView(view, R.id.itin_heading_textview);
		mFindItinBtn = Ui.findView(view, R.id.find_itinerary_button);
		mEmailEdit = Ui.findView(view, R.id.email_edit_text);
		mItinNumEdit = Ui.findView(view, R.id.itin_number_edit_text);

		FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mFindItinBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mEmailEdit, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mItinNumEdit, Font.ROBOTO_LIGHT);

		initOnClicks();

		return view;
	}

	private void initOnClicks() {
		mFindItinBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String emailAddr = mEmailEdit.getText().toString();
				String itinNumber = mItinNumEdit.getText().toString();
				if (mListener != null) {
					mListener.onFindItinClicked(emailAddr, itinNumber);
				}

				ItineraryManager.getInstance().addGuestTrip(emailAddr, itinNumber, true);
				//TODO: When we understand the flow better, we should hook up an interface instead of finishing the parent
				//We may also want to use some startActivityForResult business. Currently it isn't really clear where we should be
				//waiting for the newly added guest itin to fetch information (or determine validity). To be continued...
				getActivity().finish();

			}

		});
	}

	public void setListener(AddGuestItineraryDialogListener listener) {
		mListener = listener;
	}

	public interface AddGuestItineraryDialogListener {
		public void onFindItinClicked(String email, String itinNumber);

		public void onCancel();
	}

}
