package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.NavUtils;
import com.mobiata.android.util.Ui;

/**
 * Created by sugupta on 1/21/14.
 */
public class TravelocityLauncherFragment extends Fragment {

    public static final String TAG = TravelocityLauncherFragment.class.getName();
    private boolean mLaunchingActivity = false;
    private ImageView mSlidingImage;
    private int mCurrentImageIndex = 0;
    private static final int IMAGE_ROTATION_PERIOD = 8000;
    private Handler mHandler;
    private Runnable mUpdateResults;

    public static TravelocityLauncherFragment newInstance() {
        return new TravelocityLauncherFragment();
    }

    private static final Integer[] BACKGROUND_RES_IDS = new Integer[] {
            R.drawable.bg_launch_sf_placeholder,
            R.drawable.bg_launch_london,
            R.drawable.bg_launch_ny,
            R.drawable.bg_launch_paris,
            R.drawable.bg_launch_sea,
            R.drawable.bg_launch_sf,
            R.drawable.bg_launch_toronto,
            R.drawable.bg_launch_hongkong,
            R.drawable.bg_launch_petronas,
            R.drawable.bg_launch_vegas
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       final View view = inflater.inflate(R.layout.fragment_tvly_launch, container, false);
       Ui.findView(view, R.id.tvly_flights).setOnClickListener(mHeaderItemOnClickListener);
       Ui.findView(view, R.id.tvly_hotels).setOnClickListener(mHeaderItemOnClickListener);
       mSlidingImage = Ui.findView(view, R.id.tvly_home_bg_view);
       mHandler = new Handler();
       mUpdateResults = new Runnable() {
            public void run() {
                animateAndSlideShow();
                mHandler.postDelayed(this, IMAGE_ROTATION_PERIOD);
            }
        };
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLaunchingActivity = false;
        mHandler.postDelayed(mUpdateResults, IMAGE_ROTATION_PERIOD);
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateResults);
    }

    private void animateAndSlideShow() {
        mCurrentImageIndex = (mCurrentImageIndex + 1) % BACKGROUND_RES_IDS.length;
        mSlidingImage.setImageResource(BACKGROUND_RES_IDS[mCurrentImageIndex]);
    }

    private final View.OnClickListener mHeaderItemOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Bundle animOptions = AnimUtils.createActivityScaleBundle(v);

            switch (v.getId()) {
                case R.id.tvly_hotels:
                    if (!mLaunchingActivity) {
                        mLaunchingActivity = true;
                        NavUtils.goToHotels(getActivity(), animOptions);
                        OmnitureTracking.trackLinkLaunchScreenToHotels(getActivity());
                    }
                    break;
                case R.id.tvly_flights:
                    if (!mLaunchingActivity) {
                        mLaunchingActivity = true;
                        NavUtils.goToFlights(getActivity(), animOptions);
                        OmnitureTracking.trackLinkLaunchScreenToFlights(getActivity());
                    }
                    break;
            }
        }
    };
}
