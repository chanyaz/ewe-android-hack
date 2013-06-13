package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertSite;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.SocialUtils;

public class LocalExpertFragment extends Fragment {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final String TAG = LocalExpertFragment.class.getName();

	private static final String ARG_SITE = "ARG_SITE";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private LocalExpertSite mSite;

	// Views

	private View mCloseView;
	private ImageView mIconImageView;
	private TextView mTitleTextView;
	private Button mCallButton;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public static LocalExpertFragment newInstance(LocalExpertSite site) {
		LocalExpertFragment fragment = new LocalExpertFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_SITE, site);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			mSite = getArguments().getParcelable(ARG_SITE);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDEN METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_local_expert, container, false);

		mCloseView = Ui.findView(view, R.id.close_image_view);
		mIconImageView = Ui.findView(view, R.id.icon_image_view);
		mTitleTextView = Ui.findView(view, R.id.title_text_view);
		mCallButton = Ui.findView(view, R.id.call_button);

		// Set view values
		if (mSite != null) {
			mIconImageView.setImageResource(mSite.getCityIcon());
			mTitleTextView.setText(getString(R.string.local_expert_title_TEMPLATE, mSite.getCity()));
		}

		// Set view listeners
		mCloseView.setOnClickListener(mOnClickListener);
		mCallButton.setOnClickListener(mOnClickListener);

		return view;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS
	//////////////////////////////////////////////////////////////////////////////////////

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.close_image_view: {
				getActivity().finish();
				break;
			}
			case R.id.call_button: {
				SocialUtils.call(getActivity(), mSite.getPhoneNumber().toString());
				break;
			}
			}
		}
	};
}
