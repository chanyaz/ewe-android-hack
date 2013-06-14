package com.expedia.bookings.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.LocalExpertSite;
import com.expedia.bookings.data.LocalExpertSite.Preset;
import com.expedia.bookings.fragment.LocalExpertFragment;
import com.expedia.bookings.utils.Ui;

public class LocalExpertActivity extends SherlockFragmentActivity {
	public static final String EXTRA_LOCAL_EXPERT_SITE = "EXTRA_LOCAL_EXPERT_SITE";

	private static final String FRAGMENT_LOCAL_EXPERT = "FRAGMENT_LOCAL_EXPERT";

	private LocalExpertFragment mLocalExpertFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_local_expert);

		mLocalExpertFragment = Ui.findSupportFragment(this, FRAGMENT_LOCAL_EXPERT);
		if (mLocalExpertFragment == null) {
			LocalExpertSite site = getIntent().getExtras().getParcelable(EXTRA_LOCAL_EXPERT_SITE);
			mLocalExpertFragment = LocalExpertFragment.newInstance(site);

			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.fragment_container, mLocalExpertFragment, FRAGMENT_LOCAL_EXPERT);
			ft.commit();
		}
	}

	public static Intent createIntent(Context context, Preset preset) {
		Intent intent = new Intent(context, LocalExpertActivity.class);
		intent.putExtra(EXTRA_LOCAL_EXPERT_SITE, LocalExpertSite.buildPreset(context, preset));

		return intent;
	}
}
