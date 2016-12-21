package com.expedia.bookings.launch.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoTarget;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.launch.data.LaunchLocation;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.ContentClickableRelativeLayout;
import com.expedia.bookings.widget.RoundImageView;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

public class TabletLaunchPinDetailFragment extends Fragment {
	private ContentClickableRelativeLayout mRootC;
	private RoundImageView mRoundImage;
	private View mRoundImageTarget;
	private View mTextLayout;

	private Rect mPinOrigin;
	private Rect mPinDest;

	private LaunchLocation mLaunchLocation;

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

		mRootC.setOutsideContentClickedListener(mDismissListener);

		TabletLaunchControllerFragment parent = (TabletLaunchControllerFragment) getParentFragment();
		parent.registerStateListener(mDetailsStateListener, false);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		Events.unregister(this);
		super.onPause();
	}

	@Subscribe
	public void onLaunchCollectionsAvailable(final Events.LaunchCollectionsAvailable event) {
		onLaunchMapPinClicked(new Events.LaunchMapPinClicked(event.selectedLocation));
	}

	private PicassoTarget callback = new PicassoTarget() {

		@Override
		public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
			super.onBitmapLoaded(bitmap, from);
			mRoundImage.setImageBitmap(bitmap);
		}

		@Override
		public void onBitmapFailed(Drawable errorDrawable) {
			super.onBitmapFailed(errorDrawable);
			mRoundImage.setImageDrawable(errorDrawable);
		}

		@Override
		public void onPrepareLoad(Drawable placeHolderDrawable) {
			super.onPrepareLoad(placeHolderDrawable);
			mRoundImage.setImageDrawable(placeHolderDrawable);
		}
	};

	@Subscribe
	public void onLaunchMapPinClicked(final Events.LaunchMapPinClicked event) {
		if (event.launchLocation != null) {
			clearOldImageDownloadCallback();
			mLaunchLocation = event.launchLocation;
			final String imageUrl = getResizedImageUrl(getActivity(), event.launchLocation);

			new PicassoHelper.Builder(mRoundImage)
				.setError(R.drawable.launch_circle_placeholder)
				.setPlaceholder(R.drawable.launch_circle_placeholder)
				.setTarget(callback).build().load(
				imageUrl);

			TextView textTitle = Ui.findView(mRootC, R.id.text_title);
			textTitle.setText(event.launchLocation.title);

			TextView textDescription = Ui.findView(mRootC, R.id.text_description);
			textDescription.setText(event.launchLocation.description);

			TextView textBookNow = Ui.findView(mRootC, R.id.button_explore_now);
			textBookNow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					boolean fromLastSearch = event.launchLocation.id.equals("last-search");
					event.launchLocation.location.setImageCode(event.launchLocation.imageCode);
					Events.post(new Events.SearchSuggestionSelected(event.launchLocation.location,  fromLastSearch));
				}
			});
		}
	}

	private void clearOldImageDownloadCallback() {
	}

	public static String getResizedImageUrl(Context context, LaunchLocation launchLocation) {
		String url = launchLocation.getImageUrl();
		int width = context.getResources().getDimensionPixelSize(R.dimen.launch_pin_detail_size);
		return new Akeakamai(url) //
			.downsize(Akeakamai.pixels(width), Akeakamai.preserve()) //
			.build();
	}

	public void setOriginRect(Rect origin) {
		mPinOrigin = origin;
	}

	private SingleStateListener<LaunchState> mDetailsStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.DETAILS, true, new ISingleStateListener() {

		private boolean mJustFadeAway = false;
		private float mMidX;
		private float mMidY;
		private float mRatio;

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mPinDest = ScreenPositionUtils.getGlobalScreenPosition(mRoundImageTarget, false, false);

			mJustFadeAway = mPinOrigin == null || mPinDest == null;

			if (!mJustFadeAway) {
				mMidX = (mPinOrigin.left + (mPinOrigin.right - mPinOrigin.left) / 2.0f)
					- (mPinDest.left + (mPinDest.right - mPinDest.left) / 2.0f);

				mMidY = (mPinOrigin.top + (mPinOrigin.bottom - mPinOrigin.top) / 2.0f)
					- (mPinDest.top + (mPinDest.bottom - mPinDest.top) / 2.0f);

				mRatio = mPinOrigin.width() / (float) mPinDest.width();
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			if (mJustFadeAway) {
				mRoundImage.setAlpha(percentage);
				mTextLayout.setAlpha(percentage);
			}
			else {
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
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
			mTextLayout.setAlpha(isReversed ? 0.0f : 1.0f);
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			mRoundImage.setAlpha(1.0f);
		}

		private float delta(float start, float end, float percentage) {
			return (end - start) * percentage + start;
		}
	});

	private View.OnClickListener mDismissListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			TabletLaunchControllerFragment parent = (TabletLaunchControllerFragment) getParentFragment();
			parent.setLaunchState(LaunchState.OVERVIEW, true);
		}
	};


}
