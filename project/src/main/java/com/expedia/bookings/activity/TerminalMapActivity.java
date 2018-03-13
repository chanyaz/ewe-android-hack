package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.TerminalMapFragment;
import com.expedia.bookings.fragment.TerminalMapLegendDialogFragment;
import com.expedia.bookings.itin.common.ItinToolbarWithSpinner;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.AirportMap;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;
import java.util.ArrayList;

public class TerminalMapActivity extends AppCompatActivity {

	public static final String ARG_AIRPORT_CODE = "ARG_AIRPORT_CODE";

	private static final String FRAG_TAG_TERMINAL_MAP = "FRAG_TAG_TERMINAL_MAP";
	private static final String STATE_POSITION = "STATE_POSITION";

	private TerminalMapFragment mTerminalMap;
	private Airport mAirport;
	private ArrayList<String> mTerminalNames;
	private int mSpinnerPosition = 0;


	public static Intent createIntent(Context context, String airportCode) {
		Intent intent = new Intent(context, TerminalMapActivity.class);
		intent.putExtra(ARG_AIRPORT_CODE, airportCode);
		return intent;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.terminal_maps_container_with_toolbar);

		ItinToolbarWithSpinner toolbar = findViewById(R.id.toolbar);
		toolbar.setButtonListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showLegendDialog();
			}
		});
		toolbar.setBackOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
				overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after);
			}
		});

		toolbar.setButtonText(R.string.legend);


		if (getIntent().hasExtra(ARG_AIRPORT_CODE)) {
			String airportCode = getIntent().getStringExtra(ARG_AIRPORT_CODE);
			mAirport = FlightStatsDbUtils.getAirport(airportCode);
		}
		else {
			throw new RuntimeException("NO AIRPORT CODE PASSED TO TERMINAL MAP ACTIVITY");
		}

		//Setup dropdown nav
		mTerminalNames = new ArrayList<String>();
		if (mAirport != null && mAirport.hasAirportMaps()) {
			for (AirportMap map : mAirport.mAirportMaps) {
				mTerminalNames.add(map.mName);
			}
		}
		if (!mTerminalNames.isEmpty()) {
			toolbar.setSpinnerList(mTerminalNames);
		}

		toolbar.setSpinnerListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				if (i < mAirport.mAirportMaps.size()) {
					mSpinnerPosition = i;
					mTerminalMap.loadMap(mAirport.mAirportMaps.get(i));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
			}
		});

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_POSITION)) {
			mSpinnerPosition = savedInstanceState.getInt(STATE_POSITION);
		}

		mTerminalMap = Ui.findSupportFragment(this, FRAG_TAG_TERMINAL_MAP);
		if (mTerminalMap == null) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			mTerminalMap = TerminalMapFragment.newInstance();
			transaction.add(R.id.fragment_container, mTerminalMap, FRAG_TAG_TERMINAL_MAP);
			transaction.commit();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_POSITION, mSpinnerPosition);
	}

	public void showLegendDialog() {
		TerminalMapLegendDialogFragment frag = TerminalMapLegendDialogFragment.newInstance();
		frag.show(getSupportFragmentManager(), TerminalMapLegendDialogFragment.TAG);
	}
}
