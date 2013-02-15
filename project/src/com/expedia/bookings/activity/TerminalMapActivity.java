package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.TerminalMapFragment;
import com.expedia.bookings.fragment.TerminalMapLegendDialogFragment;
import com.expedia.bookings.utils.ActionBarNavUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.AirportMap;
import com.mobiata.flightlib.data.sources.FlightStatsDbUtils;

public class TerminalMapActivity extends SherlockFragmentActivity implements OnNavigationListener {

	public static final String ARG_AIRPORT_CODE = "ARG_AIRPORT_CODE";

	private static final String FRAG_TAG_TERMINAL_MAP = "FRAG_TAG_TERMINAL_MAP";
	private static final String STATE_POSITION = "STATE_POSITION";

	private TerminalMapFragment mTerminalMap;
	private Airport mAirport;
	private ArrayList<String> mTerminalNames;
	private SpinnerAdapter mMapSelectorAdapter;
	private int mSpinnerPosition = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_terminal_map);

		// Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_expedia_white_logo_small);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayUseLogoEnabled(false);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		if (getIntent().hasExtra(ARG_AIRPORT_CODE)) {
			String airportCode = getIntent().getStringExtra(ARG_AIRPORT_CODE);
			mAirport = FlightStatsDbUtils.getAirport(airportCode);
		}
		else {
			throw new RuntimeException("NO AIRPORT CODE PASSED TO TERMINAL MAP ACTIVITY");
		}

		//Setup dropdown nav
		mTerminalNames = new ArrayList<String>();
		if (mAirport != null) {
			for (AirportMap map : mAirport.mAirportMaps) {
				mTerminalNames.add(map.mName);
			}
		}
		mMapSelectorAdapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item_terminal_chooser,
				mTerminalNames);
		actionBar.setListNavigationCallbacks(mMapSelectorAdapter, this);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_POSITION)) {
			mSpinnerPosition = savedInstanceState.getInt(STATE_POSITION);
			actionBar.setSelectedNavigationItem(mSpinnerPosition);
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

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (itemPosition < mAirport.mAirportMaps.size()) {
			mSpinnerPosition = itemPosition;
			mTerminalMap.loadMap(mAirport.mAirportMaps.get(itemPosition));
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.menu_legend:
			showLegendDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getSupportMenuInflater();
		inflater.inflate(R.menu.menu_terminal_map, menu);
		ActionBarNavUtils.setupActionLayoutButton(this, menu, R.id.menu_legend);
		return true;
	}

	public void showLegendDialog() {
		TerminalMapLegendDialogFragment frag = TerminalMapLegendDialogFragment.newInstance();
		frag.show(getSupportFragmentManager(), TerminalMapLegendDialogFragment.TAG);
	}
}
