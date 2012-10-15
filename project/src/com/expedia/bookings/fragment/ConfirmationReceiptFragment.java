package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.widget.HotelItemizedOverlay;
import com.expedia.bookings.widget.ReceiptWidget;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.mobiata.android.MapUtils;
import com.mobiata.android.util.Ui;

public class ConfirmationReceiptFragment extends Fragment {

	private ReceiptWidget mReceiptWidget;

	private MapView mMapView;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup receipt = (ViewGroup) inflater.inflate(R.layout.fragment_confirmation_receipt, container, false);

		mReceiptWidget = new ReceiptWidget(getActivity(), receipt.findViewById(R.id.receipt), false);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), receipt);

		// Font face can't be set in xml
		TextView enjoyYourStay = Ui.findView(receipt, R.id.text_enjoy_your_stay);
		enjoyYourStay.setTypeface(FontCache.getTypeface(Font.ROBOTO_LIGHT));

		TextView contactView = Ui.findView(receipt, R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		/*
		 * The map section (only appears on phone versions)
		 */
		configureMapSection(receipt);

		/*
		 * The ratings (only appear on phone versions)
		 */
		configureRatingsSection(receipt);

		/*
		 * The rest of the receipt details (on both tablet & phone versions)
		 */
		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(),
				Db.getBookingResponse(), Db.getBillingInfo(), Db.getCouponDiscountRate());

		return receipt;
	}

	@Override
	public void onDestroyView() {
		ViewGroup mapViewLayout = (ViewGroup) getView().findViewById(R.id.receipt_map_layout);
		if (mapViewLayout != null && mMapView != null) {
			mapViewLayout.removeView(mMapView);
			mMapView.setEnabled(true);
			mMapView.getOverlays().clear();
		}
		super.onDestroyView();
	}

	private void configureMapSection(ViewGroup container) {
		ViewGroup mapContainer = (ViewGroup) container.findViewById(R.id.receipt_map_layout);
		if (mapContainer == null) {
			return;
		}

		// Show on the map where the hotel is
		mMapView = MapUtils.createMapView(getActivity());
		mapContainer.addView(mMapView);

		List<Property> properties = new ArrayList<Property>(1);
		properties.add(Db.getSelectedProperty());
		List<Overlay> overlays = mMapView.getOverlays();
		HotelItemizedOverlay overlay = new HotelItemizedOverlay(getActivity(), properties, mMapView);
		overlays.add(overlay);
		MapController mc = mMapView.getController();
		GeoPoint center = overlay.getCenter();
		GeoPoint offsetCenter = new GeoPoint(center.getLatitudeE6() + 1000, center.getLongitudeE6() - 8000);
		mc.setCenter(offsetCenter);
		mc.setZoom(15);
		// disabling the map so that it does not respond to touch events 
		mMapView.setEnabled(false);
	}

	private void configureRatingsSection(ViewGroup container) {
		// Ratings
		RatingBar hotelRating = (RatingBar) container.findViewById(R.id.hotel_rating_bar);
		if (hotelRating != null) {
			hotelRating.setRating((float) Db.getSelectedProperty().getHotelRating());
		}
		RatingBar userRating = (RatingBar) container.findViewById(R.id.user_rating_bar);
		if (userRating != null) {
			userRating.setRating((float) Db.getSelectedProperty().getAverageExpediaRating());
		}
	}
}
