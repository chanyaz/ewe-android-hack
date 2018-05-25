package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.Strings;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.HtmlUtils;
import com.mobiata.android.util.ViewUtils;

public class HotelRulesFragment extends Fragment {
	public static final String TAG = HotelRulesFragment.class.toString();

	private static LineOfBusiness lob;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_rules, container, false);

		lob = (LineOfBusiness) getActivity().getIntent().getExtras().get(HotelRulesActivity.LOB_KEY);

		populateHeaderRows(view);
		ViewUtils.setAllCaps((TextView) Ui.findView(view, R.id.cancellation_policy_header_text_view));

		String cancellationPolicy = "";

		if (lob == LineOfBusiness.HOTELS) {
			HotelOffersResponse.HotelRoomResponse room = Db.getTripBucket()
				.getHotelV2().mHotelTripResponse.newHotelProductResponse.hotelRoomResponse;
			if (room != null) {
				cancellationPolicy = room.cancellationPolicy;
			}
		}

		if (Strings.isNotEmpty(cancellationPolicy)) {
			TextView cancellationPolicyTextView = Ui.findView(view, R.id.cancellation_policy_text_view);
			cancellationPolicyTextView.setText(HtmlCompat.fromHtml(cancellationPolicy));
		}

	return view;
}

	private void populateHeaderRows(View view) {

		final PointOfSale pos = PointOfSale.getPointOfSale();

		// Terms and Conditions
		TextView terms = Ui.findView(view, R.id.terms_and_conditions);
		terms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(pos.getTermsAndConditionsUrl());
				builder.setTitle(R.string.terms_and_conditions);
				startActivity(builder.getIntent());
			}
		});

		// Terms of Booking
		if (pos.getTermsOfBookingUrl() != null) {
			TextView booking = Ui.findView(view, R.id.terms_of_booking);
			booking.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(pos.getTermsOfBookingUrl());
					builder.setTitle(R.string.Terms_of_Booking);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			Ui.findView(view, R.id.terms_of_booking).setVisibility(View.GONE);
			Ui.findView(view, R.id.terms_of_booking_divider).setVisibility(View.GONE);
		}

		// Privacy Policy
		TextView privacy = Ui.findView(view, R.id.privacy_policy);
		privacy.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
				builder.setUrl(PointOfSale.getPointOfSale().getPrivacyPolicyUrl());
				builder.setTitle(R.string.privacy_policy);
				startActivity(builder.getIntent());
			}
		});

		// ATOL Information for UK pos
		TextView atolInformation = Ui.findView(view, R.id.atol_information);
		if (PointOfSale.getPointOfSale().showAtolInfo()) {
			atolInformation.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());

					String message = getString(R.string.lawyer_label_atol_long_message);
					String html = HtmlUtils.wrapInHeadAndBody(message);
					builder.setHtmlData(html);

					builder.setTitle(R.string.lawyer_label_atol_information);
					startActivity(builder.getIntent());
				}
			});
		}
		else {
			atolInformation.setVisibility(View.GONE);
			Ui.findView(view, R.id.atol_information_divider).setVisibility(View.GONE);
		}
	}
}
