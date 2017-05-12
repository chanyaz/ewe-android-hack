package com.expedia.bookings.activity;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.expedia.bookings.R;
import com.expedia.bookings.fragment.AccountTravelerInfoFragment;

public class AccountInfoActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_account_traveler_info);
		getFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, new AccountTravelerInfoFragment())
			.commit();
	}

	@Override
	public void onBackPressed() {
		FragmentManager fragmentManager = getFragmentManager();
		if (fragmentManager.getBackStackEntryCount() > 1) {
			fragmentManager.popBackStack();
		}
		else {
			super.onBackPressed();
		}
	}
}
