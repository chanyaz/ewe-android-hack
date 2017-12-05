package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.dialog.ThrobberDialog;
import com.expedia.bookings.fragment.SimpleSupportDialogFragment;
import com.expedia.bookings.section.TravelerAutoCompleteAdapter;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.flight.FlightsV2Tracking;
import com.expedia.bookings.tracking.PackagesTracking;
import com.expedia.bookings.utils.TravelerUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class TravelerButton extends LinearLayout {

	public TravelerButton(Context context) {
		super(context);
	}

	public TravelerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TravelerButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private static final String DL_FETCH_TRAVELER_INFO = "DL_FETCH_TRAVELER_INFO";
	private static final String FTAG_FETCH_TRAVELER_INFO = "FTAG_FETCH_TRAVELER_INFO";

	private LineOfBusiness lineOfBusiness;

	private TravelerAutoCompleteAdapter mTravelerAdapter;
	private ListPopupWindow mStoredTravelerPopup;
	private ITravelerButtonListener mTravelerButtonListener;
	private String mLastSelectedTravelerTuid;
	private int addNewTravelerPosition = 1;

	public interface ITravelerButtonListener {
		void onTravelerChosen(Traveler traveler);
		void onAddNewTravelerSelected();
	}

	@InjectView(R.id.select_traveler_button)
	Button selectTraveler;

	@OnClick(R.id.select_traveler_button)
	public void onShowTraveler() {
		Ui.hideKeyboard((Activity) getContext());
		postDelayed(new Runnable() {
			@Override
			public void run() {
				showSavedTravelers();
			}
		}, 100L);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater inflater = LayoutInflater.from(getContext());
		inflater.inflate(R.layout.checkout_traveler_button, this);
		ButterKnife.inject(this);
		mTravelerAdapter = new TravelerAutoCompleteAdapter(getContext(), Ui.obtainThemeResID(getContext(), R.attr.traveler_checkout_circle_drawable));
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (dl.isDownloading(getTravelerDownloadKey())) {
			dl.registerDownloadCallback(getTravelerDownloadKey(), mTravelerDetailsCallback);
		}
	}

	public void setTravelButtonListener(ITravelerButtonListener listener) {
		mTravelerButtonListener = listener;
	}

	public void setLOB(LineOfBusiness lob) {
		lineOfBusiness = lob;
	}

	@VisibleForTesting
	public LineOfBusiness getLineOfBusiness() {
		return lineOfBusiness;
	}

	public void updateSelectTravelerText(String text) {
		selectTraveler.setText(text);
	}

	private void onStoredTravelerSelected(int position) {
		boolean isAddNewTravelerSelected = (position == addNewTravelerPosition);
		if (isAddNewTravelerSelected) {
			Traveler emptyTraveler = new Traveler();
			emptyTraveler.setIsSelectable(false);
			deselectCurrentTraveler();
			Db.getWorkingTravelerManager().shiftWorkingTraveler(emptyTraveler);
			if (mTravelerButtonListener != null) {
				mTravelerButtonListener.onAddNewTravelerSelected();
			}
			selectTraveler.setText(getResources().getString(R.string.add_new_traveler));
			mStoredTravelerPopup.dismiss();
			return;
		}
		else if (position == 0) {
			return;
		}

		final Traveler selectedTraveler = mTravelerAdapter.getItem(position);
		mLastSelectedTravelerTuid = selectedTraveler.getTuid().toString();
		if (selectedTraveler.isSelectable()) {
			if (lineOfBusiness == LineOfBusiness.PACKAGES) {
				new PackagesTracking().trackCheckoutSelectTraveler();
			}
			else if (lineOfBusiness == LineOfBusiness.FLIGHTS_V2) {
				FlightsV2Tracking.trackCheckoutSelectTraveler();
			}
			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			if (dl.isDownloading(getTravelerDownloadKey())) {
				dl.cancelDownload(getTravelerDownloadKey());
			}

			BackgroundDownloader.Download<SignInResponse> travelerDetailsDownload =
				new BackgroundDownloader.Download<SignInResponse>() {
					@Override
					public SignInResponse doDownload() {
						ExpediaServices services = new ExpediaServices(getContext());
						BackgroundDownloader.getInstance().addDownloadListener(getTravelerDownloadKey(), services);
						return services.travelerDetails(selectedTraveler, 0);
					}
				};

			// Begin loading flight details in the background, if we haven't already
			// Show a loading dialog
			ThrobberDialog df = ThrobberDialog
				.newInstance(getResources().getString(R.string.loading_traveler_info));
			df.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), FTAG_FETCH_TRAVELER_INFO);
			dl.startDownload(getTravelerDownloadKey(), travelerDetailsDownload, mTravelerDetailsCallback);
			mStoredTravelerPopup.dismiss();
		}

	}

	private void showSavedTravelers() {
		if (mStoredTravelerPopup == null) {
			mStoredTravelerPopup = new ListPopupWindow(getContext());
			mStoredTravelerPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					onStoredTravelerSelected(position);
				}
			});
		}
		mStoredTravelerPopup.setInputMethodMode(ListPopupWindow.INPUT_METHOD_NEEDED);
		mStoredTravelerPopup.setAnchorView(selectTraveler);
		int selectTravelerBtnHeight = selectTraveler.getHeight();
		mStoredTravelerPopup.setVerticalOffset(-selectTravelerBtnHeight);
		mStoredTravelerPopup.setAdapter(mTravelerAdapter);
		mStoredTravelerPopup.setModal(true);
		mStoredTravelerPopup.show();
	}

	private BackgroundDownloader.OnDownloadComplete<SignInResponse> mTravelerDetailsCallback = new BackgroundDownloader.OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {

			ThrobberDialog df = (ThrobberDialog) ((AppCompatActivity) getContext()).getSupportFragmentManager()
				.findFragmentByTag(FTAG_FETCH_TRAVELER_INFO);
			if (df != null) {
				df.dismiss();
			}

			if (results == null || results.hasErrors()) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
					getResources().getString(R.string.unable_to_load_traveler_message));
				dialogFragment.show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "errorFragment");
				if (results != null && results.hasErrors()) {
					String error = results.getErrors().get(0).getPresentableMessage(getContext());
					Log.e("Traveler Details Error:" + error);
				}
				else {
					Log.e("Traveler Details Results == null!");
				}
			}
			else {
				deselectCurrentTraveler();
				Traveler mainTraveler = results.getTraveler();
				Db.getWorkingTravelerManager().shiftWorkingTraveler(mainTraveler);
				String travelerFullName;
				travelerFullName = mainTraveler.getFullNameBasedOnPos();
				selectTraveler.setText(travelerFullName);
				if (mTravelerButtonListener != null) {
					mTravelerButtonListener.onTravelerChosen(mainTraveler);
				}
			}
		}
	};

	private String getTravelerDownloadKey() {
		return DL_FETCH_TRAVELER_INFO + mLastSelectedTravelerTuid;
	}

	public void dismissPopup() {
		if (mStoredTravelerPopup != null) {
			mStoredTravelerPopup.dismiss();
		}
	}

	private void deselectCurrentTraveler() {
		Traveler previousTraveler = Db.getWorkingTravelerManager().getWorkingTraveler();
		TravelerUtils.resetPreviousTravelerSelectState(previousTraveler, getContext());
	}
}
