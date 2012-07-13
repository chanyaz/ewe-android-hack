package com.expedia.bookings.fragment;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelDescription;
import com.expedia.bookings.data.HotelDescription.DescriptionSection;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.DbPropertyHelper;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.android.util.Ui;

public class HotelDetailsIntroFragment extends Fragment {

	private static final int ROOMS_LEFT_CUTOFF = 5;

	private boolean mIsStartingReviewsActivity = false;

	public static HotelDetailsIntroFragment newInstance() {
		return new HotelDetailsIntroFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_hotel_details_intro, container, false);
	}

	@Override
	public void onResume() {
		super.onResume();
		populateViews();
		mIsStartingReviewsActivity = false;
	}

	public void populateViews() {
		View view = getView();
		populateBannerSection(view, DbPropertyHelper.getBestRateProperty());
		populateIntroParagraph(view, DbPropertyHelper.getBestDescriptionProperty());
	}

	private void populateBannerSection(View view, Property property) {
		Rate rate = property.getLowestRate();

		// Sale banner
		TextView saleBannerTextView = Ui.findView(view, R.id.sale_banner_text_view);
		TextView promoTextView = Ui.findView(view, R.id.promo_text_view);
		if (rate.isOnSale()) {
			saleBannerTextView.setVisibility(View.VISIBLE);
			saleBannerTextView.setText(getString(R.string.minus_x_percent, rate.getSavingsPercent() * 100));
		}
		else {
			saleBannerTextView.setVisibility(View.GONE);
		}

		// Promo text, i.e. "Mobile Only!"
		int roomsLeft = property.getRoomsLeftAtThisRate();
		if (property.isLowestRateMobileExclusive()) {
			promoTextView.setText(getString(R.string.mobile_exclusive));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else if (roomsLeft > 0 && roomsLeft <= ROOMS_LEFT_CUTOFF) {
			promoTextView.setText(getResources().getQuantityString(R.plurals.num_rooms_left, roomsLeft,
					roomsLeft));
			promoTextView.setVisibility(View.VISIBLE);
		}
		else {
			promoTextView.setVisibility(View.GONE);

		}

		// "From <strike>$400</strike>" (if it's on sale) or else just "From"
		TextView fromTextView = Ui.findView(view, R.id.from_text_view);
		if (rate.isOnSale()) {
			fromTextView.setText(Html.fromHtml(
					getString(R.string.from_template, StrUtils.formatHotelPrice(rate.getDisplayBaseRate())), null,
					new StrikethroughTagHandler()));

		}
		else {
			fromTextView.setText(R.string.from);
		}

		// Rate
		TextView rateTextView = Ui.findView(view, R.id.rate_text_view);
		rateTextView.setText(StrUtils.formatHotelPrice(rate.getDisplayRate()));
	}

	private void populateIntroParagraph(View view, Property property) {
		String unparsedDescriptionText = property.getDescriptionText();

		HotelDescription.SectionStrings.initSectionStrings(getActivity());
		HotelDescription hotelDescription = new HotelDescription(getActivity());

		hotelDescription.parseDescription(unparsedDescriptionText);
		List<DescriptionSection> sections = hotelDescription.getSections();

		final TextView titleView = Ui.findView(view, R.id.title_text);
		final TextView bodyView = Ui.findView(view, R.id.body_text);

		CharSequence title, body;
		if (sections != null && sections.size() >= 1) {
			title = Html.fromHtml(sections.get(0).title);
			body = Html.fromHtml(sections.get(0).description);
		}
		else {
			title = Html.fromHtml("");
			body = Html.fromHtml(unparsedDescriptionText);
		}

		// Add "read more" button if the intro paragraph is too long
		int cutoff = 180;
		if (body.length() > cutoff) {
			final CharSequence untruncated = body;
			final View readMoreView = Ui.findView(view, R.id.read_more_layout);
			final View fadeOverlay = Ui.findView(view, R.id.body_text_fade_bottom);
			readMoreView.setVisibility(View.VISIBLE);
			fadeOverlay.setVisibility(View.VISIBLE);
			readMoreView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					bodyView.setText(untruncated);
					readMoreView.setVisibility(View.GONE);
					fadeOverlay.setVisibility(View.GONE);
				}
			});
			body = body.subSequence(0, cutoff) + "...";
		}

		titleView.setVisibility(TextUtils.isEmpty(title) ? View.INVISIBLE : View.VISIBLE);
		titleView.setText(title);
		bodyView.setText(body);
	}
}
