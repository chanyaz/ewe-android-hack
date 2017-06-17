package com.expedia.bookings.deeplink;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;

import com.expedia.bookings.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class DeepLinkTestActivity extends AppCompatActivity {

	//@InjectView(R.id.link_list)
	RecyclerView mRecycler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_deeplink_test);

		ButterKnife.inject(this);

		mRecycler.setAdapter(new DeepLinksAdapter());
	}
}
