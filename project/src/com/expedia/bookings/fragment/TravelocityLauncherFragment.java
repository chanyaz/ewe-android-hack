package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    public static TravelocityLauncherFragment newInstance() {
        return new TravelocityLauncherFragment();
    }

    // Background images
    private static final Integer BACKGROUND_RES_IDS = R.drawable.bg_launch_london;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
       final View view = inflater.inflate(R.layout.fragment_tvly_launch, container, false);
       Ui.findView(view, R.id.tvly_flights).setOnClickListener(mHeaderItemOnClickListener);
       Ui.findView(view, R.id.tvly_hotels).setOnClickListener(mHeaderItemOnClickListener);
       return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        mLaunchingActivity = false;
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
