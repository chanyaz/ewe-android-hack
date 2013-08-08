package com.expedia.bookings.dialog;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.AddFlightsIntentUtils;

public class SocialMessageChooserDialogFragment extends DialogFragment {

	private ItinContentGenerator mItinContentGenerator;

	private String mSubject;
	private String mShortMessage;
	private String mLongMessage;

	private TripComponent.Type mType;

	public static SocialMessageChooserDialogFragment newInstance(ItinContentGenerator<?> generator) {
		String subject = generator.getShareSubject();
		String shortMessage = generator.getShareTextShort();
		String longMessage = generator.getShareTextLong();
		TripComponent.Type type = generator.getType();

		return newInstance(generator, subject, shortMessage, longMessage, type);
	}

	private static SocialMessageChooserDialogFragment newInstance(ItinContentGenerator<?> generator, String subject,
			String shortMessage, String longMessage, TripComponent.Type type) {
		SocialMessageChooserDialogFragment fragment = new SocialMessageChooserDialogFragment();

		fragment.mItinContentGenerator = generator;
		fragment.mSubject = subject;
		fragment.mShortMessage = shortMessage;
		fragment.mLongMessage = longMessage;
		fragment.mType = type;

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NO_FRAME, R.style.SocialMessageChooserDialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_message_style_chooser, container, false);

		Ui.findView(view, R.id.long_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.email(getActivity(), mSubject, mLongMessage);
				dismiss();

				OmnitureTracking.trackItinShare(getActivity(), mType, true);
			}
		});

		Ui.findView(view, R.id.short_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.share(getActivity(), mSubject, mShortMessage);
				dismiss();

				OmnitureTracking.trackItinShare(getActivity(), mType, false);
			}
		});

		// Share with FlightTrack
		if (mItinContentGenerator instanceof FlightItinContentGenerator) {
			// Grab the Flight segments for the given leg/itin
			ItinCardDataFlight cardData = (ItinCardDataFlight) mItinContentGenerator.getItinCardData();
			FlightTrip flightTrip = ((TripFlight) cardData.getTripComponent()).getFlightTrip();
			List<Flight> flights = flightTrip.getLeg(cardData.getLegNumber()).getSegments();
			final Intent intent = AddFlightsIntentUtils.getIntent(flights);

			if (NavUtils.canHandleIntent(getActivity(), intent)) {
				View ft = Ui.findView(view, R.id.flighttrack_button);
				ft.setVisibility(View.VISIBLE);
				ft.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						startActivity(intent);
						dismiss();
					}
				});
			}
		}

		return view;
	}
}
