package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.interfaces.IResultsHotelGalleryBackClickedListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RecyclerGallery;
import com.squareup.otto.Subscribe;

public class ResultsHotelGalleryFragment extends Fragment {

	public static ResultsHotelGalleryFragment newInstance() {
		return new ResultsHotelGalleryFragment();
	}

	private ViewGroup mGalleryActionBar;
	private TextView mHotelText;

	private View mBackground;

	private RecyclerGallery mRecyclerView;

	private IResultsHotelGalleryBackClickedListener mHotelGalleryBackClickedListener;

	private static final String INSTANCE_CURRENT_IMAGE = "INSTANCE_CURRENT_IMAGE";
	private static final int NO_IMAGE = 0;
	private int mCurrentImagePosition = NO_IMAGE;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mHotelGalleryBackClickedListener = Ui.findFragmentListener(this, IResultsHotelGalleryBackClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup rootView = Ui.inflate(inflater, R.layout.fragment_tablet_hotel_gallery, null);
		mGalleryActionBar = Ui.findView(rootView, R.id.gallery_action_bar);
		TextView doneText = Ui.findView(rootView, R.id.done_button);
		mHotelText = Ui.findView(rootView, R.id.photos_for_hotel_text);
		mRecyclerView = Ui.findView(rootView, R.id.recycler_view);
		mRecyclerView.setMode(RecyclerGallery.MODE_CENTER);

		mBackground = Ui.findView(rootView, R.id.background_view);

		doneText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHotelGalleryBackClickedListener.onHotelGalleryBackClicked();
			}
		});

		if (savedInstanceState != null) {
			mCurrentImagePosition = savedInstanceState.getInt(INSTANCE_CURRENT_IMAGE, NO_IMAGE);
		}

		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mCurrentImagePosition = mRecyclerView.getSelectedItem();
		outState.putInt(INSTANCE_CURRENT_IMAGE, mCurrentImagePosition);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Db.getHotelSearch().getSelectedProperty() != null) {
			bind(Db.getHotelSearch().getSelectedProperty());
		}
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	private void bind(Property property) {
		String photosForText = getString(R.string.photos_for_TEMPLATE, property.getName());
		mHotelText.setText(photosForText);

		if (property.getMediaList() != null) {
			mRecyclerView.setDataSource(property.getMediaList());
			mRecyclerView.scrollToPosition(mCurrentImagePosition);
			mCurrentImagePosition = NO_IMAGE;
		}
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	public void setAnimationPercentage(float p) {
		mBackground.setAlpha(p);
		mRecyclerView.setAlpha(p);
		mGalleryActionBar.setTranslationY(-mGalleryActionBar.getHeight() * (1.0f - p));
	}

	public void setHardwareLayer(int layerValue) {
		mGalleryActionBar.setLayerType(layerValue, null);
		mRecyclerView.setLayerType(layerValue, null);
	}

	@Subscribe
	public void onEvent(Events.HotelAvailabilityUpdated event) {
		bind(Db.getHotelSearch().getSelectedProperty());
	}
}
