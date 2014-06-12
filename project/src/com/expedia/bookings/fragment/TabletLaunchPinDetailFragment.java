package com.expedia.bookings.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;

public class TabletLaunchPinDetailFragment extends Fragment {
	private ViewGroup mRootC;
	private View mRoundImage;
	private View mTextLayout;

	private int mPinOriginX = -100;
	private int mPinOriginY = -210;
	private float mScaleOrigin = 0.5f;
	private int mTextOriginX = -400;

	// TODO: associated map pin data

	public static TabletLaunchPinDetailFragment newInstance() {
		TabletLaunchPinDetailFragment frag = new TabletLaunchPinDetailFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(R.layout.fragment_tablet_launch_pin_detail, container, false);

		mRoundImage = Ui.findView(mRootC, R.id.round_image);
		mTextLayout = Ui.findView(mRootC, R.id.text_layout);

		((TabletLaunchControllerFragment) getParentFragment()).registerStateListener(mDetailsStateListener, false);

		return mRootC;
	}

	public void bind(Rect origin, final LaunchLocation metadata) {
		//TODO: why doesn't this work? Rect localOrigin = ScreenPositionUtils.translateGlobalPositionToLocalPosition(origin, mRootC);
		mPinOriginX = origin.left - mRoundImage.getLeft() - 64; // TODO: resource? or calculate "64"?
		mPinOriginY = origin.top - mRoundImage.getTop() - 50 - 64; // TODO: resource? or calculate "50" (status bar?) and "64"?
		mScaleOrigin = (float) origin.width() / (float) getResources().getDimensionPixelSize(R.dimen.launch_pin_detail_size);

		ImageView roundImage = Ui.findView(mRootC, R.id.round_image);
		roundImage.setImageDrawable(new RoundBitmapDrawable(getActivity(), R.drawable.mappin_madrid));

		TextView textTitle = Ui.findView(mRootC, R.id.text_title);
		textTitle.setText(metadata.title);

		TextView textDescription = Ui.findView(mRootC, R.id.text_description);
		textDescription.setText(metadata.description);

		TextView textBookNow = Ui.findView(mRootC, R.id.button_book_now);
		textBookNow.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Events.post(new Events.SearchSuggestionSelected(metadata.location, null));
			}
		});
	}

	private SingleStateListener<LaunchState> mDetailsStateListener = new SingleStateListener<>(
		LaunchState.DEFAULT, LaunchState.DETAILS, true, new ISingleStateListener() {

		@Override
		public void onStateTransitionStart(boolean isReversed) {
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			float scale = delta(mScaleOrigin, 1f, percentage);
			float translationx = delta(mPinOriginX, 0f, percentage);
			float translationy = delta(mPinOriginY, 0f, percentage);
			float textx = delta(mTextOriginX, 0f, percentage);
			float textalpha = delta(0f, 1f, percentage);
			mRootC.setTranslationX(translationx);
			mRootC.setTranslationY(translationy);
			mRoundImage.setScaleX(scale);
			mRoundImage.setScaleY(scale);
			mTextLayout.setTranslationX(textx);
			mTextLayout.setAlpha(textalpha);
			mTextLayout.setScaleX(scale);
			mTextLayout.setScaleY(scale);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
		}

		private float delta(float start, float end, float percentage) {
			return (end - start) * percentage + start;
		}
	});
}
