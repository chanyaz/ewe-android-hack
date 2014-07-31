package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.squareup.otto.Subscribe;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment implements L2ImageCache.OnBitmapLoaded {

	private static final String ARG_DEST_CODES = "ARG_DEST_CODES";
	private static final String ARG_BLUR = "ARG_BLUR";

	private static final String INSTANCE_DEST_CODES = "INSTANCE_DEST_CODES";
	private static final String INSTANCE_CODES_INDEX = "INSTANCE_CODES_INDEX";
	private static final String INSTANCE_BLUR = "INSTANCE_BLUR";

	private ArrayList<String> mDestCodes;
	private int mCodesIndex;
	private boolean mBlur;

	private ViewGroup mRootC;

	private boolean mIsLandscape;

	public static ResultsBackgroundImageFragment newInstance(boolean blur) {
		ArrayList<String> codes = Sp.getParams().getDestination().getPossibleImageCodes();
		return newInstance(codes, blur);
	}

	public static ResultsBackgroundImageFragment newInstance(String destCode, boolean blur) {
		ArrayList<String> codes = new ArrayList<>();
		codes.add(destCode);
		return newInstance(codes, blur);
	}

	public static ResultsBackgroundImageFragment newInstance(ArrayList<String> destCodes, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		args.putStringArrayList(ARG_DEST_CODES, destCodes);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}

	// For Tablet Checkout, where our desired background image does not necessarily correspond
	// to our current search parameters.
	public static ResultsBackgroundImageFragment newInstance(LineOfBusiness lob, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		ArrayList<String> destCodes = getMostRelevantDestinationCodes(lob);
		args.putStringArrayList(ARG_DEST_CODES, destCodes);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Fragment arguments
		if (savedInstanceState == null) {
			Bundle args = getArguments();
			mDestCodes = args.getStringArrayList(ARG_DEST_CODES);
			mCodesIndex = 0;
			mBlur = args.getBoolean(ARG_BLUR);
		}
		else {
			mDestCodes = savedInstanceState.getStringArrayList(INSTANCE_DEST_CODES);
			mCodesIndex = savedInstanceState.getInt(INSTANCE_CODES_INDEX, 0);
			mBlur = savedInstanceState.getBoolean(INSTANCE_BLUR);
		}

		mIsLandscape = getResources().getBoolean(R.bool.landscape);
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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(INSTANCE_DEST_CODES, mDestCodes);
		outState.putInt(INSTANCE_CODES_INDEX, mCodesIndex);
		outState.putBoolean(INSTANCE_BLUR, mBlur);
	}

	///////////////////////////////////////////////////////////////
	// Otto

	@Subscribe
	public void onSpChange(Sp.SpUpdateEvent event) {
		ArrayList<String> newCodes = Sp.getParams().getDestination().getPossibleImageCodes();
		if (!newCodes.equals(mDestCodes)) {
			mDestCodes = newCodes;
			mCodesIndex = 0;
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
		if (getActivity() == null) {
			return;
		}

		Point landscape = Ui.getLandscapeScreenSize(getActivity());
		String destinationCode = mDestCodes.get(mCodesIndex);
		String baseUrl = Images.getTabletDestination(destinationCode);
		final String url = new Akeakamai(baseUrl)
			.downsize(Akeakamai.pixels(landscape.x), Akeakamai.preserve())
			.quality(60)
			.build();

		L2ImageCache.sDestination.clearCallbacksByUrl(url);

		// Check whether we already have this image cached
		Bitmap bitmap = L2ImageCache.sDestination.getImage(url, mBlur /*blurred*/, true /*checkdisk*/);

		// If the bitmap isn't in cache, throw up the default destination image right away
		if (bitmap == null) {
			bitmap = L2ImageCache.sDestination.getImage(getResources(), R.drawable.bg_tablet_dest_image_default, false /*blurred*/);
			onBitmapLoaded(url, bitmap);
		}

		L2ImageCache.sDestination.loadImage(url, mBlur, this);
	}

	private static ArrayList<String> getMostRelevantDestinationCodes(LineOfBusiness lob) {
		ArrayList<String> destCodes = new ArrayList<>();
		if (lob == LineOfBusiness.FLIGHTS) {
			destCodes.add(Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId());
		}
		else if (lob == LineOfBusiness.HOTELS) {
			destCodes.add(Db.getTripBucket().getHotel().getHotelSearchParams().getCorrespondingAirportCode());
		}
		else {
			if (!TextUtils.isEmpty(Sp.getParams().getDestination().getImageCode())) {
				destCodes.add(Sp.getParams().getDestination().getImageCode());
			}
			destCodes.add(Sp.getParams().getDestination().getAirportCode());
		}
		return destCodes;
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
		if (mCodesIndex + 1 < mDestCodes.size()) {
			mCodesIndex++;
			loadImage();
		}
		else {
			// Fall back to bg_tablet_dest_image_default
			Bitmap bitmap = L2ImageCache.sDestination.getImage(getResources(), R.drawable.bg_tablet_dest_image_default, false /*blurred*/);
			onBitmapLoaded(null, bitmap);
		}
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
		float scale = 1.0f;
		float screenWidth;
		float screenHeight;
		Point landscape = Ui.getLandscapeScreenSize(getActivity());
		if (mIsLandscape) {
			// Fit width in landscape
			screenWidth = landscape.x;
			screenHeight = landscape.y;
			scale = screenWidth / bitmap.getWidth();
		}
		else {
			screenWidth = landscape.y;
			screenHeight = landscape.x;
			scale = landscape.x / bitmap.getWidth();
		}

		int viewWidth = (int) (scale * bitmap.getWidth());
		int viewHeight = (int) (scale * bitmap.getHeight());

		boolean flip = false;
		int y = 0;
		while (y < screenHeight) {
			ImageView image = new ImageView(getActivity());
			image.setLayoutParams(new ViewGroup.LayoutParams(viewWidth, viewHeight));
			image.setTranslationX((screenWidth - viewWidth) / 2f);
			image.setTranslationY(y);
			image.setScaleY(flip ? -1f : 1f);
			image.setImageBitmap(bitmap);
			image.setAlpha(alpha);
			mRootC.addView(image);
			flip = !flip;
			y += viewHeight;
		}
	}

	// This will fade out the first half of the children (with index in [0, split) ),
	// and fade in the second half of the children at the same time (with index in [split, childCount) ).
	private void crossfade(int split) {
		final int children = mRootC.getChildCount();
		final int fsplit = split <= children ? split : 0;
		PropertyValuesHolder fadeIn = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f);
		ObjectAnimator[] animators = new ObjectAnimator[children - split];
		for (int i = split; i < children; i++) {
			animators[i - split] = ObjectAnimator.ofPropertyValuesHolder(mRootC.getChildAt(i), fadeIn);
		}
		AnimatorSet set = new AnimatorSet();
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				for (int i = 0; i < fsplit; i++) {
					ImageView image = (ImageView) mRootC.getChildAt(0);
					image.setImageBitmap(null);
					mRootC.removeViewAt(0);
				}
			}
		});
		set.playTogether(animators);
		set.start();
	}
}
