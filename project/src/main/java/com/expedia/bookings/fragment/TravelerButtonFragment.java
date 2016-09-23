package com.expedia.bookings.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.enums.PassengerCategory;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.section.TravelerAutoCompleteAdapter;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.TravelerUtils;
import com.expedia.bookings.widget.CheckoutInfoStatusImageView;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class TravelerButtonFragment extends LobableFragment {

	public static TravelerButtonFragment newInstance(LineOfBusiness lob, int dbTravelerNumber) {
		TravelerButtonFragment frag = new TravelerButtonFragment();
		frag.setLob(lob);
		frag.setTravelerNumber(dbTravelerNumber);
		return frag;
	}

	public interface ITravelerButtonListener {
		boolean travelerIsValid(int travelerNumber);

		void onTravelerEditButtonPressed(int travelerNumber);

		void onTravelerChosen();

		void onAddNewTravelerSelected(int travelerNumber);
	}

	private static final String STATE_TRAVELER_NUMBER = "STATE_TRAVELER_NUMBER";
	private static final String DL_FETCH_TRAVELER_INFO = "DL_FETCH_TRAVELER_INFO";
	private static final String FTAG_FETCH_TRAVELER_INFO = "FTAG_FETCH_TRAVELER_INFO";

	private int mTravelerNumber = -1;
	private SectionTravelerInfo mSectionTraveler;
	private ViewGroup mTravelerSectionContainer;
	private ViewGroup mEmptyViewContainer;
	private TextView mEditTravelerButton;
	private TextView mSavedTravelerSpinner;
	private TravelerAutoCompleteAdapter mTravelerAdapter;
	private ListPopupWindow mStoredTravelerPopup;

	private String mEmptyViewLabel;
	private ITravelerButtonListener mTravelerButtonListener;

	public void setEnabled(boolean enable) {
		mTravelerSectionContainer.setEnabled(enable);
		mEmptyViewContainer.setEnabled(enable);
		mSectionTraveler.setEnabled(enable);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mTravelerButtonListener = Ui.findFragmentListener(this, ITravelerButtonListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mTravelerNumber = savedInstanceState.getInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
		}

		View rootView = inflater.inflate(R.layout.fragment_checkout_traveler_button, null);
		mTravelerSectionContainer = Ui.findView(rootView, R.id.traveler_section_container);
		mEmptyViewContainer = Ui.findView(rootView, R.id.empty_traveler_container);

		mSectionTraveler = addTravelerSectionToLayout(mTravelerSectionContainer);
		TravelerUtils.setPhoneTextViewVisibility(mTravelerSectionContainer, mTravelerNumber);

		mTravelerAdapter = new TravelerAutoCompleteAdapter(getActivity());
		setUpStoredTravelers(mTravelerSectionContainer);

		addEmptyTravelerToLayout(mEmptyViewContainer);

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (dl.isDownloading(getTravelerDownloadKey())) {
			dl.registerDownloadCallback(getTravelerDownloadKey(), mTravelerDetailsCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (getActivity().isFinishing()) {
			dl.cancelDownload(getTravelerDownloadKey());
		}
		else {
			dl.unregisterDownloadCallback(getTravelerDownloadKey(), mTravelerDetailsCallback);
		}
	}

	private void onStoredTravelerSelected(int position) {
		if (position == 1) {
			/*
			 Let's reset selectable state for the current traveler and remove him from DB.
			 Since we are adding a new traveler, let's add a new blank traveler and set state to new.
			*/
			Traveler currentTraveler = Db.getTravelers().get(mTravelerNumber);
			TravelerUtils.resetPreviousTravelerSelectState(currentTraveler);
			Db.getTravelers().remove(mTravelerNumber);
			Traveler traveler = new Traveler();
			traveler.setIsNew(true);
			Db.getTravelers().add(mTravelerNumber, traveler);
			bindToDb();
			mTravelerButtonListener.onAddNewTravelerSelected(mTravelerNumber);
			mStoredTravelerPopup.dismiss();
			return;
		}
		else if (position == 0) {
			return;
		}
		Traveler traveler = mTravelerAdapter.getItem(position);
		if (traveler.isSelectable()) {
			Db.getWorkingTravelerManager().setWorkingTravelerAndBase(traveler);
			//Cancel previous download
			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			if (dl.isDownloading(getTravelerDownloadKey())) {
				dl.cancelDownload(getTravelerDownloadKey());
			}

			// Begin loading flight details in the background, if we haven't already
			// Show a loading dialog
			ThrobberDialog df = ThrobberDialog
				.newInstance(getString(R.string.loading_traveler_info));
			df.show(getChildFragmentManager(), FTAG_FETCH_TRAVELER_INFO);
			dl.startDownload(getTravelerDownloadKey(), mTravelerDetailsDownload,
				mTravelerDetailsCallback);
			mStoredTravelerPopup.dismiss();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_TRAVELER_NUMBER, mTravelerNumber);
	}

	public void setTravelerNumber(int travelerNumber) {
		mTravelerNumber = travelerNumber;
	}

	@Override
	public void onLobSet(LineOfBusiness lob) {
		//We do everything at bind time
	}

	private void bindTravelerSection() {
		if (getLob() == LineOfBusiness.FLIGHTS) {
			mSectionTraveler.bind(getDbTraveler(), Db.getTripBucket().getFlight().getFlightSearchParams());
		}
		else {
			mSectionTraveler.bind(getDbTraveler());
		}
	}

	public void bindToDb() {
		if (mSectionTraveler != null && hasDbTraveler()) {
			if (isValid()) {
				//Valid traveler
				bindTravelerSection();
				setShowTravelerView(true);
				setTravelerCheckoutStatus(true);

			}
			else if (isPartiallyFilled()) {
				//Partially filled button
				mSectionTraveler.bind(getDbTraveler());
				setShowTravelerView(true);
				setTravelerCheckoutStatus(false);
			}
			else {
				//Empty button
				setShowTravelerView(false);
				setShowSavedTravelers(User.isLoggedIn(getActivity()));
			}
		}
	}

	public boolean hasDbTraveler() {
		return Db.getTravelers() != null && mTravelerNumber >= 0 && mTravelerNumber < Db.getTravelers().size();
	}

	public Traveler getDbTraveler() {
		if (hasDbTraveler()) {
			return Db.getTravelers().get(mTravelerNumber);
		}
		return null;
	}

	public boolean isPartiallyFilled() {
		Traveler trav = getDbTraveler();
		if (trav != null && !trav.isNew()) {
			return trav.hasName();
		}
		return false;
	}

	public boolean isValid() {
		// Short circuit in case if the last unsaved traveler was a new one.
		if (getDbTraveler() != null && getDbTraveler().isNew()) {
			return false;
		}
		return mTravelerButtonListener.travelerIsValid(mTravelerNumber);
	}

	private void setTravelerCheckoutStatus(boolean valid) {
		CheckoutInfoStatusImageView v = Ui.findView(mTravelerSectionContainer, R.id.display_picture);
		v.setTraveler(getDbTraveler());
		v.setStatusComplete(valid);
		setShowSavedTravelers(User.isLoggedIn(getActivity()));
	}

	private void setShowSavedTravelers(boolean showSpinner) {
		mSavedTravelerSpinner.setVisibility(showSpinner ? View.VISIBLE : View.GONE);
	}

	private void setShowTravelerView(boolean showTraveler) {
		if (showTraveler) {
			mEmptyViewContainer.setVisibility(View.GONE);
			setUpStoredTravelers(mTravelerSectionContainer);
			mTravelerSectionContainer.setVisibility(View.VISIBLE);
		}
		else {
			mEmptyViewContainer.setVisibility(View.VISIBLE);
			setUpStoredTravelers(mEmptyViewContainer);
			mTravelerSectionContainer.setVisibility(View.GONE);
		}
	}

	private ViewGroup mMeasureParent;

	// Copied from AOSP, ListPopupWindow.java
	private int measureContentWidth(ListAdapter adapter) {
		// Menus don't tend to be long, so this is more sane than it looks.
		int width = 0;
		View itemView = null;
		int itemType = 0;
		final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final int positionType = adapter.getItemViewType(i);
			if (positionType != itemType) {
				itemType = positionType;
				itemView = null;
			}
			if (mMeasureParent == null) {
				mMeasureParent = new FrameLayout(getActivity());
			}
			itemView = adapter.getView(i, itemView, mMeasureParent);
			itemView.measure(widthMeasureSpec, heightMeasureSpec);
			width = Math.max(width, itemView.getMeasuredWidth());
		}
		return width + 32;
	}

	/*
	 * THE ACTUAL DISPLAY VIEWS, NOTE THAT THESE ARE EASY TO OVERRIDE
	 */

	public SectionTravelerInfo addTravelerSectionToLayout(ViewGroup group) {
		SectionTravelerInfo travSec = null;
		if (getLob() == LineOfBusiness.FLIGHTS) {
			travSec = Ui.inflate(this, R.layout.section_flight_display_traveler_info_btn, null);
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			travSec = Ui.inflate(this, R.layout.section_hotel_display_traveler_info_btn, null);
		}
		if (travSec != null) {
			group.addView(travSec);
		}
		return travSec;
	}

	public void setEmptyViewLabel(String label) {
		mEmptyViewLabel = label;
	}

	public String getEmptyViewLabel() {
		return mEmptyViewLabel;
	}

	public View addEmptyTravelerToLayout(ViewGroup group) {
		View v = Ui.inflate(getActivity(), R.layout.snippet_booking_overview_traveler, group);
		TextView tv = Ui.findView(v, R.id.traveler_empty_text_view);
		tv.setText(mEmptyViewLabel);
		setUpStoredTravelers(v);
		return v;
	}

	public void setUpStoredTravelers(View v) {
		mSavedTravelerSpinner = Ui.findView(v, R.id.saved_traveler_fake_spinner);
		if (User.isLoggedIn(getActivity())) {
			mSavedTravelerSpinner.setVisibility(View.VISIBLE);
			mSavedTravelerSpinner.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showSavedTravelers();
				}
			});
		}
		mEditTravelerButton = Ui.findView(v, R.id.edit_traveler_button);
		mEditTravelerButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTravelerButtonListener.onTravelerEditButtonPressed(mTravelerNumber);
			}
		});
	}

	private void showSavedTravelers() {
		if (mStoredTravelerPopup == null) {
			mStoredTravelerPopup = new ListPopupWindow(getActivity());
			mStoredTravelerPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onStoredTravelerSelected(position);
				}
			});
		}
		mStoredTravelerPopup.setAnchorView(mSavedTravelerSpinner);
		// TODO Need to ask design for specific offSet for landscape/portrait and devices.
		mStoredTravelerPopup.setHorizontalOffset(200);
		mStoredTravelerPopup.setAdapter(mTravelerAdapter);
		mStoredTravelerPopup.setContentWidth(measureContentWidth(mTravelerAdapter));
		mStoredTravelerPopup.show();
	}

	/**
	 * There can be more than one instance of {@link TravelerButtonFragment}, so let's make sure the download key used is unique.
	 *
	 * @return
	 */
	private String getTravelerDownloadKey() {
		return DL_FETCH_TRAVELER_INFO + mTravelerNumber;
	}

	private BackgroundDownloader.Download<SignInResponse> mTravelerDetailsDownload = new BackgroundDownloader.Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(getTravelerDownloadKey(), services);
			return services.travelerDetails(Db.getWorkingTravelerManager().getWorkingTraveler(), 0);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {

			ThrobberDialog df = (ThrobberDialog) getChildFragmentManager().findFragmentByTag(FTAG_FETCH_TRAVELER_INFO);
			if (df != null) {
				df.dismiss();
			}

			if (results == null || results.hasErrors()) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
					getString(R.string.unable_to_load_traveler_message));
				dialogFragment.show(getFragmentManager(), "errorFragment");
				if (results != null && results.hasErrors()) {
					String error = results.getErrors().get(0).getPresentableMessage(getActivity());
					Log.e("Traveler Details Error:" + error);
				}
				else {
					Log.e("Traveler Details Results == null!");
				}
			}
			else {
				PassengerCategory category;
				// The traveler MUST be an adult if he or she is the traveler on
				// a hotel booking.
				if (getLob() == LineOfBusiness.HOTELS) {
					category = PassengerCategory.ADULT;
				}
				else {
					FlightSearchParams searchParams = Db.getTripBucket().getFlight().getFlightSearchParams();
					category = Db.getTravelers().get(mTravelerNumber).getPassengerCategory(searchParams);
				}
				results.getTraveler().setPassengerCategory(category);
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(results.getTraveler());
				Traveler currentTraveler = Db.getTravelers().get(mTravelerNumber);
				TravelerUtils.resetPreviousTravelerSelectState(currentTraveler);
				Db.getTravelers().remove(mTravelerNumber);
				Db.getTravelers().add(mTravelerNumber, results.getTraveler());
				bindToDb();
				mTravelerButtonListener.onTravelerChosen();
			}
		}
	};
}
