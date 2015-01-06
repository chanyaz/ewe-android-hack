package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PaletteCallback;
import com.expedia.bookings.bitmaps.PicassoHelper;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.utils.Akeakamai;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.Images;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment {

	private static final String ARG_DEST_CODES = "ARG_DEST_CODES";
	private static final String ARG_BLUR = "ARG_BLUR";

	private static final String INSTANCE_DEST_CODES = "INSTANCE_DEST_CODES";
	private static final String INSTANCE_CODES_INDEX = "INSTANCE_CODES_INDEX";
	private static final String INSTANCE_BLUR = "INSTANCE_BLUR";

	private static final String DEFAULT_IMAGE_PSEUDO_URL = "<default>";

	private ArrayList<String> mDestCodes;
	private int mCodesIndex;

	private ViewGroup mRootC;

	private boolean mIsLandscape;

	private boolean mBlur;
	private String mCurrentlyDesiredUrl;

	private ImageView mImageView;

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
		mImageView = Ui.findView(mRootC, R.id.imageView);
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
		final int width = (int) (landscape.x * 0.8f);
		String destinationCode = mDestCodes.get(mCodesIndex);

		String baseUrl = Images.getTabletDestination(destinationCode);

		final String url = new Akeakamai(baseUrl)
			.downsize(Akeakamai.pixels(width), Akeakamai.preserve())
			.quality(75)
			.build();

		mCurrentlyDesiredUrl = url;

		PaletteCallback paletteCallback = new PaletteCallback(mImageView) {
			@Override
			public void onSuccess(int vibrantColor) {
				if (!mBlur) {
					int transparentAvgColor = new ColorBuilder(vibrantColor)
						.setSaturation(0.2f) //
						.setOpacity(0.35f) //
						.setAlpha(0xE5) //
						.build();
					Db.setFullscreenAverageColor(transparentAvgColor);
				}
			}

			@Override
			public void onFailed() {
				if (FragmentBailUtils.shouldBail(getActivity())) {
					return;
				}

				Log.e("ResultsBackgroundImageFragment - onBitmapLoadFailed");
				if (mCodesIndex + 1 < mDestCodes.size()) {
					mCodesIndex++;
					loadImage();
				}
			}
		};

		new PicassoHelper.Builder(mImageView).setPlaceholder(R.drawable.bg_tablet_dest_image_default)
			.applyBlurTransformation(mBlur).applyPaletteTransformation(paletteCallback).fade().build().load(url);
	}

	private static ArrayList<String> getMostRelevantDestinationCodes(LineOfBusiness lob) {
		ArrayList<String> destCodes = new ArrayList<>();
		if (lob == LineOfBusiness.FLIGHTS) {
			destCodes.add(
				Db.getTripBucket().getFlight().getFlightSearchParams().getArrivalLocation().getDestinationId());
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
}
