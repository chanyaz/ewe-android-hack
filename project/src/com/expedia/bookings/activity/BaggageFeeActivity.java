package com.expedia.bookings.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.fragment.BaggageFeeFragment;
import com.expedia.bookings.fragment.BaggageFeeFragment.BaggageFeeListener;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;

public class BaggageFeeActivity extends SherlockFragmentActivity implements BaggageFeeListener {

	private static final String FRAG_TAG = "FRAG_TAG";

	BaggageFeeFragment mFragment;

	/** Called when the activity is first created. */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		String origin = "";
		String destination = "";

		//Pull in origin and destination from the intent
		Intent intent = getIntent();
		if (intent.hasExtra(BaggageFeeFragment.TAG_ORIGIN) && intent.hasExtra(BaggageFeeFragment.TAG_DESTINATION)) {
			origin = intent.getStringExtra(BaggageFeeFragment.TAG_ORIGIN);
			destination = intent.getStringExtra(BaggageFeeFragment.TAG_DESTINATION);
		}
		else {
			Log.e("BaggageFeeActivity requires that the intent contains origin and destination values");
			exit();
		}

		mFragment = Ui.findSupportFragment(this, FRAG_TAG);
		if (mFragment == null) {
			mFragment = BaggageFeeFragment.newInstance(origin, destination,
					intent.getIntExtra(BaggageFeeFragment.ARG_LEG_POSITION, 0));
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, mFragment, FRAG_TAG);
			ft.commit();
		}

	}

	@Override
	public void exit() {
		finish();
	}

	@Override
	public void setLoading(boolean loading) {
		getSherlock().setProgressBarIndeterminateVisibility(loading);
	}

}
