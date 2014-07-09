package com.expedia.bookings.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
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

	private ImageView mImageView;

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
		mImageView = new ImageView(getActivity());
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView.setLayoutParams(params);
		mImageView.setScaleType(ImageView.ScaleType.MATRIX);

		loadImage();

		return mImageView;
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
			final String url = new Akeakamai(Images.getTabletDestination(mDestinationCode)) //
				.resizeExactly(landscape.x, landscape.y) //
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
			destination =  Sp.getParams().getDestination().getAirportCode();
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

	private void handleBitmap(final Bitmap bitmap, boolean fade) {
		//TODO: TEMPORARY
		//mImageView.setImageResource(R.drawable.temporary_paris_backdrop);
		//mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		//TODO: TEMPORARY
		if (bitmap != null) {
			if (mImageView != null && getActivity() != null) {
				Point screen = Ui.getScreenSize(getActivity());
				Matrix topCrop = BitmapUtils.createFitWidthMatrix(bitmap.getWidth(), bitmap.getHeight(), screen.x, screen.y);

				if (fade) {
					kickoffCrossfade(bitmap, topCrop);
				}
				else {
					mImageView.setImageMatrix(topCrop);
					mImageView.setImageBitmap(bitmap);
				}
			}
		}
		else {
			Log.v("bitmap null null null");
		}
	}

	private void kickoffCrossfade(final Bitmap bitmap, final Matrix newMatrix) {
		mImageView.animate()
			.alpha(0f)
			.setDuration(mImageView.getDrawable() == null ? 0 : HALF_FADE_IN_TIME)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mImageView.setImageMatrix(newMatrix);
					mImageView.setImageBitmap(bitmap);
					mImageView.animate()
				.alpha(1f)
				.setDuration(HALF_FADE_IN_TIME)
				.setListener(null);
				}
			});
	}
}
