package com.expedia.bookings.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.expedia.bookings.R;
import com.expedia.bookings.fragment.FlightPenaltyRulesFragment;
import com.expedia.bookings.utils.Ui;

public class FlightPenaltyRulesActivity extends SherlockFragmentActivity implements
		FlightPenaltyRulesFragment.FlightPenaltyRulesFragmentListener {

	public static final String ARG_URL = "ARG_URL";

	private FlightPenaltyRulesFragment mFragment;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setTitle(R.string.legal_information);
		setContentView(R.layout.activity_flight_penalty_rules);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			mFragment = FlightPenaltyRulesFragment.newInstance(getIntent().getExtras().getString(ARG_URL));

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.penalty_rules_content_container, mFragment, FlightPenaltyRulesFragment.TAG);
			ft.commit();
		}
		else {
			mFragment = Ui.findSupportFragment(this, FlightPenaltyRulesFragment.TAG);
		}
	}

	@Override
	public void setLoading(boolean loading) {
		getSherlock().setProgressBarIndeterminateVisibility(loading);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			finish();
		}
		}
		return super.onOptionsItemSelected(item);
	}

}