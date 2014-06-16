package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.UrlBitmapDrawable;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.base.Fragment;
import com.expedia.bookings.graphics.RoundBitmapDrawable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RoundImageView;

public class TabletLaunchPinDetailFragment extends Fragment {
	private ViewGroup mRootC;
	private RoundImageView mRoundImage;
	private View mRoundImageTarget;
	private View mTextLayout;

	private Rect mPinOrigin;
	private Rect mPinDest;
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
		mRoundImageTarget = Ui.findView(mRootC, R.id.round_image_target);
		mTextLayout = Ui.findView(mRootC, R.id.text_layout);

		((TabletLaunchControllerFragment) getParentFragment()).registerStateListener(mDetailsStateListener, false);

		return mRootC;
	}

	public void bind(Rect origin, final LaunchLocation metadata) {
		mPinOrigin = origin;

		UrlBitmapDrawable bitmap = UrlBitmapDrawable.loadImageView(metadata.getImageUrl(), mRoundImage);
		bitmap.setOnBitmapLoadedCallback(new L2ImageCache.OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				mRoundImage.setImageBitmap(bitmap);
			}

			@Override
			public void onBitmapLoadFailed(String url) {

			}
		});

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

		private float mMidX;
		private float mMidY;
		private float mRatio;

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mPinDest = ScreenPositionUtils.getGlobalScreenPosition(mRoundImageTarget, false, false);
			mMidX = (mPinOrigin.left + (mPinOrigin.right - mPinOrigin.left) / 2.0f)
				- (mPinDest.left + (mPinDest.right - mPinDest.left) / 2.0f);

			mMidY = (mPinOrigin.top + (mPinOrigin.bottom - mPinOrigin.top) / 2.0f)
				- (mPinDest.top + (mPinDest.bottom - mPinDest.top) / 2.0f);

			mRatio = mPinOrigin.width() / (float) mPinDest.width();
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			float scale = delta(mRatio, 1.0f, percentage);

			float translationx = delta(mMidX, 0.0f, percentage);
			float translationy = delta(mMidY, 0.0f, percentage);

			mRoundImage.setTranslationX(translationx);
			mRoundImage.setTranslationY(translationy);
			mRoundImage.setScaleX(scale);
			mRoundImage.setScaleY(scale);

			mTextLayout.setTranslationX(translationx);
			mTextLayout.setAlpha(percentage);
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
