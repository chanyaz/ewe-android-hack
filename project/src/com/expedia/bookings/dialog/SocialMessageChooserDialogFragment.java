package com.expedia.bookings.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;

public class SocialMessageChooserDialogFragment extends DialogFragment {
	private String mSubject;
	private String mShortMessage;
	private String mLongMessage;

	private TripComponent.Type mType;

	public static SocialMessageChooserDialogFragment newInstance(ItinContentGenerator<?> generator) {
		String subject = generator.getShareSubject();
		String shortMessage = generator.getShareTextShort();
		String longMessage = generator.getShareTextLong();
		TripComponent.Type type = generator.getType();

		return newInstance(subject, shortMessage, longMessage, type);
	}

	private static SocialMessageChooserDialogFragment newInstance(String subject, String shortMessage,
			String longMessage, TripComponent.Type type) {
		SocialMessageChooserDialogFragment fragment = new SocialMessageChooserDialogFragment();
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

		return view;
	}
}
