package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.BitmapUtils;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.squareup.otto.Subscribe;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment implements L2ImageCache.OnBitmapLoaded {

	private static final int HALF_FADE_IN_TIME = 500;

	private static final String ARG_DEST_CODE = "ARG_DEST_CODE";
	private static final String ARG_BLUR = "ARG_BLUR";

	private String mDestinationCode;
	private boolean mBlur;

	private ViewGroup mRootC;

	public static ResultsBackgroundImageFragment newInstance(String destination, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DEST_CODE, destination);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}


	// For Tablet Checkout, where our desired background image does not necessarily correspond
	// to our current search parameters.
	public static ResultsBackgroundImageFragment newInstance(LineOfBusiness lob, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		String destination = getMostRelevantDestinationCode(lob);
		args.putString(ARG_DEST_CODE, destination);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Fragment arguments
		Bundle args = getArguments();
		mDestinationCode = args.getString(ARG_DEST_CODE);
		mBlur = args.getBoolean(ARG_BLUR);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_background_image, null);

		loadImage();

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		Sp.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
	}

	///////////////////////////////////////////////////////////////
	// Otto

	@Subscribe
	public void onSpChange(Sp.SpUpdateEvent event) {
		String newCode = Sp.getParams().getDestination().getAirportCode();
		if (!TextUtils.isEmpty(newCode) && !newCode.equals(mDestinationCode)) {
			getArguments().putString(ARG_DEST_CODE, newCode);
			mDestinationCode = newCode;

			loadImage();
		}
	}

	public void setBlur(boolean blur) {
		if (mBlur == blur) {
			// Nothing to see here
			return;
		}

		mBlur = blur;
		loadImage();
	}

	private void loadImage() {
		if (getActivity() != null) {
			Point landscape = Ui.getLandscapeScreenSize(getActivity());
			final String url = new Akeakamai(Images.getTabletDestination(mDestinationCode))
				.downsize(Akeakamai.pixels(landscape.x), Akeakamai.preserve())
				.quality(60)
				.build();

			L2ImageCache.sDestination.clearCallbacksByUrl(url);
			L2ImageCache.sDestination.loadImage(url, mBlur, this);
		}
	}

	private static String getMostRelevantDestinationCode(LineOfBusiness lob) {
		String destination;
		if (lob == LineOfBusiness.FLIGHTS) {
			destination = Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId();
		}
		else if (lob == LineOfBusiness.HOTELS) {
			destination = Db.getTripBucket().getHotel().getHotelSearchParams().getCorrespondingAirportCode();
		}
		else {
			destination = Sp.getParams().getDestination().getAirportCode();
		}
		return destination;
	}

	///////////////////////////////////////////////////////////////
	// OnBitmapLoaded

	@Override
	public void onBitmapLoaded(String url, Bitmap bitmap) {
		if (bitmap != null && !mBlur) {
			int avgColor = BitmapUtils.getAvgColorOnePixelTrick(bitmap);
			int transparentAvgColor = new ColorBuilder(avgColor) //
				.setSaturation(0.2f) //
				.setOpacity(0.35f) //
				.setAlpha(0xE5) //
				.build();
			Db.setFullscreenAverageColor(transparentAvgColor);
		}

		handleBitmap(bitmap, true);
	}

	@Override
	public void onBitmapLoadFailed(String url) {
		Log.e("ResultsBackgroundImageFragment - onBitmapLoadFailed");
	}

	///////////////////////////////////////////////////////////////
	// private methods

	private void handleBitmap(final Bitmap bitmap, boolean fade) {
		if (bitmap == null) {
			Log.v("bitmap null null null");
			return;
		}

		if (mRootC == null || getActivity() == null || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) {
			// Silently don't draw the bitmap
			return;
		}

		if (fade) {
			int split = mRootC.getChildCount();
			addNewViews(bitmap, 0f);
			crossfade(split);
		}
		else {
			mRootC.removeAllViews();
			addNewViews(bitmap, 1f);
		}
	}

	// This adds ImageViews to the base layout (assumed to be a FrameLayout), tiled vertically
	// with every second tile flipped vertically.
	private void addNewViews(Bitmap bitmap, float alpha) {
		Point screen = Ui.getScreenSize(getActivity());
		int scaledWidth = calculateTileWidth(screen.x);
		float scale = (float) scaledWidth / bitmap.getWidth();
		int scaledHeight = (int) (scale * bitmap.getHeight());
		boolean flip = false;
		int y = calculateTopOffset(scaledWidth);
		while (y < screen.y) {
			ImageView image = new ImageView(getActivity());
			image.setLayoutParams(new ViewGroup.MarginLayoutParams(scaledWidth, scaledHeight));
			image.setTranslationY(y);
			image.setScaleY(flip ? -1f : 1f);
			image.setImageBitmap(bitmap);
			image.setAlpha(alpha);
			mRootC.addView(image);
			flip = !flip;
			y += scaledHeight;
		}
	}

	// This will fade out the first half of the children (with index in [0, split) ),
	// and fade in the second half of the children at the same time (with index in [split, childCount) ).
	private void crossfade(final int split) {
		final int children = mRootC.getChildCount();
		PropertyValuesHolder fadeIn = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f);
		ObjectAnimator[] animators = new ObjectAnimator[children - split];
		for (int i = split; i < children; i++) {
			animators[i - split] = ObjectAnimator.ofPropertyValuesHolder(mRootC.getChildAt(i), fadeIn);
		}
		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				for (int i = 0; i < split; i++) {
					ImageView image = (ImageView) mRootC.getChildAt(0);
					image.setImageBitmap(null);
					mRootC.removeViewAt(0);
				}
			}
		});
		set.playTogether(animators);
		set.start();
	}

	// Possibly upscales the tile so that the top offset isn't below the top of the screen.
	private int calculateTileWidth(int minimum) {
		float percentOfImage = 0.29f;
		float percentOfScreen = (1f - getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1)) / 2f;

		int imageWidth = (int)( (percentOfScreen * AndroidUtils.getScreenSize(getActivity()).y) / percentOfImage);

		return Math.max(minimum, imageWidth);
	}

	// This calculates a top image offset such that the row %{percentOfImage} * {height of image}
	// down the destination image appears located at %{percentOfScreen} down from the top of this fragment.
	private int calculateTopOffset(int imageWidth) {
		float percentOfImage = 0.29f;
		float percentOfScreen = (1f - getResources().getFraction(R.fraction.results_grid_bottom_half, 1, 1)) / 2f;

		float imageOffset = imageWidth * percentOfImage;

		float screenOffset = percentOfScreen * AndroidUtils.getScreenSize(getActivity()).y;

		return Math.min(0, (int)(screenOffset - imageOffset));
	}
}
