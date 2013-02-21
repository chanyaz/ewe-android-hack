package com.expedia.bookings.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.mobiata.android.SocialUtils;
import com.mobiata.android.util.Ui;

public class SocialMessageChooserDialogFragment extends DialogFragment {
	private String mSubject;
	private String mShortMessage;
	private String mLongMessage;

	public static SocialMessageChooserDialogFragment newInstance(String subject, String shortMessage, String longMessage) {
		SocialMessageChooserDialogFragment fragment = new SocialMessageChooserDialogFragment();
		fragment.mSubject = subject;
		fragment.mShortMessage = shortMessage;
		fragment.mLongMessage = longMessage;

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
			}
		});

		Ui.findView(view, R.id.short_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				SocialUtils.share(getActivity(), mSubject, mShortMessage);
				dismiss();
			}
		});

		return view;
	}
}