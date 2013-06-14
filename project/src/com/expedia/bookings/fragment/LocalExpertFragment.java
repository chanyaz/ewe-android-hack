package com.expedia.bookings.fragment;

import java.lang.ref.WeakReference;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertAttraction;
import com.expedia.bookings.data.LocalExpertSite;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AttractionBubbleView;
import com.mobiata.android.SocialUtils;

public class LocalExpertFragment extends Fragment {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final String TAG = LocalExpertFragment.class.getName();

	private static final String ARG_SITE = "ARG_SITE";

	private static final int MSG_ADVANCE = 0;

	private static final int START_DELAY = 500;
	private static final int ADVANCE_DELAY = 2500;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private LocalExpertSite mSite;

	private AdvanceHandler mHandler = new AdvanceHandler(this);
	private int mAdvanceIndex = 0;

	// Views

	private ImageView mBackgroundImageView;
	private AttractionBubbleView mLargeAttractionBubbleView;
	private AttractionBubbleView mSmallAttractionBubbleView;
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
		final View view = inflater.inflate(R.layout.fragment_local_expert, container, false);

		mBackgroundImageView = Ui.findView(view, R.id.background_image_view);
		mLargeAttractionBubbleView = Ui.findView(view, R.id.large_attraction_bubble_view);
		mSmallAttractionBubbleView = Ui.findView(view, R.id.small_attraction_bubble_view);
		mCloseView = Ui.findView(view, R.id.close_image_view);
		mIconImageView = Ui.findView(view, R.id.icon_image_view);
		mTitleTextView = Ui.findView(view, R.id.title_text_view);
		mCallButton = Ui.findView(view, R.id.call_button);

		// Set view values
		mBackgroundImageView.setImageResource(mSite.getBackgroundResId());
		mIconImageView.setImageResource(mSite.getCityIcon());
		mTitleTextView.setText(getString(R.string.local_expert_title_TEMPLATE, mSite.getCity()));

		// Set view listeners
		mCloseView.setOnClickListener(mOnClickListener);
		mCallButton.setOnClickListener(mOnClickListener);

		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_ADVANCE), START_DELAY);
			}
		});

		return view;
	}

	public void advanceAttractions() {
		final List<LocalExpertAttraction> attractions = mSite.getAttractions();
		final int size = attractions.size();

		if (mAdvanceIndex == 0) {
			mLargeAttractionBubbleView.setAttraction(attractions.get(++mAdvanceIndex % size));
			mSmallAttractionBubbleView.setAttraction(attractions.get(++mAdvanceIndex % size));
		}
		else if (mAdvanceIndex % 2 == 0) {
			mLargeAttractionBubbleView.setAttraction(attractions.get(++mAdvanceIndex % size));
		}
		else {
			mSmallAttractionBubbleView.setAttraction(attractions.get(++mAdvanceIndex % size));
		}

		mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_ADVANCE), ADVANCE_DELAY);
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

	private static final class AdvanceHandler extends Handler {
		private final WeakReference<LocalExpertFragment> mLocalExpertFragment;

		public AdvanceHandler(LocalExpertFragment fragment) {
			mLocalExpertFragment = new WeakReference<LocalExpertFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_ADVANCE: {
				LocalExpertFragment fragment = mLocalExpertFragment.get();
				if (fragment != null) {
					fragment.advanceAttractions();
				}
			}
			}
		}

	}
}
