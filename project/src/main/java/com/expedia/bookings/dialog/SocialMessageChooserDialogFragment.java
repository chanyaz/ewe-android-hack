package com.expedia.bookings.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FacebookShareActivity;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.itin.FlightItinContentGenerator;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.facebook.Session;
import com.mobiata.android.Log;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

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

		// Disabling the short button share options from chooser for AAG,as there responsive page is not ready.
		Ui.findView(view, R.id.short_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.share(getActivity(), mSubject, mShortMessage);
				dismiss();

				OmnitureTracking.trackItinShare(getActivity(), mType, false);
			}
		});

		if (ProductFlavorFeatureConfiguration.getInstance().isFacebookShareIntegrationEnabled() && AndroidUtils
			.isPackageInstalled(getActivity(), "com.facebook.katana")) {
			View facebookButton = Ui.findView(view, R.id.facebook_button);
			facebookButton.setVisibility(View.VISIBLE);
			facebookButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					startActivity(FacebookShareActivity.createIntent(getActivity(), mItinContentGenerator));
					dismiss();

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("FB: onActivityResult");
		Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
	}
}
