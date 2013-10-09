package com.expedia.bookings.dialog;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FacebookShareActivity;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataActivity;
import com.expedia.bookings.data.trips.ItinCardDataCar;
import com.expedia.bookings.data.trips.ItinCardDataFlight;
import com.expedia.bookings.data.trips.ItinCardDataHotel;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.data.trips.TripFlight;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.itin.ActivityItinContentGenerator;
import com.expedia.bookings.widget.itin.CarItinContentGenerator;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.expedia.bookings.widget.itin.HotelItinContentGenerator;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.facebook.Session;
import com.facebook.SessionState;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.AddFlightsIntentUtils;

public class SocialMessageChooserDialogFragment extends DialogFragment {

	private ItinContentGenerator<? extends ItinCardData> mItinContentGenerator;

	private String mSubject;
	private String mShortMessage;
	private String mLongMessage;

	private TripComponent.Type mType;

	public static SocialMessageChooserDialogFragment newInstance(ItinContentGenerator<? extends ItinCardData> generator) {
		SocialMessageChooserDialogFragment fragment = new SocialMessageChooserDialogFragment();

		fragment.mItinContentGenerator = generator;
		fragment.mSubject = generator.getShareSubject();
		fragment.mShortMessage = generator.getShareTextShort();
		fragment.mLongMessage = generator.getShareTextLong();
		fragment.mType = generator.getType();

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

		if (AndroidUtils.isPackageInstalled(getActivity(), "com.facebook.katana")) {
			View facebookButton = Ui.findView(view, R.id.facebook_button);
			facebookButton.setVisibility(View.VISIBLE);
			facebookButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//dismiss();

					Session currentSession = Session.getActiveSession();
					if (currentSession == null || currentSession.getState().isClosed()) {
						Session session = new Session.Builder(getActivity()).setApplicationId(
								ExpediaServices.getFacebookAppId(getActivity())).build();
						Session.setActiveSession(session);
						currentSession = session;
					}
					if (!currentSession.isOpened()) {
						Log.d("FB: doFacebookLogin - !currentSession.isOpened()");
						Session.OpenRequest openRequest = null;

						openRequest = new Session.OpenRequest(SocialMessageChooserDialogFragment.this);

						//We need an email address to do any sort of Expedia account creation/linking
						List<String> permissions = new ArrayList<String>();
						permissions.add("email");

						if (openRequest != null) {
							openRequest.setPermissions(permissions);
							currentSession.addCallback(mFacebookStatusCallback);
							currentSession.openForRead(openRequest);
						}
					}
					else {
						Log.d("FB: doFacebookLogin - currentSession.isOpened()");
						postToFacebook();
					}

					OmnitureTracking.trackItinShare(getActivity(), mType, false);
				}
			});
		}

		// Share with FlightTrack
		if (mItinContentGenerator instanceof FlightItinContentGenerator) {
			final Intent intent = ((FlightItinContentGenerator) mItinContentGenerator).getShareWithFlightTrackIntent();

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

	Session.StatusCallback mFacebookStatusCallback = new Session.StatusCallback() {

		// callback when session changes state
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			handleFacebookResponse(session, state, exception);
		}
	};

	public void handleFacebookResponse(Session session, SessionState state, Exception exception) {
		if (session.isOpened()) {
			postToFacebook();
		}
		else {
			Log.d("FB: handleFacebookResponse - else");
		}

	}

	private void postToFacebook() {
		startActivity(FacebookShareActivity.createIntent(getActivity(), mItinContentGenerator));
		dismiss();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("FB: onActivityResult");
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().addCallback(mFacebookStatusCallback);
		}
	}
}
