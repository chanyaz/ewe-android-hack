package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TextView;

public class SweepstakesFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_sweepstakes, container, false);

		FontCache.setTypeface((TextView) view.findViewById(R.id.enter_title_text_view), Font.BEBAS_NEUE);
		FontCache.setTypeface((TextView) view.findViewById(R.id.confirmation_title_text_view), Font.BEBAS_NEUE);

		Ui.setOnClickListener(view, R.id.no_thanks_button, mOnClickListener);
		Ui.setOnClickListener(view, R.id.enter_button, mOnClickListener);
		Ui.setOnClickListener(view, R.id.done_button, mOnClickListener);

		return view;
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.no_thanks_button: {
                getActivity().finish();
				break;
			}
			case R.id.enter_button: {
				break;
			}
			case R.id.done_button: {
                getActivity().finish();
				break;
			}
			}
		}
	};
}
